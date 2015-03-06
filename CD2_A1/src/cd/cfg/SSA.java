package cd.cfg;

import cd.Main;
import cd.exceptions.ToDoException;
import cd.ir.Ast.MethodDecl;
import cd.ir.ControlFlowGraph;

public class SSA {
    
    public final Main main;
    
    public SSA(final Main main) {
        this.main = main;
    }
    
    public void compute(final MethodDecl mdecl) {
        final ControlFlowGraph cfg = mdecl.cfg;
        
        main.debug("Computing SSA form for %s", mdecl.name);
        
        {
            throw new ToDoException();
        }
    }
}
