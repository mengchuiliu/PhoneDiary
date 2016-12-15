package com.rdcx.loction;

public class XY {
	public XY(double x, double y) {
		this.x = x;
		this.y = y;
	}
	double x;
	double y;
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof XY)) return false;
		XY a = (XY)o;
		return a.x == x && a.y == y;
	}
	@Override
	public String toString() {
		return "(" + Double.toString(x) + "," + Double.toString(y) + ")";
	}
	
}
