package com.rdcx.loction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ClusterBase<V, D> implements ICluster<V> {
	public ClusterBase(IVectorCalculator<V, D> vc) {
		Calculator = vc;
	}
	public final IVectorCalculator<V, D> Calculator;
	public abstract Collection<Collection<Integer>> process(V[] vectors);
	public List<D> getDistances(List<Pair<V, List<V>>> res) {
		List<D> list = new ArrayList<D>();
		for(Pair<V, List<V>> p : res) {
			for(V v : p.b) {
				list.add(this.Calculator.distance(p.a, v));
			}
		}
		return list;
	}
	
	public D maxDistance(List<Pair<V, List<V>>> res) {
		D d = null;
		for(D t : this.getDistances(res)) {
			if(d == null || Calculator.compare(d, t) < 0) {
				d = t;
			}
		}
		return d;
	}
}
