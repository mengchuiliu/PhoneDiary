package com.rdcx.loction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class XYCluster extends ClusterBase<XY, Double>{
	public XYCluster(int targetCount, int divideCount) {
		super(new IVectorCalculator<XY, Double>() {

			@Override
			public XY avg(XY a, XY b) {
				return new XY((a.x + b.x) / 2, (a.y + b.y) / 2) ;
			}

			@Override
			public XY mix(XY a, double proportionA, XY b, double proportionB) {
				return new XY(
						(a.x * proportionA + b.x * proportionB) / (proportionA + proportionB),
						(a.y * proportionA + b.y * proportionB) / (proportionA + proportionB)
						);
			}

			@Override
			public Double distance(XY a, XY b) {
				double dx = a.x - b.x, dy = a.y - b.y;
				return Math.sqrt(dx * dx + dy * dy);
			}

			@Override
			public int compare(Double a, Double b) {
				return a.compareTo(b);
			}
			
		});
		this.targetCount = targetCount;
		this.divideCount = divideCount;
	}
	public final int targetCount, divideCount;
	
	private static class IJ {
		public IJ(int i, int j) {
			this.i = i;
			this.j = j;
		}
		public int i,j;
	}

	public static Collection<Collection<Integer>> process(XY[] vs, int divide) {
		if(vs == null) return null;
		if(vs.length == 0) return new ArrayList<Collection<Integer>>();
		
		// 收集区域
		XY xy = vs[0];
		double xmax = xy.x, ymax = xy.y,
				xmin = xy.x, ymin = xy.y;
		for(int i = 1; i < vs.length; i++) {
			xy = vs[i];
			if(xmax < xy.x) { xmax = xy.x; }
			else if(xmin > xy.x) { xmin = xy.x; }
			if(ymax < xy.y) { ymax = xy.y; }
			else if(ymin > xy.y) { ymin = xy.y; }
		}
		// 计算横纵划分
		double xd = xmax - xmin,
				yd = ymax - ymin;
		xd *= 1.00000001;
		yd *= 1.00000001;
		int xc, yc;
		if(xd >= yd) {
			xc = divide;
			yc = (int)Math.ceil(divide * yd / xd);
			if (yc == 0) {
				yc = 1;
			}
		} else {
			yc = divide;
			xc = (int)Math.ceil(divide * xd / yd);
			if(xc == 0) {
				xc = 1;
			}
		}
		
		
		// 建立划分映射map[i,j]内包含点的集合
		List<List<Collection<Integer>>> map = new ArrayList<List<Collection<Integer>>>();
		for(int i = 0; i < xc; i++) {
			List<Collection<Integer>> line = new ArrayList<Collection<Integer>>();
			map.add(line);
			for(int j = 0; j < yc; j++) {
				line.add(null);
			}
		}
		// 数据打表
		for(int i = 0; i < vs.length; i++) {
			XY v = vs[i];
			int ix = (int)((v.x - xmin) * xc / xd),
				iy = (int)((v.y - ymin) * yc / yd);
			List<Collection<Integer>> line = map.get(ix);
			Collection<Integer> c = line.get(iy);
			if(c == null) {
				c = new ArrayList<Integer>();
				line.set(iy, c);
			}
			c.add(i);
		}
		// 防止重复合并，添加边缘行和列优化合并算法不用越界检查
		boolean[][] dones = new boolean[xc + 2][];
		for(int i = 0; i < xc; i++) {
			List<Collection<Integer>> line = map.get(i);
			boolean[] bline = new boolean[yc + 2];
			dones[i + 1] = bline;
			for(int j = 0; j < yc; j++) {
				bline[j + 1] = line.get(j) == null;
			}
		}
		dones[0] = new boolean[yc + 2];
		dones[xc + 1] = new boolean[yc + 2];
		// 边缘点已完成
		for(int i = 0; i <= xc; i++) {
			dones[i + 1][0] = true;
			dones[i][yc + 1] = true;
		}
		for(int i = 0; i <= yc; i++) {
			dones[0][i] = true;
			dones[xc + 1][i + 1] = true;
		}
		// 222
		// 212
		// 222
		int[] offset = new int[] {
				-1, -1,
				-1, 0,
				-1, 1,
				0, -1,
				0, 1,
				1, -1,
				1, 0,
				1, 1
		};
		Collection<Collection<Integer>> res = new ArrayList<Collection<Integer>>();
		for(int i = 0; i < xc; i++) {
			for(int j = 0; j < yc; j++) {
				if(!dones[i + 1][j + 1]) {
					dones[i + 1][j + 1] = true;
					Collection<Integer> c = map.get(i).get(j);
					Stack<IJ> stack = new Stack<IJ>();
					stack.push(new IJ(i, j));
					int imax = i, imin = i,
						jmax = j, jmin = j;
					do {
						while(stack.size() > 0) {
							IJ ij = stack.pop();
							for(int k = 0; k < 16; k+= 2) {
								int ii = ij.i + offset[k], jj = ij.j + offset[k + 1];
								if(!dones[ii + 1][jj + 1]) {
									dones[ii + 1][jj + 1] = true;
									c.addAll(map.get(ii).get(jj));
									stack.push(new IJ(ii, jj));
									// 记录连接区域四边
									if(imax < ii) imax = ii;
									else if(imin > ii) imin = ii;
									if(jmax < jj) jmax = jj;
									else if(jmin < jj) jmin = jj;
								}
							}
						}
						// 连接区域可能不规则，其所在矩形区域扫描
						double ri = imax - imin, rj = jmax - jmin;
						for(int ii = imin; ii <= imax; ii++) {
							for(int jj = jmin; jj <= jmax; jj++) {
								double
									di = Math.abs(imax + imin - ii * 2) / ri,
									dj = Math.abs(jmax + jmin - jj * 2) / rj;
								// 椭圆形区域收集
								if(di * di + dj * dj <= 1.0 &&
									!dones[ii + 1][jj + 1]) {
									dones[ii + 1][jj + 1] = true;
									c.addAll(map.get(ii).get(jj));
									stack.push(new IJ(ii, jj));
								}
							}
						}
					} while(stack.size() > 0);
					res.add(c);
				}
			}
		}
		return res;
	}

	@Override
	public Collection<Collection<Integer>> process(XY[] vectors) {
		Collection<Collection<Integer>> best = null;
		float bestd = Float.MAX_VALUE;
		for(int i = divideCount; i > 0; i--) {
			Collection<Collection<Integer>> res = process(vectors, i);
			float d = Math.abs(0.5f + (float)res.size() - (float)targetCount);
			if(bestd > d) {
				bestd = d;
				best = res;
			}
		}
		return best;
	}
}
