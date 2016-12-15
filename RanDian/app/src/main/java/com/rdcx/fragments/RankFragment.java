package com.rdcx.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rdcx.bean.AppRankInfo;
import com.rdcx.myview.RankListAdapter;
import com.rdcx.randian.MyApplication;
import com.rdcx.randian.MyWeiBoShareActivity;
import com.rdcx.randian.R;
import com.rdcx.randian.WebHtmlActivity;
import com.rdcx.service.NetDataGetter;
import com.rdcx.service.NetManager;
import com.rdcx.tools.Call;
import com.rdcx.tools.DB;
import com.rdcx.tools.Operation;
import com.rdcx.tools.SP;
import com.rdcx.utils.Constants;
import com.rdcx.utils.Utils;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXImageObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xlistview.XListView;

import static android.widget.LinearLayout.OnClickListener;

/**
 * Created by Administrator on 2016/3/14 0014.
 * 排行榜
 *
 * @author mengchuiliu
 */
@SuppressLint({"HandlerLeak", "SetTextI18n", "RtlHardcoded"})
public class RankFragment extends Fragment implements XListView.IXListViewListener {
    int date = 1;//1-->日，2-->月
    private NetDataGetter netDataGetter;
    int type = 1; //1-->个人，2-->应用
    View view;
    GridView gridView;
    private ViewFlipper flipper = null;
    PopupWindow popupWindow;
    GridViewAdapter adapter;
    XListView listView;

    HorizontalScrollView scrollView;
    int selectLoc = 0;
    TextView rankEmpty, rankDay, rankMonth, rankChoose;
    LinearLayout rank_myself, ll_rank_date;

    String userId;

    RankListAdapter rankListAdapter;
    ArrayList<AppRankInfo> rankInfos = new ArrayList<>();

    private int loadMore = 1;

    ImageView myself_icon, ranking_icon;
    TextView my_rank_num, my_ranking, myself_data;

    boolean isCover, isCover2;
    String servicePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        userId = SP.getString(getActivity(), SP.USER_ID, "-1");
        rankListAdapter = new RankListAdapter(getActivity());
        view = inflater.inflate(R.layout.fragment_rank, null);

        myself_icon = (ImageView) view.findViewById(R.id.myself_icon);
        ranking_icon = (ImageView) view.findViewById(R.id.ranking_icon);
        my_rank_num = (TextView) view.findViewById(R.id.my_rank_num);
        my_ranking = (TextView) view.findViewById(R.id.my_ranking);
        myself_data = (TextView) view.findViewById(R.id.myself_data);

        scrollView = (HorizontalScrollView) view.findViewById(R.id.list_hs);
        gridView = (GridView) view.findViewById(R.id.rank_grid);
        flipper = (ViewFlipper) view.findViewById(R.id.rank_flipper);
        flipper.setFlipInterval(50);
        rankEmpty = (TextView) view.findViewById(R.id.rankEmpty);
        rankDay = (TextView) view.findViewById(R.id.rank_day);
        rankMonth = (TextView) view.findViewById(R.id.rank_month);
        rankChoose = (TextView) view.findViewById(R.id.rank_choose);
        rank_myself = (LinearLayout) view.findViewById(R.id.rank_myself);
        rank_myself.setVisibility(View.GONE);
        ll_rank_date = (LinearLayout) view.findViewById(R.id.ll_rank_date);
        setGridView();
        setPageView();
        getOneSelfData("1", "1");
        return view;
    }

    //获取应用排行榜显示数据
    private void getAppData(String dimension, String page, final boolean isCache) {
        DB.getDataInterface(getActivity()).getRankList(userId, dimension, page, new NetManager.DataArray() {
            @Override
            public void getServiceData(JSONArray jsonArray) {
                try {
                    JSONObject object = jsonArray.getJSONObject(0);
                    String resp = object.getString("resp");
                    if (resp.equals("000000")) {
                        JSONArray array = object.getJSONArray("modelList");
                        if (array.length() > 0) {
                            listView.setPullRefreshEnable(false);
                            listView.setPullLoadEnable(true);
                            if (isCache) {
                                rankInfos.clear();
                            }
                            isfresh = 0;
                            rankEmpty.setVisibility(View.GONE);
                            for (int j = 0; j < array.length(); j++) {
                                JSONObject jsonObject = array.getJSONObject(j);
                                JSONObject jsonObject1 = jsonObject.getJSONObject("model");
                                AppRankInfo appRankInfo = new AppRankInfo();
                                String iconPath = jsonObject1.getString("icon");
                                new RankIcon(appRankInfo).execute(iconPath);
                                appRankInfo.number = j + loadMore;
                                appRankInfo.appCount = jsonObject.getInt("count");
                                appRankInfo.total = jsonObject.getLong("sum");
                                appRankInfo.appName = jsonObject1.getString("name").trim();
                                rankInfos.add(appRankInfo);
                            }
                            rankListAdapter.setListData(rankInfos);
                            rankListAdapter.setType(2);
                            rankListAdapter.notifyDataSetChanged();
                        } else {
                            listView.setPullLoadEnable(false);
                            if (rankInfos.size() <= 0) {
                                listView.setPullRefreshEnable(true);
                                rankEmpty.setVisibility(View.VISIBLE);
                            }
                        }
                    } else {
                        listView.setPullLoadEnable(false);
                        if (rankInfos.size() <= 0) {
                            listView.setPullRefreshEnable(true);
                            rankEmpty.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (Utils.isNetworkAvailable(getActivity())) {
                    Toast.makeText(getActivity(), "网络异常，请稍后重试！", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                }
                listView.setPullLoadEnable(false);
                if (rankInfos.size() <= 0) {
                    listView.setPullRefreshEnable(true);
                    rankEmpty.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //获取个人排行榜数据
    private void getOneSelfData(String dimen, String date) {
        if ("1".equals(date)) {
            ranking_icon.setVisibility(View.VISIBLE);
            my_ranking.setVisibility(View.VISIBLE);
        } else {
            ranking_icon.setVisibility(View.GONE);
            my_ranking.setVisibility(View.GONE);
        }

        if ("3".equals(dimen)) {
            dimen = "43";
        } else if ("4".equals(dimen)) {
            dimen = "45";
        } else if ("5".equals(dimen)) {
            dimen = "61";
        } else if ("6".equals(dimen)) {
            dimen = "7";
        }
        final String type = dimen;

        Map<String, String> map = new HashMap<>();
        map.put("dimension", dimen);
        map.put("type", date);
        SP.cache(getContext(), Constants.GetPersonalRank, map, System.currentTimeMillis() - Constants.ONE_HOUR * 7, new SP.OnCache() {
            @Override
            public void onCache(String cache) {
                if (cache == null) {
                    rank_myself.setVisibility(View.GONE);
                    if (Utils.isNetworkAvailable(getActivity())) {
                        Toast.makeText(getActivity(), "网络异常，请稍后重试！", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                    }
                    listView.setPullLoadEnable(false);
                    if (rankInfos.size() <= 0) {
                        listView.setPullRefreshEnable(true);
                        rankEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    try {
                        com.alibaba.fastjson.JSONObject object = com.alibaba.fastjson.JSONObject.parseObject(cache);
                        //                        JSONObject object = jsonArray.getJSONObject(0);
                        String resp = object.getString("resp");
                        if (resp.equals("000000")) {
                            rank_myself.setVisibility(View.VISIBLE);
                            int ranknumoff = object.getIntValue("count");
                            com.alibaba.fastjson.JSONObject jsonObject1 = object.getJSONObject("model");
                            int ranknum = jsonObject1.getIntValue("count");
                            //                        long mydata = jsonObject1.getLong("date");
                            long mydata = getYestodayDate(type);
                            if (mydata <= 0) {
                                myself_data.setText("0分钟");
                            } else {
                                myself_data.setText(DB.getTimeByRank(mydata));
                            }
                            myself_icon.setImageBitmap(Utils.getPortraitBitmap(getActivity(), handler, true));
                            my_rank_num.setText(ranknum + ".");
                            if (ranknumoff > 0) {
                                ranking_icon.setImageResource(R.mipmap.rank_falling);
                            } else {
                                ranking_icon.setImageResource(R.mipmap.rank_rise);
                            }
                            my_ranking.setText("" + Math.abs(ranknumoff));

                            com.alibaba.fastjson.JSONArray array = object.getJSONArray("modelList");
                            if (array.size() > 0) {
                                listView.setPullRefreshEnable(false);
                                listView.setPullLoadEnable(false);
                                rankInfos.clear();
                                isfresh = 0;
                                rankEmpty.setVisibility(View.GONE);
                                for (int j = 0; j < array.size(); j++) {
                                    com.alibaba.fastjson.JSONObject jsonObject = array.getJSONObject(j);
                                    AppRankInfo appRankInfo = new AppRankInfo();
                                    //根据userId获取路径
                                    appRankInfo.number = j + 1;
                                    appRankInfo.total = jsonObject.getLong("date");
                                    appRankInfo.appName = jsonObject.getIntValue("userId") + "";
                                    appRankInfo.userId = jsonObject.getString("userId");
                                    getRankIcon(jsonObject.getIntValue("userId") + "", appRankInfo);
                                    rankInfos.add(appRankInfo);
                                }
                                rankListAdapter.setListData(rankInfos);
                                rankListAdapter.setType(1);
                                rankListAdapter.notifyDataSetChanged();
                            } else {
                                listView.setPullLoadEnable(false);
                                if (rankInfos.size() <= 0) {
                                    listView.setPullRefreshEnable(true);
                                    rankEmpty.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            rank_myself.setVisibility(View.GONE);
                            listView.setPullLoadEnable(false);
                            if (rankInfos.size() <= 0) {
                                listView.setPullRefreshEnable(true);
                                rankEmpty.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private long getYestodayDate(String dimen) {
        long result = 0L;
        try {
            int type = Integer.valueOf(dimen);
            HashMap<String, Long> dbTime = DB.getDBTime(DB.TYPE_YESTERDAY);
            long start = dbTime.get("start");
            long end = dbTime.get("end");
            List<Operation> operationList = null;
            List<Call> callList = null;
            switch (type) {
                case 1:
                    operationList = Operation.selectOperationGroup(getContext(), start, end);
                    for (Operation operation : operationList) {
                        if (!Operation.SCREEN_ON.equals(operation.packageName)) {
                            result += operation.duration;
                        }
                    }
                    break;
                case 2:
                    callList = Call.selectCall(getContext(), start, end);
                    for (Call call : callList) {
                        result += call.duration * 1000L;
                    }
                    break;
                case 43:
                    operationList = Operation.selectOperation(getContext(), start, end, null, "社交");
                    for (Operation operation : operationList) {
                        result += operation.duration;
                    }
                    break;
                case 45:
                    operationList = Operation.selectOperation(getContext(), start, end, null, "购物");
                    for (Operation operation : operationList) {
                        result += operation.duration;
                    }
                    break;
                case 61:
                    operationList = Operation.selectOperation(getContext(), start, end, null, "影音");
                    for (Operation operation : operationList) {
                        result += operation.duration;
                    }
                    break;
                case 7:
                    operationList = Operation.selectOperation(getContext(), start, end, null, "棋牌天地,休闲娱乐,策略塔防,角色动作,飞行射击,速度激情,益智游戏,网络游戏,经营养成,体育竞技,儿童最爱");
                    for (Operation operation : operationList) {
                        result += operation.duration;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
        }
        return result / 1000L;
    }

    private void getRankIcon(String userid, final AppRankInfo appRankInfo) {
        HashMap<String, String> map = new HashMap<>();
        map.put("userId", userid);
        SP.cache(getContext(), Constants.GetRankIcon, map, System.currentTimeMillis() - Constants.ONE_HOUR * 7, new SP.OnCache() {
            @Override
            public void onCache(String cache) {
                if (cache == null) {
                    if (Utils.isNetworkAvailable(getActivity())) {
                        Toast.makeText(getActivity(), "网络异常，请稍后重试！", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        com.alibaba.fastjson.JSONObject object = com.alibaba.fastjson.JSONObject.parseObject(cache);
                        String resp = object.getString("resp");
                        if (resp.equals("000000")) {
                            String path = object.getString("phonePath");
                            String nickName = object.getString("phoneName");
                            if (!TextUtils.isEmpty(nickName)) {
                                appRankInfo.appName = nickName;
                            }
                            new RankIcon(appRankInfo).execute(path);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private int isfresh = 0;

    /**
     * 图片加载任务
     */
    class RankIcon extends AsyncTask<String, Void, Bitmap> {
        private AppRankInfo appRankInfo;

        public RankIcon(AppRankInfo appRankInfo) {
            this.appRankInfo = appRankInfo;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            if (TextUtils.isEmpty(params[0])) {
                return null;
            } else {
                return Utils.getBitmap(Constants.head_url + params[0].replace("\\", "/"));
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            isfresh++;
            appRankInfo.appIcon = bitmap;
            if (isfresh == rankInfos.size()) {
                rankListAdapter.notifyDataSetChanged();
            }
        }
    }

    //获取维度type
    private String getDimension(int index) {
        switch (index) {
            case 0:
                return "43";
            case 1:
                return "45";
            case 2:
                return "61";
            case 3:
                return "7";
            case 4:
                return "55";
            case 5:
                return "71";
            case 6:
                return "53";
            case 7:
                return "41";
            case 8:
                return "47";
            case 9:
                return "57";
            case 10:
                return "65";
            case 11:
                return "51";
            case 12:
                return "73";
            case 13:
                return "49";
            case 14:
                return "67";
            case 15:
                return "69";
            case 16:
                return "63";
            default:
                return "";
        }
    }

    private void setPageView() {
        addListView();
        listView.setAdapter(rankListAdapter);
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        flipper.addView(listView, 0);
    }

    private void addListView() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT);
        listView = new XListView(getActivity());
        listView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        listView.setLayoutParams(params);

        listView.setPullRefreshEnable(false);
        listView.setPullLoadEnable(true);
        listView.setXListViewListener(this);

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Log.d("test", "RankFragment parent=>:" + parent + ",view=>:" + view + ",position=>:" + position + ",id=>" + id);
                    if (type == 1) {
                        AppRankInfo rankInfo = (AppRankInfo) parent.getAdapter().getItem(position);
                        Intent intent = new Intent(getActivity(), WebHtmlActivity.class);
                        intent.putExtra("url", "home.html?beUserId=" + rankInfo.userId);
                        startActivity(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        view.findViewById(R.id.list_choose).setOnClickListener(onClickListener);
        view.findViewById(R.id.rank_share).setOnClickListener(onClickListener);
        view.findViewById(R.id.rank_choose_icon).setOnClickListener(onClickListener);
        rankChoose.setOnClickListener(onClickListener);
        rankDay.setOnClickListener(onClickListener);
        rankMonth.setOnClickListener(onClickListener);
    }


    //维度选择窗口
    private void getRankTitleView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View title = inflater.inflate(R.layout.popup_rank_title, null);
        popupWindow = new PopupWindow(title, LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        // 需要设置一下此参数，点击外边可消失
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // 设置点击窗口外边窗口消失
        popupWindow.setOutsideTouchable(true);
        // 设置此参数获得焦点，否则无法点击
        popupWindow.setFocusable(true);
        // 相对某个控件的位置（正左下方），无偏移
        if (!popupWindow.isShowing()) {
            popupWindow.showAsDropDown(scrollView, 45, 10);
        }

        GridView gv_title = (GridView) title.findViewById(R.id.gv_rank_title);
        GridViewAdapter adapter_title;
        if (type == 1) {
            adapter_title = new GridViewAdapter(getActivity(), Constants.rankOneself, false);
        } else {
            adapter_title = new GridViewAdapter(getActivity(), Constants.rankTitle, false);
        }
        adapter_title.setSelectPosition(selectLoc);
        gv_title.setAdapter(adapter_title);

        gv_title.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                popupWindow.dismiss();
                selectLoc = position;
                setScrollTitle(selectLoc);
                if (type == 1) {
                    getOneSelfData(String.valueOf(selectLoc + 1), String.valueOf(date));
                } else if (type == 2) {
                    getAppData(getDimension(selectLoc), "1", true);
                    loadMore = 1;
                }
            }
        });
    }

    private void setScrollTitle(final int position) {
        adapter.setSelectPosition(position);
        adapter.notifyDataSetChanged();
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                if (position > 2)
                    scrollView.scrollTo((int) ((position - 2) * 72.5 * density - 0.5f), 0);
                else
                    scrollView.scrollTo((int) (density - 0.5f), 0);
            }
        });
    }

    float density;

    /**
     * 设置GirdView参数，绑定数据
     */
    private void setGridView() {
        int size = 0;
        if (type == 1) {
            size = Constants.rankOneself.length;
        } else if (type == 2) {
            size = Constants.rankTitle.length;
        }
        int length = 70;
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        density = dm.density;
        int gridviewWidth = (int) (size * (length + 2.5) * density);
        int itemWidth = (int) (length * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridviewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        gridView.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
        gridView.setColumnWidth(itemWidth); // 设置列表项宽
        gridView.setHorizontalSpacing(5); // 设置列表项水平间距
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setNumColumns(size); // 设置列数量=列表集合数

        if (type == 1) {
            adapter = new GridViewAdapter(getActivity(), Constants.rankOneself, true);
        } else {
            adapter = new GridViewAdapter(getActivity(), Constants.rankTitle, true);
        }
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectLoc = position;
                setScrollTitle(selectLoc);
                if (type == 1) {
                    getOneSelfData(String.valueOf(selectLoc + 1), String.valueOf(date));
                } else if (type == 2) {
                    getAppData(getDimension(selectLoc), "1", true);
                    loadMore = 1;
                }
            }
        });
    }

    /**
     * GirdView 数据适配器
     */
    public class GridViewAdapter extends BaseAdapter {
        Context context;
        String[] list;
        private int selectPosition;
        private boolean flag;

        public GridViewAdapter(Context _context, String[] _list, boolean flag) {
            this.list = _list;
            this.context = _context;
            this.flag = flag;
        }

        public void setSelectPosition(int selectPosition) {
            this.selectPosition = selectPosition;
        }

        @Override
        public int getCount() {
            return list.length;
        }

        @Override
        public Object getItem(int position) {
            return list[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                holder = new Holder();
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                convertView = layoutInflater.inflate(R.layout.item_rank_title, null);
                holder.textView = (TextView) convertView.findViewById(R.id.rank_title);
                holder.line1 = convertView.findViewById(R.id.line1);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.textView.setText(list[position]);
            if (selectPosition == position) {
                holder.textView.setTextColor(getResources().getColor(R.color.diaryword));
            } else {
                holder.textView.setTextColor(Color.WHITE);
            }
            if (flag) {
                holder.line1.setVisibility(View.VISIBLE);
            } else {
                holder.line1.setVisibility(View.GONE);
            }
            return convertView;
        }

        class Holder {
            TextView textView;
            View line1;
        }
    }

    //滑动监听
    GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.OnGestureListener() {
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
            if (Math.abs(e1.getX() - e2.getX()) < Math.abs(e1.getY() - e2.getY())) {
                return false;
            }
            int length;
            if (type == 1) {
                length = Constants.rankOneself.length;
            } else {
                length = Constants.rankTitle.length;
            }

            if (e1.getX() - e2.getX() > 50) {
                // 向左滑
                if (selectLoc == length - 1) {
                    selectLoc = 0;
                } else {
                    selectLoc++;
                }
                setScrollTitle(selectLoc);
                addListView();
                //缓存数据
                if (type == 1) {
                    getOneSelfData(String.valueOf(selectLoc + 1), String.valueOf(date));
                } else {
                    getAppData(getDimension(selectLoc), "1", true);
                    loadMore = 1;
                }

                listView.setAdapter(rankListAdapter);
                gvFlag++;
                flipper.addView(listView, gvFlag);
                flipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_in));
                flipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_out));
                flipper.showNext();
                flipper.removeViewAt(0);
                return true;
            } else if (e1.getX() - e2.getX() < -50) {
                if (selectLoc == 0) {
                    selectLoc = length - 1;
                } else {
                    selectLoc--;
                }
                setScrollTitle(selectLoc);
                addListView();
                //缓存数据
                if (type == 1) {
                    getOneSelfData(String.valueOf(selectLoc + 1), String.valueOf(date));
                } else {
                    getAppData(getDimension(selectLoc), "1", true);
                    loadMore = 1;
                }

                listView.setAdapter(rankListAdapter);
                gvFlag++;
                flipper.addView(listView, gvFlag);
                flipper.setInAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_in));
                flipper.setOutAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_out));
                flipper.showPrevious();
                flipper.removeViewAt(0);
                return true;
            }
            return false;
        }
    });

    //刷新
    @Override
    public void onRefresh() {
        rankEmpty.setVisibility(View.GONE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (type == 1) {
                    getOneSelfData(String.valueOf(selectLoc + 1), String.valueOf(date));
                } else {
                    getAppData(getDimension(selectLoc), loadMore + "", true);
                }
                rankListAdapter.notifyDataSetChanged();
                listView.stopRefresh();
                listView.stopLoadMore();
                listView.setRefreshTime("刚刚");
            }
        }, 2000);
    }

    //加载
    @Override
    public void onLoadMore() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (type == 1) {
                    getOneSelfData(String.valueOf(selectLoc + 1), String.valueOf(date));
                } else {
                    loadMore += 10;
                    if (loadMore > 100) {
                        listView.setPullLoadEnable(false);
                    } else {
                        getAppData(getDimension(selectLoc), loadMore + "", false);
                    }
                }
                rankListAdapter.notifyDataSetChanged();
                listView.stopRefresh();
                listView.stopLoadMore();
                listView.setRefreshTime("刚刚");
            }
        }, 500);
    }

    ProgressDialog pd = null;
    Thread thread = null;
    String rankPath = Environment.getExternalStorageDirectory() + "/ZhangXin/ScreenShot/RankScreen.png";

    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.list_choose://维度选择
                    getRankTitleView();
                    break;
                case R.id.rank_share://排行榜分享
                    if (pd == null) {
                        pd = ProgressDialog.show(getActivity(), "", "分享图片生成中...", true);
                        pd.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                if (keyCode == KeyEvent.KEYCODE_BACK) {
                                    if (pd.isShowing()) {
                                        pd.dismiss();
                                    }
                                }
                                return false;
                            }
                        });
                    }
                    final Bitmap bitmap = takeScreenShot(getActivity());
                    if (thread == null) {
                        thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                File file = new File(Environment.getExternalStorageDirectory(), "/ZhangXin/ScreenShot");
                                if (!file.exists()) {
                                    file.mkdirs();
                                }
                                if (bitmap != null) {
                                    Utils.savePhotoToSDCard(bitmap, rankPath);
                                } else {
                                    Log.e("my_log", "截图失败!");
                                }
                                handler.sendEmptyMessage(0);
                            }
                        });
                        thread.start();
                    }
                    break;
                case R.id.rank_choose://排行榜选择
                    showChoose();
                    break;
                case R.id.rank_choose_icon://排行榜选择
                    showChoose();
                    break;
                case R.id.rank_day://日数据
                    date = 1;
                    rankDay.setBackgroundColor(getResources().getColor(R.color.rank_date_bg));
                    rankMonth.setBackgroundColor(getResources().getColor(R.color.transparent));
                    onLoadMore();
                    break;
                case R.id.rank_month://月数据
                    date = 2;
                    rankDay.setBackgroundColor(getResources().getColor(R.color.transparent));
                    rankMonth.setBackgroundColor(getResources().getColor(R.color.rank_date_bg));
                    onLoadMore();
                    break;
                case R.id.choose_day:
                    popupWindow.dismiss();
                    rankChoose.setText("个人排行榜");
                    rank_myself.setVisibility(View.VISIBLE);
                    ll_rank_date.setVisibility(View.VISIBLE);
                    selectLoc = 0;
                    type = 1;
                    setGridView();
                    rankListAdapter.setListData(null);
                    rankListAdapter.notifyDataSetChanged();
                    setScrollTitle(selectLoc);
                    getOneSelfData("1", "1");
                    break;
                case R.id.choose_week:
                    popupWindow.dismiss();
                    rank_myself.setVisibility(View.GONE);
                    ll_rank_date.setVisibility(View.GONE);
                    rankChoose.setText("应用排行榜");
                    selectLoc = 0;
                    type = 2;
                    loadMore = 1;
                    getAppData("43", "1", true);
                    setGridView();
                    setScrollTitle(selectLoc);
                    break;
                case R.id.diary_cover:
                    dialogCancel();
                    if (isCover && !isCover2) {
                        Toast.makeText(getActivity().getApplicationContext(), "分享内容生成中,请稍后再试！",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (isCover2) {
                        Toast.makeText(getActivity().getApplicationContext(), "分享失败,请稍后再试！",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    new NetDataGetter(getActivity().getApplicationContext()).dynamic(SP.getString(getActivity(), SP.USER_ID, "-1"),
                            "", servicePath, "", "", new NetManager.DataArray() {
                                @Override
                                public void getServiceData(org.json.JSONArray jsonArray) {
                                    try {
                                        org.json.JSONObject object = jsonArray.getJSONObject(0);
                                        String resp = object.getString("resp");
                                        if (resp.equals("000000")) {
                                            //分享成功
                                            Toast.makeText(getActivity(), "排行榜分享成功!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getActivity().getApplicationContext(), "发现分享失败,请稍后再试！",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } catch (org.json.JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Toast.makeText(getActivity().getApplicationContext(),
                                            "系统繁忙，发现分享失败!", Toast.LENGTH_SHORT).show();
                                }
                            });
                    break;
                case R.id.diary_qq:
                    dialogCancel();
                    if (isCover && !isCover2) {
                        Toast.makeText(getActivity().getApplicationContext(), "分享内容生成中,请稍后再试！",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (isCover2) {
                        Toast.makeText(getActivity().getApplicationContext(), "分享失败,请稍后再试！",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    final Bundle params = new Bundle();
                    params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
                    params.putString(QzoneShare.SHARE_TO_QQ_TITLE, "手机日记•分享");// 必填
                    // 必填,点击分享内容所需要的链接地址
                    params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, Constants.head_url + servicePath);
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(rankPath);
                    params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, arrayList);// 分享的图片，选填
                    MyApplication.mTencent.shareToQzone(getActivity(), params, new IUiListener() {
                        @Override
                        public void onError(UiError error) {
                            Toast.makeText(getActivity().getApplicationContext(), "QQ分享失败！", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onComplete(Object response) {
                            Toast.makeText(getActivity().getApplicationContext(), "日记本QQ分享成功！", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(getActivity().getApplicationContext(), "QQ分享取消！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case R.id.diary_wx:
                    dialogCancel();
                    MyApplication.flag = false;
                    MyApplication.wxType = 3;
                    Bitmap thumb;
                    WXImageObject imgObj;
                    thumb = BitmapFactory.decodeFile(rankPath);
                    imgObj = new WXImageObject();
                    imgObj.setImagePath(rankPath);

                    WXMediaMessage msg = new WXMediaMessage();
                    msg.mediaObject = imgObj;
                    Bitmap thumbBmp = Bitmap.createScaledBitmap(thumb, 150, 150, true);
                    thumb.recycle();
                    msg.thumbData = Utils.bmpByte(thumbBmp, true); // 设置缩略图

                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = Utils.buildTransaction("img");
                    req.message = msg;
                    req.scene = SendMessageToWX.Req.WXSceneTimeline;
                    boolean fla = MyApplication.api.sendReq(req);
                    if (!fla) {
                        Toast.makeText(getActivity(), "微信分享失败！", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.diary_wb:
                    dialogCancel();
                    Intent intent1 = new Intent(getActivity(), MyWeiBoShareActivity.class);
                    intent1.putExtra("content", "");
                    intent1.putExtra("sharePath", rankPath);
                    startActivityForResult(intent1, 66);
                    break;
                case R.id.diary_cancel:
                    dialog.dismiss();
                    break;
            }
        }
    };

    //显示个人或者应用排行榜
    private void showChoose() {
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
            popupWindow.showAsDropDown(rankChoose, -2, 8);
        }
        TextView onselfe = (TextView) view.findViewById(R.id.choose_day);
        onselfe.setText("个人排行榜");
        onselfe.setOnClickListener(onClickListener);
        TextView apps = (TextView) view.findViewById(R.id.choose_week);
        apps.setText("应用排行榜");
        apps.setOnClickListener(onClickListener);
        view.findViewById(R.id.choose_month).setVisibility(View.GONE);
        view.findViewById(R.id.line_rank).setVisibility(View.GONE);
    }

    private void dialogCancel() {
        dialog.dismiss();
        if (pd == null) {
            pd = ProgressDialog.show(getActivity(), "", "排行榜分享中...");
        }
        pd.show();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pd.dismiss();
                pd = null;
            }
        }, 1000);
    }

    AlertDialog dialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    if (thread != null) {
                        thread = null;
                    }
                    File f = new File(rankPath);
                    if (f.exists()) {
                        Utils.uploadFile(handler, f, Constants.PHOTO_UPLOAD, 2);
                    } else {
                        Toast.makeText(getActivity(), "排行榜图片分享失败!请稍后重试", Toast.LENGTH_SHORT).show();
                        Log.e("my_log", "排行榜截图失败!");
                    }
                    break;
                case 2:
                    if (pd != null) {
                        pd.dismiss();
                        pd = null;
                    }
                    if (msg.obj.toString() == null || msg.obj.toString().equals("")) {
                        if (Utils.isNetworkAvailable(getActivity().getApplicationContext())) {
                            Toast.makeText(getActivity().getApplicationContext(), "分享发现失败,请稍后再试！",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    "请先检查网络是否连接成功？", Toast.LENGTH_LONG).show();
                        }
                        return;
                    }
                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(msg.obj.toString());
                        org.json.JSONObject object = jsonArray.getJSONObject(0);
                        String resp = object.getString("resp");
                        String photoPath = object.getString("filePath");
                        if (resp.equals("000000")) {
                            isCover = false;
                            servicePath = photoPath;

                            View view = LayoutInflater.from(getActivity()).inflate(R.layout.diary_share_view, null);
                            view.findViewById(R.id.diary_cover).setOnClickListener(onClickListener);
                            view.findViewById(R.id.diary_qq).setOnClickListener(onClickListener);
                            view.findViewById(R.id.diary_wx).setOnClickListener(onClickListener);
                            view.findViewById(R.id.diary_wb).setOnClickListener(onClickListener);
                            view.findViewById(R.id.diary_cancel).setOnClickListener(onClickListener);
                            if (dialog == null) {
                                dialog = new AlertDialog.Builder(getActivity()).create();
                            }
                            dialog.show();
                            Window window = dialog.getWindow();
                            window.setGravity(Gravity.CENTER);
                            window.setContentView(view);
                            dialog.setCanceledOnTouchOutside(false);
                        } else {
                            isCover2 = true;
                        }
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 16:
                    if (msg.obj != null) {
                        byte[] data = (byte[]) msg.obj;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        if (bitmap != null) {
                            myself_icon.setImageBitmap(Utils.toRoundBitmap(bitmap));
                            Utils.setPortraitBitmap(getActivity(), bitmap);
                        }
                    } else {
                        Log.e("my_log", "头像加载失败！");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    // 获取指定Activity的截屏，保存到png文件
    private Bitmap takeScreenShot(Activity activity) {
        // View是你需要截图的View
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();

        // 获取状态栏高度
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = 0;

        // 获取屏幕长和高
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();
        // 去掉标题栏
        Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
        view.destroyDrawingCache();
        return b;
    }
}
