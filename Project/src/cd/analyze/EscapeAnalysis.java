package cd.analyze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cd.Main;
import cd.analyze.AliasSet.AliasSetData;
import cd.analyze.CallGraphGenerator.CallGraph;
import cd.debug.AstDump;
import cd.debug.AstOneLine;
import cd.ir.Ast;
import cd.ir.Ast.Assign;
import cd.ir.Ast.ClassDecl;
import cd.ir.Ast.Expr;
import cd.ir.Ast.Field;
import cd.ir.Ast.Index;
import cd.ir.Ast.MethodCall;
import cd.ir.Ast.MethodCallExpr;
import cd.ir.Ast.NewArray;
import cd.ir.Ast.NewObject;
import cd.ir.Ast.NullConst;
import cd.ir.Ast.ReturnStmt;
import cd.ir.Ast.ThisRef;
import cd.ir.Ast.Var;
import cd.ir.Ast.WhileLoop;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.Phi;
import cd.ir.Symbol.ClassSymbol;
import cd.ir.Symbol.MethodSymbol;
import cd.ir.Symbol.VariableSymbol;
import cd.util.DepthFirstSearchPreOrder;

public class EscapeAnalysis {

	class AliasContext {
		public final AliasSet receiver;
		public final ArrayList<AliasSet> parameters;
		public final AliasSet result;

		public AliasContext(AliasSet receiver, ArrayList<AliasSet> parameters,
				AliasSet result) {
			super();
			this.receiver = receiver;
			this.parameters = parameters;
			this.result = result;
		}

		public AliasContext(MethodSymbol method) {
			receiver = new AliasSet();
			result = AliasSet.forType(method.returnType);
			parameters = new ArrayList<>();
			for (VariableSymbol param : method.parameters) {
				parameters.add(AliasSet.forType(param.type));
			}
		}
		
		private AliasContext(AliasContext copy) {
			HashMap<AliasSetData, AliasSet> copies = new HashMap<>();
			this.receiver = copy.receiver.deepCopy(copies);
			this.result = copy.result.deepCopy(copies);
			this.parameters = new ArrayList<>(copy.parameters.size());
			for (AliasSet param : copy.parameters) {
				this.parameters.add(param.deepCopy(copies));
			}
		}

		public void unify(AliasContext other) {
			this.receiver.unify(other.receiver);
			this.result.unify(other.result);
			assert (parameters.size() == other.parameters.size());
			for (int i=0; i < parameters.size(); i++) {
				parameters.get(i).unify(other.parameters.get(i));
			}
		}
		
		/*public void unifyEscapes(AliasContext other) {
			this.receiver.unifyEscapes(other.receiver);
			this.result.unifyEscapes(other.result);
			assert (parameters.size() == other.parameters.size());
			for (int i=0; i < parameters.size(); i++) {
				parameters.get(i).unifyEscapes(other.parameters.get(i));
			}
		}*/

		public AliasContext deepCopy() {
			return new AliasContext(this);
		}

		@Override
		public String toString() {
			return "[receiver=" + receiver + ", parameters="
					+ parameters + ", result=" + result + "]";
		}
	}
	
	private Main main;
	private CallGraphSCC scc;
	
	private Map<MethodSymbol, AliasContext> methodContexts;
	private Map<Ast, AliasContext> siteContexts;
	private CallGraph callGraph;
	private Map<Ast, Boolean> multiSites;
	private MethodSymbol threadStart, objectLock, objectUnlock;

	public EscapeAnalysis(Main main) {
		this.main = main;
		this.threadStart = main.threadType.getMethod("start");
		this.objectLock = main.objectType.getMethod("lock");
		this.objectUnlock = main.objectType.getMethod("unlock");
	}
	
	public void analyze(List<ClassDecl> astRoots) {
		//// Phase 1 ////
		
		// generate call graph
		callGraph = new CallGraphGenerator(main).compute(astRoots);
		//callGraph = cg.graph;
		//callees = cg.targets;
		
		callGraph.debugPrint();

		// generate scc
		scc = new CallGraphSCC(callGraph);
		scc.debugPrint();
		
		// find multiply executed thread allocation sites
		multiSites = findThreadAllocationSites();

		//// Phase 2 ////
		methodContexts = new HashMap<MethodSymbol, AliasContext>();
		siteContexts = new HashMap<Ast, AliasContext>();

		// traverse SCC methods in bottom-up topological order
		analyzeBottomUp();
		//// Phase 3 ////
		// top-down traversal to unify method contexts
		mergeTopDown();

		// debug print
		System.err.println("Method Contexts");
		for (Entry<MethodSymbol, AliasContext> entry : methodContexts.entrySet()) {
			System.err.println(entry.getKey().fullName() + ": " + entry.getValue());
		}
		/*
		System.err.println("Site Contexts");
		for (Entry<Ast, AliasContext> entry : siteContexts.entrySet()) {
			Ast ast = entry.getKey();
			System.err.println(AstOneLine.toString(ast) + ": " + entry.getValue());
		}*/
		
		/*for (Set<MethodSymbol> component : scc.getSortedComponents()) {
			// create method context for all methods in component
			for (MethodSymbol method : component) {
				System.err.println(method.fullName() + ":");
				new AstVisitor<Void, Integer>() {

					@Override
					public Void dfltExpr(Expr ast, Integer ident) {
						String id = new String(new char[ident]).replace("\0", "  ");;
						System.err.println(id + AstOneLine.toString(ast) + ":" + ast.aliasSet);
						return super.dfltExpr(ast, ident+2);
					}
					
				}.visit(method.ast, 4);
			}
		}*/
		//System.err.println(AstDump.toString(astRoots));
	}

	private void analyzeBottomUp() {
		// add method contexts for thread entry points
		for (MethodSymbol entry : callGraph.roots) {
			methodContexts.put(entry, new AliasContext(entry));
		}

		for (Set<MethodSymbol> component : scc.getSortedComponents()) {
			// create method context for all methods in component
			for (MethodSymbol method : component) {
				if (!methodContexts.containsKey(method)) {
					methodContexts.put(method, new AliasContext(method));
				}
			}

			// applying intraprocedural analysis
			for (MethodSymbol method : component) {
				ClassSymbol owner = method.owner;
				// don't analyze built-in objects
				if (owner != main.objectType && owner != main.threadType) {
					MethodAnalayzer visitor = new MethodAnalayzer(method);
					visitor.analyize();
				}
			}
		}
	}
	
	private void mergeTopDown() {
		List<Set<MethodSymbol>> reversed = new LinkedList<>(scc.getSortedComponents());
		Collections.reverse(reversed);
		for (Set <MethodSymbol> component : reversed) {
			for (final MethodSymbol method : component) {
				System.err.println(method.fullName() +":"+ methodContexts.get(method));

				new AstVisitor<Void, Void>() {
					private void merge(Ast ast, MethodSymbol sym) {
						// TODO if node is not part of CFG (because dead), sc will trigger nullpointer
						AliasContext sc = siteContexts.get(ast);
						System.err.println("  " + AstOneLine.toString(ast) + "<-" + sc);

						for (MethodSymbol target : callGraph.targets.get(sym)) {
							AliasContext mc = methodContexts.get(target).deepCopy();
							sc.unify(mc);
							System.err.println("    "+target.fullName() + ":" + mc);
						}
						//sc.unifyEscapes(mc); //TODO not precise enough, bad2.javali
						//
					}

					@Override
					public Void methodCall(MethodCall ast, Void arg) {
						merge(ast, ast.sym);
						return super.methodCall(ast, arg);
					}

					@Override
					public Void methodCall(MethodCallExpr ast, Void arg) {
						merge(ast, ast.sym);
						return super.methodCall(ast, arg);
					}
				}.visit(method.ast, null);
			}
		}
	}


	private Map<Ast, Boolean> findThreadAllocationSites() {
		final Map<Ast, Boolean> multiSite = new HashMap<>();
		for (MethodSymbol sym : callGraph.graph.keySet()) {
			if (sym.owner == main.objectType || sym.owner == main.threadType) {
				continue;
			}
			
			final boolean calleeInLoop = callGraph.calledInLoop.get(sym);
			final boolean calleeInMultiSCC = scc.getComponent(sym).size() > 1;
			new AstVisitor<Void, Boolean>() {

				@Override
				public Void methodCall(MethodCall ast, Boolean stmtInLoop) {
					if (ast.sym == threadStart) {
						multiSite.put(ast, stmtInLoop || calleeInLoop || calleeInMultiSCC);
					}
					return null;
				}

				@Override
				public Void whileLoop(WhileLoop ast, Boolean stmtInLoop) {
					return super.whileLoop(ast, true);
				}

			}.visit(sym.ast, false);
		}
		
		return multiSite;
	}

	private class MethodAnalayzer extends AstVisitor<AliasSet, Void> {
		private final HashMap<VariableSymbol, AliasSet> as = new HashMap<>();
		private final AliasContext methodContext;
		private final MethodSymbol method;
		private final List<MethodCall> threadStarts = new ArrayList<MethodCall>();
		
		public MethodAnalayzer(MethodSymbol method) {
			AliasContext mc = methodContexts.get(method);
			this.methodContext = mc;
			this.method = method;

			// add alias sets of parameter to lookup for variables
			for (int i=0; i < method.parameters.size(); i++) {
				as.put(method.parameters.get(i), mc.parameters.get(i));
			}
		}
		
		private AliasSet lookup(VariableSymbol var) {
			AliasSet varSet = as.get(var);
			if (varSet == null) {
				if (var.type.isReferenceType()) {
					varSet = new AliasSet();
				} else {
					varSet = AliasSet.BOTTOM;
				}
				as.put(var, varSet);
			}
			return varSet;
		}
		
		public void analyize() {
			for(BasicBlock bb : new DepthFirstSearchPreOrder(method.ast.cfg)) {
				for (Phi phi : bb.phis.values()) {
					AliasSet setV = lookup(phi.lhs);
					for (Expr expr : phi.rhs) {
						AliasSet setVi = visit(expr, null);
						setV.unify(setVi);
					}
				}

				if (bb.condition != null) {
					visit(bb.condition, null);
				}

				for (Ast ast : bb.instructions) {
					visit(ast, null);
				}
			}
			
			for (MethodCall callSite : threadStarts) {
				visitThreadStart(callSite);
			}
		}
		
		private void visitThreadStart(MethodCall threadStart) {
			AliasContext sc = siteContexts.get(threadStart);
			ClassSymbol thread = ((ClassSymbol)threadStart.receiver().type);
			MethodSymbol threadRun = thread.getMethod("run");

			// mark thread as escaped
			sc.receiver.setEscapes(true);

			// make sure to escape `this` in thread entry
			for (MethodSymbol entry: callGraph.targets.get(threadRun)) {
				methodContexts.get(entry).receiver.unify(sc.receiver);
			}

			// unify thread with itself if started multiple times
			// TODO check if really needed
			if (multiSites.get(threadStart))  {
				sc.unify(sc);
			}
		}

		@Override
		public AliasSet visit(Expr expr, Void arg) {
			expr.aliasSet = super.visit(expr, arg);
			return expr.aliasSet;
		}

		@Override
		protected AliasSet dfltExpr(Expr ast, Void arg) {
			// make sure to return any alias symbol
			AliasSet set = visitChildren(ast, arg);
			if (set != null) {
				return set;
			} else if(ast.type != null && ast.type.isReferenceType()) {
				return new AliasSet();
			} else {
				return AliasSet.BOTTOM;
			}
		}

		@Override
		public AliasSet nullConst(NullConst ast, Void arg) {
			// technically we should return ⊥, but we might unify it later with an object
			return new AliasSet();
		}

		@Override
		public AliasSet newObject(NewObject ast, Void arg) {
			return new AliasSet();
		}

		@Override
		public AliasSet newArray(NewArray ast, Void arg) {
			return new AliasSet();
		}

		@Override
		public AliasSet field(Field ast, Void arg) {
			AliasSet set = visit(ast.arg(), null);
			if (ast.type.isReferenceType()) {
				return set.fieldMap(ast.fieldName);
			} else {
				return AliasSet.BOTTOM;
			}
		}

		@Override
		public AliasSet index(Index ast, Void arg) {
			AliasSet setLeft = visit(ast.left(), arg);
			AliasSet setRight = visit(ast.right(), arg);
			assert (setRight.isBottom());

			if (ast.type.isReferenceType()) {
				return setLeft.fieldMap("$ELT");
			} else {
				return AliasSet.BOTTOM;
			}
		}

		@Override
		public AliasSet thisRef(ThisRef ast, Void arg) {
			return methodContext.receiver;
		}

		@Override
		public AliasSet var(Var ast, Void arg) {
			return lookup(ast.sym);
		}
		
		@Override
		public AliasSet assign(Assign ast, Void arg) {
			AliasSet setLeft = visit(ast.left(), null);
			AliasSet setRight = visit(ast.right(), null);

			setLeft.unify(setRight);

			return null;
		}

		@Override
		public AliasSet returnStmt(ReturnStmt ast, Void arg) {
			AliasSet result = visitChildren(ast, null);
			if (result != null) {
				result.unify(methodContext.result);
			}
			return null;
		}

		/**
		 * Helper function to generate AliasContext for MethodCall[Expr] nodes
		 */
		private AliasContext createSiteContext(Ast call) {
			AliasSet receiver, result;
			ArrayList<AliasSet> parameters = new ArrayList<>();

			if (call instanceof MethodCall) {
				// no return type
				result = AliasSet.BOTTOM;

				// for all arguments, generate alias sets
				receiver = visit(((MethodCall) call).receiver(), null);
				for (Expr param : ((MethodCall) call).argumentsWithoutReceiver()) {
					parameters.add(visit(param, null));
				}
			} else if (call instanceof MethodCallExpr) {
				// determine if we need an alias set
				if (((MethodCallExpr) call).sym.returnType.isReferenceType()) {
					result = new AliasSet();
				} else {
					result = AliasSet.BOTTOM;
				}

				// for all arguments, generate alias sets
				receiver = visit(((MethodCallExpr) call).receiver(), null);
				for (Expr param : ((MethodCallExpr) call).argumentsWithoutReceiver()) {
					parameters.add(visit(param, null));
				}
			} else {
				throw new IllegalArgumentException("argument must be method call");
			}

			AliasContext sc = new AliasContext(receiver, parameters, result);
			siteContexts.put(call, sc);
			return sc;
		}

		private void methodInvocation(MethodSymbol m, AliasContext sc) {
			for (MethodSymbol p : callGraph.targets.get(m)) {
				AliasContext mc = methodContexts.get(p);
				if (scc.stronglyConnected(method, p)) {
					sc.unify(mc);
				} else {
					sc.unify(mc.deepCopy());
				}
			}
		}
		
		@Override
		public AliasSet methodCall(MethodCall ast, Void arg) {
			AliasContext sc = createSiteContext(ast);
			
			if (ast.sym == threadStart) {
				threadStarts.add(ast);
			} else if (ast.sym == objectLock || ast.sym == objectUnlock) {
				sc.receiver.setLocked(true);
			} else {
				methodInvocation(ast.sym, sc);
			}
			return null;
		}

		@Override
		public AliasSet methodCall(MethodCallExpr ast, Void arg) {
			AliasContext sc = createSiteContext(ast);
			methodInvocation(ast.sym, sc);
			return sc.result;
		}
	}
	
}
