package cd.cfg;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cd.Main;
import cd.debug.AstOneLine;
import cd.ir.Ast;
import cd.ir.Ast.Expr;
import cd.ir.Ast.Field;
import cd.ir.Ast.Index;
import cd.ir.Ast.MethodCall;
import cd.ir.Ast.MethodCallExpr;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.NewArray;
import cd.ir.Ast.NewObject;
import cd.ir.Ast.ThisRef;
import cd.ir.Ast.Var;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.ir.Symbol.ArrayTypeSymbol;
import cd.ir.Symbol.ClassSymbol;
import cd.ir.Symbol.TypeSymbol;
import cd.ir.Symbol.VariableSymbol;
import cd.util.Pair;

public class EscapeAnalyzer {
	public final Main main;

	public final static int WHITE = 0, GREY = 1, BLACK = 2;
	
	private MethodDecl mdecl;
	private PrintWriter pw;
	
	public EscapeAnalyzer(Main main) {
		this.main = main;
	}
	
	static int prt = 0;
	
	private static int nodeCount = 0;
	private static int clusterInd = 0;
	
	private final static String THREAD_START = "start", THREAD_RUN = "run", THREAD_JOIN = "join";
	
	private class GraphNode {
		int index;
		
		final static String ARRAY = "array", REF_PARAM="ref_param", ESCAPED="escaped",
				THREAD="thread", THIS="this", RETURNED = "returned";
		
		HashSet<String> properties = new HashSet<>();
		
		boolean dfsColor = false, writeColor = false;
		
		GraphNode copy = null;
		
		HashMap<String, HashMap<Integer, GraphNode>> outNodes = new HashMap<>();

		GraphNode(String... props) {
			
			for (String prop : props) {
				properties.add(prop);
			}
			
			index = ++nodeCount;
		}
		
		GraphNode(int index, Set<String> properties) {
			this.index = index;
			this.properties = new HashSet<>(properties);
		}
		
		GraphNode() {
			index = ++nodeCount;
		}
		
		GraphNode addReference(String label) {
			return addReference(label, null);
		}
		
		public void setThread() {
			properties.add(THREAD);
		}
		
		public void setArray() {
			properties.add(ARRAY);
		}
		
		public void setEscaped() {
			properties.add(ESCAPED);
		}
		
		public void setReturned() {
			properties.add(RETURNED);
		}
		
		boolean containsProps(String... props) {
			for (String prop : props) {
				if (properties.contains(prop)) {
					return true;
				}
			}
			
			return false;
		}
		
		GraphNode addReference(String label, GraphNode n) {
			if (outNodes.containsKey(label) == false) {
				outNodes.put(label, new HashMap<Integer, GraphNode>());
			}
			
			if (n == null) {
				n = new GraphNode();
			}
			
			outNodes.get(label).put(n.index, n);
			
			return n;
		}
		
		void clearReference(String label) {
			if (outNodes.containsKey(label)) {
				outNodes.get(label).clear();
			}
		}
		
		boolean notAlloc() {
			return !(properties.size() == 0 ||
					(properties.size() == 1 && properties.contains("thread"))); // might check this
		}
		
		GraphNode deepCopy() {
			if (copy == null) {
				copy = new GraphNode(index, properties);
				
				for (String label : outNodes.keySet()) {
					HashMap<Integer, GraphNode> list = new HashMap<>();
					
					for (GraphNode next : outNodes.get(label).values()) {
						list.put(next.index, next.deepCopy());
					}
					
					copy.outNodes.put(label, list);
				}
			}
			
			return copy;
		}
		
		void eraseCopy() {
			if (copy != null) {
				copy = null;
				
				for (String label : outNodes.keySet()) {
					for (GraphNode next : outNodes.get(label).values()) {
						next.eraseCopy();
					}
				}
			}
		}
		
		boolean merge(GraphNode node, HashSet<Integer> visited) {
			boolean ret = false;
		
			if (visited.contains(node.index)) {
				return false;
			}
		
			visited.add(node.index);
			
			for (String label : node.outNodes.keySet()) {
				if (outNodes.containsKey(label)) {
					for (GraphNode n : node.outNodes.get(label).values()) {
						if (outNodes.get(label).containsKey(n.index)) {
							ret = outNodes.get(label).get(n.index).merge(n, visited) || ret;
						} else {
							outNodes.get(label).put(n.index, n.deepCopy());
							ret = true;
						}
					}
				} else {
					ret = true;
					HashMap<Integer, GraphNode> list = new HashMap<>();
					
					for (GraphNode n : node.outNodes.get(label).values()) {
						list.put(n.index, n.deepCopy());
					}
					outNodes.put(label, list);
				}
			}
			
			return ret;
		}

		void buildNodes(String path, Set<GraphNode> results) {
			Pair<Integer> next = computeNextLabel(path);
			boolean addArray = false;
			
			if (next.a != path.length() && path.charAt(next.a) == '[') {
				// we need to add the array property if we encounter the indexing parameter
				addArray = true;
			}
			
			HashMap<Integer, GraphNode> list = outNodes.get(path.substring(0, next.a));
			
			if (list == null) {
				addReference(path.substring(0, next.a));
				list = outNodes.get(path.substring(0, next.a));
			}
			
			for (GraphNode nextNode : list.values()) {
				if (addArray) {
					nextNode.properties.add(ARRAY);
				}
				
				if (next.b == path.length()) {
					results.add(nextNode);
				} else {
					nextNode.buildNodes(path.substring(next.b + 1), results);
				}
			}
		}
		
		boolean updateState(GraphNode predecessor) {
			boolean modified = false;
			
			for (String prop : predecessor.properties) {
				if (properties.contains(prop) == false) {
					properties.add(prop);
					modified = true;
				}
			}
			
			return modified;
		}
		
		private void printProperties() {
			System.err.println(index);
			
			for (String x : properties) {
				System.err.println(x);
			}
		}
		
		void dfs() {
			dfsColor = true;
//			printProperties();
			
			for (String label : outNodes.keySet()) {
//				System.err.println("label " + label);
				for (GraphNode next : outNodes.get(label).values()) {
					if (next.updateState(this) || !next.dfsColor) {
						next.dfs();
					}
				}
			}
		}
		
		void write(String name, PrintWriter out) {
			writeColor = true;
			
			out.write(String.format("%d[label=\"%d: ", index, index));
			for (String prop : properties) {
				out.write(prop + " ");
			}
//			printProperties();
			
			out.write("\"];\n");
			
			for (String label : outNodes.keySet()) {
				for (GraphNode next : outNodes.get(label).values()) {
					out.write(String.format("%d -> %d[label=%s];\n", index, next.index, label));
					
					if (!next.writeColor) {
						next.write(name + "." + label, out);
					}
				}
			}
		}
		
		void checkThread(Graph threadGraph, String prefix) {
			// does not support cycles
			
			for (GraphNode threadNode : threadGraph.buildNodes(prefix)) {
//				System.err.println("-- " + prefix + " " + threadNode.properties);
				if (threadNode.properties.size() > 1) { // they must have the 'this' property
					this.properties.add("escaped");
					return ;
				}
			}

			for (String label : outNodes.keySet()) {
				for (GraphNode nextNode : outNodes.get(label).values()) {
					nextNode.checkThread(threadGraph, prefix + "." + label);
				}
			}
		}
	}
	
	public class Graph {
		Map<String, List<GraphNode>> nodes = new HashMap<>();
		Set<GraphNode> returnSet = new HashSet<>();
		int curBB; // used for processing
		
		Graph deepCopy() {
			Graph g = new Graph();
			
			for (String n : nodes.keySet()) {
				for (GraphNode node : nodes.get(n)) {
					g.put(n, node.deepCopy());
				}
			}
			
			for (String n : nodes.keySet()) {
				for (GraphNode node : nodes.get(n)) {
					node.eraseCopy();
				}
			}
			
			return g;
		}
		
		void clearName(String name) {
			nodes.get(name).clear();
		}
		
		void put(String name, GraphNode n) {
			if (!nodes.containsKey(name)) {
				nodes.put(name, new ArrayList<GraphNode>());
			}
			nodes.get(name).add(n);
		}
		
		boolean nodeHasProp(String name, String... props) {
			boolean rez = false;
			
			for (GraphNode node : nodes.get(name)) {
				rez = node.containsProps(props) || rez;
			}
			
			return rez;
		}
			
		boolean merge(Graph g) {
			boolean ret = false;
			
			for (String n : g.nodes.keySet()) {
				if (nodes.containsKey(n)) {
					List<GraphNode> glist = g.nodes.get(n), list = nodes.get(n);
					
					for (int i = 0; i < glist.size(); i++) {
						if (glist.size() > list.size()) {
							list.add(glist.get(i));
							ret = true;
						} else {
							ret = list.get(i).merge(glist.get(i), new HashSet<Integer>()) || ret;
						}
					}
				} else {
					for (GraphNode node : g.nodes.get(n)) {
						put(n, node.deepCopy());
					}
					ret = true;
				}
			}
			
			return ret;
		}
		
		Set<GraphNode> buildNodes(String path) {
			HashSet<GraphNode> results = new HashSet<>();
			
			buildNodes(path, results);
			
			return results;
		}
		
		void buildNodes(String path, Set<GraphNode> results) {
			Pair<Integer> next = computeNextLabel(path);
			
			for (GraphNode n : nodes.get(path.substring(0, next.a))) {
				if (next.b == path.length()) {
					results.add(n);
				} else {
					n.buildNodes(path.substring(next.b + 1), results);
				}
			}
		}
		
		void dfs() {
			for (String  n : nodes.keySet()) {
				for (GraphNode node : nodes.get(n)) {
					node.dfs();
				}
			}
		}
		
		void checkThread(String path, Graph threadGraph) {
			for (GraphNode node : nodes.get(path)) {
				node.checkThread(threadGraph, "this");
			}
		}
		
		void write(PrintWriter out) {
			for (String n : nodes.keySet()) {
				for (GraphNode node : nodes.get(n)) {
					out.write(String.format("%s_%s_%s -> %d;\n", mdecl.sym.owner, mdecl.sym.name, n, node.index));
					if (!node.writeColor) {
						node.write(n, out);
					}
				}
			}
		}
	}
	
	public Pair<Integer> computeNextLabel(String path) {
		int x = path.indexOf('.'), y = path.indexOf('[');
		
		if (x == -1) {
			x = path.length();
		}
		if (y == -1) {
			y = path.length();
		}
		
		int last = x;
		if (y < x) {
			x = y;
		}
		
		return new Pair<Integer>(x, last);
	}
	
	private boolean isThreadClass(ClassSymbol cs) {
		return cs.superClass.name.equals("Thread");
	}
	
	private boolean inheritsThread(ClassSymbol curClass) {
		
		for (; curClass != null; curClass = curClass.superClass) {
			if (curClass.name.equals("Thread")) {
				return true;
			}
		}
		
		return false;
	}
	
	private HashMap<Integer, HashSet<String>> joins = new HashMap<>();
	
	public void compute(MethodDecl md, PrintWriter pw) {
		System.err.println("Checking " + md.name);
		
		this.mdecl = md;
		this.pw = pw;
		
		mdecl.analyzedColor = GREY;
		ControlFlowGraph cfg = md.cfg;
		
		Graph g = new Graph(); //cfg.start.escapeGraph;
		
		for (int i = 0; i < md.argumentNames.size(); i++) {
			if (!isScalarType(md.argumentTypes.get(i))) {
				// should not put scalar types
				g.put(md.argumentNames.get(i), new GraphNode(GraphNode.REF_PARAM));
			}
		}
		
		LinkedList<String> threadVars = new LinkedList<>();
		
		for (String var : md.sym.locals.keySet()) {
			if (var.contains("_") == false) { // the pre-SSA variables are not removed from the locals :(
				continue;
			}
			TypeSymbol type = md.sym.locals.get(var).type;
			
			if (isScalarType(type)) {
				continue;
			}
			g.put(var, new GraphNode());
			
			if (type instanceof ClassSymbol) {
				if (inheritsThread((ClassSymbol)type)) {
					System.err.println("alloc thread");
					// we should also consider the other threads (fields)
					
					for (GraphNode node : g.nodes.get(var)) {
						node.setThread();
					}
					
					threadVars.add(var);
				}
			}
			if (type instanceof ArrayTypeSymbol) { // we should also consider the other arrays
				for (GraphNode node : g.nodes.get(var)) {
					node.setArray();
				}
			}
		}
		
		g.put("this", new GraphNode(GraphNode.THIS));
		
		mdecl.cfg.start.escapeGraph = g;
		
		LinkedList<BasicBlock> worklist = new LinkedList<>();
		worklist.add(cfg.end);
		Set<Integer> usedBBs = new HashSet<>();
		usedBBs.add(cfg.end.index);
		
		while (worklist.size() > 0) {
			BasicBlock curBB = worklist.poll();
			
			HashSet<String> curJoins = new HashSet<>();
			if (curBB.successors.size() > 0) {
				curJoins = new HashSet<>(joins.get(curBB.successors.get(0).index));
				
				for (int i = 1; i < curBB.successors.size(); i++) {
					curJoins.retainAll(joins.get(curBB.successors.get(i).index));
				}
			}
			
			for (Ast instr : curBB.instructions) {
				if (instr instanceof MethodCall) {
					getJoins.visit(instr, curJoins);
				}
			}
			
			joins.put(curBB.index, curJoins);
			
			for (BasicBlock nextBB : curBB.predecessors) {
				if (!usedBBs.contains(nextBB.index)) {
					usedBBs.add(nextBB.index);
					worklist.add(nextBB);
				}
			}
		}
		
		
		worklist.add(mdecl.cfg.start);
		usedBBs.clear();
		usedBBs.add(mdecl.cfg.start.index);
		
		while (worklist.size() > 0) {
			BasicBlock curBB = worklist.poll();
			
			if (curBB.predecessors.size() > 0) {
				curBB.escapeGraph = new Graph();
				
				for (BasicBlock pred : curBB.predecessors) {
					if (pred.escapeGraph != null) {
						curBB.escapeGraph.merge(pred.escapeGraph);
					}
				}
			}
			
			for (VariableSymbol phiSym : curBB.phis.keySet()) {
				if (isScalarType(phiSym.type)) {
					continue;
				}
				
				String varName = curBB.phis.get(phiSym).lhs.name;
				
				curBB.escapeGraph.clearName(varName);
				
				for (Expr e : curBB.phis.get(phiSym).rhs) {
					if (e instanceof Var) {
						Var v = (Var)e;
						if (isScalarType(v.type)) {
							continue;
						}
						// we should watch out this - might not work
						if (curBB.escapeGraph.nodes.containsKey(v.sym.name)) {
							for (GraphNode rightNode : curBB.escapeGraph.nodes.get(v.sym.name)) {
								curBB.escapeGraph.put(varName, rightNode);
							}
						}
					}
				}
			}
			
			curBB.escapeGraph.curBB = curBB.index;
			for (Ast instr : curBB.instructions) {
				addToGraph.visit(instr, curBB.escapeGraph);
			}
			
			for (BasicBlock nextBB : curBB.successors) {
				if (!usedBBs.contains(nextBB.index)) {
					usedBBs.add(nextBB.index);
					worklist.add(nextBB);
				}
			}
		}
		
		while (true) {
			boolean cont = false;
			
			for (BasicBlock curBB : md.cfg.allBlocks) {
				if (curBB.predecessors.size() > 0) {
					for (BasicBlock pred : curBB.predecessors) {
						cont = curBB.escapeGraph.merge(pred.escapeGraph) || cont;
					}
				}
			}

			if (!cont) {
				break;
			}
		}
		
		
		
		
		g = cfg.end.escapeGraph;

		for (String thread : threadVars) {
			ClassSymbol type = (ClassSymbol)md.sym.locals.get(thread).type;
			
			// we should be sure that 'join' is called
			MethodDecl threadMethod = type.getMethod(THREAD_RUN).ast;
			if (threadMethod != null && threadMethod.analyzedColor == BLACK) {
				g.checkThread(thread, threadMethod.cfg.end.escapeGraph);
			}
		}

		g.dfs();
		
		pw.write(String.format("subgraph cluster_%d{\n", EscapeAnalyzer.clusterInd++));
		g.write(pw);
		pw.write("}\n");
		
		for (BasicBlock curBB : cfg.allBlocks) {
			for (Ast instr : curBB.instructions) {
				allocOnStackVisitor.visit(instr, g);
			}
		}
		
		mdecl.analyzedColor = BLACK;
		
		System.err.println("Finished checking " + md.name);
	}
	
	private boolean isScalarType(TypeSymbol type) {
		return isScalarType(type.name);
	}
	
	private boolean isScalarType(String type) {
		if (type.equals("int") || type.equals("float") ||
			type.equals("boolean") || type.equals("<null>")) {
			return true;
		}
		
		return false;
	}
	
	private AstVisitor<Void, Set<String>> getJoins =
			new AstVisitor<Void, Set<String>>() {
		public Void methodCall(Ast.MethodCall ast, Set<String> joins) {
			if (isScalarType(ast.allArguments().get(0).type)) {
				// caller may be <null> due to constant propagation
				return null;
			}
			
			ClassSymbol caller = (ClassSymbol)(ast.allArguments().get(0).type);
			
			if (inheritsThread(caller) && ast.methodName.equals(THREAD_JOIN)) {
				joins.add(AstOneLine.toString(ast.allArguments().get(0)));
			}
			
			return null;
		}
	};
			
			
	private AstVisitor<List<GraphNode>, Graph> addToGraph = 
			new AstVisitor<List<GraphNode>, Graph>() {
		public List<GraphNode> assign(Ast.Assign ast, Graph g) {
			visit(ast.left(), g);
			visit(ast.right(), g);
			
			if (ast.left() instanceof Var ) {
				Var v = (Var)ast.left();
				if (isScalarType(v.type)) {
					return null;
				}
				
				if (ast.right() instanceof NewObject) {
					// we already created nodes for variabiles, can skip this
					return null;
				}
				
				if (ast.right() instanceof Field) {
					Field f = (Field)ast.right();

					g.clearName(v.sym.name);
					for (GraphNode n : g.buildNodes(AstOneLine.toString(f))) {
						g.put(v.sym.name, n);
					}
				}
				if (ast.right() instanceof MethodCallExpr) {
					visit(ast.right(), g);
					
					g.clearName(v.sym.name);
					// must be careful if the node already exists
					g.put(v.sym.name, new GraphNode(GraphNode.ESCAPED));
				}
			}
			
			// we do not have array! UPDATE: we do now
			if (ast.left() instanceof Index) {
				Index i = (Index)ast.left();
				
				if (!isScalarType(i.type)) {
					if (ast.right() instanceof Field || ast.right() instanceof Var ||
							ast.right() instanceof ThisRef) {
						for (GraphNode node : g.buildNodes(AstOneLine.toString(ast.right()))) {
							node.properties.add(GraphNode.ARRAY);
						}
					}
				}
			}
							
			if (ast.left() instanceof Field) {
				Field f = (Field)ast.left();
				
				if (isScalarType(f.type)) {
					return null;
				}
				
				GraphNode obj = null;
				if (ast.right() instanceof NewObject || ast.right() instanceof NewArray) {
					obj = new GraphNode();
				}
						
				for (GraphNode node : g.buildNodes(AstOneLine.toString(f.arg()))) {
					node.clearReference(f.fieldName);
					
					if (obj != null) {
						node.addReference(f.fieldName, obj);
						continue;
					}
			
					if (ast.right() instanceof MethodCallExpr) {
						node.properties.add(GraphNode.ESCAPED); // we should check this
						continue;
					}
					
					if (ast.right() instanceof Var || ast.right() instanceof Field) {
						for (GraphNode n : g.buildNodes(AstOneLine.toString(ast.right()))) {
							node.addReference(f.fieldName, n);
						}
					}
				}
				
			}
			
			return null;
		}
		
		private void analyzeMethod(List<Expr> arguments, List<VariableSymbol> params,
				Expr caller, MethodDecl method, Graph g) {
			
			if (isScalarType(caller.type)) {
				// caller may be <null> due to constant propagation
				return ;
			}
			
			ClassSymbol callClass = (ClassSymbol)caller.type;
			
			if (inheritsThread(callClass) && method.name.equals(THREAD_JOIN)) {
				return ;
			}
			
			if (inheritsThread(callClass) && method.name.equals(THREAD_START)) {
				method = callClass.getMethod(THREAD_RUN).ast;
			}
			
			if (method.analyzedColor == EscapeAnalyzer.WHITE) {
				(new EscapeAnalyzer(main)).compute(method, pw);
			}

			for (GraphNode node : g.buildNodes(AstOneLine.toString(caller))) {
				if (isThreadClass(method.sym.owner) && method.name.equals(THREAD_RUN) &&
						joins.get(g.curBB).contains(AstOneLine.toString(caller))) {
					node.setThread();
				} else {
					node.setEscaped();
				}
			}
			
			for (int i = 0; i < arguments.size(); i++) {
				Expr arg = arguments.get(i);
				VariableSymbol var = params.get(i);

				if (isScalarType(var.type)) {
					continue;
				}
				
				if (arg instanceof Var || arg instanceof Field) {
					// we are only interested in vars or fields
					if (method.analyzedColor == EscapeAnalyzer.GREY ||
							method.cfg.end.escapeGraph.nodeHasProp(var.name, "escaped")) {
						for (GraphNode node : g.buildNodes(AstOneLine.toString(arg))) {
							node.setEscaped();
						}
					}
				}
			}
		}
			
		public List<GraphNode> methodCall(Ast.MethodCallExpr ast, Graph g) {
			analyzeMethod(ast.argumentsWithoutReceiver(), ast.sym.parameters,
					ast.allArguments().get(0), ast.sym.ast, g);
			
			return dflt(ast, g);
		}
		
		public List<GraphNode> methodCall(Ast.MethodCall ast, Graph g) {
			
			analyzeMethod(ast.argumentsWithoutReceiver(), ast.sym.parameters,
					ast.allArguments().get(0), ast.sym.ast, g);
			
			return dflt(ast, g);
		}
		
		public List<GraphNode> returnStmt(Ast.ReturnStmt ast, Graph g) {
			
			if (ast.arg() == null) {
				return null;
			}
			
			visit(ast.arg(), g);
			
					
			if (isScalarType(ast.arg().type)) {
				return null;
			}
			
			if (ast.arg() instanceof Var || ast.arg() instanceof Field || ast.arg() instanceof ThisRef) {
				for (GraphNode x : g.buildNodes(AstOneLine.toString(ast.arg()))) {
					x.setReturned();
				}
			}
			
			return null;
		}
		
	};
	
	private AstVisitor<List<GraphNode>, Graph> allocOnStackVisitor = 
			new AstVisitor<List<GraphNode>, Graph>() {
		public List<GraphNode> assign(Ast.Assign ast, Graph g) {
			if (ast.right() instanceof NewObject) {
				for (GraphNode node : g.buildNodes(AstOneLine.toString(ast.left()))) {
					if (node.notAlloc()) {
						return null;
					}
				}
				
				((NewObject)ast.right()).stackAlloc = true;
				System.err.println("stack " + AstOneLine.toString(ast.left()));
			}
			
			return null;
		}
	};
}
