package cd.analyze.stackalloc;

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
import cd.ir.Ast.Assign;
import cd.ir.Ast.Expr;
import cd.ir.Ast.Field;
import cd.ir.Ast.Free;
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
import cd.ir.Symbol.MethodSymbol;
import cd.ir.Symbol.TypeSymbol;
import cd.ir.Symbol.VariableSymbol;
import cd.util.Pair;

public class EscapeAnalyzer {
	public final Main main;

	public final static int WHITE = 0, GREY = 1, BLACK = 2;
	
	private MethodDecl mdecl;
	private PrintWriter pw, pr;
	
	public EscapeAnalyzer(Main main) {
		this.main = main;
	}
	
	static int prt = 0;
	
	private static int nodeCount = 0;
	private static int clusterInd = 0;
	
	private final static String THREAD_START = "start", THREAD_RUN = "run", THREAD_JOIN = "join";
	static int funcVars = 0;
	
	private class GraphNode {
		int index;
		
		final static String ARRAY = "array", REF_PARAM="ref_param", ESCAPED="escaped",
				THREAD="thread", THIS="this", RETURNED = "returned";
		
		HashSet<String> properties = new HashSet<>();
		
		boolean dfsColor = false, writeColor = false;
		
		GraphNode copy = null;
		
		HashMap<String, HashMap<Integer, GraphNode>> outNodes = new HashMap<>();

		GraphNode(String... props) {
			// create a new GraphNode with certain properties
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
			// create a reference from a this node to 'n' with a certain label
			if (outNodes.containsKey(label) == false) {
				outNodes.put(label, new HashMap<Integer, GraphNode>());
			}
			
			if (n == null) {
				n = new GraphNode(++nodeCount, properties);
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
					(properties.size() == 1 && properties.contains(THREAD)) ||
					(properties.size() == 1 && properties.contains(ARRAY)));
		}
		
		GraphNode deepCopy() {
			if (copy == null) {
				// if we don't already have a copy, create a new one
				copy = new GraphNode(index, properties);
				
				for (String label : outNodes.keySet()) {
					// deep copy all the out nodes
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
		
		public boolean merge(GraphNode node, HashSet<Integer> visited) {
			// merge current node with another one
			boolean ret = false;
		
			// avoid cycles
			if (visited.contains(node.index)) {
				return false;
			}
		
			visited.add(node.index);
			
			for (String label : node.outNodes.keySet()) {
				if (outNodes.containsKey(label)) {
					//  merge the same nodes or add new ones
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
			// for a given path in the graph (variable or field), return the nodes
			// which correspond to it
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
		
		boolean updateState(GraphNode predecessor, boolean onlyEscape) {
			boolean modified = false;
			
			for (String prop : predecessor.properties) {
				if (onlyEscape && !prop.equals(ESCAPED)) {
					continue;
				}
				
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
		
		void dfs(boolean onlyEscape) {
			dfsColor = true;
			
			for (String label : outNodes.keySet()) {
				for (GraphNode next : outNodes.get(label).values()) {
					// if we update the state of the next node, recurse into it (some other
					// nodes might change later)
					if (next.updateState(this, onlyEscape) || !next.dfsColor) {
						next.dfs(onlyEscape);
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
			
			// thread arrays will always escape
			for (GraphNode threadNode : threadGraph.buildNodes(prefix)) {
				if (threadNode.properties.size() > 1) { // they must have the 'this' property
					this.properties.add(ESCAPED);
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
		Map<MethodSymbol, Set<MethodSymbol>> callTargets;
		Map<String, HashMap<Integer, GraphNode>> nodes = new HashMap<>();
		Set<GraphNode> returnSet = new HashSet<>();
		int curBB; // used for processing
		HashSet<Expr> methodRets = new HashSet<>();
		
		Graph(Map<MethodSymbol, Set<MethodSymbol>> callTargets) {
			this.callTargets = callTargets;
		}
		
		Graph deepCopy() {
			Graph g = new Graph(callTargets);
			// deep copy every node
			
			for (String n : nodes.keySet()) {
				for (GraphNode node : nodes.get(n).values()) {
					g.put(n, node.deepCopy());
				}
			}
			
			for (String n : nodes.keySet()) {
				for (GraphNode node : nodes.get(n).values()) {
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
				nodes.put(name, new HashMap<Integer, GraphNode>());
			}
			nodes.get(name).put(n.index, n);
		}
		
		boolean nodeHasProp(String name, String... props) {
			boolean rez = false;
			
			for (GraphNode node : nodes.get(name).values()) {
				rez = node.containsProps(props) || rez;
			}
			
			return rez;
		}
		
		boolean merge(Graph g) {
			boolean ret = false;
			
			for (String n : g.nodes.keySet()) {
				if (nodes.containsKey(n)) {
					for (GraphNode gn : g.nodes.get(n).values()) {
						
						if (!nodes.get(n).containsKey(gn.index)) {
							nodes.get(n).put(gn.index, gn.deepCopy());
							ret = true;
						} else {
							ret = nodes.get(n).get(gn.index).merge(gn, new HashSet<Integer>()) || ret;
						}
					}
				} else {
					for (GraphNode node : g.nodes.get(n).values()) {
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
			
			if (!nodes.containsKey(path.substring(0, next.a))) {
				return ;
			}
			
			for (GraphNode n : nodes.get(path.substring(0, next.a)).values()) {
				if (next.b == path.length()) {
					results.add(n);
				} else {
					n.buildNodes(path.substring(next.b + 1), results);
				}
			}
		}
		
		String newFunctionNode() {
			// we create new nodes for function returned values
			// for the moment we mark all of them as escaped - too conservative
			GraphNode x = new GraphNode(GraphNode.ESCAPED);
			
			put("_funcRet" + (++funcVars), x);
			
			return "_funcRet" + funcVars;
		}
			
		void dfs(boolean onlyEscape) {
			for (String  n : nodes.keySet()) {
				for (GraphNode node : nodes.get(n).values()) {
					node.dfs(onlyEscape);
				}
			}
		}
		
		void checkThread(String path, Graph threadGraph) {
			for (GraphNode node : nodes.get(path).values()) {
				node.checkThread(threadGraph, "this");
			}
		}
		
		void write(PrintWriter out) {
			for (String n : nodes.keySet()) {
				out.write(String.format("%d[label=%s]\n", ++nodeCount, n));
				for (GraphNode node : nodes.get(n).values()) {
					out.write(String.format("%d -> %d;\n", nodeCount, node.index));
					if (!node.writeColor) {
						node.write(n, out);
					}
				}
			}
		}
	}
	
	public Pair<Integer> computeNextLabel(String path) {
		// given a path in the graph (essentially a variable of field),
		// determine the label of the next edge
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
	
	private boolean inheritsThread(ClassSymbol curClass) {
		for (; curClass != null; curClass = curClass.superClass) {
			if (curClass.name.equals("Thread")) {
				return true;
			}
		}
		
		return false;
	}
	
	private HashMap<Integer, HashSet<String>> joins = new HashMap<>();
	
	public void compute(MethodDecl md, PrintWriter pw, PrintWriter pr,
			Map<MethodSymbol, Set<MethodSymbol>> callTargets) {
		boolean cont = false;
		
		for (BasicBlock bb : md.cfg.allBlocks) {
			for (Ast instr : bb.instructions) {
				if (instr instanceof Assign) {
					Assign a = (Assign)instr;
					
					if (a.right() instanceof NewObject) {
						cont = true;
					}
				}
			}
		}
		// we don't want to analyze the current method if it doesn't contain any 'new' statements
		if (cont) {
			compute(md, pw, pr, callTargets, new ArrayList<Set<GraphNode>>(), true);
		}
	}
	
	public void compute(MethodDecl md, PrintWriter pw, PrintWriter pr,
			Map<MethodSymbol, Set<MethodSymbol>> callTargets,
			List<Set<GraphNode>> parameters,
			boolean allocate) {
		this.mdecl = md;
		this.pw = pw;
		this.pr = pr;
		
		mdecl.analyzedColor = BLACK;
		ControlFlowGraph cfg = md.cfg;
		
		Graph g = new Graph(callTargets);
		
		for (int i = 0; i < md.argumentNames.size(); i++) {
			if (isScalarType(md.argumentTypes.get(i))) {
				// should not put scalar types
				continue;
			}
			if (parameters.size() == 0) {
				g.put(md.argumentNames.get(i), new GraphNode(GraphNode.REF_PARAM));
			} else {
				for (GraphNode node: parameters.get(i)) {
					g.put(md.argumentNames.get(i), node);
				}
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
			
			// create a new node for each local var
			g.put(var, new GraphNode());
			
			if (type instanceof ClassSymbol) {
				if (inheritsThread((ClassSymbol)type)) {
					// we currently don't consider the other types of threads - fields
					
					for (GraphNode node : g.nodes.get(var).values()) {
						node.setThread();
					}
					
					threadVars.add(var);
				}
			}
			if (type instanceof ArrayTypeSymbol) {
				for (GraphNode node : g.nodes.get(var).values()) {
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
		
		// we want to determine which thread joins reach each block - do a backward control flow analysis 
		while (worklist.size() > 0) {
			BasicBlock curBB = worklist.poll();
			
			HashSet<String> curJoins = new HashSet<>();
			if (curBB.successors.size() > 0) {
				// intersect the joins sets of the successors
				if (joins.containsKey(curBB.successors.get(0).index)) {
					curJoins = new HashSet<>(joins.get(curBB.successors.get(0).index));
				}
				
				for (int i = 1; i < curBB.successors.size(); i++) {
					HashSet<String> join = joins.get(curBB.successors.get(i).index);
					if (join != null) {
						curJoins.retainAll(join);
					}
				}
			}
			
			// add current join set
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
		
		// do one iteration through the basic blocks to build the first stage of the graph
		worklist.add(mdecl.cfg.start);
		usedBBs.clear();
		usedBBs.add(mdecl.cfg.start.index);
		
		while (worklist.size() > 0) {
			BasicBlock curBB = worklist.poll();
			
			if (curBB.predecessors.size() > 0) {
				curBB.escapeGraph = new Graph(callTargets);
				
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
						for (GraphNode rightNode : curBB.escapeGraph.buildNodes(v.sym.name)) {
							curBB.escapeGraph.put(varName, rightNode);
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
		
		// second stage of building the graph - merge the graph of a basic block with the graphs
		// of its predecessors until no more changes happen
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
			if (threadMethod != null && threadMethod.sym != null && threadMethod.analyzedColor == BLACK) {
				g.checkThread(thread, threadMethod.cfg.end.escapeGraph);
			}
		}

		g.dfs(allocate == false /* onlyEscape */);
		
		if (allocate) {
			pw.write(String.format("subgraph cluster_%d{\n", EscapeAnalyzer.clusterInd++));
			pw.write(String.format("label=\"%s.%s\"\n", mdecl.sym.owner, mdecl.name));
			g.write(pw);
			pw.write("}\n");
			
			for (BasicBlock curBB : cfg.allBlocks) {
				for (Ast instr : curBB.instructions) {
					allocOnStackVisitor.visit(instr, g);
				}
			}
			
			for (Expr e : g.methodRets) {
				// e should be only Var
				boolean bad = false;
				for (GraphNode n : g.buildNodes(AstOneLine.toString(e))) {
					if (n.notAlloc()) {
						bad = true;
					}
				}
				
				if (!bad) {
					cfg.end.instructions.add(new Free(e));
				}
			}
		}
		
		mdecl.analyzedColor = WHITE;
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
			
			
	private AstVisitor<String, Graph> addToGraph = 
			new AstVisitor<String, Graph>() {
		public String assign(Ast.Assign ast, Graph g) {
			String left = visit(ast.left(), g);
			String right = visit(ast.right(), g);
			
			if (ast.left() instanceof Var ) {
				Var v = (Var)ast.left();
				if (isScalarType(v.type)) {
					return null;
				}
				
				if (ast.right() instanceof NewObject) {
					// we already created nodes for variables, can skip this
					return null;
				}
				
				if (ast.right() instanceof Field || ast.right() instanceof ThisRef) {
					if (g.nodes.get(v.sym.name).size() == 1) {
						GraphNode curN = g.nodes.get(v.sym.name).values().iterator().next();
						
						String leftS = right.substring(0, right.lastIndexOf('.'));
						
						for (GraphNode n : g.buildNodes(leftS)) {
							n.addReference(right.substring(right.lastIndexOf('.') + 1), curN);
						}
					} else {
						g.clearName(v.sym.name);
						
						for (GraphNode n : g.buildNodes(right)) {
							g.put(v.sym.name, n);
						}
					}
				}
				
				if (ast.right() instanceof MethodCallExpr || ast.right() instanceof Var) {
					
					if (ast.right() instanceof MethodCallExpr) {
						g.methodRets.add(ast.left());
					}
					
					g.clearName(v.sym.name);
					for (GraphNode n : g.buildNodes(right)) {
						g.put(v.sym.name, n);
					}
				}
			}
			
			if (ast.left() instanceof Index) {
				Index i = (Index)ast.left();
				
				if (!isScalarType(i.type)) {
					if (ast.right() instanceof Field || ast.right() instanceof Var ||
							ast.right() instanceof ThisRef) {
						for (GraphNode arrNode : g.buildNodes(left)) {
							for (GraphNode node : g.buildNodes(right)) {
								arrNode.addReference("arr", node);
							}
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
					return null;
				}
						
				for (GraphNode node : g.buildNodes(left.substring(0, left.lastIndexOf('.')))) {
					node.clearReference(f.fieldName);
					
					if (obj != null) {
						node.addReference(f.fieldName, obj);
						continue;
					}
			
					if (ast.right() instanceof MethodCallExpr) {
						// we currently mark returned values as escaped - too conservative
						node.properties.add(GraphNode.ESCAPED);
						continue;
					}
					
					if (ast.right() instanceof Var || ast.right() instanceof Field ||
							ast.right() instanceof ThisRef || ast.right() instanceof Index) {
						for (GraphNode n : g.buildNodes(right)) {
							node.addReference(f.fieldName, n);
						}
					}
				}
				
			}
			
			return null;
		}
		
		private void analyzeMethod(List<Expr> arguments, List<VariableSymbol> params,
				Expr callee, MethodDecl method, Graph g) {
			
			if (isScalarType(callee.type)) {
				// caller may be <null> due to constant propagation
				return ;
			}
			
			ClassSymbol callClass = (ClassSymbol)callee.type;
			
			if (inheritsThread(callClass) && method.name.equals(THREAD_START)) {
				// change analyzed method from 'start' to 'run'
				method = callClass.getMethod(THREAD_RUN).ast;

		        if (method.sym == null) {
		        	// for cases in which run method is not overwritten by user
		        	method.analyzedColor = EscapeAnalyzer.BLACK;
		        }
			} else if (main.isBuiltinMethod(method.sym)) {
				return;
			}
			
			if (!main.isBuiltinMethod(method.sym) && // this is needed for 'run' method which is not overwritten 
					method.analyzedColor == EscapeAnalyzer.WHITE) {
				ArrayList<Set<GraphNode>> argsNodes = new ArrayList<>();
				
				for (int i = 0; i < arguments.size(); i++) {
					Expr arg = arguments.get(i);
					VariableSymbol var = params.get(i);
	
					String argName = visit(arg, g);
					
					if (isScalarType(var.type)) {
						argsNodes.add(null);
						continue;
					}
					
					// keep nodes of parameters for recursive evaluation of method
					argsNodes.add(g.buildNodes(argName));
				}
				
				(new EscapeAnalyzer(main)).compute(method, pw, pr, g.callTargets, argsNodes, false);
			} else {
				// mark every parameter as escaped
				for (int i = 0; i < arguments.size(); i++) {
					Expr arg = arguments.get(i);
					VariableSymbol var = params.get(i);

					String argName = visit(arg, g);
					
					if (isScalarType(var.type)) {
						continue;
					}

					for (GraphNode node : g.buildNodes(argName)) {
						node.setEscaped();
					}
				}
			}

			String callerName = visit(callee, g);
			for (GraphNode node : g.buildNodes(callerName)) {
				if ((method.sym == null || inheritsThread(method.sym.owner)) && method.name.equals(THREAD_RUN)) {
					// we have to be sure that a join is always called to the thread in order
					// for it not to escape
					
					if (joins.get(g.curBB).contains(callerName)) {
						node.setThread();
					} else {
						node.setEscaped();
					}
				}
				if (method.cfg != null && (method.analyzedColor == EscapeAnalyzer.BLACK ||
						method.cfg.end.escapeGraph.nodeHasProp("this", "escaped"))) {
					node.setEscaped();
				}
			}
		}
			
		public String methodCall(Ast.MethodCallExpr ast, Graph g) {
			for (MethodSymbol callee : g.callTargets.get(ast.sym)) {
				analyzeMethod(ast.argumentsWithoutReceiver(), ast.sym.parameters,
						ast.allArguments().get(0), callee.ast, g);
			}
			
			return g.newFunctionNode();
		}
		
		public String methodCall(Ast.MethodCall ast, Graph g) {
			
			for (MethodSymbol callee : g.callTargets.get(ast.sym)) {
				analyzeMethod(ast.argumentsWithoutReceiver(), ast.sym.parameters,
						ast.allArguments().get(0), callee.ast, g);
			}
			
			return "";
		}
		
		public String var(Ast.Var ast, Graph g) {
			return AstOneLine.toString(ast);
		}
		
		public String thisRef(Ast.ThisRef ast, Graph g) {
			return AstOneLine.toString(ast);
		}
		
		public String field(Ast.Field ast, Graph g) {
			return visit(ast.arg(), g) + "." + ast.fieldName;
		}
		
		public String index(Ast.Index ast, Graph g) {
			return AstOneLine.toString(ast);
		}
		
		public String returnStmt(Ast.ReturnStmt ast, Graph g) {
			if (ast.arg() == null) {
				return null;
			}
			
			visit(ast.arg(), g);
			
					
			if (isScalarType(ast.arg().type)) {
				return null;
			}
			
			if (ast.arg() instanceof Var || ast.arg() instanceof Field || ast.arg() instanceof ThisRef ||
					ast.arg() instanceof Index) {
				for (GraphNode x : g.buildNodes(visit(ast.arg(), g))) {
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
				pr.write("stack " + AstOneLine.toString(ast.left()) + "\n");
			}
			
			return null;
		}
	};
}
