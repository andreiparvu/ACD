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
import cd.ir.Symbol.TypeSymbol;

public class CallGraphGenerator {
	
	public final Main main;
	
	public CallGraphGenerator(Main main) {
		this.main = main;
	}
	
	public static class ThreadStart {
		public boolean multiple = false;
	}
	
	public static class CallGraph {
		public final Map<MethodSymbol, Set<MethodSymbol>> graph;
		public final Map<MethodSymbol, Set<MethodSymbol>> targets;
		
		public CallGraph(
				Map<MethodSymbol, Set<MethodSymbol>> graph,
				Map<MethodSymbol, Set<MethodSymbol>> targets) {
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
		final Map<MethodSymbol, Set<MethodSymbol>> targets = computeTargets(astRoots);
		final HashMap<MethodSymbol, Set<MethodSymbol>> reachableMethods = new HashMap<>();
		final LinkedList<MethodSymbol> worklist = new LinkedList<>();
		
		// Add all entry points as roots
		MethodSymbol root = main.mainType.getMethod("main");
		addToWorklist(reachableMethods, worklist, root);

		MethodSymbol threadRun = main.threadType.getMethod("run");
		for (MethodSymbol threadRoot : targets.get(threadRun)) {
			addToWorklist(reachableMethods, worklist, threadRoot);
		}

		// currently implements CHA
		while (!worklist.isEmpty()) {
			final MethodSymbol caller = worklist.poll();
			final Set<MethodSymbol> callees = reachableMethods.get(caller);

			caller.ast.body().accept(new AstVisitor<Void, Void>() {
				private void traverseMethod(MethodSymbol sym) {
					for (MethodSymbol target : targets.get(sym)) {
						addToWorklist(reachableMethods, worklist, target);
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

	private void addToWorklist(
			final HashMap<MethodSymbol, Set<MethodSymbol>> reachableMethods,
			final LinkedList<MethodSymbol> worklist, MethodSymbol target) {
		if (!reachableMethods.containsKey(target)) {
			reachableMethods.put(target, new HashSet<MethodSymbol>());
			worklist.add(target);
		}
	}

	public Map<MethodSymbol, Set<MethodSymbol>> computeTargets(List<ClassDecl> astRoots) {
		HashMap<MethodSymbol, Set<MethodSymbol>> targets = new HashMap<>();
		// initialize all method symbols to static target
		for (TypeSymbol typesym : main.allTypeSymbols) {
			if (typesym instanceof ClassSymbol) {
				ClassSymbol cls = (ClassSymbol)typesym;
				for (MethodSymbol method : cls.methods.values()) {
					Set<MethodSymbol> targetSet = new HashSet<MethodSymbol>();
					targetSet.add(cls.getMethod(method.name));
					targets.put(method, targetSet);
				}
			}
		}

		// add possible child targets
		for (ClassDecl cls : astRoots) {
			for (MethodSymbol method : cls.sym.methods.values()) {
				// for every method, add it to the lists of all super classes

				ClassSymbol superClass = cls.sym.superClass;
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
