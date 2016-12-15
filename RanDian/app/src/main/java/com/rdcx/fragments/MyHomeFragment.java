package com.rdcx.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.rdcx.bean.AppDate;
import com.rdcx.bean.CustomDate;
import com.rdcx.bean.WeekDate;
import com.rdcx.myview.CalendarCard;
import com.rdcx.myview.CalendarViewAdapter;
import com.rdcx.myview.GalleryAdapter;
import com.rdcx.myview.GridViewAdapter;
import com.rdcx.myview.ListAdapter;
import com.rdcx.myview.MyAdapter;
import com.rdcx.myview.MyGallery;
import com.rdcx.myview.MyViewPager;
import com.rdcx.myview.TranslatePageTransformer;
import com.rdcx.randian.HomeActivity;
import com.rdcx.randian.R;
import com.rdcx.randian.ScanActivity;
import com.rdcx.tools.PermissionTools;
import com.rdcx.tools.SP;
import com.rdcx.tools.SyncThread;
import com.rdcx.utils.Constants;
import com.rdcx.utils.DateUtil;
import com.rdcx.utils.Utils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/3/14 0014.
 * 主页
 *
 * @author mengchuiliu
 */
@SuppressLint({"HandlerLeak", "SetTextI18n", "RtlHardcoded"})
public class MyHomeFragment extends Fragment {
    View view;
    private MyViewPager vp;
    MyAdapter homeAdapter;
    TextView choose_date, home_calendar, home_tv;
    private MyGallery gallery;
    private int[] image;
    int weekNum;//当前星期数
    private int currentPosition = 1;
    ImageView head_portrait;
    ListView home_list;
    ListAdapter listAdapter;
    private long currentTime = 0L;//时间long类型

    private String selcetDay;
    private String selcetMonth;
    private String selcetWeek;
    String userId;

    PopupWindow popupWindow;
    AlertDialog dialog;

    float prevoiusY = 0, prevoiusX = 0;
    float sx = 0, ex = 0;

    public MyHomeFragment() {
        selcetWeek = "";
        selcetMonth = "" + DateUtil.getMonth();
        selcetDay = DateUtil.getCurTime();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        image = Constants.image;
        currentTime = System.currentTimeMillis();
        weekNum = DateUtil.getWeekNum();
        selcetWeek = weekNum + "";

        view = inflater.inflate(R.layout.fragment_home, null);
        initView(view);

        userId = SP.getString(getActivity(), SP.USER_ID, "-1");
        head_portrait.setImageBitmap(Utils.getPortraitBitmap(getActivity(), handler, true));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        head_portrait.setImageBitmap(Utils.getPortraitBitmap(getActivity(), handler, false));

        if (type == 0) {
            home_calendar.setText(selcetDay);
            image = Constants.image;
        } else if (type == 1) {
            home_calendar.setText(selcetMonth);
            image = Constants.image1;
        } else {
            home_calendar.setText(selcetWeek);
            image = Constants.image;
        }

        refreshAndStartAnimiation(true); // 刷新一下数据
    }

    private void initView(View view) {
        head_portrait = (ImageView) view.findViewById(R.id.head_portrait);
        choose_date = (TextView) view.findViewById(R.id.head_text);
        home_calendar = (TextView) view.findViewById(R.id.home_calendar);
        home_tv = (TextView) view.findViewById(R.id.home_tv);

        head_portrait.setOnClickListener(onClickListener);
        choose_date.setOnClickListener(onClickListener);
        home_calendar.setOnClickListener(onClickListener);
        view.findViewById(R.id.home_scan).setOnClickListener(onClickListener);

        vp = (MyViewPager) view.findViewById(R.id.main_vp);
        homeAdapter = new MyAdapter(getActivity().getSupportFragmentManager());
        homeAdapter.setFgs(image, type, currentTime);
        // 页面滑动监听器
        vp.setPageTransformer(true, new TranslatePageTransformer());
        vp.setAdapter(homeAdapter);

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.e("my_log", "=======>" + position + "==" + currentPosition);
                if (position < 1) { //首位之前，跳转到末尾（N）
                    position = image.length;
                    currentPosition = position;
                    vp.setCurrentItem(position, false);
                } else if (position > image.length) { //末位之后，跳转到首位（1）
                    position = 1;
                    currentPosition = position;
                    vp.setCurrentItem(position, false); //false:不显示跳转过程的动画
                }
                if (position > currentPosition) {
                    gallery.setSelection(position - 2 + (image.length * 100), true);
                    gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null); //再模拟按下
                } else if (position < currentPosition) {
                    gallery.setSelection(position + (image.length * 100), true);
                    gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null); //再模拟按下
                } else {
                    gallery.setSelection(position - 1 + (image.length * 100), true);
                }
                currentPosition = position;
                refreshAndStartAnimiation(false);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });


        GalleryAdapter adapter = new GalleryAdapter(getActivity(), image);
        gallery = (MyGallery) view.findViewById(R.id.gallery);
        gallery.setAdapter(adapter);
        gallery.setSpacing(20);
        gallery.setUnselectedAlpha(0.3f);
        gallery.setSelection(image.length * 100 + (image.length - 1));
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e("my_log", "=======>" + position);
                Message message = new Message();
                message.what = 0;
                message.arg1 = position % image.length;
                handler.sendMessage(message);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        home_tv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        sx = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        ex = event.getX();
                        break;
                }

                if ((ex - sx) > 50.0) {
                    gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null); //再模拟按下
                    return true;
                } else if ((ex - sx) < -50.0) {
                    gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null); //再模拟按下
                    return true;
                }
                return false;
            }
        });


        home_list = (ListView) view.findViewById(R.id.home_list);
        home_list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    //记录按下时的x坐标
                    case MotionEvent.ACTION_DOWN:
                        prevoiusY = event.getY();
                        prevoiusX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        //判断是横向操作还是垂直操作
                        if (Math.abs(event.getX() - prevoiusX) > Math.abs(event.getY() - prevoiusY)) {
                            if (event.getX() - prevoiusX > 50.0) {
                                gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_LEFT, null); //再模拟按下
                                return true;
                            } else if (event.getX() - prevoiusX < -50.0) {
                                gallery.onKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT, null); //再模拟按下
                                return true;
                            }
                        }
                        break;
                }
                return false;
            }
        });
        listAdapter = new ListAdapter(getContext());
    }


    // TODO------------------日维度----------------------------
    private int type = 0;//0为日，1为月,2为周
    private ViewPager mViewPager;
    private int mCurrentIndex = 498;
    protected CalendarViewAdapter adapter;
    private SildeDirection mDirection = SildeDirection.NO_SILDE;
    private TextView monthText;
    private int currentMonth = DateUtil.getMonth();
    private int currentYear = DateUtil.getYear();
    GridViewAdapter gridViewAdapter;

    enum SildeDirection {
        RIGHT, LEFT, NO_SILDE
    }

    ArrayList<WeekDate> weekDates = new ArrayList<>();//周月数据源

    //获取数据源
    private ArrayList<WeekDate> getData() {
        weekDates.clear();
        if (type == 1) {
            for (int i = 1; i < 13; i++) {
                WeekDate weekDate = new WeekDate();
                if (i < 10) {
                    weekDate.setMonth(" " + i + "月");
                } else {
                    weekDate.setMonth(i + "月");
                }
                weekDates.add(weekDate);
            }
            return weekDates;
        } else if (type == 2) {
            int sum = weekNum % 9;
            for (int i = 1; i <= sum; i++) {
                int num = weekNum - sum + i;
                WeekDate weekDate = new WeekDate();
                weekDate.setWeekNumber(num);
                weekDate.setWeekFirstDay(DateUtil.getYearWeekFirstDay(currentYear, num));
                weekDate.setWeekEndDay(DateUtil.getYearWeekEndDay(currentYear, num));
                weekDate.setStartTime(DateUtil.getFirstDayLong(currentYear, num));
                weekDate.setEndTime(DateUtil.getEndDayLong(currentYear, num));
                weekDates.add(weekDate);
            }
            for (int j = 1; j <= 9 - sum; j++) {
                int num = weekNum + j;
                WeekDate weekDate = new WeekDate();
                weekDate.setWeekNumber(num);
                weekDate.setWeekFirstDay(DateUtil.getYearWeekFirstDay(currentYear, num));
                weekDate.setWeekEndDay(DateUtil.getYearWeekEndDay(currentYear, num));
                weekDate.setStartTime(DateUtil.getFirstDayLong(currentYear, num));
                weekDate.setEndTime(DateUtil.getEndDayLong(currentYear, num));
                weekDates.add(weekDate);
            }
            return weekDates;
        }
        return weekDates;
    }

    private ViewFlipper flipper = null;
    private int selectPosition = 0;
    private GridView weekView;
    private int monthSelection = -1;
    private int myCurrentYear = DateUtil.getYear();
    private long myCurrentWeek = -1;

    private void getCalendarView(View view) {
        LinearLayout ll_date = (LinearLayout) view.findViewById(R.id.ll_date);
        mViewPager = (ViewPager) view.findViewById(R.id.vp_calendar);
        monthText = (TextView) view.findViewById(R.id.tvCurrentMonth);
        flipper = (ViewFlipper) view.findViewById(R.id.flipper);
        view.findViewById(R.id.btnPreMonth).setOnClickListener(onClickListener);
        view.findViewById(R.id.btnNextMonth).setOnClickListener(onClickListener);
        mViewPager.setVisibility(View.VISIBLE);
        ll_date.setVisibility(View.GONE);
        flipper.setVisibility(View.GONE);
        mCurrentIndex = 498;
        if (type == 0) {
            ll_date.setVisibility(View.VISIBLE);
            CalendarCard[] views = new CalendarCard[3];
            for (int i = 0; i < 3; i++) {
                views[i] = new CalendarCard(getActivity(), new Cell(), dayChooseDate);
            }
            setPageDate(views);
        } else if (type == 1) {
            monthText.setText(currentYear + "年");
            monthText.setTextSize(18);
            View[] views = new View[3];
            gridViewAdapter = new GridViewAdapter(getActivity(), type);
            gridViewAdapter.setData(getData());
            if (monthSelection == -1) {
                monthSelection = currentMonth - 1;
            }
            if (currentYear == myCurrentYear) {
                gridViewAdapter.setSelectPosition(monthSelection);
            } else {
                gridViewAdapter.setSelectPosition(-1);
            }
            gridViewAdapter.setCurrentPosition(currentMonth - 1);
            for (int i = 0; i < 3; i++) {
                views[i] = LayoutInflater.from(getActivity()).inflate(R.layout.view_month, null);
                GridView gridView = (GridView) views[i].findViewById(R.id.gv_month);
                gridView.setAdapter(gridViewAdapter);
                gridView.setOnItemClickListener(onItemClickListener);
            }
            setPageDate(views);
        } else if (type == 2) {
            monthText.setText(currentYear + "年");
            monthText.setTextSize(18);
            mViewPager.setVisibility(View.GONE);
            flipper.setVisibility(View.VISIBLE);
            addGridView();
            gridViewAdapter = new GridViewAdapter(getActivity(), type);
            gridViewAdapter.setData(getData());
            selectPosition = weekNum % 9 - 1;
            if (selectPosition == -1) {
                selectPosition = 8;
            }
            if (monthSelection == -1) {
                monthSelection = selectPosition;
            }
            if (myCurrentWeek == -1 || DateFormat.format("yyyy-MM-dd", myCurrentWeek).equals(DateFormat.format("yyyy-MM-dd", weekDates.get(monthSelection).getStartTime()))) {
                gridViewAdapter.setSelectPosition(monthSelection);
            } else {
                gridViewAdapter.setSelectPosition(-1);
            }
            gridViewAdapter.setCurrentPosition(selectPosition);
            weekView.setAdapter(gridViewAdapter);
            flipper.addView(weekView, 0);
        }
    }

    //滑动监听
    GestureDetector gestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            int gvFlag = 0;
            if (e1.getX() - e2.getX() > 50) {
                // 向左滑
                slideWeek(true, gvFlag);
                return true;
            } else if (e1.getX() - e2.getX() < -50) {
                slideWeek(false, gvFlag);
                return true;
            }
            return false;
        }
    });

    /**
     * 周维度左右滑动
     * flag true->右,false->左
     */
    private void slideWeek(boolean flag, int gvFlag) {
        if (flag) {
            weekNum += 9;
            if (weekNum > 54) {
                weekNum = weekNum % 9;
                if (weekNum == 0) {
                    weekNum = 9;
                }
                currentYear++;
                monthText.setText(currentYear + "年");
            }
            addGridView();
            gridViewAdapter.setData(getData());
            gridViewAdapter.setSelectPosition(selectPosition);
            weekView.setAdapter(gridViewAdapter);
            gvFlag++;
            flipper.addView(weekView, gvFlag);
            flipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_in));
            flipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_out));
            flipper.showNext();
            flipper.removeViewAt(0);
        } else {
            weekNum -= 9;
            if (weekNum <= 0) {
                weekNum = weekNum + 54;
                currentYear--;
                monthText.setText(currentYear + "年");
            }
            addGridView();
            weekView.setAdapter(gridViewAdapter);
            gridViewAdapter.setData(getData());
            gridViewAdapter.setSelectPosition(selectPosition);
            gvFlag++;
            flipper.addView(weekView, gvFlag);
            flipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_in));
            flipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_out));
            flipper.showPrevious();
            flipper.removeViewAt(0);
        }
        setWeekView();
    }

    private void setWeekView() {
        if (myCurrentYear == currentYear && weekNum == DateUtil.getWeekNum()) {
            gridViewAdapter.setCurrentPosition(selectPosition);
        } else {
            gridViewAdapter.setCurrentPosition(-1);
        }
        if (DateFormat.format("yyyy-MM-dd", myCurrentWeek).equals(DateFormat.format("yyyy-MM-dd", weekDates.get(monthSelection).getStartTime()))) {
            gridViewAdapter.setSelectPosition(monthSelection);
        } else {
            gridViewAdapter.setSelectPosition(-1);
        }
        gridViewAdapter.notifyDataSetInvalidated();
    }

    // 网格试图控件设置
    private void addGridView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                Gallery.LayoutParams.MATCH_PARENT, Gallery.LayoutParams.MATCH_PARENT);
        params.setMargins(20, 20, 20, 20);
        weekView = new GridView(getActivity());
        weekView.setNumColumns(3);
        weekView.setGravity(Gravity.CENTER);
        weekView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        weekView.setVerticalSpacing(50);
        weekView.setHorizontalSpacing(5);
        weekView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        weekView.setOnItemClickListener(onItemClickListener);
        weekView.setLayoutParams(params);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            monthSelection = position;
            gridViewAdapter.setSelectPosition(position);
            gridViewAdapter.notifyDataSetChanged();
            if (type == 1) {
                currentMonth = position + 1;
                currentTime = DateUtil.getDateLong(currentYear + "-" + currentMonth + "-1");
                myCurrentYear = currentYear;
                home_calendar.setText(currentMonth + "");
                selcetMonth = currentMonth + "";
            } else {
                currentTime = weekDates.get(position).getStartTime();
                myCurrentWeek = weekDates.get(position).getStartTime();
                home_calendar.setText(weekDates.get(position).getWeekNumber() + "");
                selcetWeek = weekDates.get(position).getWeekNumber() + "";
            }
            if (currentTime > System.currentTimeMillis()) {
                Toast.makeText(getActivity(), "暂无数据", Toast.LENGTH_SHORT).show();
            }
            handler.sendEmptyMessage(2);
            refreshAndStartAnimiation(true);
        }
    };

    private void setPageDate(View[] views) {
        adapter = new CalendarViewAdapter(views);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(498);
        mViewPager.addOnPageChangeListener(onPageChangeListener);
    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (type == 0) {
                measureDirection(position);
                updateCalendarView(position);
            } else {
                if (position > mCurrentIndex) {
                    currentYear++;
                } else if (position < mCurrentIndex) {
                    currentYear--;
                }
                monthText.setText(currentYear + "年");
                mCurrentIndex = position;
                if (DateUtil.getYear() == currentYear) {
                    gridViewAdapter.setCurrentPosition(DateUtil.getMonth() - 1);
                } else {
                    gridViewAdapter.setCurrentPosition(-1);
                }
                if (currentYear == myCurrentYear) {
                    gridViewAdapter.setSelectPosition(monthSelection);
                } else {
                    gridViewAdapter.setSelectPosition(-1);
                }
                gridViewAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private void measureDirection(int arg0) {
        if (arg0 > mCurrentIndex) {
            mDirection = SildeDirection.RIGHT;
        } else if (arg0 < mCurrentIndex) {
            mDirection = SildeDirection.LEFT;
        }
        mCurrentIndex = arg0;
    }

    private void updateCalendarView(int position) {
        CalendarCard[] mShowViews = (CalendarCard[]) adapter.getAllItems();
        if (mDirection == SildeDirection.RIGHT) {
            mShowViews[position % mShowViews.length].rightSlide();
        } else if (mDirection == SildeDirection.LEFT) {
            mShowViews[position % mShowViews.length].leftSlide();
        }
        mDirection = SildeDirection.NO_SILDE;
    }

    private String dayChooseDate = "";

    class Cell implements CalendarCard.OnCellClickListener {
        //点击日期监听
        @Override
        public void clickDate(CustomDate date) {
            long current = DateUtil.getDateLong(date.toString());
            if (current > System.currentTimeMillis()) {
                Toast.makeText(getActivity(), "未来日期不可选!", Toast.LENGTH_SHORT).show();
            } else {
                dayChooseDate = date.toString();
                home_calendar.setText(date.getDay() + "");
                selcetDay = date.getDay() + "";
                currentTime = current;
                handler.sendEmptyMessage(2);
                refreshAndStartAnimiation(true);
            }
        }

        //滑动日期监听
        @Override
        public void changeDate(CustomDate date) {
            monthText.setText(date.year + "年" + date.month + "月");
        }
    }

    // TODO-------------------------日维度----------------------------

    private void refresh(int type) {
        if (type == 0 || type == 2) {
            image = Constants.image;
        } else {
            image = Constants.image1;
        }
        GalleryAdapter adapter = new GalleryAdapter(getActivity(), image);
        gallery.setAdapter(adapter);
        gallery.setSelection(image.length * 100);
        adapter.notifyDataSetChanged();
    }

    //刷新数据
    private void refreshAndStartAnimiation(boolean refresh) {
        showText("", null); // 设置文案为空
        if (refresh) {
            for (int i = 0, len = homeAdapter.getCount(); i < len; i++) {
                ((HomeFragment) homeAdapter.getItem(i)).set(i, type, currentTime);
            }
        }
        ((HomeFragment) homeAdapter.getItem(currentPosition)).refreshData(getContext(), handler);
    }

    /**
     * 显示时间维度
     */
    private void showDate(TextView textView) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.popup_window_date, null);
        popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // 需要设置一下此参数，点击外边可消失
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 设置点击窗口外边窗口消失
        popupWindow.setOutsideTouchable(true);
        // 设置此参数获得焦点，否则无法点击
        popupWindow.setFocusable(true);
        // 相对某个控件的位置（正左下方），无偏移
        if (!popupWindow.isShowing()) {
            popupWindow.showAsDropDown(textView, -12, 5);
        }
        view.findViewById(R.id.choose_day).setOnClickListener(onClickListener);
        view.findViewById(R.id.choose_week).setOnClickListener(onClickListener);
        view.findViewById(R.id.choose_month).setOnClickListener(onClickListener);
//        view.findViewById(R.id.choose_year).setOnClickListener(this);
    }

    //点击监听
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.head_portrait://点击头像显示侧拉菜单
                    ((HomeActivity) getActivity()).getLeft();
                    break;
                case R.id.head_text://点击更换周期
                    showDate(choose_date);
                    break;
                case R.id.home_calendar://点击更换日历
                    currentMonth = DateUtil.getMonth();
                    currentYear = DateUtil.getYear();

                    weekNum = DateUtil.getWeekNum();
                    selcetWeek = weekNum + "";

                    if (dialog == null) {
                        dialog = new AlertDialog.Builder(getActivity()).create();
                    }
                    dialog.show();
                    //日维度
                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.calendar_view, null);
                    getCalendarView(view);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams
                            .WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    DisplayMetrics dm = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                    Window dialogWindow = dialog.getWindow();
                    WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                    if (type == 0) {
                        lp.width = dm.widthPixels * 4 / 5;
                        lp.height = dm.heightPixels * 6 / 10;
                    } else if (type == 1) {
                        lp.width = dm.widthPixels * 4 / 5;
                        lp.height = dm.heightPixels / 2;
                    } else if (type == 2) {
                        lp.width = dm.widthPixels * 4 / 5;
                        lp.height = dm.heightPixels * 11 / 20;
                    }
                    dialogWindow.setGravity(Gravity.CENTER);
                    dialogWindow.setContentView(view, params);
                    break;
                case R.id.home_scan://点击扫描
                    SyncThread.start(getContext(), SyncThread.TYPE_ONE_HOUR);
                    startActivity(new Intent(getActivity(), ScanActivity.class));
                    break;
                case R.id.choose_day:
                    popupWindow.dismiss();
                    choose_date.setText("日 • 记");
                    home_calendar.setText(DateUtil.getCurTime());
                    type = 0;
                    currentTime = System.currentTimeMillis();
                    refresh(type);
                    refreshAndStartAnimiation(true);
                    dayChooseDate = "";
                    monthSelection = -1;
                    myCurrentYear = DateUtil.getYear();
                    break;
                case R.id.choose_week:
                    popupWindow.dismiss();
                    choose_date.setText("周 • 记");
                    home_calendar.setText(selcetWeek);
                    type = 2;
                    currentTime = System.currentTimeMillis();
                    refresh(type);
                    refreshAndStartAnimiation(true);
                    dayChooseDate = "";
                    monthSelection = -1;
                    myCurrentYear = DateUtil.getYear();
                    break;
                case R.id.choose_month:
                    popupWindow.dismiss();
                    choose_date.setText("月 • 记");
                    home_calendar.setText("" + DateUtil.getMonth());
                    type = 1;
                    currentTime = System.currentTimeMillis();
                    refresh(type);
                    refreshAndStartAnimiation(true);
                    dayChooseDate = "";
                    monthSelection = -1;
                    myCurrentYear = DateUtil.getYear();
                    break;
                case R.id.btnPreMonth:
                    if (type == 2) {
                        slideWeek(false, 0);
                    } else {
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1);
                    }
                    break;
                case R.id.btnNextMonth:
                    if (type == 2) {
                        slideWeek(true, 0);
                    } else {
                        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                    }
                    break;
            }
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (msg.arg1 == 0) {
                        currentPosition = 1;
                        vp.setCurrentItem(1, false);
                    } else if (msg.arg1 == (image.length - 1)) {
                        currentPosition = image.length;
                        vp.setCurrentItem(image.length, false);
                    } else {
                        currentPosition = msg.arg1 + 1;
                        vp.setCurrentItem(msg.arg1 + 1, true);
                    }
                    break;
                case 16:
                    if (msg.obj != null) {
                        byte[] data = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bitmap != null) {
                            head_portrait.setImageBitmap(Utils.toRoundBitmap(bitmap));
                            Utils.setPortraitBitmap(getActivity(), bitmap);
                        }
                    } else {
                        Log.e("my_log", "头像加载失败！");
                    }
                    break;
                case 2:
                    dialog.dismiss();
                    break;
                case 10:
                    if (currentPosition == msg.arg1) {
                        ((HomeFragment) homeAdapter.getItem(msg.arg1)).invalidate(MyHomeFragment.this);
                    }
                    break;
//                case 11:
//                    if (currentPosition == msg.arg1) {
//                        ((HomeFragment) homeAdapter.getItem(msg.arg1)).showChampion();
//                    }
//                    break;
                case 18://贴标签
                    ((HomeFragment) homeAdapter.getItem(msg.arg1)).setLable(MyHomeFragment.this);
                    break;
                default:
                    break;
            }
        }
    };

    public void showText(String ruleText, ArrayList<AppDate> appDateList) {
        if (appDateList != null) {
            home_tv.clearAnimation();
            home_tv.setVisibility(View.GONE);
            home_list.setVisibility(View.VISIBLE);
            listAdapter.setDate(appDateList);
            home_list.setAdapter(listAdapter);
            listAdapter.notifyDataSetChanged();
        } else {
            home_tv.clearAnimation();
            home_tv.setVisibility(View.VISIBLE);
            home_list.setVisibility(View.GONE);
            home_tv.setText(ruleText == null ? "" : Html.fromHtml(ruleText.replaceAll("(\\d+小时)|(\\d+[分秒张个次])", "<b><big>$0</big></b>")));
            home_tv.startAnimation(getHomeTvAnimation());
        }
    }

    private Animation homeTvAnimation;

    // 获取文字弹出的动画效果
    private Animation getHomeTvAnimation() {
        if (homeTvAnimation == null) {
            homeTvAnimation = new AlphaAnimation(0F, 1F);
            homeTvAnimation.setDuration(2400);
            homeTvAnimation.setInterpolator(new DecelerateInterpolator());
        }
        return homeTvAnimation;
    }
}
