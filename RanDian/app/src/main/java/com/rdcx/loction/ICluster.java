package com.rdcx.loction;

import java.util.Collection;

public interface ICluster<V> {
	Collection<Collection<Integer>> process(V[] vectors);
}
