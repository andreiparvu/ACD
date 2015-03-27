package cd.ir;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
<<<<<<< HEAD
import java.util.Set;
=======
>>>>>>> master

import cd.ir.Ast.BooleanConst;
import cd.ir.Ast.Expr;
<<<<<<< HEAD
import cd.ir.Ast.IntConst;
import cd.ir.Ast.LeafExpr;
import cd.ir.Ast.Var;
=======
import cd.ir.Ast.FloatConst;
import cd.ir.Ast.IntConst;
import cd.ir.Ast.LeafExpr;
>>>>>>> master
import cd.ir.Symbol.VariableSymbol;

public class Phi {
	public final VariableSymbol v0sym;
	public VariableSymbol lhs;
	public List<Expr> rhs = new ArrayList<Expr>(); // Always an Ast.Var or an Ast.Const!
	public boolean isConstant;
<<<<<<< HEAD
=======
	public boolean containsUninitalized;
>>>>>>> master
	
	public Phi(VariableSymbol v0sym, int predCount) {
		this.v0sym = v0sym;
		this.lhs = v0sym;
		this.isConstant = false;
<<<<<<< HEAD
=======
		this.containsUninitalized = false;
>>>>>>> master
		for (int i = 0; i < predCount; i++)
			rhs.add(Ast.Var.withSym(v0sym));
	}
	
	public void checkIfConstant(Map<String, LeafExpr> toPropagate) {
<<<<<<< HEAD
	    if (rhs.size() == 1) {
	        toPropagate.put(lhs.name, (LeafExpr)rhs.get(0));
	        isConstant = true;
	        return ;
	    }
=======
>>>>>>> master
	    
	    for (Expr e : rhs) {
	        if (e.isConstant() == 0) {
	            return ;
	        }
	    }
	    
	    for (int i = 1; i < rhs.size(); i++) {
<<<<<<< HEAD
	        if (!rhs.get(0).compareTo((IntConst)rhs.get(i))) {
	            return ;
	        }
=======
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
>>>>>>> master
	    }
	    
	    isConstant = true;
	    toPropagate.put(lhs.name, (LeafExpr)rhs.get(0));
	}
	
<<<<<<< HEAD
	public void detectUses(Set<String> usedVars) {
	    for (Expr e : rhs) {
	        if (e instanceof Var) {
	            usedVars.add(((Var)e).sym.name);
	        }
	    }
	}
	
=======
>>>>>>> master
	public String toString() {
		return String.format("<%s = phi%s>", lhs, rhs); 
	}
}
