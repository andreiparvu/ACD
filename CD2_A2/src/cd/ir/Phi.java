package cd.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cd.ir.Ast.Expr;
import cd.ir.Ast.IntConst;
import cd.ir.Ast.LeafExpr;
import cd.ir.Symbol.VariableSymbol;

public class Phi {
	public final VariableSymbol v0sym;
	public VariableSymbol lhs;
	public List<Expr> rhs = new ArrayList<Expr>(); // Always an Ast.Var or an Ast.Const!
	public boolean isConstant;
	
	public Phi(VariableSymbol v0sym, int predCount) {
		this.v0sym = v0sym;
		this.lhs = v0sym;
		this.isConstant = false;
		for (int i = 0; i < predCount; i++)
			rhs.add(Ast.Var.withSym(v0sym));
	}
	
	public void checkIfConstant(Map<String, LeafExpr> toPropagate) {
	    
	    for (Expr e : rhs) {
	        if (e.isConstant() == 0) {
	            return ;
	        }
	    }
	    
	    for (int i = 1; i < rhs.size(); i++) {
	        if (!rhs.get(0).compareTo((IntConst)rhs.get(i))) {
	            return ;
	        }
	    }
	    
	    isConstant = true;
	    toPropagate.put(lhs.name, (LeafExpr)rhs.get(0));
	}
	
	public String toString() {
		return String.format("<%s = phi%s>", lhs, rhs); 
	}
}
