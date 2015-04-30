package cd.cfg;

import java.io.PrintWriter;
import java.util.Arrays;
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
import cd.ir.Ast.MethodCallExpr;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.NewObject;
import cd.ir.Ast.Var;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.ir.Symbol.TypeSymbol;
import cd.ir.Symbol.VariableSymbol;

public class EscapeAnalyzer {
	public final Main main;

	public final static int WHITE = 0, GREY = 1, BLACK = 2;
	
	private MethodDecl mdecl;
	private PrintWriter pw;
	
	public EscapeAnalyzer(Main main) {
		this.main = main;
	}
	
	private int nodeCount = 0;
	
	private class GraphNode {
		int index;
		boolean isEscaped = false;
		boolean isReturned = false;
		boolean isRefParameter = false;
		boolean dfsColor = false;
		
		GraphNode copy = null;
		
		HashMap<String, HashMap<Integer, GraphNode>> outNodes = new HashMap<>();

		GraphNode(boolean isEscaped, boolean isRefParameter) {
			this.isEscaped = isEscaped;
			this.isRefParameter = isRefParameter;
			index = ++nodeCount;
		}
		
		GraphNode(int index, boolean isEscaped, boolean isRefParameter, boolean isReturned) {
			this.index = index;
			this.isEscaped = isEscaped;
			this.isRefParameter = isRefParameter;
			this.isReturned = isReturned;
		}
		
		GraphNode() {
			index = ++nodeCount;
		}
		
		GraphNode addReference(String label) {
			return addReference(label, null);
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
		
		GraphNode deepCopy() {
			if (copy == null) {
				copy = new GraphNode(index, isEscaped, isRefParameter, isReturned);
				
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
			if (path.length() == 0) {
				results.add(this);
				return ;
			}
			
			int x = path.indexOf('.');
			HashMap<Integer, GraphNode> list;
			
			if (x == -1) {
				x = path.length();
			}
			
			list = outNodes.get(path.substring(0, x));
			
			if (list == null) {
				addReference(path.substring(0, x));
				list = outNodes.get(path.substring(0, x));
			}
			
			for (GraphNode next : list.values()) {
				if (x == path.length()) {
					results.add(next);
				} else {
					next.buildNodes(path.substring(x + 1), results);
				}
			}
		}
		
		boolean updateState(GraphNode predecessor) {
			boolean old1 = isReturned, old2 = isEscaped, old3 = isRefParameter;
			
			isReturned = isReturned || predecessor.isReturned;
			isEscaped = isEscaped || predecessor.isEscaped;
			isRefParameter = isRefParameter || predecessor.isRefParameter;
			
			return isReturned != old1 || isEscaped != old2 || isRefParameter != old3;
		}
		
		void dfs() {
			for (String label : outNodes.keySet()) {
				for (GraphNode next : outNodes.get(label).values()) {
					if (next.updateState(this)) {
						next.dfs();
					}
					
					next.dfs();
				}
			}
		}
		
		void write(String name, PrintWriter out) {
			dfsColor = !dfsColor;
			
			for (String label : outNodes.keySet()) {
				for (GraphNode next : outNodes.get(label).values()) {
					out.write(String.format("%d -> %d[label=%s];\n", index, next.index, label));
					
					if (next.dfsColor == dfsColor) {
						continue;
					}
					
					next.write(name + "." + label, out);
				}
			}
		}
	};
	
	public class Graph {
		Map<String, GraphNode> nodes = new HashMap<>();
		Set<GraphNode> returnSet = new HashSet<>();
		boolean dfsColor = false;
		
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
			int x = path.indexOf('.');
			GraphNode n;
			
			if (x == -1) {
				x = path.length();
			}
			
			n = nodes.get(path.substring(0, x));
			
			if (x == path.length()) {
				results.add(n);
			} else {
				n.buildNodes(path.substring(x + 1), results);
			}
		}
		
		void dfs() {
			for (String  n : nodes.keySet()) {
				nodes.get(n).dfs();
			}
		}
		
		void write(PrintWriter out) {
			dfsColor = !dfsColor;
			
			for (String  n : nodes.keySet()) {
				out.write(String.format("%s_%s -> %d;\n", mdecl.sym.name, n, nodes.get(n).index));
				if (nodes.get(n).dfsColor != dfsColor) {
					nodes.get(n).write(n, out);
				}
			}
		}
	}
	
	public void compute(MethodDecl md, PrintWriter pw) {
		this.mdecl = md;
		this.pw = pw;
		
		mdecl.analyzedColor = GREY;
		ControlFlowGraph cfg = md.cfg;
		LinkedList<BasicBlock> worklist = new LinkedList<>();
		
		for (BasicBlock curBB : cfg.allBlocks) {
			curBB.escapeGraph = new Graph();
		}
		
		worklist.add(cfg.start);
		
		for (String arg : md.argumentNames) {
			cfg.start.escapeGraph.nodes.put(arg, new GraphNode(false, true));
		}
		for (String var : md.sym.locals.keySet()) {
			if (var.contains("_") == false) { // the pre-SSA variables are not removed from the locals :(
				continue;
			}
			cfg.start.escapeGraph.nodes.put(var, new GraphNode());
		}
		
		cfg.start.escapeGraph.nodes.put("this", new GraphNode(true, false));
		
		int cnt = 0;
		while (worklist.size() > 0) {
			if (cnt == 5 * cfg.allBlocks.size()) {
				break;
			}
			cnt++;
			
			BasicBlock curBB = worklist.poll();
			Graph curGraph;
			
			if (curBB.predecessors.size() == 0) {
				curGraph = curBB.escapeGraph;
			} else {
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
		
		Graph g = cfg.end.escapeGraph;

		g.write(pw);
		
		for (BasicBlock curBB : cfg.allBlocks) {
			for (Ast instr : curBB.instructions) {
				allocOnStackVisitor.visit(instr, g);
			}
		}
	}
	
	private boolean isScalarType(TypeSymbol type) {
		if (type.name.equals("int") || type.name == "float" || type.name == "boolean") {
			return true;
		}
		
		return false;
	}
	
	private AstVisitor<List<GraphNode>, Graph> addToGraph = 
			new AstVisitor<List<GraphNode>, Graph>() {
		public List<GraphNode> assign(Ast.Assign ast, Graph g) {
			if (ast.left() instanceof Var ) {
				Var v = (Var)ast.left();
				if (isScalarType(v.type)) {
					return null;
				}
				
				if (ast.right() instanceof NewObject) {
					g.nodes.put(v.sym.name, new GraphNode());
				}
				if (ast.right() instanceof Field) {
					Field f = (Field)ast.right();
					
					for (GraphNode n : g.buildNodes(AstOneLine.toString(f))) {
						g.nodes.put(v.sym.name, n);
					}
				}
				if (ast.right() instanceof MethodCallExpr) {
					visit(ast.right(), g);
					
					g.nodes.put(v.sym.name, new GraphNode(true, false));
				}
			}
			
			if (ast.left() instanceof Field) {
				Field f = (Field)ast.left();
				
				if (isScalarType(f.type)) {
					return null;
				}
				
				GraphNode obj = null;
				if (ast.right() instanceof NewObject) {
					obj = new GraphNode();
				}
						
				for (GraphNode node : g.buildNodes(AstOneLine.toString(f.arg()))) {
					node.clearReference(f.fieldName);
					
					if (obj != null) {
						node.addReference(f.fieldName, obj);
						continue;
					}
			
					if (ast.right() instanceof MethodCallExpr) {
						node.isEscaped = true; // we should check this
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
		
		public List<GraphNode> methodCall(Ast.MethodCall ast, Graph g) {
			MethodDecl method = ast.sym.ast;
			
			System.err.println(method);
			if (method.analyzedColor == EscapeAnalyzer.WHITE) {
				EscapeAnalyzer.this.compute(method, pw);
			}

			for (int i = 0; i < ast.argumentsWithoutReceiver().size(); i++) {
				Expr arg = ast.argumentsWithoutReceiver().get(i);
				VariableSymbol var = ast.sym.parameters.get(i);
				
				if (isScalarType(var.type)) {
					continue;
				}
				
				if (method.analyzedColor == EscapeAnalyzer.GREY ||
						method.cfg.end.escapeGraph.nodes.get(var.name).isEscaped) {
					for (GraphNode node : g.buildNodes(AstOneLine.toString(arg))) {
						node.isEscaped = true;
					}
				}
			}
			
			return null;
		}
		
		public List<GraphNode> returnStmt(Ast.ReturnStmt ast, Graph g) {
			List<GraphNode> x = visit(ast.arg(), g);
			
			if (x != null) {
				for (GraphNode node : x) {
					node.isReturned = true;
					g.returnSet.add(node);
				}
			}
			
			return null;
		}
		
		public List<GraphNode> dflt(Ast ast, Graph g) {
			return null;
		}
		
	};
	
	private AstVisitor<List<GraphNode>, Graph> allocOnStackVisitor = 
			new AstVisitor<List<GraphNode>, Graph>() {
		public List<GraphNode> assign(Ast.Assign ast, Graph g) {
			if (ast.right() instanceof NewObject) {
				for (GraphNode node : g.buildNodes(AstOneLine.toString(ast.left()))) {
					System.err.println(node.index + ":" + node.isEscaped);
					if (node.isEscaped || node.isReturned || node.isRefParameter) {
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
