package com.rdcx.loction;

import java.util.Comparator;

public interface IVectorCalculator<V, D> extends Comparator<D> {
    V avg(V a, V b);

    V mix(V a, double proportionA, V b, double proportionB);

    D distance(V a, V b);

    int compare(D a, D b);
}
