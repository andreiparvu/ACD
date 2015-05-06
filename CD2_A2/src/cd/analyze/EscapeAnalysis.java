package cd.analyze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cd.Main;
import cd.analyze.CallGraphGenerator.CallGraph;
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
import cd.ir.Symbol.TypeSymbol;
import cd.ir.Symbol.VariableSymbol;

public class EscapeAnalysis {

	static class AliasSet {
		static class AliasSetData {
			private boolean escapes = false;
			private final Map<String, AliasSet> fieldMap = new HashMap<>();
		}
		
		private AliasSetData ref = new AliasSetData();
		
		public static AliasSet BOTTOM = new AliasSet();
		static { BOTTOM.ref = null;	}

		public static AliasSet forType(TypeSymbol type) {
			if (type.isReferenceType()) {
				return new AliasSet();
			}
			return BOTTOM;
		}

		void unify(AliasSet other) {
			if (this.ref == other.ref) return;

			Map<String, AliasSet> thisFields = this.ref.fieldMap;
			Map<String, AliasSet> otherFields = other.ref.fieldMap;

			this.ref.escapes |= other.ref.escapes;

			Set<String> fieldUnion = new HashSet<>(thisFields.keySet());
			fieldUnion.addAll(otherFields.keySet());

			for (String field : fieldUnion) {
				AliasSet thisSet = thisFields.get(field);
				AliasSet otherSet = otherFields.get(field);
				if (thisSet != null && otherSet != null) {
					// field in both maps, unify them
					//thisSet.setEscapes(this.ref.escapes);
					thisSet.ref.escapes |= this.ref.escapes;
					thisSet.unify(otherSet);
				} else if (thisSet == null) {
					// missing in this
					//otherSet.setEscapes(this.ref.escapes);
					otherSet.ref.escapes |= this.ref.escapes;
					thisFields.put(field, otherSet);
				}
				// we don't care if otherSet is null, `other` will be deleted
			}

			// `this` is the unified alias set
			other.ref = this.ref;
		}
		
		void unifyEscapes(AliasSet other) {
			if (this.ref == other.ref) return;
			this.ref.escapes |= other.ref.escapes;

			Map<String, AliasSet> thisFields = this.ref.fieldMap;
			Map<String, AliasSet> otherFields = other.ref.fieldMap;

			Set<String> intersection = new HashSet<>(thisFields.keySet());
			intersection.retainAll(otherFields.keySet());

			for (String field : intersection) {
				thisFields.get(field).unifyEscapes(otherFields.get(field));
			}
		}
		
		public AliasSet deepCopy() {
			if (this.isBottom()) return BOTTOM;
			
			AliasSet copy = new AliasSet();
			copy.ref.escapes = this.ref.escapes;
			for (Entry<String, AliasSet> entry : ref.fieldMap.entrySet()) {
				AliasSet sub = entry.getValue();
				if (sub == this) {
					sub = copy;
				} else {
					sub = sub.deepCopy();
				}
					// TODO this is probably not enough to deal with selfcontaining sets
				copy.ref.fieldMap.put(entry.getKey(), sub);
			}
			return copy;
		}
		
		public boolean isBottom() {
			return this.ref == null;
		}

		public AliasSet fieldMap(String key) {
			if (!this.ref.fieldMap.containsKey(key)) {
				this.ref.fieldMap.put(key, new AliasSet());
			}
			return this.ref.fieldMap.get(key);
		}
		
		public boolean escapes() {
			return this.ref == null ? false : this.ref.escapes;
		}
		
		public void setEscapes(boolean value) {
			this.ref.escapes = value;
			for (AliasSet children : this.ref.fieldMap.values()) {
				children.setEscapes(value);
			}
		}
		
		@Override
		public String toString() {
			if (isBottom()) {
				return "⊥";
			} else {
				return String.format("<%s, %s>", ref.escapes, ref.fieldMap.keySet());
			}
		}
	}

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
			this.receiver = copy.receiver.deepCopy();
			this.result = copy.result.deepCopy();
			this.parameters = new ArrayList<>(copy.parameters.size());
			for (AliasSet param : copy.parameters) {
				this.parameters.add(param.deepCopy());
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
		
		public void unifyEscapes(AliasContext other) {
			// TODO there sees to be a bug here!
			System.out.println(this.toString());
			System.out.println(other.toString());
			
			this.receiver.unifyEscapes(other.receiver);
			this.result.unifyEscapes(other.result);
			assert (parameters.size() == other.parameters.size());
			for (int i=0; i < parameters.size(); i++) {
				parameters.get(i).unifyEscapes(other.parameters.get(i));
			}
		}

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

	public EscapeAnalysis(Main main) {
		this.main = main;
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
		for (Entry<MethodSymbol, AliasContext> entry : methodContexts.entrySet()) {
			System.err.println(entry.getKey().fullName() + ": " + entry.getValue());
		}
	}

	private void analyzeBottomUp() {
		for (Set<MethodSymbol> component : scc.getSortedComponents()) {
			// create method context for all methods in component
			for (MethodSymbol method : component) {
				methodContexts.put(method, new AliasContext(method));
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
			for (MethodSymbol method : component) {
				new AstVisitor<Void, Void>() {
					@Override
					public Void methodCall(MethodCall ast, Void arg) {
						AliasContext mc = methodContexts.get(ast.sym);
						AliasContext sc = siteContexts.get(ast);
						sc.unifyEscapes(mc);
						return super.methodCall(ast, arg);
					}

					@Override
					public Void methodCall(MethodCallExpr ast, Void arg) {
						AliasContext mc = methodContexts.get(ast.sym);
						AliasContext sc = siteContexts.get(ast);
						sc.unifyEscapes(mc);
						return super.methodCall(ast, arg);
					}
				}.visit(method.ast, null);
			}
		}
	}


	private Map<Ast, Boolean> findThreadAllocationSites() {
		final MethodSymbol threadStart = main.threadType.getMethod("start");
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
		private final List<MethodCall> threadStarts = new ArrayList<>();	
		
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
			for(BasicBlock bb : method.ast.cfg.allBlocks) {
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
		public AliasSet visit(Ast ast, Void arg) {
			System.err.println(AstOneLine.toString(ast));
			return super.visit(ast, arg);
		}
		
		@Override
		public AliasSet visit(Expr ast, Void arg) {
			System.err.println(AstOneLine.toString(ast));
			return super.visit(ast, arg);
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

		private AliasContext createSiteContext(Ast call) {
			AliasSet receiver, result;
			ArrayList<AliasSet> parameters = new ArrayList<>();

			if (call instanceof MethodCall) {
				result = AliasSet.BOTTOM;
				receiver = visit(((MethodCall) call).receiver(), null);
				for (Expr param : ((MethodCall) call).argumentsWithoutReceiver()) {
					parameters.add(visit(param, null));
				}
			} else if (call instanceof MethodCallExpr) {
				if (((MethodCallExpr) call).sym.returnType.isReferenceType()) {
					result = new AliasSet();
				} else {
					result = AliasSet.BOTTOM;
				}
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
			methodInvocation(ast.sym, sc);
			
			if (ast.sym == main.threadType.getMethod("start")) {
				sc.receiver.setEscapes(true);
				if (multiSites.get(ast))  {
					sc.unify(sc);
				}
			}
			return null;
		}

		@Override
		public AliasSet methodCall(MethodCallExpr ast, Void arg) {
			AliasContext sc = createSiteContext(ast);
			methodInvocation(ast.sym, sc);
			return sc.result;
		}
		
		/*public void visitThreadStart() {
			for (MethodCall call : threadStarts) {
				AliasContext mc = methodContexts.get(call.sym);

				sc.receiver.setEscapes(true);

				// no method can be strongly connected to Thread.start()
				sc.unify(mc.deepCopy());
				if (multiExec) {
					sc.unify(sc);
				}
			}
			
		}*/
	}
}
