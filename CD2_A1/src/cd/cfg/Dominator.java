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

	public Dominator(Main main) {
		this.main = main;
	}

	public void compute(ControlFlowGraph cfg) {
		computeDominatorTree(cfg);
		computeFrontier(cfg);
	}

	private void computeDominatorTree(ControlFlowGraph cfg) {
		ArrayList<BitSet> in = new ArrayList<BitSet>(), out = new ArrayList<BitSet>();
		Boolean hasChanged = true;
		int numberOfBlocks = cfg.count();

		for (int i = 0; i < numberOfBlocks; i++) {
			in.add(new BitSet(numberOfBlocks));
			out.add(new BitSet(numberOfBlocks));
			if (i != 0) {
				out.get(i).set(0, numberOfBlocks);
			}
		}

		out.get(0).set(0);
		while (hasChanged) {
			hasChanged = false;

			for (int i = 1; i < numberOfBlocks; i++) {
				in.get(i).set(0, numberOfBlocks);
				BitSet cur = new BitSet(numberOfBlocks);

				for (BasicBlock pred : cfg.allBlocks.get(i).predecessors) {
					in.get(i).and(out.get(pred.index));
				}
				cur.or(in.get(i));
				cur.set(i);

				if (cur.equals(out.get(i)) == false) {
					out.get(i).set(0, numberOfBlocks, false);
					out.get(i).or(cur);

					hasChanged = true;
				}
			}
		}
		
		LinkedList<Integer> q = new LinkedList<>();
		
		q.add(cfg.start.index);
		
		while (q.isEmpty() == false) {
			int curBB = q.poll();
			
			for (int i = 0; i < numberOfBlocks; i++) {
				if (i != curBB && out.get(i).get(curBB)) {
					out.get(i).set(curBB, false);
					if (out.get(i).cardinality() == 1) {
						cfg.allBlocks.get(curBB).dominatorTreeChildren.add(cfg.allBlocks.get(i));
						cfg.allBlocks.get(i).dominatorTreeParent = cfg.allBlocks.get(curBB);
						q.add(i);
					}
				}
			}
		}
	}

	private void computeFrontier(ControlFlowGraph cfg) {
		for (BasicBlock node : cfg.allBlocks) {
			for (BasicBlock pred : node.predecessors) {
				BasicBlock cur = pred;
				
				while (cur != null && cur != node.dominatorTreeParent) {
					cur.dominanceFrontier.add(node);
					cur = cur.dominatorTreeParent;
				}
			}
		}
	}
}
