package com.rdcx.loction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class MinDistanceCluster<Vector, Distance> extends ClusterBase<Vector, Distance> {

	public MinDistanceCluster(IVectorCalculator<Vector, Distance> vc, int targetCount)  {
		super(vc);
		this.targetCount = targetCount <= 1 ? 2 : targetCount;
	}
	private int targetCount;

	@Override
	public Collection<Collection<Integer>> process(Vector[] vectors) {
		int count = vectors.length;
		ArrayList<Distance> all = new ArrayList<Distance>();
		final List<List<Distance>> ds = new ArrayList<List<Distance>>();
		for(int i = 0; i < count; i++) {
			List<Distance> list = new ArrayList<Distance>();
			ds.add(list);
			for(int j = 0; j < count; j++) {
				Distance d = Calculator.distance(vectors[i], vectors[j]);
				if(i < j) {
					all.add(d);
				}
				list.add(d);
			}
		}
		Distance[] da1 = all.toArray(null);
		all.clear();
		Arrays.sort(da1, Calculator);
		int x = count * (count - 1) / targetCount / 2;
		Distance xd = da1[x], d = da1[0];
		all.add(d);
		x = 0;
		for(int i = 1; i < da1.length; i++) {
			if(this.Calculator.compare(d, da1[i]) == 0) {
				continue;
			}
			if(this.Calculator.compare(d, xd) == 0) {
				x = i;
			}
			d = da1[i];
			all.add(d);
		}
		final Distance[] da = all.toArray(null);
		BinaryApproach.Provider<Collection<Collection<Integer>>> p = new BinaryApproach.Provider<Collection<Collection<Integer>>>() {

			@Override
			public Collection<Collection<Integer>> function(int x) {
				return process(da[x], ds);
			}

			@Override
			public int getDirection(Collection<Collection<Integer>> t) {
				return t.size() - targetCount;
			}

			@Override
			public boolean choose(Collection<Collection<Integer>> forTrue,
					Collection<Collection<Integer>> forFalse) {
				return Math.abs(forTrue.size() - targetCount) <= Math.abs(forFalse.size());
			}
			
		};

		return BinaryApproach.approach(0, da.length, p);
	}
	
	private Collection<Collection<Integer>> process(Distance d, List<List<Distance>> mappedDs) {
		Collection<Collection<Integer>> res = new ArrayList<Collection<Integer>>();
		boolean[] dones = new boolean[mappedDs.size()];
		for(int i = 0; i < dones.length; i++) {
			if(dones[i]) continue;
			List<Integer> list = new ArrayList<Integer>();
			res.add(list);
			Stack<Integer> stack = new Stack<Integer>();
			stack.push(i);
			while(stack.size() != 0) {
				int j = stack.pop();
				list.add(j);
				dones[j] = true;
				List<Distance> ds = mappedDs.get(j);
				for(int k = 0; k < dones.length; k++) {
					if(!dones[k] && Calculator.compare(ds.get(k), d) <= 0) {
						stack.push(k);
					}
				}
			}
		}
		return res;
	}
}
