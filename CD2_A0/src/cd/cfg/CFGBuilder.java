package cd.cfg;

import cd.Main;
import cd.exceptions.ToDoException;
import cd.ir.Ast;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.Stmt;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
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
		
    CFGVisitor v = new CFGVisitor();
    v.gen(mdecl, cfg.start);
		{
//			throw new ToDoException();
		}
	}

	private class CFGVisitor extends AstVisitor<Void, BasicBlock> {
    void gen(Ast ast, BasicBlock blk) {
      visit(ast, blk);
    }
    
    protected Void dfltStmt(Stmt ast, BasicBlock blk) {
      blk.addInstruction(ast);
      System.out.println(ast);
      System.out.println("da");
      return dflt(ast, blk);
    }
  }
}
