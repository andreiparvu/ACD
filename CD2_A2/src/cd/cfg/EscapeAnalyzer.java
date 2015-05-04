package cd.cfg;

import java.io.PrintWriter;
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
	
	private static int nodeCount = 0;
	private static int clusterInd = 0;
	
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
		
		void merge(GraphNode node) {
			for (String label : node.outNodes.keySet()) {
				if (outNodes.containsKey(label)) {
					for (GraphNode n : node.outNodes.get(label).values()) {
						if (outNodes.get(label).containsKey(n.index)) {
							outNodes.get(label).get(n.index).merge(n);
						} else {
							outNodes.get(label).put(n.index, n);
						}
					}
				} else {
					outNodes.put(label, node.outNodes.get(label));
				}
			}
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
			
			out.write(String.format("%d[label=\"", index));
			for (String prop : properties) {
				out.write(prop + " ");
			}
			printProperties();
			
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
				System.err.println("-- " + prefix + " " + threadNode.properties);
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
		Map<String, GraphNode> nodes = new HashMap<>();
		Set<GraphNode> returnSet = new HashSet<>();
		
		Graph deepCopy() {
			Graph g = new Graph();
			
			for (String n : nodes.keySet()) {
				g.nodes.put(n, nodes.get(n).deepCopy());
			}
			
			for (String n : nodes.keySet()) {
				nodes.get(n).eraseCopy();
			}
			
			return g;
		}
		
		void merge(Graph g) {
			for (String n : g.nodes.keySet()) {
				if (nodes.containsKey(n)) {
					nodes.get(n).merge(g.nodes.get(n));
				} else {
					nodes.put(n, g.nodes.get(n));
				}
			}
		}
		
		Set<GraphNode> buildNodes(String path) {
			HashSet<GraphNode> results = new HashSet<>();
			
			buildNodes(path, results);
			
			return results;
		}
		
		void buildNodes(String path, Set<GraphNode> results) {
			Pair<Integer> next = computeNextLabel(path);
			
			GraphNode n = nodes.get(path.substring(0, next.a));
			
			if (next.b == path.length()) {
				results.add(n);
			} else {
				n.buildNodes(path.substring(next.b + 1), results);
			}
		}
		
		void dfs() {
			for (String  n : nodes.keySet()) {
				nodes.get(n).dfs();
			}
		}
		
		void checkThread(String path, Graph threadGraph) {
			nodes.get(path).checkThread(threadGraph, "this");
		}
		
		void write(PrintWriter out) {
			for (String  n : nodes.keySet()) {
				out.write(String.format("%s_%s_%s -> %d;\n", mdecl.sym.owner, mdecl.sym.name, n, nodes.get(n).index));
				if (!nodes.get(n).writeColor) {
					nodes.get(n).write(n, out);
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
	
	public void compute(MethodDecl md, PrintWriter pw) {
		System.err.println("Checking " + md.name);
		
		
		this.mdecl = md;
		this.pw = pw;
		
		mdecl.analyzedColor = GREY;
		ControlFlowGraph cfg = md.cfg;
		LinkedList<BasicBlock> worklist = new LinkedList<>();
		
		for (BasicBlock curBB : cfg.allBlocks) {
			curBB.escapeGraph = new Graph();
		}
		
		worklist.add(cfg.start);
		
		Graph g = cfg.start.escapeGraph;
		
		for (int i = 0; i < md.argumentNames.size(); i++) {
			if (!isScalarType(md.argumentTypes.get(i))) {
				// should not put scalar types
				g.nodes.put(md.argumentNames.get(i), new GraphNode(GraphNode.REF_PARAM));
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
			g.nodes.put(var, new GraphNode());
			
			if (type instanceof ClassSymbol) {
				if (((ClassSymbol)type).superClass.name.equals("Thread")) {
					System.err.println("alloc thread");
					// we should also consider the other threads (fields)
					
					g.nodes.get(var).setThread();
					
					threadVars.add(var);
				}
			}
			if (type instanceof ArrayTypeSymbol) { // we should also consider the other arrays
				g.nodes.get(var).setArray();
			}
		}
		
		g.nodes.put("this", new GraphNode(GraphNode.THIS));
		
		int cnt = 0;
		while (worklist.size() > 0) {
			if (cnt == 5 * cfg.allBlocks.size()) { // this is a hack
				break;
			}
			cnt++;
			
			BasicBlock curBB = worklist.poll();
			Graph curGraph;
			
			if (curBB.predecessors.size() == 0) {
				curGraph = curBB.escapeGraph;
			} else {
				// merge the graphs of the predecessors
				curGraph = curBB.predecessors.get(0).escapeGraph.deepCopy();
				
				for (int i = 1; i < curBB.predecessors.size(); i++) {
					curGraph.merge(curBB.predecessors.get(i).escapeGraph);
				}
			}
			
			
			for (VariableSymbol phiSym : curBB.phis.keySet()) {
				GraphNode node = new GraphNode();
				
				for (Expr e : curBB.phis.get(phiSym).rhs) {
					if (e instanceof Var) {
						Var v = (Var)e;
						// we should watch out this - might not work
						node.addReference("phi", curGraph.nodes.get(v.sym.name));
					}
				}
				curGraph.nodes.put(curBB.phis.get(phiSym).lhs.name, node);
			}
			
			for (Ast instr : curBB.instructions) {
				addToGraph.visit(instr, curGraph);
			}
			
			curBB.escapeGraph = curGraph;
			
			for (BasicBlock nextBB : curBB.successors) {
				worklist.add(nextBB);
			}
		}
		
		g = cfg.end.escapeGraph;

		for (String thread : threadVars) {
			ClassSymbol type = (ClassSymbol)md.sym.locals.get(thread).type;
			
			// we should be sure that 'join' is called
			MethodDecl threadMethod = type.methods.get("start").ast;
			if (threadMethod.analyzedColor == BLACK) {
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
				type.equals("boolean")) {
			return true;
		}
		
		return false;
	}
	
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
					
					for (GraphNode n : g.buildNodes(AstOneLine.toString(f))) {
						// must be careful if the nod already exists
						g.nodes.put(v.sym.name, n);
					}
				}
				if (ast.right() instanceof MethodCallExpr) {
					visit(ast.right(), g);
					
					// must be careful if the node already exists
					g.nodes.put(v.sym.name, new GraphNode(GraphNode.ESCAPED));
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
			
			if (method.analyzedColor == EscapeAnalyzer.WHITE) {
				(new EscapeAnalyzer(main)).compute(method, pw);
			}
			
			for (GraphNode node : g.buildNodes(AstOneLine.toString(caller))) {
				if (isThreadClass(method.sym.owner) && method.name.equals("start") && arguments.size() == 0) {
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
							method.cfg.end.escapeGraph.nodes.get(var.name).properties.contains("escaped")) {
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
			for (GraphNode x : g.buildNodes(AstOneLine.toString(ast.arg()))) {
				x.setReturned();
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
