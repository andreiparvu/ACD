package cd.cfg;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

import cd.Main;
import cd.ir.BasicBlock;
import cd.ir.ControlFlowGraph;

/** 
 * Computes dominators and dominator tree of a control-flow graph.
 */
public class Dominator {

	public final Main main;
	ArrayList<BitSet> inDom = new ArrayList<BitSet>(), outDom = new ArrayList<BitSet>();
	
	public Dominator(Main main) {
		this.main = main;
	}

	public void compute(ControlFlowGraph cfg) {
		computeDominatorTree(cfg);
		computeFrontier(cfg.start);
	}

	private void computeDominatorTree(ControlFlowGraph cfg) {
		Boolean hasChanged = true;
		int numberOfBlocks = cfg.count();

		ArrayList<BitSet> outCopy = new ArrayList<>();
		
		// initialize CFG analysis, in and out
		for (int i = 0; i < numberOfBlocks; i++) {
			inDom.add(new BitSet(numberOfBlocks));
			outDom.add(new BitSet(numberOfBlocks));
			if (i != 0) {
				outDom.get(i).set(0, numberOfBlocks);
			}
			outCopy.add(new BitSet(numberOfBlocks));
		}

		outDom.get(0).set(0);
		while (hasChanged) {
			hasChanged = false;

			for (int i = 1; i < numberOfBlocks; i++) {
				inDom.get(i).set(0, numberOfBlocks);
				BitSet cur = new BitSet(numberOfBlocks);

				// do the intersection of the predecessors
				for (BasicBlock pred : cfg.allBlocks.get(i).predecessors) {
					inDom.get(i).and(outDom.get(pred.index));
				}
				cur.or(inDom.get(i));
				cur.set(i);

				if (cur.equals(outDom.get(i)) == false) {
					// set hasChanged to true if there is a difference from the previous out
					outDom.get(i).set(0, numberOfBlocks, false);
					outDom.get(i).or(cur);

					outCopy.get(i).set(0, numberOfBlocks, false);
                    outCopy.get(i).or(cur);
                    
					hasChanged = true;
				}
			}
		}
		
		// create dominance tree, using BFS
		LinkedList<Integer> q = new LinkedList<>();
		
		q.add(cfg.start.index);
		
		while (q.isEmpty() == false) {
			int curBB = q.poll();
			
			for (int i = 0; i < numberOfBlocks; i++) {
				if (i != curBB && outCopy.get(i).get(curBB)) {
					outCopy.get(i).set(curBB, false);
					// i-th block has only one remaining dominator (itself), so the current
					// block must be its immediate dominator
					if (outCopy.get(i).cardinality() == 1) {
						cfg.allBlocks.get(curBB).dominatorTreeChildren.add(cfg.allBlocks.get(i));
						cfg.allBlocks.get(i).dominatorTreeParent = cfg.allBlocks.get(curBB);
						q.add(i);
					}
				}
			}
		}
	}

	private void computeFrontier(BasicBlock curBB) {
	    for (BasicBlock child : curBB.dominatorTreeChildren) {
	        computeFrontier(child);
	        // add DFup of the children to the current block
	        for (BasicBlock df : child.dominanceFrontier) {
	            if (outDom.get(df.index).get(curBB.index) == false) {
	                curBB.dominanceFrontier.add(df);
	            }
	        }
	    }
	    // add DFlocal
	    for (BasicBlock succ : curBB.successors) {
	        if (outDom.get(succ.index).get(curBB.index) == false) {
	            curBB.dominanceFrontier.add(succ);
	        }
	    }
	}
}
