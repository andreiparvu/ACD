package cd.cfg;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
	
	private final boolean INTENSE_DEBUG = true;
	private final int MAX_INNER = 16, MAX_OUTER = 16; 
	
	public final Main main;
	public int changes = 0;
	
	public static int overall;
	
	private int nrTemp = 0;
	
	public Optimizer(Main main) {
		this.main = main;
	}
	
	private String phase() {
		return format(".opt.%d", overall++);		
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
	    return Var.withSym(new VariableSymbol("temp_" + nrTemp,
	            new PrimitiveTypeSymbol("temp_" + nrTemp), VariableSymbol.Kind.LOCAL));
	}
	
	public void compute(MethodDecl md) {
	    ControlFlowGraph cfg = md.cfg;

	    Map<String, LeafExpr> propagations = new HashMap<>();
		int oldChanges = 0;
		int cnt = 0;
		do {
			oldChanges = changes;
			/*
			 * To do: 
			 * (1) constant propagation
			 * (2) copy propagation
			 * (3) common sub-expression elimination
			 */
			for (BasicBlock bb : cfg.allBlocks) {
			    for (Ast instr : bb.instructions) {
			        constantFolding.visit(instr, null);
			    }
			    if (bb.condition != null) {
			        bb.condition = (Expr)constantFolding.visit(bb.condition, null);
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
			if (cnt == 5) {
//				break;
			}
			cnt++;
		} while (changes != oldChanges);
	}
	
	private void identifySubexpression(BasicBlock curBB, ExpressionManager exprManager) {
	    List<String> curExpressions = new ArrayList<>();
	    exprManager.subexpressions = curExpressions;
	    
		for (int i = 0; i < curBB.instructions.size(); i++) {
		    exprManager.curPosition = i;
			canonicExpressionVisitor.visit(curBB.instructions.get(i), exprManager);
		}
		exprManager.curPosition = curBB.instructions.size();
		if (curBB.condition != null) {
			canonicExpressionVisitor.visit(curBB.condition, exprManager);
		}
		
		for (BasicBlock bb : curBB.dominatorTreeChildren) {
			identifySubexpression(bb, exprManager);
		}
		
		int nrAdded = 0;
		for (String expr : curExpressions) {
		    System.err.println(expr);
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
	
	private AstRewriteVisitor<ExpressionManager> canonicExpressionVisitor = new AstRewriteVisitor<ExpressionManager>() {
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
			if (ast.left() instanceof LeafExpr && ast.right() instanceof LeafExpr) {
				LeafExpr left = (LeafExpr)ast.left(), right = (LeafExpr)ast.right();
				
				
				if (left.isPropagatable && right.isPropagatable) {
					String leftStr = AstOneLine.toString(left);
					String rightStr = AstOneLine.toString(right);

					if (ast.isCommutative()) {
						if (leftStr.compareTo(rightStr) > 0) {
							String tmp = leftStr;
							leftStr = rightStr;
							rightStr = tmp;
						}
					}
					
					String canonicForm = String.format("%s %s %s", ast.operator.repr, leftStr, rightStr);
					if (!exprManager.info.containsKey(canonicForm)) {
					    Var next;
					    boolean isTemp = false;
					    if (exprManager.curVar == null) {
					        exprManager.subexpressions.add(canonicForm);
					        next = newTempVar();
					        isTemp = true;
					    } else {
					        next = exprManager.curVar;
					    }
					    
					    exprManager.info.put(canonicForm,
					            (new ExpressionManager()).new Data(exprManager.curPosition, next, ast, isTemp));
					} else {
					    changes++;
					    exprManager.isUsed.add(canonicForm);
					    return exprManager.info.get(canonicForm).substitute;
					}
				}
				

			}
			return dflt(ast, exprManager);
		}
	};
	
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
	        if (toPropagate.containsKey(ast.sym.name)) {
//		        System.err.println("==" + ast.sym.name);

	            changes++;
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
}
