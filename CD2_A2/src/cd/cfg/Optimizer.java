package cd.cfg;

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
import cd.ir.Ast.FloatConst;
import cd.ir.Ast.IntConst;
import cd.ir.Ast.LeafExpr;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.UnaryOp;
import cd.ir.Ast.BinaryOp.BOp;
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
		return Var.withSym(sym);
	}

	public void compute(MethodDecl md) {
		ControlFlowGraph cfg = md.cfg;
		mdecl = md;

		Map<String, LeafExpr> propagations = new HashMap<>();
		int oldChanges = 0;
		do {
			oldChanges = changes;

			// constant fold instructions, conditions and phis
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

			System.err.println("phase" + changes);
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

		@Override
		public Ast visitChildren(Expr expr, A arg) {
			ListIterator<Ast> children = expr.rwChildren.listIterator();
			while (children.hasNext()) {
				Expr child = (Expr)children.next();
				if (child != null) {
					Ast replace = visit(child, arg);
					if (replace != child) {
						System.err.format("Replace: %s <- %s\n", AstOneLine.toString(child), AstOneLine.toString(replace));
						changes++;

						children.set(replace);
					}
				}
			}
			return expr;
		}
	}

	private void identifySubexpression(BasicBlock curBB, ExpressionManager exprManager) {
		List<String> curExpressions = new ArrayList<>();
		exprManager.subexpressions = curExpressions;

		for (int i = 0; i < curBB.instructions.size(); i++) {
			exprManager.curPosition = i;
			// generate the polish form of all the expressions
			generateCanonicalForm.visit(curBB.instructions.get(i), null);
			// generate new temporary variables or replace expressions with existing variables
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
					// have to insert a new temporary variable, right before
					// the instruction in which is was created
					curBB.instructions.add(data.position + nrAdded,
					                       new Assign(data.substitute, data.node));
					mdecl.sym.locals.put(data.substitute.sym.name, data.substitute.sym);
					nrAdded++;
				}
			}
			exprManager.info.remove(expr);
		}

	}

	private AstVisitor<Void, Void> generateCanonicalForm = new AstVisitor<Void, Void>() {

		@Override
		protected Void dfltExpr(Expr ast, Void arg) {
			if (ast.isCachable()) {
				ast.canonicalForm = AstOneLine.toString(ast);
			}

			return super.dfltExpr(ast, arg);
		}

		@Override
		public Void binaryOp(BinaryOp ast, Void arg) {
			visit(ast.left(), arg);
			visit(ast.right(), arg);

			String leftStr = ast.left().canonicalForm;
			String rightStr = ast.right().canonicalForm;

			if (ast.isCachable()) {
				assert leftStr != null && rightStr != null;
				if (ast.isCommutative()) {
					if (leftStr.compareTo(rightStr) > 0) {
						String tmp = leftStr;
						leftStr = rightStr;
						rightStr = tmp;
					}
				}

				// use Polish form: operator / left / right
				ast.canonicalForm = String.format("%s %s %s", ast.operator.repr, leftStr, rightStr);
			}

			return null;
		}

		@Override
		public Void unaryOp(UnaryOp ast, Void arg) {
			visit(ast.arg(), arg);

			if (ast.arg().isCachable()) {
				assert ast.arg().canonicalForm != null;
				ast.canonicalForm = String.format("%s %s", ast.operator.repr, ast.arg().canonicalForm);
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
				assert ast.isCachable();
				if (!exprManager.info.containsKey(ast.canonicalForm)) {
					Var next;
					boolean isTemp = false;
					// determine is we use the current variable, or create a temporary one
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
					// cache the expression
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
			// make sure left() and right() are rewritten
			visitChildren(ast, arg);

			Expr left = ast.left(), right = ast.right();

			if (left instanceof BooleanConst && right instanceof BooleanConst) {
				return booleanConstOp(ast, (BooleanConst)left, (BooleanConst)right);
			} else if (left instanceof IntConst && right instanceof IntConst) {
				return intConstOp(ast, (IntConst)left, (IntConst)right);
			} else if (left instanceof FloatConst && right instanceof FloatConst) {
				return floatConstOp(ast, (FloatConst)left, (FloatConst)right);
			} else if (ast.type.equals(main.intType)) {
				return intBinOpSimplification(ast);
			}

			return ast;
		}

		private Expr intConstOp(BinaryOp op, IntConst left, IntConst right) {
			switch(op.operator) {
			case B_PLUS:
				return new IntConst(left.value + right.value);
			case B_MINUS:
				return new IntConst(left.value - right.value);
			case B_TIMES:
				return new IntConst(left.value * right.value);
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
			case B_EQUAL:
				return new BooleanConst(left.value == right.value);
			case B_GREATER_OR_EQUAL:
				return new BooleanConst(left.value >= right.value);
			case B_GREATER_THAN:
				return new BooleanConst(left.value > right.value);
			case B_LESS_OR_EQUAL:
				return new BooleanConst(left.value <= right.value);
			case B_LESS_THAN:
				return new BooleanConst(left.value < right.value);
			case B_NOT_EQUAL:
				return new BooleanConst(left.value != right.value);
			default:
				break;
			}

			return op;
		}

		private Expr intBinOpSimplification(BinaryOp ast) {
			Expr left = ast.left();
			Expr right = ast.right();

			assert ast.type.equals(main.intType);
			assert !(right instanceof IntConst && left instanceof IntConst);

			String leftStr = AstOneLine.toString(left), rightStr = AstOneLine.toString(right);

			// CachableExpr - CachableExpr
			if (ast.operator == BOp.B_MINUS &&
			        leftStr.equals(rightStr) &&
			        left.isCachable() && right.isCachable()) {
				return new IntConst(0);
			} else if (right instanceof IntConst) {
				int rightVal = ((IntConst)right).value;

				// Expr - 0
				if (ast.operator == BOp.B_MINUS && rightVal == 0) {
					return left;
				}

				// Expr / 1
				if (ast.operator == BOp.B_DIV && rightVal == 1) {
					return left;
				}

				// Expr + IntConst | Expr * IntConst
				return intBinOpAsymmetricSimplification(ast, (IntConst)right, left);
			} else if (left instanceof IntConst) {
				int leftVal = ((IntConst)left).value;

				// 0 - Expr
				if (ast.operator == BOp.B_MINUS && leftVal == 0) {
					UnaryOp newAst = new UnaryOp(UOp.U_MINUS, right);
					newAst.type = ast.type;
					return newAst;
				}

				// IntConst + Expr | IntConst * Expr
				return intBinOpAsymmetricSimplification(ast, (IntConst)left, right);
			}


			return ast;
		}

		private Expr intBinOpAsymmetricSimplification(BinaryOp ast, IntConst first, Expr second) {
			// 0 + Expr
			if (ast.operator == BOp.B_PLUS && first.value == 0) {
				return second;
			}

			// 1 * Expr
			if (ast.operator == BOp.B_TIMES && first.value == 1) {
				return second;
			}

			// IntConst * CachableExpr
			if (ast.operator == BOp.B_TIMES && second.isCachable()) {
				switch (first.value) {
				case 0:
					return new IntConst(0);
				case 2:
					// make sure to keep type information
					BinaryOp newAst = ast.deepCopy();
					newAst.operator = BOp.B_PLUS;
					newAst.setLeft(second);
					newAst.setRight(second);
					return newAst;
				}
			}

			return ast;
		}

		private Expr booleanConstOp(BinaryOp op, BooleanConst left, BooleanConst right) {
			switch (op.operator) {
			case B_OR:
				return new BooleanConst(left.value || right.value);
			case B_AND:
				return new BooleanConst(left.value && right.value);
			default:
				return op;
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
		strictfp private Expr floatConstOp(BinaryOp op, FloatConst left, FloatConst right) {
			switch(op.operator) {
			case B_PLUS:
				return new FloatConst(left.value + right.value);
			case B_MINUS:
				return new FloatConst(left.value - right.value);
			case B_TIMES:
				return new FloatConst(left.value * right.value);
			case B_DIV:
				return new FloatConst(left.value / right.value);
			case B_EQUAL:
				return new BooleanConst(left.value == right.value);
			case B_GREATER_OR_EQUAL:
				return new BooleanConst(left.value >= right.value);
			case B_GREATER_THAN:
				return new BooleanConst(left.value > right.value);
			case B_LESS_OR_EQUAL:
				return new BooleanConst(left.value <= right.value);
			case B_LESS_THAN:
				return new BooleanConst(left.value < right.value);
			case B_NOT_EQUAL:
				return new BooleanConst(left.value != right.value);
			default:
				break;
			}

			return op;
		}


		@Override
		strictfp public Expr unaryOp(UnaryOp op, Void arg) {
			Ast child = visit(op.arg(), arg);
			if (child instanceof IntConst) {
				IntConst val = (IntConst) child;
				switch (op.operator) {
				case U_MINUS:
					return new IntConst(-val.value);
				case U_PLUS:
					return new IntConst(val.value);
				default:
					break;
				}
			} else if (child instanceof FloatConst) {
				FloatConst val = (FloatConst) child;
				switch (op.operator) {
				case U_MINUS:
					return new FloatConst(-val.value);
				case U_PLUS:
					return new FloatConst(val.value);
				default:
					break;
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
		// propagate copies in phi nodes
		for (Phi phi : bb.phis.values()) {
			for (int i = 0; i < phi.rhs.size(); i++) {
				phi.rhs.set(i, (Expr)propagateVisitor.visit(phi.rhs.get(i), toPropagate));
			}
		}
		// remove if phi is constant - no matter from which basic block you come, we same result will be phi-ed
		Iterator<Map.Entry<VariableSymbol, Phi>> iter = bb.phis.entrySet().iterator();
		while (iter.hasNext()) {
			if ((iter.next()).getValue().isConstant) {
				iter.remove();
			}
		}

		for (Ast instr : bb.instructions) {
			// propagate variables and collect new variables to propagate
			propagateVisitor.visit(instr, toPropagate);
			collectVisitor.visit(instr, toPropagate);
		}
		if (bb.condition != null) {
			bb.condition = (Expr)propagateVisitor.visit(bb.condition, toPropagate);
		}

		for (BasicBlock next : bb.dominatorTreeChildren) {
			propagateCopies(next, toPropagate);
		}
	}

	private OptimizerAstRewriter<Map<String, LeafExpr>> propagateVisitor = new OptimizerAstRewriter<Map<String, LeafExpr>>() {
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
	};

	private AstVisitor<Void, Map<String, LeafExpr>> collectVisitor = new AstVisitor<Void, Map<String, LeafExpr>>() {
		public Void assign(Ast.Assign ast, Map<String, LeafExpr> toPropagate) {
			if (ast.left() instanceof Ast.Var && ast.right() instanceof LeafExpr) {
				if (((LeafExpr)ast.right()).isCachable()) {
					toPropagate.put(((Ast.Var)ast.left()).sym.name, (LeafExpr)ast.right());
				}
			}

			return dfltStmt(ast, null);
		}

		public Void visitChildren(Ast ast, Map<String, LeafExpr> arg) {
			return null;
		}
	};
}
