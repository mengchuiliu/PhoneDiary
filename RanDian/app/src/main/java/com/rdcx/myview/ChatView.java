package com.rdcx.myview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

import com.rdcx.tools.DB;
import com.rdcx.tools.Operation;
import com.rdcx.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/12/16 0016.
 * <p/>
 * 绘制面积图
 */
public class ChatView extends View {

    public static class ChatData {
        public String text;
        public long time;
        public int value;
        public boolean label;
        public int color;

        public ChatData(String text, int value, boolean label) {
            this.text = text;
            this.value = value;
            this.label = label;
        }

        public ChatData(String text, long time, int value, boolean label) {
            this(text, value, label);
            this.time = time;
        }

        @Override
        public String toString() {
            return "ChatData{" +
                    "text='" + text + '\'' +
                    ", time=" + DateFormat.format("yyyy-MM-dd HH:mm:ss", time) +
                    ", value=" + value +
                    ", label=" + label +
                    ", color=" + color +
                    '}';
        }
    }

    public static class ChatList {
        public String text;
        public Drawable icon;
        public int value;
        public List<ChatData> chatDatas;
        private Shader shader;

        public ChatList(List<ChatData> chatDatas) {
            this.chatDatas = chatDatas;
        }

        public ChatList(String text, Drawable icon, int value, List<ChatData> chatDatas) {
            this(chatDatas);
            this.text = text;
            this.icon = icon;
            this.value = value;
        }
    }

    public ChatView(Context context) {
        super(context);
    }

    public ChatView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean pieGraph = false;
    private boolean areaGraph = false;
    private boolean timeAxis = false;

    private List<ChatData> chatDatas;
    private HashMap<String, Drawable> iconMap;
    //    private List<List<ChatView.ChatData>> chatDatasList;
    private List<ChatList> chatLists;
    private String xUnit, yUnit;
    private int curMinutes;
    private int backgroundColor = 0x00000000;
    private int coorAreaColor = 0x11FFFFFF;
    private int coorColor = 0xFF4BC2BC;
    private float coorWidth = 0.01F;
    private float top = 0.1F;
    private float right = 0.05F;
    private float bottom = 0.1F;
    private float left = 0.1F;

    private boolean first = true;
    private Paint mPaint;
    private Paint tPaint;
    private RectF r;
    private Shader shader;
    private float width;
    private float height;
    private float xMin;
    private float xMax;
    private float yMin;
    private float yMax;
    private int xStepCount;
    private float xStepLength;
    private int maxVal;
    private int yMaxVal;
    private int totalValue;
    private int yStepCount;
    private float yStepValue;
    private float yStepLength;
    private float animationRate;

    private float x, y;
    private Animation animation;
    private ArrayList<Float> maxPoints = null; // 记录下最大值的所有点
    private HashMap<List<ChatData>, Object> pathMap;

    private ArrayList<Integer> colorList = null;
    private ArrayList<Integer> secondColorList = null;
    private HashMap<String, Integer> colorMap;

    public void init() {
        first = false;
        colorList = null;
        secondColorList = null;
        colorMap = null;
        mPaint = new Paint();
        tPaint = new Paint();
        mPaint.setAntiAlias(true); // 消除锯齿
        r = new RectF();
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        coorWidth = initFloat(coorWidth, height);
        top = initFloat(top, height);
        bottom = initFloat(bottom, height);
        left = initFloat(left, width);
        right = initFloat(right, width);
        xMin = left;
        xMax = width - right;
        yMin = top;
        yMax = height - bottom;
        animationRate = 1.0F;
        if (chatLists != null && chatDatas != null) {
            xStepCount = chatDatas.size();
            xStepLength = (xMax - xMin) / xStepCount;
            maxVal = -Integer.MAX_VALUE;
            yMaxVal = 5;
            totalValue = 0;
            for (ChatList chatList : chatLists) {
                for (ChatData chatData : chatList.chatDatas) {
                    totalValue += chatData.value;
                    if (yMaxVal < chatData.value) {
                        yMaxVal = chatData.value;
                    }
                    if (maxVal < chatData.value) {
                        maxVal = chatData.value;
                    }
                }
            }
            yStepValue = Math.max(yMaxVal / 5, 5F); // 将最大值分成五份，每份小于 5 ，则每份的长度就为 5.
            yStepValue = (float) (Math.ceil(yStepValue / 5) * 5); // 每步的值是 5 的倍数
            yMaxVal = (int) (Math.ceil(yMaxVal / yStepValue) * yStepValue); // 重新计算最大值
            yStepCount = (int) (yMaxVal / yStepValue);
            yStepLength = (yMax - yMin) / yStepCount;
        }
        x = y = 0.0F;
        pathMap = null;
        maxPoints = new ArrayList<>();
    }

    // 初始化数据
    private float initFloat(float f, float ref) {
        if (f < 1) {
            return ref * f;
        } else {
            return f;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (first) {
            init();
        }

        if (areaGraph) {
            drawBackground(canvas);
            drawCoordinate(canvas);
            if (chatLists != null) {
                canvas.save();
                canvas.scale(1.0F, animationRate);
                for (int i = 0, len = chatLists.size(); i < len; i++) {
                    ChatList chatList = chatLists.get(i);
                    drawLine(canvas, xStepLength / 1.5F, i, chatList);
                    drawMax(canvas);
                }
                drawAreaTips(canvas);
                canvas.restore();
            }
        } else if (pieGraph) {
            drawBackground(canvas);
            if (chatDatas != null) {
                drawPie(canvas);
            }
        } else if (timeAxis) {
            drawBackground(canvas);
            if (chatLists != null) {
                if (chatLists.size() == 4) {
                    drawTimeAxisDay(canvas);
                } else if (chatLists.size() == 7) {
                    drawTimeAxisWeek(canvas);
                }
            }
        }

    }

    /**
     * 动画效果
     */
    public void startAnimation() {

        clearAnimation();

        if (animation == null) {
            animation = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    animationRate = interpolatedTime;
                    invalidate();
                }
            };
            animation.setDuration(800L);
            animation.setInterpolator(new DecelerateInterpolator());
        }
        startAnimation(animation);

    }

    // 设置X轴上显示的单位
    public void setxUnit(String xUnit) {
        this.xUnit = xUnit;
    }

    public void setyUnit(String yUnit) {
        this.yUnit = yUnit;
    }

    public void setCurMinutes(int curMinutes) {
        this.curMinutes = curMinutes;
    }

    // 获取缓存中的 Path
    private Object getTempPath(List<ChatData> tempList) {
        if (pathMap == null) {
            pathMap = new HashMap<>();
        }
        return pathMap.get(tempList);
    }

    // 设置缓存中的 Path
    private void setTempPath(List<ChatData> tempList, Object obj) {
        if (pathMap == null) {
            pathMap = new HashMap<>();
        }
        pathMap.put(tempList, obj);
    }

    private int randomColor() {
        if (colorList == null) {
            colorList = new ArrayList<>();
            colorList.add(0x88FFC65C);
            colorList.add(0x8832D6C5);
            colorList.add(0x883399FF);
            colorList.add(0x88FE8080);
            colorList.add(0x88AFA9FE);
            colorList.add(0x889AC1E4);
            colorList.add(0x88A26DB6);
        }
        if (secondColorList == null) {
            secondColorList = new ArrayList<>();
            secondColorList.add(0x55FFFFFF);
            secondColorList.add(0x55000000);
            secondColorList.add(0x550000FF);
            secondColorList.add(0x5500FF00);
            secondColorList.add(0x55FF0000);
            secondColorList.add(0x5500FFFF);
            secondColorList.add(0x55FF00FF);
            secondColorList.add(0x55FFFF00);
        }
        return colorList.size() == 0 ? (secondColorList.size() == 0 ? 0xFFFFFFFF : secondColorList.remove((int) (Math.random() * secondColorList.size()))) : colorList.remove((int) (Math.random() * colorList.size()));
    }

    private int randomColor(String name) {
        if (colorMap == null) {
            colorMap = new HashMap<>();
        }
        if (colorMap.get(name) == null) {
            colorMap.put(name, randomColor());
        }
        return colorMap.get(name);
    }

    public void drawAreaGraph(List<ChatList> chatLists) {
        this.chatLists = chatLists;
        if (chatLists != null && chatLists.size() > 0) {
            this.chatDatas = chatLists.get(0).chatDatas;
        }
        first = true;
        setGraphBooleanFalse();
        areaGraph = true;
        startAnimation();
    }

    public void drawPieGraph(List<ChatList> chatLists) {
        this.chatLists = chatLists;
        if (chatLists != null && chatLists.size() > 0) {
            this.chatDatas = chatLists.get(0).chatDatas;
        }
        first = true;
        setGraphBooleanFalse();
        pieGraph = true;
        startAnimation();
    }

    // 绘制时光轴
    public void drawTimeAxis(List<ChatData> chatDatas, HashMap<String, Drawable> iconMap, List<ChatList> chatLists) {
        this.chatDatas = chatDatas;
        this.iconMap = iconMap;
        this.chatLists = chatLists;
        first = true;
        setGraphBooleanFalse();
        timeAxis = true;
        startAnimation();
    }

    private void setGraphBooleanFalse() {
        areaGraph = false;
        pieGraph = false;
        timeAxis = false;
    }

    // 绘制整个背景
    private void drawBackground(Canvas canvas) {
        mPaint.setColor(backgroundColor);
        r.set(0, 0, width, height);
        canvas.drawRect(r, mPaint);
    }

    // 绘制坐标轴
    private void drawCoordinate(Canvas canvas) {
        // 绘制坐标轴背景区域
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(coorAreaColor);
        r.set(xMin, yMin, xMax, yMax);
        canvas.drawRect(r, mPaint);

        // 绘制X轴和Y轴
        mPaint.setColor(coorColor);
        mPaint.setStrokeWidth(coorWidth);
        canvas.drawLine(xMin, yMin, xMin, yMax + coorWidth / 2, mPaint);
        canvas.drawLine(xMin, yMax, xMax, yMax, mPaint);

        if (chatDatas != null) {
            // 绘制X轴上的刻度
            mPaint.setStrokeWidth(coorWidth / 2);
            tPaint.setColor(coorColor);
            tPaint.setTextAlign(Paint.Align.CENTER);
            tPaint.setTextSize(bottom * 0.5F);
            tPaint.setFakeBoldText(false);
            for (int i = 0; i <= xStepCount; i++) {
                ChatData chatData = i < xStepCount ? chatDatas.get(i) : new ChatData(xUnit, 0, true);
                x = xMin + (i * xStepLength);
                y = yMax + (coorWidth / 2);
                if (chatData.label) {
                    canvas.drawLine(x, y, x, y - 3.5F * coorWidth, mPaint);
                }
                if (chatData.text != null) {
                    canvas.drawText(chatData.text, x, y + bottom / 2, tPaint);
                }
            }

            // 绘制Y轴上的刻度和小圆点
            tPaint.setColor(coorColor);
            tPaint.setTextAlign(Paint.Align.RIGHT);
            tPaint.setTextSize(bottom * 0.5F);
            tPaint.setFakeBoldText(false);
            for (int i = 0; i <= yStepCount; i++) {
                x = xMin;
                y = yMax - i * yStepLength;
                mPaint.setColor(0xFF939FA7);
                canvas.drawCircle(x, y, coorWidth, mPaint);
                mPaint.setColor(0xFF6E898E);
                canvas.drawCircle(x, y, coorWidth / 2, mPaint);
                canvas.drawText(i == yStepCount ? yUnit : String.valueOf((int) (yStepValue * i)), x - 2F * coorWidth, y + bottom * 0.2F, tPaint);
            }
        }

    }

    // 获取图上各数据点，包括原点和X轴最后一个点
    private float[] getPoints(List<ChatData> tempList) {
        // 寻找图上各点
        int besselPointsLength = xStepCount * 2 + 4;
        float[] besselPoints = new float[besselPointsLength];
        besselPoints[0] = xMin;
        besselPoints[1] = yMax;
        for (int i = 0; i < xStepCount; i++) {
            ChatData chatData = tempList.get(i);
            x = xStepLength / 2 + xMin + (i * xStepLength);
            y = yMax - chatData.value * (yMax - yMin) / yMaxVal;
            besselPoints[(i << 1) + 2] = x;
            besselPoints[(i << 1) + 3] = y;
            if (chatData.value > 0 && chatData.value == maxVal) {
                maxPoints.add(x);
                maxPoints.add(y);
            }
        }
        besselPoints[besselPointsLength - 2] = xMax;
        besselPoints[besselPointsLength - 1] = yMax;
        return besselPoints;
    }

    // 获取控制点
    private float[] getRefPoint(float x1, float y1, float x2, float y2, float x3, float y3, float offset) {
        float da = (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        float[] va = new float[]{(x2 - x1) / da, (y2 - y1) / da};
        float db = (float) Math.sqrt((x3 - x2) * (x3 - x2) + (y3 - y2) * (y3 - y2));
        float[] vb = new float[]{(x3 - x2) / db, (y3 - y2) / db};
        float[] vt = new float[]{va[0] + vb[0], va[1] + vb[1]};
        float dt = (float) Math.sqrt(vt[0] * vt[0] + vt[1] * vt[1]);
        vt = new float[]{vt[0] / dt, vt[1] / dt};
        return new float[]{x2 + vt[0] * offset, Math.min(y2 + vt[1] * offset, yMax), x2 - vt[0] * offset, Math.min(y2 - vt[1] * offset, yMax)};
    }


    public void drawLine(Canvas canvas, float offset, int index, ChatList chatList) {
        float[] coors = (float[]) getTempPath(chatList.chatDatas);
        if (coors == null) {
            coors = getPoints(chatList.chatDatas);
            setTempPath(chatList.chatDatas, coors);
        }

        mPaint.setStrokeWidth(coorWidth / 2);
        for (int i = 0, len = coors.length; i < len - 2; i += 2) {
            mPaint.setColor(randomColor(chatList.text));
            canvas.drawLine(coors[i], coors[i + 1], coors[i + 2], coors[i + 3], mPaint);

            if (coors[i + 1] < yMax) {
                canvas.drawCircle(coors[i], coors[i + 1], 1F * coorWidth, mPaint);
                mPaint.setColor(Color.WHITE);
                canvas.drawCircle(coors[i], coors[i + 1], 0.5F * coorWidth, mPaint);
            }
        }
    }

    // 绘制平滑的贝塞尔曲线
    public void drawBessel(Canvas canvas, float offset, int index, ChatList chatList) {
        Path path = (Path) getTempPath(chatList.chatDatas);
        if (path == null) {
            float[] coors = getPoints(chatList.chatDatas);
            path = new Path();
            path.moveTo(coors[0], coors[1]);
            float[] refPoints, oldPoints = null;
            for (int i = 0, len = coors.length; i < len; i += 2) {
                if (i >= len - 2) {
                    path.lineTo(coors[i], coors[i + 1]);
                    continue;
                } else if (i >= len - 4) {
                    refPoints = getRefPoint(coors[i], coors[i + 1], coors[i + 2], coors[i + 3], coors[i + 2] + 1, coors[i + 3], offset);
                } else {
                    refPoints = getRefPoint(coors[i], coors[i + 1], coors[i + 2], coors[i + 3], coors[i + 4], coors[i + 5], offset);
                }
                if (oldPoints == null) {
                    path.quadTo(refPoints[2], refPoints[3], coors[i + 2], coors[i + 3]);
                } else {
                    if (coors[i + 1] == coors[i + 3] && coors[i + 1] == yMax) {
                        path.lineTo(coors[i + 2], coors[i + 3]);
                    } else {
                        path.cubicTo(oldPoints[0], oldPoints[1], refPoints[2], refPoints[3], coors[i + 2], coors[i + 3]);
                    }
                }
                oldPoints = refPoints;
            }
            path.close();
            setTempPath(chatList.chatDatas, path);
        }

        if (shader == null) {
            shader = new LinearGradient(xMin, yMin, xMin, yMax, new int[]{0x882BB2B9, 0x00FFFFFF}, null, Shader.TileMode.CLAMP);
        }

        mPaint.setShader(shader);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, mPaint);
        mPaint.setShader(null);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(randomColor(chatList.text));
        mPaint.setStrokeWidth(coorWidth / 3);
        canvas.drawPath(path, mPaint);
    }

    private void drawMax(Canvas canvas) {
        tPaint.setTextSize(0.5F * bottom);
        tPaint.setTextAlign(Paint.Align.CENTER);
        tPaint.setColor(Color.BLACK);

        int divide = maxVal / 60;
        int remainder = maxVal % 60;
        String maxStr = (divide > 0 ? divide + "分" : "") + (remainder > 0 ? remainder + "秒" : "");
        for (int i = 0, len = maxPoints.size(); i < len && i < 2; i += 2) {
            canvas.drawText(maxStr, maxPoints.get(i), maxPoints.get(i + 1) - 0.3F * bottom, tPaint);
        }

    }

    private void drawAreaTips(Canvas canvas) {

        float cubeWidth = 0.5F * bottom;

        mPaint.setStyle(Paint.Style.FILL);

        tPaint.setTextSize(cubeWidth);
        tPaint.setTextAlign(Paint.Align.LEFT);
        tPaint.setColor(Color.BLACK);
        for (int i = 0, len = chatLists.size(); i < len; i++) {
            ChatList chatList = chatLists.get(i);
            if (chatList.text == null) {
                continue;
            }
            x = xMax - 6 * cubeWidth;
            y = yMin + 1.5F * i * cubeWidth;
            mPaint.setColor(randomColor(chatList.text));
            canvas.drawRect(x, y - 0.8F * cubeWidth, x + cubeWidth, y + 0.2F * cubeWidth, mPaint);

            String text = (text = chatList.text) == null ? "" : (text.length() > 4 ? text.substring(0, 4) + "..." : text);
            canvas.drawText(text, x + 1.6F * cubeWidth, y, tPaint);
        }
    }

    private void drawPie(Canvas canvas) {

        float cubeWidth = 0.5F * bottom;

        mPaint.setTextSize(cubeWidth);
        mPaint.setTextAlign(Paint.Align.LEFT);
        for (int i = 0, len = chatDatas.size(); i < len; i++) {
            ChatData chatData = chatDatas.get(i);
            x = xMax - 6 * cubeWidth;
            y = yMin + 1.5F * i * cubeWidth;
            mPaint.setColor(chatData.color == 0 ? (chatData.color = randomColor()) : chatData.color);
            canvas.drawRect(x, y - 0.8F * cubeWidth, x + cubeWidth, y + 0.2F * cubeWidth, mPaint);

            mPaint.setColor(Color.BLACK);
            String text = (text = chatData.text) == null ? "" : (text.length() > 4 ? text.substring(0, 4) + "..." : text);
            canvas.drawText(text, x + 1.6F * cubeWidth, y, mPaint);
        }

        float radius = Math.min(xMax - xMin, yMax - yMin) * 0.4F;
        float centerX = xMin + 0.5F * (xMax - xMin), centerY = yMin + 0.5F * (yMax - yMin);
        float startAngle = 0, sweepAngle;
        for (int i = 0, len = chatDatas.size(); i < len; i++) {
            ChatData chatData = chatDatas.get(i);
            mPaint.setColor(chatData.color == 0 ? (chatData.color = randomColor()) : chatData.color);
            radius *= 0.8;
            r.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
            canvas.drawArc(r, startAngle, sweepAngle = animationRate * 360.0F * chatData.value / totalValue, true, mPaint);
            startAngle += sweepAngle;
        }
    }

    private void drawTimeAxisDay(Canvas canvas) {
        int[] colors = new int[]{0xFFA0E3B0, 0xFFDED144, 0xFF8FCDF9, 0xFFFB7C35};
        float unit = Math.min(width / 24, height / 24);
        int animationAngle = (int) (animationRate * 360);

        // 绘制表盘
        mPaint.setColor(0xFF4AA6D7);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(unit);
        r.set(width / 2 - unit * 5.5F, height / 2 - unit * 5.5F, width / 2 + unit * 5.5F, height / 2 + unit * 5.5F);
        canvas.drawArc(r, -90, animationAngle, false, mPaint);
        mPaint.setStrokeWidth(2);
        r.set(width / 2 - unit * 7.5F, height / 2 - unit * 7.5F, width / 2 + unit * 7.5F, height / 2 + unit * 7.5F);
        canvas.drawArc(r, -90, animationAngle, false, mPaint);

        // 绘制刻度
        tPaint.setColor(0xFF000000);
        tPaint.setTextSize(unit);
        tPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < 24; i += 3) {
            double angle = (12 - i) * Math.PI / 12;
            canvas.drawText(String.valueOf(i), width / 2 + unit * 4.5F * (float) Math.sin(angle), height / 2 + unit * 4.5F * (float) Math.cos(angle) + unit / 3, tPaint);
            if (i * 360 / 24 > animationAngle) {
                break;
            }
        }

        // 绘制时针
        mPaint.setColor(0xFF2BC5CB);
        mPaint.setStrokeWidth(2);
        canvas.drawLine(width / 2, height / 2, width / 2, height / 2 - unit * 4, mPaint);
        double angle = Math.PI - Math.min((curMinutes / (12F * 60)), (animationAngle / 180F)) * Math.PI;
        canvas.drawLine(width / 2, height / 2, width / 2 + unit * 4 * (float) Math.sin(angle), height / 2 + unit * 4 * (float) Math.cos(angle), mPaint);

        // 绘制应用使用信息
        float startAngle = -90;
        float minAngle = startAngle;
        float sweepAngle = 0;
        for (int i = 0, len = chatDatas.size(); i < len; i++) {
            ChatData chatData = chatDatas.get(i);
            if (Operation.SCREEN_ON.equals(chatData.text)) {
//                mPaint.setColor(0xFF33FFFF);
//                mPaint.setStrokeWidth(2);
//                r.set(width / 2 - unit * 7.5F, height / 2 - unit * 7.5F, width / 2 + unit * 7.5F, height / 2 + unit * 7.5F);
//                canvas.drawArc(r, startAngle + chatData.time, chatData.value * 360F / Constants.ONE_DAY, false, mPaint);
            } else if (chatData.value > 0) {
                mPaint.setColor(colors[(int) chatData.time / 90]);
                mPaint.setStrokeWidth(unit);
                r.set(width / 2 - unit * 6.5F, height / 2 - unit * 6.5F, width / 2 + unit * 6.5F, height / 2 + unit * 6.5F);
                canvas.drawArc(r, minAngle = Math.max(minAngle, startAngle + chatData.time), sweepAngle = chatData.value * 360F / Constants.ONE_DAY, false, mPaint);
                minAngle += sweepAngle;
            }
            if (chatData.time > animationAngle) {
                break;
            }
        }

        if (iconMap != null && chatLists != null) {
            int iconWidth = (int) (unit * 1.6F), x1, y1, x2, y2;
            int iconSpace = (int) (unit * 0.2F);

            for (int i = 0; i < chatLists.size(); i++) {
                if (45 + i * 90 > animationAngle) {
                    break;
                }
                angle = -i * Math.PI / 2 + Math.PI * 3 / 4;
                x1 = (int) (width / 2 + unit * 8 * (float) Math.sin(angle));
                y1 = (int) (height / 2 + unit * 8 * (float) Math.cos(angle));

                mPaint.setColor(colors[i]);
                mPaint.setStrokeWidth(2);
                canvas.drawLine(width / 2 + unit * 6.5F * (float) Math.sin(angle), height / 2 + unit * 6.5F * (float) Math.cos(angle), x1, y1, mPaint);

                List<ChatData> list = chatLists.get(i).chatDatas;

                if (i < 2) {
                    canvas.drawLine(x1, y1, x1 + unit * 8, y1, mPaint);

                    long total = 0;
                    x2 = (int) (x1 + unit * 8);
                    y2 = y1 - iconWidth - iconSpace;
                    for (int j = 0, len = list.size(); j < len; j++) {
                        total += list.get(j).value;
                        if (j < 4) {
                            Drawable d = iconMap.get(list.get(j).text);
                            if (d == null) {
                                continue;
                            }
                            d.setAlpha((int) (255 * animationRate));
                            x2 = x2 - iconWidth - iconSpace;
                            d.setBounds(x2, y2, x2 + iconWidth, y2 + iconWidth);
                            d.draw(canvas);
                        }
                    }

                    tPaint.setColor(0xFF000000);
                    tPaint.setTextSize(unit);
                    tPaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText("总共用时" + DB.getTimeBySecond(total / 1000), x1 + unit * 8, y1 + unit, tPaint);
                } else {
                    canvas.drawLine(x1, y1, x1 - unit * 8, y1, mPaint);

                    long total = 0;
                    x2 = (int) (x1 - unit * 8) - iconWidth;
                    y2 = y1 - iconWidth - iconSpace;
                    for (int j = 0, len = list.size(); j < len; j++) {
                        total += list.get(j).value;
                        if (j < 4) {
                            Drawable d = iconMap.get(list.get(j).text);
                            if (d == null) {
                                continue;
                            }
                            d.setAlpha((int) (255 * animationRate));
                            x2 = x2 + iconWidth + iconSpace;
                            d.setBounds(x2, y2, x2 + iconWidth, y2 + iconWidth);
                            d.draw(canvas);
                        }
                    }

                    tPaint.setColor(0xFF000000);
                    tPaint.setTextSize(unit);
                    tPaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText("总共用时" + DB.getTimeBySecond(total / 1000), x1 - unit * 8, y1 + unit, tPaint);
                }

            }

        }

    }


    private void drawTimeAxisWeek(Canvas canvas) {
        int[] colors = new int[]{0xFFA0E3B0, 0xFFDED144, 0xFF8FCDF9, 0xFFFB7C35, 0xFFA0E3B0, 0xFFDED144, 0xFF8FCDF9, 0xFFFB7C35};
        float unit = Math.min(width / 28, height / 28);
        int animationAngle = (int) (animationRate * 360);

        // 绘制表盘
        mPaint.setColor(0xFF4AA6D7);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(unit);
        r.set(width / 2 - unit * 5.5F, height / 2 - unit * 5.5F, width / 2 + unit * 5.5F, height / 2 + unit * 5.5F);
        canvas.drawArc(r, -90, animationAngle, false, mPaint);
        mPaint.setStrokeWidth(2);
        r.set(width / 2 - unit * 7.5F, height / 2 - unit * 7.5F, width / 2 + unit * 7.5F, height / 2 + unit * 7.5F);
        canvas.drawArc(r, -90, animationAngle, false, mPaint);

        // 绘制刻度
        tPaint.setColor(0xFF000000);
        tPaint.setTextSize(unit);
        tPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < 7; i += 1) {
            double angle = (7 - i * 2) * Math.PI / 7;
            canvas.drawText(String.valueOf(i + 1), width / 2 + unit * 4.5F * (float) Math.sin(angle), height / 2 + unit * 4.5F * (float) Math.cos(angle) + unit / 3, tPaint);
            if (i * 360 / 7 > animationAngle) {
                break;
            }
        }

        // 绘制时针
        mPaint.setColor(0xFF2BC5CB);
        mPaint.setStrokeWidth(2);
        canvas.drawLine(width / 2, height / 2, width / 2, height / 2 - unit * 4, mPaint);
        double angle = Math.PI - Math.min((curMinutes / (7 * 12 * 60)), (animationAngle / 180F)) * Math.PI;
        canvas.drawLine(width / 2, height / 2, width / 2 + unit * 4 * (float) Math.sin(angle), height / 2 + unit * 4 * (float) Math.cos(angle), mPaint);

        // 绘制应用使用信息
        float startAngle = -90;
        float minAngle = startAngle;
        float sweepAngle = 0;
        for (int i = 0, len = chatDatas.size(); i < len; i++) {
            ChatData chatData = chatDatas.get(i);
            if (Operation.SCREEN_ON.equals(chatData.text)) {
//                mPaint.setColor(0xFF33FFFF);
//                mPaint.setStrokeWidth(2);
//                r.set(width / 2 - unit * 7.5F, height / 2 - unit * 7.5F, width / 2 + unit * 7.5F, height / 2 + unit * 7.5F);
//                canvas.drawArc(r, startAngle + chatData.time, chatData.value * 360F / Constants.ONE_DAY, false, mPaint);
            } else if (chatData.value > 0) {
                mPaint.setColor(colors[(int) (chatData.time * 7 / 360)]);
                mPaint.setStrokeWidth(unit);
                r.set(width / 2 - unit * 6.5F, height / 2 - unit * 6.5F, width / 2 + unit * 6.5F, height / 2 + unit * 6.5F);
                canvas.drawArc(r, minAngle = Math.max(minAngle, startAngle + chatData.time), sweepAngle = chatData.value * 360F / Constants.ONE_WEEK, false, mPaint);
                minAngle += sweepAngle;
            }
            if (startAngle > animationAngle) {
                break;
            }
        }

        if (iconMap != null && chatLists != null) {
            int iconWidth = (int) (unit * 1.6F), x1, y1, x2, y2;
            int iconSpace = (int) (unit * 0.2F);

            for (int i = 0; i < chatLists.size(); i++) {
                int tempAngle = 25 + i * 51 - 90;
                if (tempAngle > animationAngle) {
                    break;
                }
//                angle = -tempAngle * Math.PI / 180 + Math.PI;
                angle = tempAngle * Math.PI / 180;
                x1 = (int) (width / 2 + unit * 8 * (float) Math.cos(angle));
                y1 = (int) (height / 2 + unit * 8 * (float) Math.sin(angle));

                mPaint.setColor(colors[i]);
                mPaint.setStrokeWidth(2);
                canvas.drawLine(width / 2 + unit * 6.5F * (float) Math.cos(angle), height / 2 + unit * 6.5F * (float) Math.sin(angle), x1, y1, mPaint);

                List<ChatData> list = chatLists.get(i).chatDatas;

                if (tempAngle < 90) {
                    canvas.drawLine(x1, y1, x1 + unit * 8, y1, mPaint);

                    long total = 0;
                    x2 = (int) (x1 + unit * 8);
                    y2 = y1 - iconWidth - iconSpace;
                    for (int j = 0, len = list.size(); j < len; j++) {
                        total += list.get(j).value;
                        if (j < 4) {
                            Drawable d = iconMap.get(list.get(j).text);
                            if (d == null) {
                                continue;
                            }
                            d.setAlpha((int) (255 * animationRate));
                            x2 = x2 - iconWidth - iconSpace;
                            d.setBounds(x2, y2, x2 + iconWidth, y2 + iconWidth);
                            d.draw(canvas);
                        }
                    }

                    tPaint.setColor(0xFF000000);
                    tPaint.setTextSize(unit);
                    tPaint.setTextAlign(Paint.Align.RIGHT);
                    canvas.drawText("总共用时" + DB.getTimeBySecond(total / 1000), x1 + unit * 8, y1 + unit, tPaint);
                } else {
                    canvas.drawLine(x1, y1, x1 - unit * 8, y1, mPaint);

                    long total = 0;
                    x2 = (int) (x1 - unit * 8) - iconWidth;
                    y2 = y1 - iconWidth - iconSpace;
                    for (int j = 0, len = list.size(); j < len; j++) {
                        total += list.get(j).value;
                        if (j < 4) {
                            Drawable d = iconMap.get(list.get(j).text);
                            if (d == null) {
                                continue;
                            }
                            d.setAlpha((int) (255 * animationRate));
                            x2 = x2 + iconWidth + iconSpace;
                            d.setBounds(x2, y2, x2 + iconWidth, y2 + iconWidth);
                            d.draw(canvas);
                        }
                    }

                    tPaint.setColor(0xFF000000);
                    tPaint.setTextSize(unit);
                    tPaint.setTextAlign(Paint.Align.LEFT);
                    canvas.drawText("总共用时" + DB.getTimeBySecond(total / 1000), x1 - unit * 8, y1 + unit, tPaint);
                }

            }

        }

    }

}
