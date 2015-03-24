package cd.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cd.Main;
import cd.exceptions.SemanticFailure;
import cd.exceptions.ToDoException;
import cd.exceptions.SemanticFailure.Cause;
import cd.ir.Ast;
import cd.ir.Ast.Assign;
import cd.ir.Ast.Expr;
import cd.ir.Ast.MethodDecl;
import cd.ir.Ast.Var;
import cd.ir.Ast.VarDecl;
import cd.ir.AstVisitor;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;
import cd.ir.Phi;
import cd.ir.Symbol.MethodSymbol;
import cd.ir.Symbol.VariableSymbol;

public class SSA {
    
    public final Main main;
    
    public SSA(final Main main) {
        this.main = main;
    }
    
    private MethodSymbol msym;
    
    private Map<VariableSymbol, Integer> maxVersions;
    private Set<VariableSymbol> possibilyUninitialized;
    
    public void compute(final MethodDecl mdecl) {
        final ControlFlowGraph cfg = mdecl.cfg;
        
        main.debug("Computing SSA form for %s", mdecl.name);
        
        {
            // Phase 1: introduce Phis
            for(final BasicBlock bb : cfg.allBlocks) {
                main.debug("  Adding Phis for %s of %s", bb, mdecl.name);
                
                // Compute iterated dominance frontier for this block 'bb'.
                final Set<BasicBlock> idf = new HashSet<BasicBlock>();
                computeIteratedDominanceFrontier(bb, idf);
                main.debug("    df=%s", bb.dominanceFrontier);
                main.debug("    idf=%s", idf);
                
                // Introduce phi blocks.
                for(final Ast ast : bb.instructions)
                    new IntroducePhiVisitor().visit(ast, idf);
            }
            
            // Phase 2: Renumber
            msym = mdecl.sym;
            maxVersions = new HashMap<VariableSymbol, Integer>();
            possibilyUninitialized = new HashSet<VariableSymbol>();
            final Map<VariableSymbol, VariableSymbol> currentVersions =
                    new HashMap<VariableSymbol, VariableSymbol>();
            for(final VariableSymbol sym : mdecl.sym.parameters)
                currentVersions.put(sym, sym);
            renumberBlock(cfg.start, currentVersions);
            
            {
            	// Phase 3: Detect uses of (potentially) uninitialized variables
            	(new AstVisitor<Void, Void>() {

            		@Override
					public Void varDecl(VarDecl ast, Void arg) {
						// decls are visited before method body
						possibilyUninitialized.add(ast.sym);
						
						return super.varDecl(ast, arg);
					}

					@Override
					public Void var(Var ast, Void arg) {
						if (possibilyUninitialized.contains(ast.sym)) {
					    	throw new SemanticFailure(Cause.POSSIBLY_UNINITIALIZED, 
					    			"use of possibly possibly uninitalized variable: %s", ast.name);
						}
						return super.var(ast, arg);
					}
            		
            	}).visit(mdecl, null);
            }
        }
    }
    
    public void computeIteratedDominanceFrontier(final BasicBlock bb,
            final Set<BasicBlock> idf) {
        for(final BasicBlock b : bb.dominanceFrontier) {
            if(idf.add(b))
                computeIteratedDominanceFrontier(b, idf);
        }
    }
    
    class IntroducePhiVisitor extends AstVisitor<Void, Set<BasicBlock>> {
        
        @Override
        public Void assign(final Assign ast, final Set<BasicBlock> idf) {
            final Expr lhs = ast.left();
            if(lhs instanceof Var) {
                final Var var = (Var) lhs;
                final VariableSymbol sym = var.sym;
                addPhis(sym, idf);
            }
            return super.assign(ast, idf);
        }
        
        private void addPhis(final VariableSymbol sym, final Set<BasicBlock> idf) {
            main.debug("Introducing phis for %s at %s", sym, idf);
            for(final BasicBlock bb : idf) {
                if(bb.phis.containsKey(sym))
                    continue; // already has a phi for this symbol
                final Phi phi = new Phi(sym, bb.predecessors.size());
                bb.phis.put(sym, phi);
                main.debug("  New phi created %s at %s", phi, bb);
            }
        }
        
    }
    
    private void renumberBlock(final BasicBlock block,
            final Map<VariableSymbol, VariableSymbol> inCurrentVersions) {
        final Map<VariableSymbol, VariableSymbol> currentVersions =
                new HashMap<VariableSymbol, VariableSymbol>(inCurrentVersions);
        
        for(final Phi phi : block.phis.values()) {
            assert phi.v0sym == phi.lhs;
            phi.lhs = renumberDefinedSymbol(phi.lhs, currentVersions);
            if (phi.containsUninitalized) {
                possibilyUninitialized.add(phi.lhs);
            }
        }
        
        for(final Ast ast : block.instructions)
            renumberAST(ast, currentVersions);
        
        if(block.condition != null)
            renumberAST(block.condition, currentVersions);
        
        for(final BasicBlock succ : block.successors) {
            final int predIndex = succ.predecessors.indexOf(block);
            for(final Phi phi : succ.phis.values()) {
                final VariableSymbol cursym = currentVersions.get(phi.v0sym);
                {
                    if(cursym != null) {
                        phi.rhs.set(predIndex, Ast.Var.withSym(cursym));
                    }
                    else {
                        // Possibly uninitialized! Whatever shall we do?
                    	phi.containsUninitalized = true;
                    }
                }
            }
        }
        
        for(final BasicBlock dblock : block.dominatorTreeChildren) {
            renumberBlock(dblock, currentVersions);
        }
    }
    
    /**
     * Rewrites an instruction/expression from within a block so that all
     * references to variables use the most recent versions of that variable. If
     * there are any assignments, then updates the currentVersions table with a
     * new version for the variable.
     */
    private void renumberAST(final Ast ast,
            final Map<VariableSymbol, VariableSymbol> currentVersions) {
        new AstVisitor<Void, Void>() {
            
            /**
             * This method is reached when we encounter a potential DEFINITION
             * of a variable. Therefore, it defines a new version of that
             * variable and updates the currentVersions table.
             */
            @Override
            public Void assign(final Assign ast, final Void arg) {
                renumberAST(ast.right(), currentVersions);
                
                final Ast lhs = ast.left();
                if(lhs instanceof Ast.Var) {
                    final Var var = (Var) lhs;
                    var.setSymbol(renumberDefinedSymbol(var.sym,
                            currentVersions));
                }
                else {
                    renumberAST(ast.left(), currentVersions);
                }
                return null;
            }
            
            /**
             * This method is reached when we encounter a USE of a variable. It
             * rewrites the variable to use whatever version is current.
             */
            @Override
            public Void var(final Var ast, final Void arg) {
                {
                    if(currentVersions.containsKey(ast.sym)) {
                        ast.setSymbol(currentVersions.get(ast.sym));
                    }
                    else {
                        // Possibly uninitialized! Whatever shall we do?
                    	// TODO insert error here?
                    }
                }
                return null;
            }
            
        }.visit(ast, null);
    }
    
    private VariableSymbol renumberDefinedSymbol(final VariableSymbol lhs,
            final Map<VariableSymbol, VariableSymbol> currentVersions) {
        assert lhs.version == 0; // this should be a v0sym
        if(!maxVersions.containsKey(lhs))
            maxVersions.put(lhs, 0);
        final int version = maxVersions.get(lhs) + 1;
        maxVersions.put(lhs, version);
        final VariableSymbol sym = new VariableSymbol(lhs, version);
        msym.locals.put(sym.toString(), sym);
        currentVersions.put(lhs, sym);
        return sym;
    }
}
