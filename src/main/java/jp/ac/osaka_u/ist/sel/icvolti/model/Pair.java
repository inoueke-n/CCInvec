package jp.ac.osaka_u.ist.sel.icvolti.model;

import java.io.Serializable;

public class Pair implements Serializable{
	public final int first;
	public final int second;

	public Pair(int first, int second) {
		this.first = first;
		this.second = second;
	}

}
