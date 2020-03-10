package jp.ac.osaka_u.ist.sel.ccinvec.model;

import java.io.Serializable;

public class ClonePair implements Comparable<ClonePair>,Serializable {
	public Block cloneA;
	public Block cloneB;
	public final double sim;
//	public boolean check = false;
//	public int set;
	public ClonePair(Block cloneA, Block cloneB, double sim) {
		if (cloneA.getId() < cloneB.getId()) {
			this.cloneA = cloneA;
			this.cloneB = cloneB;
		} else {
			this.cloneA = cloneB;
			this.cloneB = cloneA;
		}
		this.sim = sim;
	}

	public void setCloneA(Block block) {
		this.cloneA = block;

	}

	public void setCloneB(Block block) {
		this.cloneB = block;

	}

	public int getCloneAId() {
		return this.cloneA.getId();
	}

	public int getCloneBId() {
		return this.cloneB.getId();
	}

	@Override
	public int compareTo(ClonePair o) {
		if (this.cloneA.getId() == o.cloneA.getId())
			return this.cloneB.getId() - o.cloneB.getId();

		return this.cloneA.getId() - o.cloneA.getId();
	}


}
