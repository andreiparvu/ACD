package cd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.runtime.ANTLRReaderStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import cd.cfg.CFGBuilder;
import cd.cfg.DeSSA;
import cd.cfg.Dominator;
import cd.cfg.EscapeAnalyzer;
import cd.analyze.CallGraphGenerator;
import cd.analyze.CallGraphSCC;
import cd.analyze.EscapeAnalysis;
import cd.cfg.CFGBuilder;
import cd.cfg.DeSSA;
import cd.cfg.Dominator;
import cd.cfg.Optimizer;
import cd.cfg.SSA;
import cd.codegen.CfgCodeGenerator;
import cd.debug.AstDump;
import cd.debug.CfgDump;
import cd.exceptions.ParseFailure;
import cd.ir.Ast;
import cd.ir.Ast.ClassDecl;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.Seq;
import cd.ir.BasicBlock;
import cd.ir.Symbol;
import cd.ir.Symbol.MethodSymbol;
import cd.ir.Symbol.PrimitiveTypeSymbol;
import cd.ir.Symbol.TypeSymbol;
import cd.ir.Symbol.VariableSymbol;
import cd.ir.Symbol.VariableSymbol.Kind;
import cd.parser.JavaliLexer;
import cd.parser.JavaliParser;
import cd.parser.JavaliWalker;
import cd.semantic.SemanticAnalyzer;
import cd.util.Pair;

/** 
 * The main entrypoint for the compiler.  Consists of a series
 * of routines which must be invoked in order.  The main()
 * routine here invokes these routines, as does the unit testing
 * code. This is not the <b>best</b> programming practice, as the
 * series of calls to be invoked is duplicated in two places in the
 * code, but it will do for now. */
public class Main {
	
	// Set to non-null to write debug info out
	public Writer debug = new PrintWriter(System.err);
	
	// Set to non-null to write dump of control flow graph
	public File cfgdumpbase;
	
	/** Symbols for the built-in primitive types */
	public PrimitiveTypeSymbol intType, floatType, voidType, booleanType;

	/** Symbols for the built-in Object and null types */
	public Symbol.ClassSymbol objectType, nullType, threadType;
	
	/** Symbol for the Main type */
	public Symbol.ClassSymbol mainType;
	
	/** List of all type symbols, used by code generator. */
	public List<TypeSymbol> allTypeSymbols;  

	public void debug(String format, Object... args) {
		if (debug != null) {
			String result = String.format(format, args);
			try {
				debug.write(result);
				debug.write('\n');
				debug.flush();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	/** Parse command line, invoke compile() routine */
	public static void main(String args[]) throws IOException {
		
		Main m = new Main();
		
		for (String file : args) {
			
			if (file.equals("-d"))
				m.debug = new OutputStreamWriter(System.err);
			else {
				{
					if (m.debug != null)
						m.cfgdumpbase = new File(file);
				}
				
				FileReader fin = new FileReader(file);

				// Parse:
				List<ClassDecl> astRoots = m.parse(file, fin, false);
				
				// Run the semantic check:
				m.semanticCheck(astRoots);
				
				// Generate code:
				String sFile = file + Config.ASMEXT;
				FileWriter fout = new FileWriter(sFile);
				m.generateCode(astRoots, fout);
				fout.close();
			}
		}
	}
	
	public Main() {
		{
			intType = new PrimitiveTypeSymbol("int");
			floatType = new PrimitiveTypeSymbol("float");
			booleanType = new PrimitiveTypeSymbol("boolean");
			voidType = new PrimitiveTypeSymbol("void");
			objectType = new Symbol.ClassSymbol("Object");
			nullType = new Symbol.ClassSymbol("<null>");
		}

		threadType = new Symbol.ClassSymbol("Thread");
		threadType.superClass = objectType;

		addRuntimeFields();
		addRuntimeMethods();
	}
	
	
	private void addRuntimeFields() {
		VariableSymbol mutexField = new VariableSymbol("$mutex", objectType, Kind.FIELD);
		mutexField.offset = 0;
		objectType.fields.put(mutexField.name, mutexField);

		VariableSymbol condMutexField = new VariableSymbol("$cond_mutex", objectType, Kind.FIELD);
		condMutexField.offset = 1;
		objectType.fields.put(condMutexField.name, condMutexField);

		objectType.totalFields = 2;
		objectType.sizeof = Config.SIZEOF_PTR * (objectType.totalFields + 1);

		VariableSymbol threadField = new VariableSymbol("$thread", objectType, Kind.FIELD);
		threadField.offset = 2;
		threadType.fields.put(threadField.name, threadField);
		threadType.totalFields = 3;
		threadType.sizeof = Config.SIZEOF_PTR * (threadType.totalFields + 1);
	}
	
	private void addRuntimeMethod(Symbol.ClassSymbol owner, String methodName, int vtableOffset) {
		Seq emptyDecl = new Seq(Collections.<Ast>emptyList());
		Seq emptyBody = new Seq(Collections.<Ast>emptyList());
		List<Pair<String>> emptyParams = Collections.emptyList();

		MethodSymbol methodSymbol = new MethodSymbol(
				new MethodDecl("void", methodName, emptyParams, emptyDecl, emptyBody));
		methodSymbol.returnType = voidType;
		methodSymbol.owner = owner;
		methodSymbol.vtableIndex = vtableOffset;
		owner.methods.put(methodName, methodSymbol);
	}

	private void addRuntimeMethods() {
		int vtableOffset = 0;
		for (String methodName : Arrays.asList("lock", "unlock", "notify", "wait")) {
			addRuntimeMethod(objectType, methodName, vtableOffset++);
		}
		objectType.totalMethods = vtableOffset;

		for (String methodName : Arrays.asList("run", "start", "join")) {
			addRuntimeMethod(threadType, methodName, vtableOffset++);
		}
		threadType.totalMethods = vtableOffset;
	}

	public List<ClassDecl> parse(Reader file, boolean debugParser)  throws IOException {
		return parse(null, file, debugParser);
	}
	
	/** Parses an input stream into an AST 
	 * @throws IOException */
	public List<ClassDecl> parse(String fileName, Reader file, boolean debugParser)  throws IOException {
		List<ClassDecl> result = new ArrayList<ClassDecl>();
		
		{
			result = parseWithAntlr(fileName, file);
		}
		
		return result;
	}
	
	public List<ClassDecl> parseWithAntlr(String file, Reader reader) throws IOException {
				
		try {
			
			ANTLRReaderStream input = new ANTLRReaderStream(reader);
			JavaliLexer lexer = new JavaliLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			
			JavaliParser parser = new JavaliParser(tokens);
			JavaliParser.unit_return parserReturn;
			parserReturn = parser.unit();
			
			CommonTreeNodeStream nodes = new CommonTreeNodeStream(parserReturn.getTree());
			nodes.setTokenStream(tokens);
			
			JavaliWalker walker = new JavaliWalker(nodes);
			
			debug("AST Resulting From Parsing Stage:");
			List<ClassDecl> result = walker.unit();
			
			dumpAst(result);
			
			return result;
			
		} catch (RecognitionException e) {
			ParseFailure pf = new ParseFailure(0, "?");
			pf.initCause(e);
			throw pf;
		}
	}
	
	public void semanticCheck(List<ClassDecl> astRoots) {
		{
			new SemanticAnalyzer(this).check(astRoots);
		}
		
		// Before generating code, optimize!
		
		{						
			// Build control flow graph:
			for (ClassDecl cd : astRoots)
				for (MethodDecl md : cd.methods())
					new CFGBuilder(this).build(md);
			CfgDump.toString(astRoots, ".cfg", cfgdumpbase, false);
			
		}
		
		{
			// Compute dominators.
			for (ClassDecl cd : astRoots)
				for (MethodDecl md : cd.methods()) {
					debug("Computing dominators of %s", md.name);
					new Dominator(this).compute(md.cfg);
					for (BasicBlock b : md.cfg.allBlocks) {
						debug("  %s df %s of %s", b, b.dominanceFrontier, md.name);
					}
				}
			CfgDump.toString(astRoots, ".dom", cfgdumpbase, true);
			
			// Introduce SSA form.
			for (ClassDecl cd : astRoots)
				for (MethodDecl md : cd.methods())
					new SSA(this).compute(md);
			CfgDump.toString(astRoots, ".ssa", cfgdumpbase, false);			

			{
				// Optimize using SSA form. 
				for (ClassDecl cd : astRoots)
					for (MethodDecl md : cd.methods())
						new Optimizer(this).compute(md);
				CfgDump.toString(astRoots, ".opt", cfgdumpbase, false);				
			}
			
			
			Map<MethodSymbol, Set<MethodSymbol>> callTargets = new CallGraphGenerator(this).computeTargets(astRoots);
			
			try {
				File f = new File(cfgdumpbase.getCanonicalFile() + ".escape.dot"),
					 rez = new File(cfgdumpbase.getCanonicalFile() + ".stack");
				PrintWriter pw = new PrintWriter(f),
							pr = new PrintWriter(rez);
				
				pw.write("digraph G {\ngraph [rankdir = \"LR\"];\n");
				for (ClassDecl cd : astRoots) {
					for (MethodDecl md : cd.methods()) {
						if (md.analyzedColor == EscapeAnalyzer.WHITE) {
							(new EscapeAnalyzer(this)).compute(md, pw, pr, callTargets);
						}
					}
				}
				pw.write("}\n");
				pw.close();
				pr.close();
			} catch (IOException ex) {
				System.err.println(ex);
			}
			
			{
//				new EscapeAnalysis(this).analyze(astRoots);
			}

			// Remove SSA form.
			for (ClassDecl cd : astRoots)
				for (MethodDecl md : cd.methods())
					new DeSSA(this).compute(md);
			CfgDump.toString(astRoots, ".dessa", cfgdumpbase, false);
		}
	}
	
	public void generateCode(List<ClassDecl> astRoots, Writer out) {
		{
			CfgCodeGenerator cg = new CfgCodeGenerator(this, out);
			cg.go(astRoots);
		}
	}

	/** Dumps the AST to the debug stream */
	private void dumpAst(List<ClassDecl> astRoots) throws IOException {
		if (this.debug == null) return;
		this.debug.write(AstDump.toString(astRoots));
	}
}
