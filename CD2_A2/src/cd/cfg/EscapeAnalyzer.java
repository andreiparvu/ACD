package cd.cfg;

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

	private MethodDecl mdecl;

	public EscapeAnalyzer(Main main) {
		this.main = main;
	}
	
	private int nodeCount = 0;
	
	private class GraphNode {
		int index;
		boolean isEscaped = false;
		boolean isReturned = false;
		GraphNode copy = null;
		
		HashMap<String, LinkedList<GraphNode>> outNodes = new HashMap<>();

		GraphNode(boolean isEscaped) {
			this.isEscaped = isEscaped;
			index = ++nodeCount;
		}
		
		GraphNode(int index, boolean isEscaped, boolean isReturned) {
			this.index = index;
			this.isEscaped = isEscaped;
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
				outNodes.put(label, new LinkedList<GraphNode>());
			}
			
			if (n == null) {
				n = new GraphNode();
			}
			
			outNodes.get(label).add(n);
			
			return n;
		}
		
		void clearReference(String label) {
			if (outNodes.containsKey(label)) {
				outNodes.get(label).clear();
			}
		}
		
		GraphNode deepCopy() {
			if (copy == null) {
				copy = new GraphNode(index, isEscaped, isReturned);
				
				for (String label : outNodes.keySet()) {
					LinkedList<GraphNode> list = new LinkedList<>();
					
					for (GraphNode next : outNodes.get(label)) {
						list.add(next.deepCopy());
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
					for (GraphNode next : outNodes.get(label)) {
						next.eraseCopy();
					}
				}
			}
		}
		
		void merge(GraphNode node) {
			for (String label : node.outNodes.keySet()) {
				if (outNodes.containsKey(label)) {
					HashMap<Integer, GraphNode> poz = new HashMap<>();
					
					for (GraphNode n : outNodes.get(label)) {
						poz.put(n.index, n);
					}
					
					for (GraphNode n : node.outNodes.get(label)) {
						if (poz.containsKey(n.index)) {
							poz.get(n.index).merge(n);
						} else {
							outNodes.get(label).add(n);
						}
					}
				} else {
					outNodes.put(label, node.outNodes.get(label));
				}
			}
		}
		
		void dfs(String name, Set<String> escaped) {
			System.err.println("here " + index + " " + outNodes.size());
			if (isEscaped || isReturned) {
				System.err.println(index);
				escaped.add(name);
			}
			
			for (String label : outNodes.keySet()) {
				for (GraphNode next : outNodes.get(label)) {
					System.err.println("Node " + index + " via " + label + " to " + next.index);
					next.isEscaped = next.isEscaped || isEscaped;
					next.isReturned = next.isReturned || isReturned;
					
					next.dfs(name + "." + label, escaped);
				}
			}
		}
				
	};
	
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
					
	}
	
	public void compute(MethodDecl md) {
		ControlFlowGraph cfg = md.cfg;
		
		for (String arg : md.argumentNames) {
			System.err.println(arg);
		}
		
		LinkedList<BasicBlock> worklist = new LinkedList<>();
		
		worklist.add(cfg.start);
		cfg.start.escapeGraph = new Graph();
		
		for (String arg : md.argumentNames) {
			cfg.start.escapeGraph.nodes.put(arg, new GraphNode());
		}
		cfg.start.escapeGraph.nodes.put("$this", new GraphNode(true));
		
		while (worklist.size() > 0) {
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
						node.addReference(v.sym.name, curGraph.nodes.get(v.sym.name));
					}
				}
				curGraph.nodes.put(phiSym.name, node);
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
		HashSet<String> escaped = new HashSet<>();
		
		for (String  n : g.nodes.keySet()) {
			System.err.println("Node " + n + " to " + g.nodes.get(n).index);
			g.nodes.get(n).dfs(n, escaped);
		}
		System.err.println("====");
		
		for (BasicBlock curBB : cfg.allBlocks) {
			for (Ast instr : curBB.instructions) {
				allocOnStackVisitor.visit(instr, g);
			}
		}
	}
	
	private boolean isScalarType(TypeSymbol type) {
		if (type.name == "int" || type.name == "float" || type.name == "boolean") {
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
					
					for (GraphNode n : visit(ast.right(), g)) {
						g.nodes.put(v.sym.name, n);
					}
				}
				if (ast.right() instanceof MethodCallExpr) {
					visit(ast.right(), g);
					
					g.nodes.put(v.sym.name, new GraphNode(true));
				}
			}
			
			if (ast.left() instanceof Field) {
				Field i = (Field)ast.left();
				
				if (isScalarType(i.type)) {
					return null;
				}
				
				GraphNode obj = null;
				if (ast.right() instanceof NewObject) {
					obj = new GraphNode();
				}
						
				for (GraphNode node : visit(i.arg(), g)) {
					node.clearReference(i.fieldName);
					
					if (obj != null) {
						node.addReference(i.fieldName, obj);
						continue;
					}
			
					if (ast.right() instanceof MethodCallExpr) {
						node.isEscaped = true; // we should check this
						continue;
					}
					
					if (ast.right() instanceof Var || ast.right() instanceof Field) {
						for (GraphNode n : visit(ast.right(), g)) {
							node.addReference(i.fieldName, n);
						}
					}
				}
				
			}
			
			return null;
		}
		
		public List<GraphNode> methodCall(Ast.MethodCall ast, Graph g) {
			for (Expr arg : ast.argumentsWithoutReceiver()) {
				List<GraphNode> args = visit(arg, g);
				
				if (args != null) {
					for (GraphNode node : args) {
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
		
		public List<GraphNode> field(Ast.Field ast, Graph g) {
			if (isScalarType(ast.type)) {
				return null;
			}
			
			List<GraphNode> nodes = visit(ast.arg(), g);
			
			LinkedList<GraphNode> rez = new LinkedList<>();
			
			for (GraphNode r : nodes) {
				rez.addAll(r.outNodes.get(ast.fieldName));
			}
			
			return rez;
		}
		
		public List<GraphNode> var(Ast.Var ast, Graph g) {
			return Arrays.asList(g.nodes.get(ast.sym.name));
		}
		
		public List<GraphNode> thisRef(Ast.ThisRef ast, Graph g) {
			
			return Arrays.asList(g.nodes.get("$this"));
		}
		
		public List<GraphNode> dflt(Ast ast, Graph g) {
			return null;
		}
		
	};
	
	private AstVisitor<List<GraphNode>, Graph> allocOnStackVisitor = 
			new AstVisitor<List<GraphNode>, Graph>() {
		public List<GraphNode> assign(Ast.Assign ast, Graph g) {
			if (ast.right() instanceof NewObject) {
				List<GraphNode> left = visit(ast.left(), g);

				for (GraphNode node : left) {
					System.err.println(node.index + ":" + node.isEscaped);
					if (node.isEscaped || node.isReturned) {
						return null;
					}
				}
				
				((NewObject)ast.right()).stackAlloc = true;
				System.err.println("stack " + AstOneLine.toString(ast.left()));
			}
			
			return null;
		}
		
		public List<GraphNode> field(Ast.Field ast, Graph g) {
			if (isScalarType(ast.type)) {
				return null;
			}
			
			List<GraphNode> nodes = visit(ast.arg(), g);
			
			LinkedList<GraphNode> rez = new LinkedList<>();
			
			for (GraphNode r : nodes) {
				rez.addAll(r.outNodes.get(ast.fieldName));
			}
			
			return rez;
		}
		
		public List<GraphNode> var(Ast.Var ast, Graph g) {
			return Arrays.asList(g.nodes.get(ast.sym.name));
		}
		
		public List<GraphNode> thisRef(Ast.ThisRef ast, Graph g) {
			
			return Arrays.asList(g.nodes.get("$this"));
		}
	};
}
