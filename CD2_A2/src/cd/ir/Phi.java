package cd.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cd.ir.Ast.BooleanConst;
import cd.ir.Ast.Expr;
import cd.ir.Ast.IntConst;
import cd.ir.Ast.LeafExpr;
import cd.ir.Ast.Var;
import cd.ir.Ast.FloatConst;
import cd.ir.Ast.IntConst;
import cd.ir.Ast.LeafExpr;
import cd.ir.Symbol.VariableSymbol;

public class Phi {
	public final VariableSymbol v0sym;
	public VariableSymbol lhs;
	public List<Expr> rhs = new ArrayList<Expr>(); // Always an Ast.Var or an Ast.Const!
	public boolean isConstant;
	public boolean containsUninitalized;
	
	public Phi(VariableSymbol v0sym, int predCount) {
		this.v0sym = v0sym;
		this.lhs = v0sym;
		this.isConstant = false;
		this.containsUninitalized = false;
		for (int i = 0; i < predCount; i++)
			rhs.add(Ast.Var.withSym(v0sym));
	}
	
	public void checkIfConstant(Map<String, LeafExpr> toPropagate) {
	    if (rhs.size() == 1) {
	        toPropagate.put(lhs.name, (LeafExpr)rhs.get(0));
	        isConstant = true;
	        return ;
	    }
	    
	    for (Expr e : rhs) {
	        if (e.isConstant() == 0) {
	            return ;
	        }
	    }
	    
	    for (int i = 1; i < rhs.size(); i++) {
	    	switch (rhs.get(0).isConstant()) {
	    	case Expr.INT:
	    		if (!rhs.get(0).compareTo((IntConst)rhs.get(i))) {
		            return ;
		        }
	    		break;
	    	case Expr.FLOAT:
	    		if (!rhs.get(0).compareTo((FloatConst)rhs.get(i))) {
		            return ;
		        }
	    		break;
	    	case Expr.BOOL:
	    		if (!rhs.get(0).compareTo((BooleanConst)rhs.get(i))) {
		            return ;
		        }
	    		break;
	    	}
	    }
	    
	    isConstant = true;
	    toPropagate.put(lhs.name, (LeafExpr)rhs.get(0));
	}
	
	public void detectUses(Set<String> usedVars) {
	    for (Expr e : rhs) {
	        if (e instanceof Var) {
	            usedVars.add(((Var)e).sym.name);
	        }
	    }
	}
	
	public String toString() {
		return String.format("<%s = phi%s>", lhs, rhs); 
	}
}
