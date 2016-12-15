package com.rdcx.loction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SimpleCluster<V, D> extends ClusterBase<V, D> {

	public SimpleCluster(IVectorCalculator<V, D> vc, int targetCount) {
		super(vc);
		this.targetCount = targetCount;
	}
	public final int targetCount;
	private static class Data<D> {
		public Data(D d, int i, int j) {
			this.d = d;
			this.i = i;
			this.j = j;
		}
		D d;
		int i, j;
	}
	
	private class Cmpr implements Comparator<Data<D>> {

		@Override
		public int compare(Data<D> lhs, Data<D> rhs) {
			return Calculator.compare(lhs.d, rhs.d);
		}
		
	}
	
	@Override
	public Collection<Collection<Integer>> process(V[] vectors) {
		int count = vectors.length;
		ArrayList<Data<D>> all = new ArrayList<Data<D>>();
		for(int i = 0; i < count; i++) {
			V vi = vectors[i];
			for(int j = 0; j < i; j++) {
				all.add(new Data<D>(Calculator.distance(vi, vectors[j]), i, j));
			}
		}
		Data<D>[] da = all.toArray(null);
		all = null;
		Arrays.sort(da, new Cmpr());
		int[] belongs = new int[count];
		ArrayList<Set<Integer>> sets = new ArrayList<Set<Integer>>(count);
		for(int i = 0; i < count; i++) {
			belongs[i] = -1;
		}
		int c = count;
		if(c > targetCount) {
			for(int k = 0; k < da.length; k++) {
				Data<D> d = da[k];
				if(belongs[d.i] < 0) {
					if(belongs[d.j] < 0) {
						Set<Integer> set = new HashSet<Integer>();
						set.add(d.i);
						set.add(d.j);
						belongs[d.i] = sets.size();
						belongs[d.j] = sets.size();
						sets.add(set);
					} else {
						sets.get(belongs[d.i] = belongs[d.j]).add(d.i);
					}
				} else {
					if(belongs[d.j] < 0) {
						sets.get(belongs[d.j] = belongs[d.i]).add(d.j);
					} else {
						int i = belongs[d.i], j = belongs[d.j];
						if(i == j) continue;
						Set<Integer> seti = sets.get(i), setj = sets.get(j);
						if(seti.size() >= setj.size()) {
							for(int t : setj) {
								belongs[t] = i;
								seti.add(t);
							}
							sets.set(j, null);
						} else {
							for(int t : seti) {
								belongs[t] = j;
								setj.add(t);
							}
							sets.set(i, null);
						}
					}
				}
				if(--c <= targetCount)
					break;
			}
		}
		ArrayList<Collection<Integer>> res = new ArrayList<Collection<Integer>>();
		for(int i = 0; i < count; i++) {
			if(belongs[i] < 0) {
				ArrayList<Integer> list = new ArrayList<Integer>();
				list.add(i);
				res.add(list);
			}
		}
		for(Set<Integer> s : sets) {
			if(s != null) {
				res.add(s);
			}
		}
		return res;
	}

}
