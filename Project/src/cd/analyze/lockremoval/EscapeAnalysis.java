package cd.analyze.lockremoval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cd.Main;
import cd.analyze.lockremoval.AliasSet.AliasSetData;
import cd.analyze.lockremoval.CallGraphGenerator.CallGraph;
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

		public AliasContext deepCopy() {
			return new AliasContext(this);
		}

// 		This code would be needed for method specialization, but breaks 
//		current use of HashMap (which compares by reference)

//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result
//					+ ((parameters == null) ? 0 : parameters.hashCode());
//			result = prime * result
//					+ ((receiver == null) ? 0 : receiver.hashCode());
//			result = prime * result
//					+ ((this.result == null) ? 0 : this.result.hashCode());
//			return result;
//		}
//
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj) {
//				return true;
//			}
//			if (obj == null) {
//				return false;
//			}
//			if (getClass() != obj.getClass()) {
//				return false;
//			}
//			AliasContext other = (AliasContext) obj;
//
//			if (parameters == null) {
//				if (other.parameters != null) {
//					return false;
//				}
//			} else if (!parameters.equals(other.parameters)) {
//				return false;
//			}
//			if (receiver == null) {
//				if (other.receiver != null) {
//					return false;
//				}
//			} else if (!receiver.equals(other.receiver)) {
//				return false;
//			}
//			if (result == null) {
//				if (other.result != null) {
//					return false;
//				}
//			} else if (!result.equals(other.result)) {
//				return false;
//			}
//			return true;
//		}

		@Override
		public String toString() {
			return "[receiver=" + receiver + ", parameters="
					+ parameters + ", result=" + result + "]";
		}
	}
	
	private Main main;
	private CallGraphSCC scc;
	
	private CallGraph callGraph;
	private Map<Ast, Boolean> multiSites;
	private MethodSymbol threadStart, objectNotify, objectWait;
	
	public EscapeAnalysis(Main main) {
		this.main = main;
		this.threadStart = main.threadType.getMethod("start");
		this.objectNotify = main.objectType.getMethod("notify");
		this.objectWait = main.objectType.getMethod("wait");
	}
	
	public void analyze(List<ClassDecl> astRoots) {
		//// Phase 1 ////
		
		// generate call graph
		callGraph = new CallGraphGenerator(main).compute(astRoots);

		// generate scc
		scc = new CallGraphSCC(callGraph);
		
		// find multiply executed thread allocation sites
		multiSites = findThreadAllocationSites();

		//// Phase 2 ////
		// traverse SCC methods in bottom-up topological order
		Map<MethodSymbol, AliasContext> methodContexts = analyzeBottomUp();

		//// Phase 3 ////
		// top-down traversal to push down escape info
		mergeTopDown(methodContexts);
	}


	private Map<MethodSymbol, AliasContext> analyzeBottomUp() {
		HashMap<MethodSymbol, AliasContext> methodContexts = new HashMap<>();
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
				MethodAnalayzer visitor = new BottomUpAnalyzer(method, methodContexts);
				visitor.analyize();
			}
		}
		
		return methodContexts;
	}
	
//	The following code is supposed to find summaries for method specialization.
//	Currently broken, as it unifies wrong alias sets.
	
//	private static class MethodCallRequest {
//		private final AliasContext methodContext;
//		private final MethodSymbol method;
//		public MethodCallRequest(AliasContext methodContext,
//				MethodSymbol method) {
//			this.methodContext = methodContext;
//			this.method = method;
//		}
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result
//					+ ((method == null) ? 0 : method.hashCode());
//			result = prime * result
//					+ ((methodContext == null) ? 0 : methodContext.hashCode());
//			return result;
//		}
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj) {
//				return true;
//			}
//			if (obj == null) {
//				return false;
//			}
//			if (getClass() != obj.getClass()) {
//				return false;
//			}
//			MethodCallRequest other = (MethodCallRequest) obj;
//			if (method == null) {
//				if (other.method != null) {
//					return false;
//				}
//			} else if (!method.equals(other.method)) {
//				return false;
//			}
//			if (methodContext == null) {
//				if (other.methodContext != null) {
//					return false;
//				}
//			} else if (!methodContext.equals(other.methodContext)) {
//				return false;
//			}
//			return true;
//		}
//		@Override
//		public String toString() {
//			return "MethodCallRequest [methodContext=" + methodContext
//					+ ", method=" + method.fullName() + "]";
//		}
//	}
//
//	private void mergeTopDownSpecialized(Map<MethodSymbol, AliasContext> methodContexts) {
//		final Queue<MethodCallRequest> queue = new ArrayDeque<>();
//		final Map<Ast, AliasContext> siteContexts = new HashMap<>();
//		
//		final Map<MethodCallRequest, List<Ast>> methodCalls = new HashMap<>();
//		final Map<List<MethodCall>, List<MethodCallRequest>> specializedMethods = new HashMap<>();
//
//		for (MethodSymbol root : callGraph.roots) {
//			AliasContext mc = methodContexts.get(root);
//			queue.add(new MethodCallRequest(mc, root));
//		}
//		
//		while (!queue.isEmpty()) {
//			MethodCallRequest req = queue.poll();
//			final List<MethodCall> removalSummary = new ArrayList<>();
//			
//			new MethodAnalayzer(req.method, req.methodContext.deepCopy(), methodContexts) {
//				private void syncInvocation(MethodCall ast, AliasContext sc) {
//					if (!sc.receiver.escapes()) {
//						removalSummary.add(ast);
//					}
//				}
//				
//				private void methodInvocation(MethodSymbol m, AliasContext sc, Ast ast) {
//					AliasContext prevSC = siteContexts.get(ast);
//					if (prevSC != null) {
//						sc.unify(prevSC);
//					}
//
//					for (MethodSymbol p : callGraph.targets.get(m)) {
//						if (!main.isBuiltinMethod(p)) {
//							AliasContext mc = methodContexts.get(p).deepCopy();
//							sc.unify(mc);
//							MethodCallRequest callee = new MethodCallRequest(mc.deepCopy(), p);
//							if (!methodCalls.containsKey(callee)) {
//								queue.add(callee);
//								methodCalls.put(callee, new ArrayList<Ast>());
//							}
//							
//							methodCalls.get(callee).add(ast);
//						}
//					}
//
//					siteContexts.put(ast, sc);
//				}
//				
//				@Override
//				public AliasSet methodCall(MethodCall ast, Void arg) {
//					AliasContext sc = createSiteContext(ast);
//					if (ast.sym.owner == main.objectType) {
//						syncInvocation(ast, sc);
//					} else {
//						methodInvocation(ast.sym, sc, ast);
//					}
//					return null;
//				}
//
//				@Override
//				public AliasSet methodCall(MethodCallExpr ast, Void arg) {
//					AliasContext sc = createSiteContext(ast);
//					methodInvocation(ast.sym, sc, ast);
//					return sc.result;
//				}
//			}.analyize();
//			
//			if (!removalSummary.isEmpty()) {
//				if (specializedMethods.containsKey(removalSummary)) {
//					specializedMethods.get(removalSummary).add(req);
//				} else {
//					List<MethodCallRequest> list = new ArrayList<>();
//					list.add(req);
//					specializedMethods.put(removalSummary, list);
//				}
//				System.err.format("%s: %x\n", req, removalSummary.hashCode());
//			}
//		}
//
//		System.err.println(specializedMethods);
//	}


	private void mergeTopDown(Map<MethodSymbol, AliasContext> methodContexts) {
		List<Set<MethodSymbol>> reversed = new LinkedList<>(scc.getSortedComponents());
		Collections.reverse(reversed);
		for (Set <MethodSymbol> component : reversed) {
			for (final MethodSymbol method : component) {

				new MethodAnalayzer(method, methodContexts) {
					private void methodInvocation(MethodSymbol m, AliasContext sc) {
						for (MethodSymbol p : callGraph.targets.get(m)) {
							if (!main.isBuiltinMethod(p)) {
								AliasContext mc = methodContexts.get(p);
								sc.unify(mc);
							}
						}
					}

					@Override
					public AliasSet methodCall(MethodCall ast, Void arg) {
						AliasContext sc = createSiteContext(ast);
						methodInvocation(ast.sym, sc);
						return null;
					}

					@Override
					public AliasSet methodCall(MethodCallExpr ast, Void arg) {
						AliasContext sc = createSiteContext(ast);
						methodInvocation(ast.sym, sc);
						return sc.result;
					}
				}.analyize();

			}
		}
	}


	private Map<Ast, Boolean> findThreadAllocationSites() {
		final Map<Ast, Boolean> multiSite = new HashMap<>();
		for (MethodSymbol sym : callGraph.graph.keySet()) {
			if (main.isBuiltinMethod(sym)) {
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

	private abstract class MethodAnalayzer extends AstVisitor<AliasSet, Void> {
		protected final HashMap<VariableSymbol, AliasSet> as = new HashMap<>();
		protected final Map<MethodSymbol, AliasContext> methodContexts;
		protected final AliasContext methodContext;
		protected final MethodSymbol method;
	
		public MethodAnalayzer(MethodSymbol method, Map<MethodSymbol, AliasContext> methodContexts) {
			this(method, methodContexts.get(method), methodContexts);
		}
		
		public MethodAnalayzer(MethodSymbol method, AliasContext mc, Map<MethodSymbol, AliasContext> methodContexts) {
			this.methodContexts = methodContexts;

			// add alias sets of parameter to lookup for variables
			for (int i=0; i < method.parameters.size(); i++) {
				as.put(method.parameters.get(i), mc.parameters.get(i));
			}

			this.methodContext = mc;
			this.method = method;
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
			if (main.isBuiltinMethod(method)) return;
			
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
		}
		
		/**
		 * Helper function to generate AliasContext for MethodCall[Expr] nodes
		 */
		protected AliasContext createSiteContext(Ast call) {
			MethodSymbol sym;
			Expr recvExpr;
			List<Expr> paramExpr;

			if (call instanceof MethodCall) {
				sym = ((MethodCall) call).sym;
				recvExpr = ((MethodCall) call).receiver();
				paramExpr = ((MethodCall) call).argumentsWithoutReceiver();
			} else if (call instanceof MethodCallExpr) {
				sym = ((MethodCallExpr) call).sym;
				recvExpr = ((MethodCallExpr) call).receiver();
				paramExpr = ((MethodCallExpr) call).argumentsWithoutReceiver();
			} else {
				throw new IllegalArgumentException("argument must be method call");
			}

			AliasSet receiver, result;
			ArrayList<AliasSet> parameters = new ArrayList<>();
			result = sym.returnType.isReferenceType() ? new AliasSet() : AliasSet.BOTTOM;
			receiver = visit(recvExpr, null);
			for (Expr param : paramExpr) {
				parameters.add(visit(param, null));
			}

			AliasContext sc = new AliasContext(receiver, parameters, result);
			return sc;
		}


		@Override
		public AliasSet visit(Expr expr, Void arg) {
			// set alias set for codegen
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
	}
	
	private class BottomUpAnalyzer extends MethodAnalayzer {
		
		public BottomUpAnalyzer(MethodSymbol method, 
				Map<MethodSymbol, AliasContext> methodContexts) {
			super(method, methodContexts);
		}

		private void visitThreadStart(MethodCall threadStart, AliasContext sc) {
			ClassSymbol thread = ((ClassSymbol)threadStart.receiver().type);
			MethodSymbol threadRun = thread.getMethod("run");

			// mark thread as escaped
			sc.receiver.setEscapes(true);

			// make sure to escape `this` in thread entry
			for (MethodSymbol entry: callGraph.targets.get(threadRun)) {
				methodContexts.get(entry).receiver.unify(sc.receiver);
			}

			// unify thread with itself if started multiple times.
			// this is currently not needed and does nothing
			// but would enable a more sophisticated escape definition 
			// as described by Ranganath et al if unify() was changed
			if (multiSites.get(threadStart))  {
				sc.unify(sc);
			}
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
				visitThreadStart(ast, sc);
			} else if (ast.sym == objectNotify || ast.sym == objectWait) {
				sc.receiver.setEscapes(true);
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
