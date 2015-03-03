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
    
    BasicBlock ret = v.gen(mdecl, cfg.start);
    if (ret != cfg.end) {
      cfg.connect(ret, cfg.end);
    }
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
    
    public BasicBlock returnStmt(Ast.ReturnStmt ast, BasicBlock blk) {
      blk.addInstruction(ast);
      cfg.connect(blk, cfg.end);
      
      return cfg.end;
    }
    
    protected BasicBlock dfltStmt(Stmt ast, BasicBlock blk) {
      if (blk != cfg.end) {
        blk.addInstruction(ast);
      }
      return dflt(ast, blk);
    }
    
    public BasicBlock ifElse(Ast.IfElse ast, BasicBlock blk) {
      cfg.terminateInCondition(blk, ast.condition());
      
      BasicBlock thenBlock = visit(ast.then(), blk.trueSuccessor()),
                  elseBlock = visit(ast.otherwise(), blk.falseSuccessor());
      
      BasicBlock ret;
      if (thenBlock == cfg.end) {
        if (elseBlock == cfg.end) {
          return cfg.end;
        }
        ret = cfg.newBlock();
        cfg.connect(elseBlock, ret);
      } else if (elseBlock == cfg.end) {
        ret = cfg.newBlock();
        cfg.connect(thenBlock, ret);
      } else {
        ret = cfg.join(thenBlock, elseBlock);
      }
      return ret;
    }
    
    public BasicBlock whileLoop(Ast.WhileLoop ast, BasicBlock blk) {
      BasicBlock whileBlock = cfg.newBlock();
      cfg.terminateInCondition(whileBlock, ast.condition());
      cfg.connect(blk, whileBlock);
      
      BasicBlock trueBlock = visit(ast.body(), whileBlock.trueSuccessor());
      
      if (trueBlock != cfg.end) {
        cfg.connect(trueBlock, whileBlock);
      }
      
      return whileBlock.falseSuccessor();
    }
  }
}
