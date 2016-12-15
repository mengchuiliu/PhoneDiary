package com.rdcx.loction;

public class BinaryApproach {
	public interface Provider<T> {
		T function(int x);
		int getDirection(T t);
		boolean choose(T forTrue, T forFalse);
	}
	public static class Result<T> {
		public T t;
		public int x;
	}
	
	public static <T> T approach(int left, int right, Provider<T> p) {
		return _approach(left, right, p).t;
	}
	public static <T> Result<T> approachDetail(int left, int right, Provider<T> p) {
		return _approach(left, right, p);
	}
	
	public static class Res<T> extends Result<T> {
		public Res(Provider<T> p, int x) {
			t = p.function(x);
			this.x = x;
			has_dir = false;
		}
		public void setDir(int dir) {
			this.dir = dir;
			has_dir = true;
		}
		public boolean has_dir;
		public int dir;
	}
	
	private static <T> Res<T> _approach(int left, int right, Provider<T> p) {
		if(left >= right) {
			return new Res<T>(p, right);
		}
		int x = (left + right) / 2;
		Res<T> r = new Res<T>(p, x), r2;
		r.setDir(p.getDirection(r.t));
		if(r.dir == 0) return r;
		if(r.dir > 0) {
			r2 = _approach(x + 1, right, p);
			if(!r2.has_dir) {
				r2.dir = p.getDirection(r2.t);
				r2.has_dir = true;
			}
			if(r2.dir >= 0) return r2;
			return p.choose(r.t, r2.t) ? r : r2;
		}
		if(left == x) return r;
		r2 = _approach(left, x - 1, p);
		if(!r2.has_dir) {
			r2.dir = p.getDirection(r2.t);
			r2.has_dir = true;
		}
		if(r2.dir <= 0) return r2;
		return p.choose(r.t, r2.t) ? r : r2;
	}
}
