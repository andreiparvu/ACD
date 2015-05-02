package cd.analyze;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cd.Main;
import cd.analyze.CallGraphGenerator.CallGraph;
import cd.ir.Ast;
import cd.ir.Ast.ClassDecl;
import cd.ir.Ast.Expr;
import cd.ir.Ast.Var;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.Phi;
import cd.ir.Symbol.MethodSymbol;
import cd.ir.Symbol.VariableSymbol;

public class EscapeAnalysis {
	
	private Main main;
	private Map<MethodSymbol, Set<MethodSymbol>> cg;
	private CallGraphSCC scc;
	
	private Map<MethodSymbol, AliasContext> mc;
	private Map<MethodSymbol, List<MethodSymbol>> callees;

	class AliasSet {
		public boolean escapes = false;

		private final Map<String, AliasSet> fieldMap = new HashMap<>();
		
		public AliasSet fieldMap(String field) {
			if (!fieldMap.containsKey(field)) {
				fieldMap.put(field, new AliasSet());
			}
			return fieldMap.get(field);
		}
		
		public AliasSet deepCopy() {
			return null;
		}
	}
	
	private void unify(AliasSet a, AliasSet b) {
		
	}
	
	class AliasContext {
		
		final public Map<VariableSymbol, AliasSet> parameters;
		final public AliasSet result;

		public AliasContext(MethodSymbol method) {
			result = new AliasSet();
			parameters = new HashMap<>();
			for (VariableSymbol param : method.parameters) {
				parameters.put(param, new AliasSet());
			}
		}
	}

	public EscapeAnalysis(Main main) {
		this.main = main;
	}

	Map<>
	
	public void analyze(List<ClassDecl> astRoots) {
		// 3.3.1 Interprocedural analysis
		
		// generate call graph
		CallGraph res = new CallGraphGenerator(main).compute(astRoots);
		cg = res.graph;
		callees = res.targets;
		
		res.debugPrint();
		
		// generate scc
		scc = new CallGraphSCC(cg);
		scc.debugPrint();

		mc = new HashMap<MethodSymbol, AliasContext>();
		
		// traverse SCC methods in bottom-up topological order
		for (Set<MethodSymbol> component : scc.getSortedComponents()) {
			// create method context for all methods in component
			for (MethodSymbol method : component) {
				mc.put(method, new AliasContext(method));
			}
			
			// applying intraprocedural analysis
			for (MethodSymbol method : component) {
				analyzeMethod(method);
			}
		}
	}
	
	public void analyzeMethod(MethodSymbol method) {
		AliasContext ctx = mc.get(method);
		LocalAliases as = new LocalAliases(ctx);
		MethodAnalyzer analyzer = new MethodAnalyzer();

		for(BasicBlock bb : method.ast.cfg.allBlocks) {
			for (Phi phi : bb.phis.values()) {
				AliasSet asV = as.get(phi.lhs);
				for (Expr expr : phi.rhs) {
					if (expr instanceof Var) {
						VariableSymbol vi = ((Var)expr).sym;
						unify(asV, as.get(vi));
					}
				}
			}
			
			if (bb.condition != null) {
				analyzer.visit(bb.condition, as);
			}
			
			for (Ast ast : bb.instructions) {
				analyzer.visit(ast, as);
			}
		}
	}
	
	private static class LocalAliases extends HashMap<VariableSymbol, AliasSet> {
		LocalAliases(AliasContext ctx) {
			super(ctx.parameters);
		}
	}
	
	private class MethodAnalyzer extends AstVisitor<Void, LocalAliases> {
		
	}
}
