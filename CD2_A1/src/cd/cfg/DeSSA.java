package cd.cfg;

import cd.Main;
import cd.exceptions.ToDoException;
import cd.ir.Ast.MethodDecl;

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
		{
			throw new ToDoException();
		}
	}
		
}
