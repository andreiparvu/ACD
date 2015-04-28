package cd.analyze;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import cd.Main;
import cd.ir.Ast.ClassDecl;
import cd.ir.Ast.MethodCall;
import cd.ir.AstVisitor;
import cd.ir.Symbol.ClassSymbol;
import cd.ir.Symbol.MethodSymbol;

public class CallGraphGenerator {
	
	public final Main main;
	
	public CallGraphGenerator(Main main) {
		this.main = main;
	}
	
	public class CallGraphNode {
		public final MethodSymbol method;
		public final LinkedList<CallGraphNode> callees = new LinkedList<>();
		
		public CallGraphNode(MethodSymbol sym) {
			this.method = sym;
		}
	}
	
	public HashMap<String, CallGraphNode> compute(List<ClassDecl> astRoots) {
		final HashMap<MethodSymbol, List<MethodSymbol>> targets = computeTargets(astRoots);
		final HashMap<String, CallGraphNode> reachableMethods = new HashMap<>();
		final LinkedList<CallGraphNode> worklist = new LinkedList<>();
		 
		CallGraphNode root = new CallGraphNode(main.mainType.methods.get("main"));
		reachableMethods.put(root.method.fullName(), root);
		worklist.add(root);

		// currently implements CHA
		while (!worklist.isEmpty()) {
			final CallGraphNode caller = worklist.poll();
			
			caller.method.ast.body().accept(new AstVisitor<Void, Void>() {
				@Override
				public Void methodCall(MethodCall ast, Void arg) {
					for (MethodSymbol target : targets.get(ast.sym)) {
						CallGraphNode callee = reachableMethods.get(target.fullName());
						if (callee == null) {
							callee = new CallGraphNode(target);
							worklist.add(callee);
							reachableMethods.put(callee.method.fullName(), callee);
						}
						
						caller.callees.add(callee);
					}

					
					return arg;
				}
			}, null);
		}
		
		return reachableMethods;
	}

	protected HashMap<MethodSymbol, List<MethodSymbol>> computeTargets(List<ClassDecl> astRoots) {
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
