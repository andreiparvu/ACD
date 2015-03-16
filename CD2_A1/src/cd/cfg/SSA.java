package cd.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import cd.Main;
import cd.debug.AstDump;
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
import cd.semantic.SymbolCreator;
import cd.semantic.SymbolCreator.MethodSymbolCreator;

public class SSA {
    
    public final Main main;
    
    private HashMap<String, Stack<Integer>> stacks = new HashMap<>();
    private HashMap<String, Integer> highestVersion = new HashMap<>();
    private MethodSymbolCreator symbolCreator;
    private Map<String, VariableSymbol> locals, params = new HashMap<>();
    
    public SSA(final Main main) {
        this.main = main;
    }
    
    public void compute(final MethodDecl mdecl) {
        symbolCreator = new SymbolCreator(main, null).new MethodSymbolCreator(mdecl.sym);
        stacks.clear();
        highestVersion.clear();
        locals = mdecl.sym.locals;
        
        for (int i=0; i<mdecl.argumentNames.size(); i++) {
        	// add initial argument version to locals
        	params.put(mdecl.argumentNames.get(i), mdecl.sym.parameters.get(i));
        }
        
        addPhis(mdecl);
        
        generateVariableVersions(mdecl);
    }
    
    private void addPhis(final MethodDecl mdecl) {
        final ControlFlowGraph cfg = mdecl.cfg;
        
        main.debug("Computing SSA form for %s", mdecl.name);
        
        
        HashMap<String, VariableSymbol> allVars = new HashMap<>(locals);

        
        allVars.putAll(params);
        for (String s : allVars.keySet()) {
            // initialize the first version of the symbol
            stacks.put(s, new Stack<Integer>());
            stacks.get(s).add(0);
            highestVersion.put(s, 0);
            
            VariableSymbol curSym = allVars.get(s);
            
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
                    main.debug("Add phi for " + s + " in " + frontier.index + " from " + bb.index);
                    
                    frontier.phis.put(curSym, new Phi(curSym, frontier.predecessors.size()));
                    
                    if (!hset.contains(frontier.index)) {
                        worklist.add(frontier);
                        hset.add(frontier.index);
                    }
                }
                hset.remove(bb.index);
            }
        }
        
        locals.clear();
    }
    
    private void generateVariableVersions(final MethodDecl mdecl) {
        final ControlFlowGraph cfg = mdecl.cfg;
        
        dfSearch(cfg.start);
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
                    
                    VariableSymbol next = createVersion(v.sym, false);
                    if (next == null) {
                    	next = varSym;
                    }
                	v.setSymbol(next);
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
    
    private class InstructionVisitor extends AstVisitor<Void, Boolean> {
        public Void var(Ast.Var ast, Boolean createVersion) {
            ast.setSymbol(createVersion(ast.sym, createVersion));
            
            return dfltExpr(ast, createVersion);
        }
        
        public Void assign(Ast.Assign ast, Boolean createVersion) {
            visit(ast.right(), false);
            // we want to create a new version for an assignment
            if (ast.left() instanceof Ast.Var) {
            	createVersion = true;
            }
            visit(ast.left(), createVersion);
            
            return null;
        }
    }
    
    private VariableSymbol createVersion(VariableSymbol x, boolean createVersion) {
        String name = x.name;
        if (createVersion) {
            int nextVer = highestVersion.get(name);
            
            stacks.get(name).add(nextVer + 1);
            highestVersion.put(name, nextVer + 1);
            
            VariableSymbol ret = new VariableSymbol(x, stacks.get(name).peek());
            symbolCreator.addSymbol(ret);
            
            return ret;
        }
        int version = stacks.get(name).peek();
        name += "_" + version;
        
        if (locals.get(name) == null) {
        	return params.get(x.name);
        }
        
        return locals.get(name);
    }
}
