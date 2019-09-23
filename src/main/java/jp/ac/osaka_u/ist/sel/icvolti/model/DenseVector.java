package jp.ac.osaka_u.ist.sel.icvolti.model;

import java.io.Serializable;

public class DenseVector implements Serializable, MyVector{

	public int dimention;
	private double[] denseVector;

	public DenseVector(int d) {
		this.dimention = d;
		this.denseVector = new double[d];
	}

	@Override
	public void set(int i, double e){
		denseVector[i] = e;
	}

	@Override
	public double get(int i){
		return denseVector[i];
	}

	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		for (Double e : denseVector) {
			buffer.append(e.floatValue() + " ");
		}
		return buffer.toString();
	}
}
