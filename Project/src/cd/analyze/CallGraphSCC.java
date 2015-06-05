package cd.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import cd.analyze.CallGraphGenerator.CallGraph;
import cd.ir.Symbol.MethodSymbol;

public class CallGraphSCC {
		/* This code is taken from
		 * Algorithms, 4th Edition by Robert Sedgewick and Kevin Wayne
		 * Addison-Wesley Professional, 2014
		 * 
		 * Code: http://algs4.cs.princeton.edu/42directed/TarjanSCC.java
		 */
		
		// marked[v] = has v been visited?
		private Set<MethodSymbol> marked = new HashSet<>();
		// id[v] = id of strong component containing v
		private Map<MethodSymbol, Integer> id = new HashMap<>();
		// low[v] = low number of v
		private Map<MethodSymbol, Integer> low = new HashMap<>();
		private int pre = 0;		// preorder number counter
		private int count = 0;		// number of strongly-connected components
		private Stack<MethodSymbol> stack = new Stack<>();

		private List<Set<MethodSymbol>> components = new ArrayList<>();

		public CallGraphSCC(CallGraph cg) {
			// find strongly connected components
			for (MethodSymbol vertex : cg.graph.keySet()) {
				if (!marked.contains(vertex)) {
					dfs(cg.graph, vertex);
				}
			}
			
			// Tarjan produces the reverse topological sort for the SCC DAG
			for (int index=0; index<count; index++) {
				components.add(new HashSet<MethodSymbol>());
			}
			
			for (MethodSymbol method : cg.graph.keySet()) {
				int index = id.get(method);
				components.get(index).add(method);
			}
		}

		private void dfs(Map<MethodSymbol, Set<MethodSymbol>> graph, MethodSymbol v) {
			marked.add(v);
			low.put(v, pre++);
			int min = low.get(v);
			stack.push(v);
			for (MethodSymbol w : graph.get(v)) {
				if (!marked.contains(w)) {
					dfs(graph, w);
				}
				if (low.get(w) < min) {
					min = low.get(w);
				}
			}

			if (min < low.get(v)) {
				low.put(v, min);
				return;
			}

			MethodSymbol w;
			do {
				w = stack.pop();
				id.put(w, count);

				low.put(w, graph.size());
			} while(!w.equals(v));

			count++;
		}
		
		public int count() {
			return count;
		}
		
		public Set<MethodSymbol> getComponent(MethodSymbol v) {
			return components.get(id.get(v));
		}
		
		public boolean stronglyConnected(MethodSymbol v, MethodSymbol w) {
			return id.get(v) == id.get(w);
		}
		
		public List<Set<MethodSymbol>> getSortedComponents() {
			return components;
		}
		
		public void debugPrint() {
			for (int index=0; index<count; index++) {
				LinkedList<String> methods = new LinkedList<>();
				for (MethodSymbol method : components.get(index)) {
					methods.add(method.fullName());
				}
				System.out.format("SCC(%d): %s\n", index, methods);
			}
		}
		
	}