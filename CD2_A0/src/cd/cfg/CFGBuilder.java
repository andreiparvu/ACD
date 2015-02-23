package cd.cfg;

import cd.Main;
import cd.exceptions.ToDoException;
import cd.ir.Ast.MethodDecl;
import cd.ir.ControlFlowGraph;

public class CFGBuilder {
	
	public final Main main;
	
	public CFGBuilder(Main main) {
		this.main = main;
	}
	
	ControlFlowGraph cfg;
	
	public void build(MethodDecl mdecl) {
		cfg = mdecl.cfg = new ControlFlowGraph();
		cfg.start = cfg.newBlock(); // Note: Use newBlock() to create new basic blocks
		cfg.end = cfg.newBlock(); // unique exit block to which all blocks that end with a return stmt. lead
		{
			throw new ToDoException();
		}
	}

}
