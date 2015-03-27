package cd.cfg;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import cd.Main;
import cd.ir.Ast;
import cd.ir.Ast.BinaryOp;
import cd.ir.Ast.BooleanConst;
import cd.ir.Ast.Expr;
import cd.ir.Ast.IntConst;
import cd.ir.Ast.LeafExpr;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.UnaryOp;
import cd.ir.Ast.UnaryOp.UOp;
import cd.ir.AstRewriteVisitor;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.ir.Phi;
import cd.ir.Symbol.VariableSymbol;

public class Optimizer {
	
	private final boolean INTENSE_DEBUG = true;
	private final int MAX_INNER = 16, MAX_OUTER = 16; 
	
	public final Main main;
	public int changes = 0;
	
	public static int overall;
	
	public Optimizer(Main main) {
		this.main = main;
	}
	
	private String phase() {
		return format(".opt.%d", overall++);		
	}
	
	public void compute(MethodDecl md) {
	    ControlFlowGraph cfg = md.cfg;

	    Map<String, LeafExpr> propagations = new HashMap<>();
		int oldChanges = 0;
		int cnt = 0;
		do {
		    LinkedList<BasicBlock> deadBlocks = new LinkedList<>();
			oldChanges = changes;
			/*
			 * To do: 
			 * (1) constant propagation
			 * (2) copy propagation
			 * (3) common sub-expression elimination
			 */
			for (BasicBlock blk : cfg.allBlocks) {
			    for (Ast instr : blk.instructions) {
			        constantFolding.visit(instr, null);
			    }
			    // might want to move this somewhere else
			    if (blk.condition != null) {
			        blk.condition = (Expr)constantFolding.visit(blk.condition, null);
			        
			        if (blk.condition.isConstant() == Ast.Expr.BOOL) {
		                System.err.println("yeah");
		                BooleanConst c = (BooleanConst)blk.condition;
		                
		                if (c.value) {
		                    if (blk.falseSuccessor() != null) {
		                        blk.falseSuccessor().deletePred(blk);
		                    }
		                    if (blk.falseSuccessor().isDead()) {
		                        deadBlocks.add(blk.falseSuccessor());
		                    }
		                    blk.deleteFalseSuccessor();
		                } else {
		                    if (blk.trueSuccessor() != null) {
		                        blk.trueSuccessor().deletePred(blk);
		                    }
		                    if (blk.trueSuccessor().isDead()) {
		                        deadBlocks.add(blk.trueSuccessor());
		                    }
		                    blk.deleteTrueSuccessor();
		                }
		                blk.condition = null;
		            }
			    }
			}
			
			for (int i = 0; i < cfg.allBlocks.size(); i++) {
			    for (Phi phi : cfg.allBlocks.get(i).phis.values()) {
			        phi.checkIfConstant(propagations);
			    }
			}
			
			propagateCopies(cfg.start, propagations);
			
			System.err.println("Phase " + changes);
			
			while (!deadBlocks.isEmpty()) {
			    BasicBlock curBB = deadBlocks.poll();
			    for (BasicBlock succ : curBB.successors) {
			        succ.deletePred(curBB);
			        changes++;
			        if (succ.isDead()) {
			            deadBlocks.add(succ);
			        }
			    }
			}
			
			if (cnt == 4) {
//			    break;
			}
			cnt++;
		} while (changes != oldChanges);
		
		Set<String> usedVars = new HashSet<>();
		for (BasicBlock bb : cfg.allBlocks) {
		    for (Ast instr : bb.instructions) {
		        detectUses.visit(instr, usedVars);
		    }
		    if (bb.condition != null) {
		        detectUses.visit(bb.condition, usedVars);
		    }
		    for (Phi phi : bb.phis.values()) {
		        phi.detectUses(usedVars);
		    }
		}

		System.err.println(usedVars.size());
		for (String kkt : usedVars) {
		    System.err.println("slbz" + kkt);
		}
		
		for (BasicBlock bb : cfg.allBlocks) {
		    Iterator<Ast> it = bb.instructions.iterator();
		    for (; it.hasNext(); ) {
		        if (detectUnused.visit(it.next(), usedVars)) {
		            it.remove();
		        }
		    }
		    Iterator<Map.Entry<VariableSymbol, Phi>> iter = bb.phis.entrySet().iterator();
		    for (; iter.hasNext(); ) {
		        if (usedVars.contains(iter.next().getValue().lhs.name) == false) {
		            iter.remove();
		        }
		    }
		}       
	}
	
	private AstRewriteVisitor<Void> constantFolding = new AstRewriteVisitor<Void>() {
		
		/* More folding ideas:
		 * 	- fold casts
		 *  - fold if
		 */
		@Override
		public Ast visitChildren(Ast ast, Void arg) {
			ListIterator<Ast> children = ast.rwChildren.listIterator();
			while (children.hasNext()) {
				Ast child = children.next();
				if (child != null) {
					Ast replace = visit(child, arg);
					if (replace != child) {
						System.err.format("Replace: %s <- %s\n", child, replace);
						changes++;
						children.set(replace);
					}
				}
			}
			return ast;
		}

		@Override
		public Ast binaryOp(BinaryOp ast, Void arg) {
			Ast left = visit(ast.left(), arg);
			Ast right = visit(ast.right(), arg);
			
			if (left instanceof BooleanConst && right instanceof BooleanConst) {
				return booleanConstOp(ast, (BooleanConst)left, (BooleanConst)right);
			}
			if (left instanceof IntConst && right instanceof IntConst) {
				return intConstOp(ast, (IntConst)left, (IntConst)right);
			}
			
			/*if (left instanceof FloatConst && right instanceof FloatConst) {
				return floatOp(ast, (FloatConst)left, (FloatConst)right);
			}*/
			
			return ast;
		}
		
		private Ast intConstOp(BinaryOp op, IntConst left, IntConst right) {
			switch(op.operator) {
			case B_PLUS: 	return new IntConst(left.value + right.value);
			case B_MINUS:	return new IntConst(left.value - right.value);
			case B_TIMES:	return new IntConst(left.value * right.value);
			case B_DIV:
				if (right.value != 0) {
					// only do compile time evaluation of no division by zeor
					return new IntConst(left.value / right.value);
				}
				break;
			case B_MOD:
				if (right.value != 0) {
					// only do compile time evaluation of no division by zeor
					return new IntConst(left.value % right.value);
				}
				break;		
			case B_EQUAL:				return new BooleanConst(left.value == right.value);
			case B_GREATER_OR_EQUAL:	return new BooleanConst(left.value >= right.value);
			case B_GREATER_THAN:		return new BooleanConst(left.value > right.value);
			case B_LESS_OR_EQUAL:		return new BooleanConst(left.value <= right.value);
			case B_LESS_THAN:			return new BooleanConst(left.value < right.value);
			case B_NOT_EQUAL: 			return new BooleanConst(left.value != right.value);
			default:	break;
			}
			
			return op;
		}
		
		private Ast booleanConstOp(BinaryOp op, BooleanConst left, BooleanConst right) {
			switch (op.operator) {
			case B_OR:	return new BooleanConst(left.value || right.value);
			case B_AND: return new BooleanConst(left.value && right.value);
			default:	return op;
			}
		}

		@Override
		public Ast unaryOp(UnaryOp op, Void arg) {
			Ast child = visit(op.arg(), arg);
			if (child instanceof IntConst) {
				IntConst val = (IntConst) child;
				switch (op.operator) {
				case U_MINUS:	return new IntConst(-val.value);
				case U_PLUS:	return new IntConst(val.value);
				default:		break;
				}

			} else	if (child instanceof BooleanConst) {
				assert op.operator == UOp.U_BOOL_NOT;
				BooleanConst val = (BooleanConst) child;

				return new BooleanConst(!val.value);
			}
			return op;
		}
	};
	
	void propagateCopies(BasicBlock bb, Map<String, LeafExpr> toPropagate) {
	    for (Phi phi : bb.phis.values()) {
	        for (int i = 0; i < phi.rhs.size(); i++) {
	            phi.rhs.set(i, (Expr)new PropagateVisitor().visit(phi.rhs.get(i), toPropagate));
	        }
	    }
	    Iterator<Map.Entry<VariableSymbol, Phi>> iter = bb.phis.entrySet().iterator();
	    while (iter.hasNext()) {
	        if ((iter.next()).getValue().isConstant) {
	            iter.remove();
	        }
	    }
	    
	    for (Ast instr : bb.instructions) {
	        new PropagateVisitor().visit(instr, toPropagate);
	        new CollectVisitor().visit(instr, toPropagate);
	    }
	    if (bb.condition != null) {
	        bb.condition = (Expr)new PropagateVisitor().visit(bb.condition, toPropagate);
	    }
	    
	    for (BasicBlock next : bb.dominatorTreeChildren) {
	        propagateCopies(next, toPropagate);
	    }
	}
	
	private class PropagateVisitor extends AstRewriteVisitor<Map<String, LeafExpr>> {
	    public Ast var(Ast.Var ast, Map<String, LeafExpr> toPropagate) {
	        System.err.println(ast.sym.name);
	        if (toPropagate.containsKey(ast.sym.name)) {
	            System.err.println(toPropagate.get(ast.sym.name));
	            changes++;
	            return toPropagate.get(ast.sym.name);
	        }
	        return dflt(ast, toPropagate);
	    }
	    
	    public Ast assign(Ast.Assign ast, Map<String, LeafExpr> toPropagate) {
	        ast.setRight((Expr)visit(ast.right(), toPropagate));
	        
	        return ast;
	    }
	}
	
	private class CollectVisitor extends AstVisitor<Void, Map<String, LeafExpr>> {
	    public Void assign(Ast.Assign ast, Map<String, LeafExpr> toPropagate) {
	        if (ast.left() instanceof Ast.Var && ast.right() instanceof LeafExpr) {
	            toPropagate.put(((Ast.Var)ast.left()).sym.name, (LeafExpr)ast.right());
	        }
	        
	        return dfltStmt(ast, null);
	    }
	    
	    public Void visitChildren(Ast ast, Map<String, LeafExpr> arg) {
	        return null;
	    }
	}
	
	private AstVisitor<Boolean, Set<String>> detectUses = new AstVisitor<Boolean, Set<String>>() {
	    public Boolean assign(Ast.Assign ast, Set<String> usedVars) {
	        Boolean isUsed = visit(ast.right(), usedVars);
	        if (!(ast.left() instanceof Ast.Var) || isUsed) {
	            visit(ast.left(), usedVars);
	        }
	        
	        return true;
	    }
	    
	    public Boolean var(Ast.Var ast, Set<String> usedVars) {
	        System.err.println(ast.sym.name);
	        usedVars.add(ast.sym.name);
	        
	        return false;
	    }

	    public Boolean visitChildren(Ast ast, Set<String> arg) {
	        Boolean lastValue = false;
	        for (Ast child : ast.children())
	            lastValue = visit(child, arg);
	        return lastValue;
	    }
	    
	    public Boolean builtInRead(Ast.BuiltInRead ast, Set<String> arg) {
	        return true;
	    }
	    
	    public Boolean builtInReadFloat(Ast.BuiltInReadFloat ast, Set<String> arg) {
	        return true;
	    }
	    
	    public Boolean methodCall(Ast.MethodCall ast, Set<String> usedVars) {
	        dfltStmt(ast, usedVars);
	        
	        return true;
	    }
	};
	
	private AstVisitor<Boolean, Set<String>> detectUnused = new AstVisitor<Boolean, Set<String>>() {
        public Boolean assign(Ast.Assign ast, Set<String> usedVars) {
            if (ast.left() instanceof Ast.Var) {
                return usedVars.contains(((Ast.Var)(ast.left())).sym.name) == false; 
            }
            
            return dfltStmt(ast, usedVars);
        }
        
        protected Boolean dflt(Ast ast, Set<String> usedVars) {
            return false;
        }
    };
}
