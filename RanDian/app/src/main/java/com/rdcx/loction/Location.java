package com.rdcx.loction;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class Location {
    public Location() {
    }

    public Location(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }

    public int level;
    public double lng, lat;
    public String name;

    public void copyTo(Location l) {
        l.level = level;
        l.lng = lng;
        l.lat = lat;
        l.name = name;
    }

    public double distance2(Location l) {
        double a = lng - l.lng, b = lat - l.lat;
        return a * a + b * b;
    }

    public static class City extends Location {
        public City(Location location) {
            location.copyTo(this);
        }

        public City clone() {
            return new City(this);
        }

        public ArrayList<Area> areas = new ArrayList<Area>();
    }

    ;

    public static class Area extends Location implements
            Comparable<Area> {
        public Area(Location location) {
            location.copyTo(this);
        }

        public City city;

        @Override
        public int compareTo(Area another) {
            int c = Double.compare(lat, another.lat);
            if (c != 0)
                return -c;
            return Double.compare(lng, another.lng);
        }
    }

    public static Area getArea(Location l) {
        Area best = null;
        double bestd = Double.MAX_VALUE;
        for (City city : allCities) {
            for (Area area : city.areas) {
                double d = area.distance2(l);
                if (bestd > d) {
                    bestd = d;
                    best = area;
                }
            }
        }
        return best;
    }

    public static Collection<Area> getAreas(Collection<Location> ls) {
        HashSet<Area> list = new HashSet<Area>();
        for (Location l : ls) {
            list.add(getArea(l));
        }
        return list;
    }

    public static Collection<City> getCities(Collection<Area> areas) {
        HashSet<City> cities = new HashSet<City>();
        for (Area a : areas) {
            cities.add(a.city);
        }
        return cities;
    }

    private static City[] allCities;

    public static City[] getAll(Context context) {
        if (allCities == null) {
            AssetManager assetManager = context.getAssets();
            InputStream input;
            String line = "";
            ArrayList<Location> locations = new ArrayList<Location>();
            try {
                input = assetManager.open("locations.txt");
                InputStreamReader isr = new InputStreamReader(input, "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                while ((line = br.readLine()) != null) {
                    Location l = new Location();
                    if (!Load(line, l))
                        continue;
                    locations.add(l);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            ArrayList<City> cities = new ArrayList<City>();
            City city = null;
            ArrayList<Area> areas = null;
            for (Location l : locations) {
                switch (l.level) {
                    case 2:
                        if (city != null) {
                            city.areas = areas;
                            cities.add(city);
                        }
                        areas = new ArrayList<Area>();
                        city = new City(l);
                        break;
                    case 3:
                        Area area = new Area(l);
                        area.city = city;
                        areas.add(area);
                        break;
                }
            }
            if (city != null)
                city.areas = areas;
            allCities = cities.toArray(new City[cities.size()]);
        }
        Log.e("my_log", "==城市==>" + allCities.length);
        return allCities;
    }

    public static void clear() {
        allCities = null;
    }

    private static boolean Load(String line, Location loc) {
        if (line.length() < 7)
            return false;

        int i0 = line.indexOf('\t', 0);
        if (i0 <= 0)
            return false;
        loc.level = Integer.parseInt(line.substring(0, i0));
        i0++;

        int i1 = line.indexOf('\t', i0);
        if (i1 < 0 || i1 == i0)
            return false;
        loc.lng = Double.parseDouble(line.substring(i0, i1));
        i1++;

        int i2 = line.indexOf('\t', i1);
        if (i2 < 0 || i2 == i1)
            return false;
        loc.lat = Double.parseDouble(line.substring(i1, i2));
        i2++;

        loc.name = line.substring(i2);
        return true;
    }

    private static final int textSize2 = 40;
    private static final int textSize = 30;
    private static final int textPadding = 0;

    public static int getCitySize(Collection<Location> ls) {
        HashMap<String, Object> cityMap = new HashMap<>();
        for (Location l : ls) {
            Area a = getArea(l);
            cityMap.put(a.city.name, cityMap);
        }
        return cityMap.size();
    }

    public static Bitmap CreateMap(Bitmap bg, Bitmap lbmp, Collection<Location> ls) {
        ArrayList<Area> lines = new ArrayList<>();
        HashSet<Area> areas = new HashSet<>();
        HashMap<String, Object> cityMap = new HashMap<>();
        for (Location l : ls) {
            Area a = getArea(l);
            lines.add(a);
            areas.add(a);
            cityMap.put(a.city.name, cityMap);
        }
        Area[] aa = areas.toArray(new Area[areas.size()]);
        // 记录四边
        double lngmin = Double.MAX_VALUE, lngmax = Double.MIN_VALUE, latmin = Double.MAX_VALUE, latmax = Double
                .MIN_VALUE;
        for (Area a : areas) {
            if (a.lng < lngmin) {
                lngmin = a.lng;
            }
            if (a.lng > lngmax) {
                lngmax = a.lng;
            }
            if (a.lat < latmin) {
                latmin = a.lat;
            }
            if (a.lat > latmax) {
                latmax = a.lat;
            }
        }
        // 放大系数设置
        double f = 0.20, latd = latmax - latmin, lngd = lngmax - lngmin;
        if (aa.length == 1) {
            Area a = aa[0];
            City c = a.city;
            f = 1.0;
            latd = 5 * Math.abs(a.lat - c.lat);
            lngd = 5 * Math.abs(a.lng - c.lng);
        }
        latmin -= latd * f;
        latmax += latd * f;
        lngmin -= lngd * f;
        lngmax += lngd * f;
        latd = latmax - latmin;
        lngd = lngmax - lngmin;

        // 矫正地理位置缩放定位
        float w = (float) bg.getWidth(), h = (float) bg.getHeight();
        float whrate = w / h;
        if (lngd > whrate * (latd)) {
            latd = (lngd / whrate - latd) / 2;
            latmax += latd;
            latmin -= latd;
            latd = lngd / whrate;
        } else {
            lngd = (latd * whrate - lngd) / 2;
            lngmax += lngd;
            lngmin -= lngd;
            lngd = latd * whrate;
        }

        Rect rc = new Rect(0, 0, bg.getWidth(), bg.getHeight());
        Bitmap bmp = Bitmap.createBitmap(rc.width(), rc.height(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        Paint p1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p1.setAlpha(175);
//        p1.setAntiAlias(true);
        c.drawBitmap(bg, rc, rc, p1);

        { // 市文字
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(textSize2);
//            textPaint.setAlpha(255);
            textPaint.setColor(Color.BLACK);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textH = fm.bottom - fm.top;
            Collection<City> cities = getCities(areas);
            if (cities.size() == 1) {
                double lngTotal = 0, latTotal = 0;
                for (Area area : areas) {
                    lngTotal += area.lng;
                    latTotal += area.lat;
                }
                lngTotal /= areas.size();
                latTotal /= areas.size();

                for (City a : cities) {
                    float textWidth = textPaint.measureText(a.name);
                    float x = (float) ((lngTotal - lngmin) * w / lngd), y = (float) ((latmax - latTotal) * h / latd);
                    RectF textRect = CenterRectF(x + textWidth, y - textH, textWidth, textH);

                    c.drawText(a.name, textRect.left, textRect.bottom - fm.bottom,
                            textPaint);
                }
            } else {
                for (City a : cities) {
                    float textWidth = textPaint.measureText(a.name);
                    float x = (float) ((a.lng - lngmin) * w / lngd), y = (float) ((latmax - a.lat)
                            * h / latd);
                    RectF textRect = CenterRectF(x, y, textWidth, textH);

                    c.drawText(a.name, textRect.left, textRect.bottom - fm.bottom,
                            textPaint);
                }
            }

        }

        { // 连线
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setARGB(128, 0, 0, 0);
            paint.setStrokeWidth(2);
            Area prev = null;
            float px = 0, py = 0;
            for (Area a : lines) {
                float x = (float) ((a.lng - lngmin) * w / lngd), y = (float) ((latmax - a.lat)
                        * h / latd);
                if (prev != null) {
                    c.drawLine(px, py, x, y, paint);
                }
                px = x;
                py = y;
                prev = a;
            }
        }

        int alpha = 128;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setARGB(alpha, 0, 222, 139);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(textSize);
        textPaint.setAlpha(alpha);
        textPaint.setColor(Color.WHITE);
        Paint.FontMetrics fm = textPaint.getFontMetrics();
        Rect lrc = new Rect(0, 0, lbmp.getWidth(), lbmp.getHeight());
        float textH = fm.bottom - fm.top;
        float rectH = textH + textPadding * 2;
        Arrays.sort(aa);
        for (Area a : aa) {
            float textWidth = textPaint.measureText(a.name);
            float x = (float) ((a.lng - lngmin) * w / lngd), y = (float) ((latmax - a.lat)
                    * h / latd);
            RectF textRect = CenterRectF(x, y, textWidth + rectH, rectH);
            textRect.offset(0, rectH / 2);
            if (cityMap.size() <= 1) {
                c.drawRoundRect(textRect, rectH / 2, rectH / 2, paint);
                c.drawText(a.name, textRect.left + rectH / 2, textRect.bottom
                        - textPadding - fm.bottom, textPaint);
            }

            RectF lrc2 = CenterRectF(x, y, lrc.width(), lrc.height());
            lrc2.offset(0, -(float) lrc.height() / 2);
            c.drawBitmap(lbmp, lrc, lrc2, null);
        }
        return bmp;
    }

    public static RectF CenterRectF(float x, float y, float w, float h) {
        return new RectF(x - w / 2, y - h / 2, x + w / 2, y + h / 2);
    }

}
