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
import cd.ir.Ast.MethodCall;
import cd.ir.Ast.MethodCallExpr;
import cd.ir.Ast.ReturnStmt;
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
			private Map<String, AliasSet> fieldMap = new HashMap<>();
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
					thisSet.setEscapes(this.ref.escapes);
					thisSet.unify(otherSet);
				} else if (thisSet == null) {
					// missing in this
					otherSet.setEscapes(this.ref.escapes);
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
				copy.ref.fieldMap.put(entry.getKey(), entry.getValue().deepCopy());
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
				return String.format("<%s, %s>", ref.escapes, ref.fieldMap);
			}
		}
	}

	class AliasContext {
		public AliasSet receiver;
		public ArrayList<AliasSet> parameters;
		public AliasSet result;

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

	private class MethodAnalayzer extends AstVisitor<Void, Void> {
		private final HashMap<VariableSymbol, AliasSet> as = new HashMap<>();
		private final AliasSet result;
		private final MethodSymbol method;
		private final List<MethodCall> threadStarts = new ArrayList<>();	
		
		public MethodAnalayzer(MethodSymbol method) {
			AliasContext mc = methodContexts.get(method);
			this.result = mc.result;
			this.method = method;

			// add alias sets of parameter to lookup for variables
			for (int i=0; i < method.parameters.size(); i++) {
				as.put(method.parameters.get(i), mc.parameters.get(i));
			}
		}
		
		public void analyize() {
			for(BasicBlock bb : method.ast.cfg.allBlocks) {
				for (Phi phi : bb.phis.values()) {
					AliasSet asV = getAS(phi.lhs);
					for (Expr expr : phi.rhs) {
						if (expr instanceof Var) {
							VariableSymbol vi = ((Var)expr).sym;
							asV.unify(getAS(vi));
						}
					}
				}

				if (bb.condition != null) {
					visit(bb.condition, null);
				}

				for (Ast ast : bb.instructions) {
					visit(ast, null);
				}

				visitThreadStart();
			}
		}

		private AliasSet getAS(VariableSymbol var) {
			if (!as.containsKey(var)) {
				as.put(var, AliasSet.forType(var.type));
			}
			return as.get(var);
		}

		@Override
		public Void assign(Assign ast, Void arg) {
			AliasSet asLeft = null, asRight = null;

			if (!ast.left().type.isReferenceType()) return null;
			if (!ast.right().type.isReferenceType()) return null;

			if (ast.left() instanceof Var) {
				asLeft = getAS( ((Var)ast.left()).sym );
				if (ast.right() instanceof Var) {
					// v1 = v2
					asRight = getAS( ((Var)ast.right()).sym );
				} else if (ast.right() instanceof Ast.Cast) {
					// v1 = (T) v2;
					Ast.Cast cast = (Ast.Cast) ast.right();
					if (cast.arg() instanceof Var) {
						asRight = getAS( ((Var)cast.arg()).sym );
					}
				} else if (ast.right() instanceof Ast.Field) {
					// v1 = v2.f
					Ast.Field field = (Ast.Field) ast.right();
					if (field.arg() instanceof Var) {
						asRight = getAS( ((Var)field.arg()).sym ).fieldMap(field.fieldName);
					}
				} else if (ast.right() instanceof Ast.Index) {
					// v1 = v2[..]
					Ast.Index index = (Ast.Index) ast.right();
					if (index.left() instanceof Var) {
						asRight = getAS( ((Var)index.left()).sym ).fieldMap("$ELT");
					}
				}
			} else if (ast.right() instanceof Var) {
				asRight = getAS( ((Var)ast.right()).sym );
				if (ast.left() instanceof Ast.Field) {
					// v1.f = v2;
					Ast.Field field = (Ast.Field) ast.left();
					if (field.arg() instanceof Var) {
						asLeft = getAS( ((Var)field.arg()).sym ).fieldMap(field.fieldName);
					}
				} else if (ast.left() instanceof Ast.Index) {
					// v1[..] = v2;
					Ast.Index index = (Ast.Index) ast.left();
					if (index.left() instanceof Var) {
						asLeft = getAS( ((Var)index.left()).sym ).fieldMap("$ELT");
					}
				}
			}

			if (asLeft != null && asRight != null) {
				System.err.println(asLeft);
				System.err.println(asRight);

				asLeft.unify(asRight);
			}

			return super.assign(ast, arg);
		}

		@Override
		public Void visit(Ast ast, Void arg) {
			System.err.println(AstOneLine.toString(ast));
			return super.visit(ast, arg);
		}

		@Override
		public Void returnStmt(ReturnStmt ast, Void arg) {
			if (ast.arg() instanceof Var) {
				VariableSymbol v = ((Var)ast.arg()).sym;
				getAS(v).unify(this.result);
				return null;
			} else {
				return super.returnStmt(ast, arg);
			}
		}
		
		// TODO deal with return value
		private AliasContext createSiteContext(MethodSymbol sym, Expr receiver, List<Expr> arguments) {
			AliasContext ctx = new AliasContext(sym);
			
			if (receiver instanceof Var) {
				VariableSymbol varSym = ((Var)receiver).sym;
				ctx.receiver = getAS(varSym);
			}
			
			for (int i=0; i < arguments.size(); i++) {
				if (arguments.get(i) instanceof Var) {
					VariableSymbol varSym = ((Var)arguments.get(i)).sym;
					ctx.parameters.set(i, getAS(varSym));
				}
			}

			return ctx;
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
		public Void methodCall(MethodCall ast, Void arg) {
			if (ast.sym == main.threadType.getMethod("start")) {
				threadStarts.add(ast);
			} else {
				AliasContext sc = createSiteContext(ast.sym, ast.receiver(), ast.argumentsWithoutReceiver());
				siteContexts.put(ast, sc);
				methodInvocation(ast.sym, sc);
			}

			return super.methodCall(ast, arg);
		}

		@Override
		public Void methodCall(MethodCallExpr ast, Void arg) {
			AliasContext sc = createSiteContext(ast.sym, ast.receiver(), ast.argumentsWithoutReceiver());
			siteContexts.put(ast, sc);

			methodInvocation(ast.sym, sc);
			return super.methodCall(ast, arg);
		}
		
		public void visitThreadStart() {
			for (MethodCall call : threadStarts) {
				AliasContext mc = methodContexts.get(call.sym);
				boolean multiExec = multiSites.get(call);

				AliasSet receiver = new AliasSet();
				if (call.receiver() instanceof Var) {
					receiver = getAS(((Var)call.receiver()).sym);
				}

				receiver.setEscapes(true);

				AliasContext sc = new AliasContext(receiver, new ArrayList<AliasSet>(), AliasSet.BOTTOM);
				siteContexts.put(call, sc);

				sc.unify(mc.deepCopy());

				if (multiExec) {
					sc.unify(sc);
				}
			}
			
		}
	}
}
