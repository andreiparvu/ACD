package cd.cfg;

import cd.Main;
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
		cfg.connect(v.gen(mdecl, cfg.start), cfg.end);
	}

	private class CFGVisitor extends AstVisitor<BasicBlock, BasicBlock> {
    BasicBlock gen(Ast ast, BasicBlock blk) {
      return visit(ast, blk);
    }
    
    public BasicBlock visitChildren(Ast ast, BasicBlock blk) {
      BasicBlock lastValue = blk;
      for (Ast child : ast.children()) {
        lastValue = visit(child, lastValue);
      }
      return lastValue;
    }
    
    protected BasicBlock dfltStmt(Stmt ast, BasicBlock blk) {
      blk.addInstruction(ast);
      return dflt(ast, blk);
    }
    
    public BasicBlock ifElse(Ast.IfElse ast, BasicBlock blk) {
      cfg.terminateInCondition(blk, ast.condition());
      
      return cfg.join(visit(ast.then(), blk.trueSuccessor()),
                      visit(ast.otherwise(), blk.falseSuccessor()));
    }
    
    public BasicBlock whileLoop(Ast.WhileLoop ast, BasicBlock blk) {
      cfg.terminateInCondition(blk, ast.condition());
      
      cfg.connect(visit(ast.body(), blk.trueSuccessor()), blk);
      
      return blk.falseSuccessor();
    }
  }
}
