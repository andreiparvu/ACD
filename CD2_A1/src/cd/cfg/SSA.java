package cd.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import cd.Main;
import cd.ir.Ast;
import cd.ir.Ast.Expr;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.Var;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.ir.Phi;
import cd.ir.Symbol;
import cd.ir.Symbol.VariableSymbol;

public class SSA {
    
    public final Main main;
    
    private HashMap<String, Stack<Integer>> stacks = new HashMap<>();
    private HashMap<String, Integer> highestVersion = new HashMap<>();
    
    public SSA(final Main main) {
        this.main = main;
    }
    
    private void addPhis(final MethodDecl mdecl) {
        final ControlFlowGraph cfg = mdecl.cfg;
        
        main.debug("Computing SSA form for %s", mdecl.name);
        
        
        // TODO: the same thing for method fields ?
        for (String s : mdecl.sym.locals.keySet()) {
            
            // initialize the first version of the symbol
            stacks.put(s, new Stack<Integer>());
            stacks.get(s).add(0);
            highestVersion.put(s, 0);
            
            System.err.println("Check " + s);
            VariableSymbol curSym = mdecl.sym.locals.get(s);
            
            LinkedList<BasicBlock> worklist = new LinkedList<>();
            HashSet<Integer> hset = new HashSet<>();
            
            // add blocks which assign a value to current symbol to worklist
            for (BasicBlock bb : cfg.allBlocks) {
                for (Symbol ss : bb.assignedSyms) {
                    if (ss.name == s) {
                        if (!hset.contains(bb.index)) {
                            worklist.add(bb);
                            hset.add(bb.index);
                        }
                    }
                }
            }

            // Add phi nodes in the dominance frontier
            while (worklist.isEmpty() == false) {
                BasicBlock bb = worklist.poll();
                
                for (BasicBlock frontier : bb.dominanceFrontier) {
                    System.err.println("Add phi for " + s + " in " + frontier.index + " from " + bb.index);
                    
                    frontier.phis.put(curSym, new Phi(curSym, frontier.predecessors.size()));
                    
                    if (!hset.contains(frontier.index)) {
                        worklist.add(frontier);
                        hset.add(frontier.index);
                    }
                }
                hset.remove(bb.index);
            }
        }
    }
    
    private VariableSymbol createVersion(VariableSymbol x, boolean createVersion) {
        String name = x.name;
        
        if (createVersion) {
            int nextVer = highestVersion.get(name);
            
            stacks.get(name).add(nextVer + 1);
            highestVersion.put(name, nextVer + 1);
        }
        return new VariableSymbol(x, stacks.get(name).peek());
    }
    
    void dfSearch(BasicBlock curBB) {
        // store the positions of the stacks before processing current BB
        HashMap<String, Integer> lastPos = new HashMap<>();
        
        for (String s : stacks.keySet()) {
            lastPos.put(s, stacks.get(s).size());
        }
        
        // create new versions for lhs of phis
        for (VariableSymbol varSym : curBB.phis.keySet()) {
            curBB.phis.get(varSym).lhs = createVersion(varSym, true);
        }
        
        // visit each instruction and create new versions
        for (Ast instr : curBB.instructions) {
            System.out.println(instr);
            (new InstructionVisitor()).visit(instr, false);
        }
        if (curBB.condition != null) {
            (new InstructionVisitor()).visit(curBB.condition, false);
        }
        
        // for each successor, check phi functions and assign a version to the
        // parameter corresponding to current BB
        for (BasicBlock succ : curBB.successors) {
            int index = succ.whichPred(curBB);
            
            for (VariableSymbol varSym : succ.phis.keySet()) {
                Expr e = succ.phis.get(varSym).rhs.get(index);
                if (e instanceof Var) {
                    Var v = (Var)e;
                    
                    v.setSymbol(createVersion(v.sym, false));
                }
            }
        }
        
        // recurse
        for (BasicBlock nextBB : curBB.dominatorTreeChildren) {
            dfSearch(nextBB);
        }
        
        // restore stacks
        for (String s : stacks.keySet()) {
            Stack st = stacks.get(s);
            
            while (st.size() > lastPos.get(s)) {
                st.pop();
            }
        }
    }
    
    private void createVersions(final MethodDecl mdecl) {
        final ControlFlowGraph cfg = mdecl.cfg;
        
        dfSearch(cfg.start);
    }
    
    public void compute(final MethodDecl mdecl) {
        addPhis(mdecl);
        
        createVersions(mdecl);
    }
    
    private class InstructionVisitor extends AstVisitor<Void, Boolean> {
        protected Void dflt(Ast ast, Boolean createVersion) {
            if (ast instanceof Var) { // should get rid of this, overload function
                Var x = (Var)ast;
                
                x.setSymbol(createVersion(x.sym, createVersion));
            }
            
            return visitChildren(ast, createVersion);
        }
        
        public Void assign(Ast.Assign ast, Boolean createVersion) {
            dfltExpr(ast.right(), false);
            // we want to create a new version for an assignment
            dfltExpr(ast.left(), true);
            
            return null;
        }
    }
}
