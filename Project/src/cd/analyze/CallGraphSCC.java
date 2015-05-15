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
		// based on http://algs4.cs.princeton.edu/42directed/TarjanSCC.java
		
		private Set<MethodSymbol> marked = new HashSet<>();;        // marked[v] = has v been visited?
		private Map<MethodSymbol, Integer> id = new HashMap<>();                // id[v] = id of strong component containing v
		private Map<MethodSymbol, Integer> low = new HashMap<>();               // low[v] = low number of v
		private int pre = 0;                 // preorder number counter
		private int count = 0;               // number of strongly-connected components
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
			// as a by product (TODO check this claim)
			for (int index=0; index<count; index++) {
				components.add(new HashSet<MethodSymbol>());
			}
			
			for (MethodSymbol method : cg.graph.keySet()) {
				int index = id.get(method);
				components.get(index).add(method);
			}
		}

		private void dfs(Map<MethodSymbol, Set<MethodSymbol>> graph, MethodSymbol v) {
//	        marked[v] = true;
			marked.add(v);
//	        low[v] = pre++;
			low.put(v, pre++);
//	        int min = low[v];
			int min = low.get(v);
//	        stack.push(v);
			stack.push(v);
//	        for (int w : G.adj(v)) {
			for (MethodSymbol w : graph.get(v)) {
//	            if (!marked[w]) dfs(G, w);
				if (!marked.contains(w)) {
					dfs(graph, w);
				}
//	            if (low[w] < min) min = low[w];
				if (low.get(w) < min) {
					min = low.get(w);
				}
			}

//	        if (min < low[v]) { low[v] = min; return; }
			if (min < low.get(v)) {
				low.put(v, min);
				return;
			}

//	        int w;
			MethodSymbol w;
			do {
//	            w = stack.pop();
				w = stack.pop();
//	            id[w] = count;
				id.put(w, count);

//	            low[w] = G.V();
				low.put(w, graph.size());
			} while(!w.equals(v)); // while (w != v);
//	        count++;

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