package cd.cfg;

import static java.lang.String.format;

import java.util.ListIterator;

import cd.Main;
import cd.exceptions.ToDoException;
import cd.ir.Ast;
import cd.ir.Ast.UnaryOp;
import cd.ir.Ast.UnaryOp.UOp;
import cd.ir.AstRewriteVisitor;
import cd.ir.Ast.BinaryOp;
import cd.ir.Ast.BooleanConst;
import cd.ir.Ast.IntConst;
import cd.ir.Ast.MethodDecl;

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
		{
			int oldChanges = 0;
			do {
				oldChanges = changes;
				/*
				 * To do: 
				 * (1) constant propagation
				 * (2) copy propagation
				 * (3) common sub-expression elimination
				 */
				constantFolding.visit(md, null);
				System.err.println("Phase " + changes);
			} while (changes != oldChanges);
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


}
