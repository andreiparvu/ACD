package cd.cfg;

import cd.Main;
import cd.ir.Ast.Assign;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.ir.Phi;
import cd.ir.Symbol.VariableSymbol;

public class DeSSA {
	
	public final Main main;
	
	public DeSSA(Main main) {
		this.main = main;
	}
	
	/**
	 * Goes over the control flow graph and removes
	 * any phi nodes, converting SSA variables into normal
	 * variables and phi nodes into standard assignments. 
	 */
	public void compute(final MethodDecl mdecl) {
	    final ControlFlowGraph cfg = mdecl.cfg;
	    
	    for (BasicBlock bb : cfg.allBlocks) {
	        for (VariableSymbol vsym : bb.phis.keySet()) {
	            Phi phi = bb.phis.get(vsym);
	            
	            for (int i = 0; i < bb.predecessors.size(); i++) {
	                bb.predecessors.get(i).instructions.add(new Assign(Ast.Var.withSym(phi.lhs),
	                        phi.rhs.get(i)));
	            }
	        }
	        bb.phis.clear();
	    }
	}
		
}
