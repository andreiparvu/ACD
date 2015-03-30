package cd.cfg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import cd.Main;
import cd.debug.AstOneLine;
import cd.ir.Ast;
import cd.ir.Ast.Assign;
import cd.ir.Ast.BinaryOp;
import cd.ir.Ast.BooleanConst;
import cd.ir.Ast.Expr;
import cd.ir.Ast.FloatConst;
import cd.ir.Ast.IntConst;
import cd.ir.Ast.LeafExpr;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.UnaryOp;
import cd.ir.Ast.UnaryOp.UOp;
import cd.ir.Ast.Var;
import cd.ir.AstRewriteVisitor;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.ir.Phi;
import cd.ir.Symbol.PrimitiveTypeSymbol;
import cd.ir.Symbol.VariableSymbol;

public class Optimizer {
	
	public final Main main;
	public int changes = 0;
	
	private MethodDecl mdecl;
	private int nrTemp = 0;
	
	public Optimizer(Main main) {
		this.main = main;
	}
	
	private class ExpressionManager {
 	    public class Data {
	        int position;
	        Var substitute;
	        Expr node;
	        boolean isTemp;
	        
	        public Data(int position, Var substitute, Expr node, boolean isTemp) {
	            this.position = position;
	            this.substitute = substitute;
	            this.node = node;
	            this.isTemp = isTemp;
	        }
	    }
 	    
	    Map<String, Data> info = new HashMap<>();
	    
	    List<String> subexpressions;
	    Set<String> isUsed = new HashSet<>();
	    int curPosition;
	    Var curVar = null;
	}
		
	private Var newTempVar() {
	    nrTemp++;
	    String tempName = "$temp_" + nrTemp;
	    VariableSymbol sym = new VariableSymbol(tempName,
	            new PrimitiveTypeSymbol(tempName), VariableSymbol.Kind.LOCAL);
	    mdecl.sym.locals.put(tempName, sym);
	    return Var.withSym(sym);
	}
	
	public void compute(MethodDecl md) {
	    ControlFlowGraph cfg = md.cfg;
	    mdecl = md;

	    Map<String, LeafExpr> propagations = new HashMap<>();
		int oldChanges = 0;
		do {
		  LinkedList<BasicBlock> deadBlocks = new LinkedList<>();
			oldChanges = changes;
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
			
			identifySubexpression(cfg.start, new ExpressionManager());
			System.err.println("Phase " + changes);
		} while (changes != oldChanges);
		
		oldChanges = 0;
		changes = 0;
		
		do {
		    oldChanges = changes;
    		Set<String> usedVars = new HashSet<>();
    		Iterator<BasicBlock> bbIt = cfg.allBlocks.iterator();
    		for (; bbIt.hasNext(); ) {
    		    BasicBlock bb = bbIt.next();
    		    
    		    if (bb.isDead() && bb.index != 0) {
    		        changes++;
    		        for (BasicBlock succ : bb.successors) {
                        succ.deletePred(bb);
                    }
    		    
    		        bbIt.remove();
    		        continue;
    		    }
    		    
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
    
    		for (BasicBlock bb : cfg.allBlocks) {
    		    Iterator<Ast> it = bb.instructions.iterator();
    		    for (; it.hasNext(); ) {
    		        Ast curInstr = it.next();
    		        String varName = detectUnused.visit(curInstr, usedVars);
    		        if (varName != null) {
    		            mdecl.sym.locals.remove(varName);
    		            changes++;
    		            it.remove();
    		        }
    		    }
    		    Iterator<Map.Entry<VariableSymbol, Phi>> iter = bb.phis.entrySet().iterator();
    		    for (; iter.hasNext(); ) {
    		        if (usedVars.contains(iter.next().getValue().lhs.name) == false) {
    		            changes++;
    		            iter.remove();
    		        }
    		    }
    		}
		} while (changes != oldChanges);
	}
	
	private abstract class OptimizerAstRewriter<A>	extends AstRewriteVisitor<A> {
		@Override
		public Ast visitChildren(Ast ast, A arg) {
			ListIterator<Ast> children = ast.rwChildren.listIterator();
			while (children.hasNext()) {
				Ast child = children.next();
				if (child != null) {
					Ast replace = visit(child, arg);
					if (replace != child) {
						System.err.format("Replace: %s <- %s\n", AstOneLine.toString(child), AstOneLine.toString(replace));
						changes++;
						
						children.set(replace);
					}
				}
			}
			return ast;
		}
	}
	
	private void identifySubexpression(BasicBlock curBB, ExpressionManager exprManager) {
	    List<String> curExpressions = new ArrayList<>();
	    exprManager.subexpressions = curExpressions;
	    
		for (int i = 0; i < curBB.instructions.size(); i++) {
		    exprManager.curPosition = i;
		    generateCanonicalForm.visit(curBB.instructions.get(i), null);
		    if (curBB.index == 6) {
		    	System.err.println(curBB.instructions.get(i));
		    }
			canonicExpressionVisitor.visit(curBB.instructions.get(i), exprManager);
		}
		exprManager.curPosition = curBB.instructions.size();
		if (curBB.condition != null) {
		    generateCanonicalForm.visit(curBB.condition, null);
			canonicExpressionVisitor.visit(curBB.condition, exprManager);
		}
		
		for (BasicBlock bb : curBB.dominatorTreeChildren) {
			identifySubexpression(bb, exprManager);
		}
		
		int nrAdded = 0;
		for (String expr : curExpressions) {
		    if (exprManager.isUsed.contains(expr)) {
		        ExpressionManager.Data data = exprManager.info.get(expr);
		        if (data.isTemp) {
		            curBB.instructions.add(data.position + nrAdded,
		                    new Assign(data.substitute, data.node));
		            nrAdded++;
		        }
		    }
		    exprManager.info.remove(expr);
		}
		        
	}
	
	private AstVisitor<Void, Void> generateCanonicalForm = new AstVisitor<Void, Void>() {

		@Override
		public Void binaryOp(BinaryOp ast, Void arg) {
			if (ast.left() instanceof LeafExpr) {
				LeafExpr left = (LeafExpr)ast.left();
				if (left.isPropagatable) {
					left.canonicalForm = AstOneLine.toString(left);
				}
			} else {
				visit(ast.left(), arg);
			}
			
			if (ast.right() instanceof LeafExpr) {
				LeafExpr right = (LeafExpr)ast.right();
				if (right.isPropagatable) {
					right.canonicalForm = AstOneLine.toString(right);
				}
			} else {
				visit(ast.right(), arg);
			}
			
			String leftStr = ast.left().canonicalForm;
			String rightStr = ast.right().canonicalForm;
			
			if (leftStr != null && rightStr != null) {
				if (ast.isCommutative()) {
					if (leftStr.compareTo(rightStr) > 0) {
						String tmp = leftStr;
						leftStr = rightStr;
						rightStr = tmp;
					}
				}
				
				ast.canonicalForm = String.format("%s %s %s", ast.operator.repr, leftStr, rightStr);
			}
			
			return null;
		}
		
	};
	
	private AstRewriteVisitor<ExpressionManager> canonicExpressionVisitor = new OptimizerAstRewriter<ExpressionManager>() {
		@Override
		public Ast assign(Ast.Assign ast, ExpressionManager exprManager) {
		    if (ast.left() instanceof Var) {
		        exprManager.curVar = (Var)ast.left();
		        ast.setRight((Expr)visit(ast.right(), exprManager));
		        
		        return ast;
		    }
		    
		    return visitChildren(ast, exprManager);
		}
		
		@Override
		public Ast dflt(Ast ast, ExpressionManager exprManager) {
		    exprManager.curVar = null;
		    return visitChildren(ast, exprManager);
		}
		
		@Override
		public Ast binaryOp(BinaryOp ast, ExpressionManager exprManager) {
			if (ast.canonicalForm != null) {
				if (!exprManager.info.containsKey(ast.canonicalForm)) {
					Var next;
					boolean isTemp = false;
					if (exprManager.curVar == null) {
						next = newTempVar();
						isTemp = true;
					} else {
						next = exprManager.curVar;
					}

					exprManager.subexpressions.add(ast.canonicalForm);
					exprManager.info.put(ast.canonicalForm,
							(new ExpressionManager()).new Data(exprManager.curPosition, next, ast, isTemp));
				} else {
					changes++;
					exprManager.isUsed.add(ast.canonicalForm);
					return exprManager.info.get(ast.canonicalForm).substitute;
				}
			}

			return dflt(ast, exprManager);
		}
	};
	
	private AstRewriteVisitor<Void> constantFolding = new OptimizerAstRewriter<Void>() {

		@Override
		public Ast binaryOp(BinaryOp ast, Void arg) {
			Ast left = visit(ast.left(), arg);
			Ast right = visit(ast.right(), arg);
			
			if (left instanceof BooleanConst && right instanceof BooleanConst) {
				return booleanConstOp(ast, (BooleanConst)left, (BooleanConst)right);
			} else if (left instanceof IntConst && right instanceof IntConst) {
				return intConstOp(ast, (IntConst)left, (IntConst)right);
			} else if (left instanceof FloatConst && right instanceof FloatConst) {
				return floatConstOp(ast, (FloatConst)left, (FloatConst)right);
			}
			
			
			
			return ast;
		}
		
		private Ast intConstOp(BinaryOp op, IntConst left, IntConst right) {
			switch(op.operator) {
			case B_PLUS: 	return new IntConst(left.value + right.value);
			case B_MINUS:	return new IntConst(left.value - right.value);
			case B_TIMES:	return new IntConst(left.value * right.value);
			case B_DIV:
				if (right.value != 0) {
					// only do compile time evaluation of no division by zero
					return new IntConst(left.value / right.value);
				}
				break;
			case B_MOD:
				if (right.value != 0) {
					// only do compile time evaluation of no division by zero
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
		
		/*private Ast intSimplification(BinaryOp ast, Expr left, Expr right) {
			if (left instanceof IntConst) {
				IntConst child = (IntConst) expr;
				if (child.value == 0) {
					
				}
			}
		}*/
		
		private Ast booleanConstOp(BinaryOp op, BooleanConst left, BooleanConst right) {
			switch (op.operator) {
			case B_OR:	return new BooleanConst(left.value || right.value);
			case B_AND: return new BooleanConst(left.value && right.value);
			default:	return op;
			}
		}

		/* This method uses the "strictfp" keyword to ensure that Java uses IEEE 754
		 * compliant floating point arithmetic. This should not make a difference on
		 * your average x86 with SSE2, but if some bored TA decides to run this on an
		 * Intel 386, this should ensures correctness. 
		 * 
		 * We also modify the code generator (which uses SSE2) to match Java semantics
		 * i.e. for equality comparison with NaN and rounding modes of IEEE 754-1985.
		 *
		 * References:
		 *  - http://math.nist.gov/javanumerics/reports/issues.html#Rounding
		 *  - Muller, Jean-Michel, et al. Handbook of floating-point arithmetic. 
		 *    Springer Science & Business Media, 2009.
		 *             
		 */
		strictfp private Ast floatConstOp(BinaryOp op, FloatConst left, FloatConst right) {
			switch(op.operator) {
			case B_PLUS: 				return new FloatConst(left.value + right.value);
			case B_MINUS:				return new FloatConst(left.value - right.value);
			case B_TIMES:				return new FloatConst(left.value * right.value);
			case B_DIV:					return new FloatConst(left.value / right.value);
			case B_EQUAL:				return new BooleanConst(left.value == right.value);
			case B_GREATER_OR_EQUAL:	return new BooleanConst(left.value >= right.value);
			case B_GREATER_THAN:		return new BooleanConst(left.value > right.value);
			case B_LESS_OR_EQUAL:		return new BooleanConst(left.value <= right.value);
			case B_LESS_THAN:			return new BooleanConst(left.value < right.value);
			case B_NOT_EQUAL: 			return new BooleanConst(left.value != right.value);
			default:					break;
			}
			
			return op;
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
			} else if (child instanceof FloatConst) {
				FloatConst val = (FloatConst) child;
				switch (op.operator) {
				case U_MINUS:	return new FloatConst(-val.value);
				case U_PLUS:	return new FloatConst(val.value);
				default:		break;
				}
			} else if (child instanceof BooleanConst) {
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
	

	private class PropagateVisitor extends OptimizerAstRewriter<Map<String, LeafExpr>> {
	    public Ast var(Ast.Var ast, Map<String, LeafExpr> toPropagate) {
	        if (toPropagate.containsKey(ast.sym.name)) {
	            return toPropagate.get(ast.sym.name);
	        }
	        return dflt(ast, toPropagate);
	    }
	    
	    public Ast assign(Ast.Assign ast, Map<String, LeafExpr> toPropagate) {
	        ast.setRight((Expr)visit(ast.right(), toPropagate));
	        
	        if (!(ast.left() instanceof Ast.Var)) {
	            ast.setLeft((Expr)visit(ast.left(), toPropagate));
	        }
	        
	        return ast;
	    }
	}
	
	private class CollectVisitor extends AstVisitor<Void, Map<String, LeafExpr>> {
	    public Void assign(Ast.Assign ast, Map<String, LeafExpr> toPropagate) {
	        if (ast.left() instanceof Ast.Var && ast.right() instanceof LeafExpr) {
	            System.err.println(ast);
	        	if (((LeafExpr)ast.right()).isPropagatable) {
	        		toPropagate.put(((Ast.Var)ast.left()).sym.name, (LeafExpr)ast.right());
	        	}
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
	        usedVars.add(ast.sym.name);
	        
	        return false;
	    }

	    public Boolean visitChildren(Ast ast, Set<String> arg) {
	        Boolean lastValue = false;
	        for (Ast child : ast.children()) {
	            if (visit(child, arg) == true) {
	                lastValue = true;
	            }
	        }
	        return lastValue;
	    }
	    
	    public Boolean binaryOp(Ast.BinaryOp ast, Set<String> arg) {
	        boolean ret, children = dfltExpr(ast, arg);
	        if (ast.operator == Ast.BinaryOp.BOp.B_DIV) {
	            // div may generate division by zero / don't eliminate
	            ret = true;
	        } else {
	            ret = children;
	        }
	        
	        return ret;
	    }
	    
	    public Boolean newArray(Ast.NewArray ast, Set<String> arg) {
	        // may generate error when provided with a negative size
	        dfltExpr(ast, arg);
	        
	        return true;
	    }
	    
	    public Boolean index(Ast.Index ast, Set<String> arg) {
	        // may generate error when provided with a negative size
	        dfltExpr(ast, arg);
	        
	        return true;
	    }
	        
	    public Boolean cast(Ast.Cast ast, Set<String> arg) {
	        dfltExpr(ast, arg);
	        
	        return true;
	    }
	    
	    public Boolean builtInRead(Ast.BuiltInRead ast, Set<String> arg) {
	        return true;
	    }
	    
	    public Boolean builtInReadFloat(Ast.BuiltInReadFloat ast, Set<String> arg) {
	        return true;
	    }
	    
	    public Boolean methodCall(Ast.MethodCallExpr ast, Set<String> usedVars) {
	        dfltExpr(ast, usedVars);
	        
	        return true;
	    }
	};
	
	private AstVisitor<String, Set<String>> detectUnused = new AstVisitor<String, Set<String>>() {
        public String assign(Ast.Assign ast, Set<String> usedVars) {
            if (ast.left() instanceof Ast.Var) {
                Var v = (Var)ast.left();
                
                if (usedVars.contains(v.sym.name) == false) {
                    return v.sym.name;
                }
            }
            
            return dfltStmt(ast, usedVars);
        }
        
        protected String dflt(Ast ast, Set<String> usedVars) {
            return null;
        }
    };
}
