package cd.analyze;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import cd.Main;
import cd.ir.Ast.ClassDecl;
import cd.ir.Ast.MethodCall;
import cd.ir.Ast.MethodCallExpr;
import cd.ir.AstVisitor;
import cd.ir.Symbol.ClassSymbol;
import cd.ir.Symbol.MethodSymbol;

public class CallGraphGenerator {
	
	public final Main main;
	
	public CallGraphGenerator(Main main) {
		this.main = main;
	}
	
	public static class CallGraph {
		public final Map<MethodSymbol, Set<MethodSymbol>> graph;
		public final Map<MethodSymbol, List<MethodSymbol>> targets;
		
		public CallGraph(
				Map<MethodSymbol, Set<MethodSymbol>> graph,
				Map<MethodSymbol, List<MethodSymbol>> targets) {
			this.graph = graph;
			this.targets = targets;
		}
		
		public void debugPrint() {
			for (Entry<MethodSymbol, Set<MethodSymbol>> method : graph.entrySet()) {
				LinkedList<String> callees = new LinkedList<>();
				for (MethodSymbol callee : method.getValue()) {
					callees.add(callee.fullName());
				}
				System.out.println(method.getKey().fullName() + ":" + Arrays.toString(callees.toArray()));
			}
		}
	}
	
	public CallGraph compute(List<ClassDecl> astRoots) {
		final Map<MethodSymbol, List<MethodSymbol>> targets = computeTargets(astRoots);
		final HashMap<MethodSymbol, Set<MethodSymbol>> reachableMethods = new HashMap<>();
		final LinkedList<MethodSymbol> worklist = new LinkedList<>();
		 
		MethodSymbol root = main.mainType.getMethod("main");
		reachableMethods.put(root, new HashSet<MethodSymbol>());
		worklist.add(root);

		// currently implements CHA
		while (!worklist.isEmpty()) {
			final MethodSymbol caller = worklist.poll();
			final Set<MethodSymbol> callees = reachableMethods.get(caller);


			caller.ast.body().accept(new AstVisitor<Void, Void>() {
				private void traverseMethod(MethodSymbol sym) {
					for (MethodSymbol target : targets.get(sym)) {
						if (!reachableMethods.containsKey(target)) {
							reachableMethods.put(target, new HashSet<MethodSymbol>());
							worklist.add(target);
						}

						callees.add(target);
					}
				}

				@Override
				public Void methodCall(MethodCall ast, Void arg) {
					traverseMethod(ast.sym);
					return arg;
				}

				@Override
				public Void methodCall(MethodCallExpr ast, Void arg) {
					traverseMethod(ast.sym);
					return arg;
				}
			}, null);
		}
		
		return new CallGraph(reachableMethods, targets);
	}

	public Map<MethodSymbol, List<MethodSymbol>> computeTargets(List<ClassDecl> astRoots) {
		HashMap<MethodSymbol, List<MethodSymbol>> targets = new HashMap<>();
		// initialize all method symbols
		for (ClassDecl cls : astRoots) {
			for (MethodSymbol method : cls.sym.methods.values()) {
				targets.put(method, new LinkedList<MethodSymbol>());
			}
		}

		for (ClassDecl cls : astRoots) {
			for (MethodSymbol method : cls.sym.methods.values()) {
				// for every method, add it to the lists of all super classes

				ClassSymbol superClass = cls.sym;
				while (superClass != null) {
					MethodSymbol superMethod = superClass.methods.get(method.name);
					if (superMethod != null) {
						targets.get(superMethod).add(method);
					}
					superClass = superClass.superClass;
				}
			}
		}
		return targets;
	}


}
