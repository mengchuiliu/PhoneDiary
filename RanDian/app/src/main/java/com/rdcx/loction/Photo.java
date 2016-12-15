package com.rdcx.loction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.LatLng;

public class Photo implements Comparable<Photo> {
    public Photo(double lat, double lng, String path, Date date) {
        this.lat = lat;
        this.lng = lng;
        this.path = path;
        this.date = date;
    }

    private final double lat;
    private final double lng;
    public final String path;
    private final Date date;

    public XY GetXY() {
        return new XY(lng, lat);
    }

    public LatLng GetLatLng() {
        return new LatLng(lat, lng);
    }

    @Override
    public int compareTo(Photo another) {
        return another.date.compareTo(date);
    }

    public static class Group implements Comparable<Group> {
        public Group(Photo[] photos) {
            double lat = 0;
            double lng = 0;
            for (Photo p : photos) {
                lat += p.lat;
                lng += p.lng;
            }
            this.lat = lat / photos.length;
            this.lng = lng / photos.length;
            Arrays.sort(photos);
            this.photos = photos;
        }

        public final Photo[] photos;
        private final double lat;
        private final double lng;

        public LatLng GetLatLng() {
            return new LatLng(lat, lng);
        }

        public static Group[] Cluster(List<Photo> photos) {
            ArrayList<XY> list = new ArrayList<XY>();
            for (Photo p : photos) {
                list.add(p.GetXY());
            }
            XYCluster xyc = new XYCluster(3, 20);
            Collection<Collection<Integer>> cci = xyc.process(list.toArray(new XY[list.size()]));
            ArrayList<Group> groups = new ArrayList<Group>();
            for (Collection<Integer> ci : cci) {
                ArrayList<Photo> ps = new ArrayList<Photo>();
                for (Integer i : ci) {
                    ps.add(photos.get(i));
                }
                if (!ps.isEmpty()) {
                    groups.add(new Group(ps.toArray(new Photo[ps.size()])));
                }
            }
            Group[] gs = groups.toArray(new Group[groups.size()]);
            // 确保低纬度盖住高纬度
            Arrays.sort(gs);
            return gs;
        }

        @Override
        public int compareTo(Group another) {
            int c = Double.compare(lat, another.lat);
            if (c != 0) return -c;
            return Double.compare(lng, another.lng);
        }

        private ArrayList<BitmapDescriptor> bmplist;

        public void setBmplist(ArrayList<BitmapDescriptor> bmplist) {
            this.bmplist = bmplist;
        }

        public ArrayList<BitmapDescriptor> getBmplist() {
            return bmplist;
        }
    }
}
