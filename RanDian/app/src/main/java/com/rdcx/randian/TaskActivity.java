package com.rdcx.randian;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.rdcx.tools.SP;
import com.rdcx.tools.TaskTools;
import com.rdcx.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private String title;
    private int type;

    private TextView taskTitleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        initParams();

        View view = findViewById(R.id.set_back);
        if (view != null) {
            view.setOnClickListener(this);
        }
        view = findViewById(R.id.task_bulb);
        if (view != null) {
            view.setOnClickListener(this);
        }

        handleTaskList();
        show();
    }

    /**
     * 设置跳转至该Activity带来的参数以及默认值
     * title ：任务的分类名称，显示在顶部
     * type ： 任务的类型，可能的取值有 1,2,3
     */
    private void initParams() {
        Intent intent = getIntent();
        if (intent != null) {
            title = getIntent().getStringExtra("title");
            type = getIntent().getIntExtra("type", 1);
        } else {
            title = "平凡的人生";
            type = 1;
        }
        taskTitleTv = (TextView) findViewById(R.id.task_title_tv);
        taskTitleTv.setText(title);
    }

    private String taskCompletedStr = null;

    private boolean isTaskCompleted(int taskId) {
        if (taskCompletedStr == null) {
            taskCompletedStr = SP.getString(this, SP.TASK_USER_COMPLETED + SP.getString(this, SP.USER_ID, ""), "");
        }
        return taskCompletedStr.startsWith(taskId + ",") || taskCompletedStr.indexOf("," + taskId + ",") > 0;
    }

    private void writeTaskCompletedStr(int taskId) {
        if (taskId > 0) {
            taskCompletedStr = taskCompletedStr == null ? taskId + "," : taskCompletedStr + taskId + ",";
        }
    }

    private com.alibaba.fastjson.JSONObject taskStatusJSON = null;

    private int getTaskStatus(int taskId) {
        if (taskStatusJSON == null) {
            String taskStatus = SP.getString(this, SP.TASK_USER_STATUS + SP.getString(this, SP.USER_ID, ""), "{}");
            taskStatusJSON = com.alibaba.fastjson.JSONObject.parseObject(taskStatus);
        }
        Integer status = taskStatusJSON.getInteger(String.valueOf(taskId));
        return status == null ? -1 : status;
    }

    private void setTaskStatus(int taskId, int status) {
        if (taskId > 0) {
            taskStatusJSON.put(String.valueOf(taskId), status);
        }
    }

    /**
     * 用来保存所有任务清单。
     */
    private List<Map<String, Object>> allTaskList = null;

    /**
     * 初始化 {@link #allTaskList} 并返回。
     *
     * @return {@link #allTaskList}
     */
    private List<Map<String, Object>> getAllTaskList() {
        if (allTaskList == null) {
            allTaskList = new ArrayList<>();

            String str = SP.cache(this, Constants.GET_TASK_BY_ALL, null, System.currentTimeMillis());
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(str);
            com.alibaba.fastjson.JSONArray modelList = jsonObject.getJSONArray("modelList");
            for (int i = 0, len = modelList.size(); i < len; i++) {
                com.alibaba.fastjson.JSONObject model = modelList.getJSONObject(i);
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("id", model.getInteger("id"));
                taskMap.put("title", model.getString("title"));
                taskMap.put("content", model.getString("content"));
                taskMap.put("type", model.getString("type"));
                taskMap.put("groupId", model.getInteger("groupId"));
                taskMap.put("camera", model.getIntValue("camera"));
                allTaskList.add(taskMap);
            }

        }
        return allTaskList;
    }

    private String handleNickName(String str, String nickName) {
        if (str == null) {
            return "";
        }
        if (nickName == null) {
            return str;
        }
        return str.replaceAll("NOTE2", "“" + nickName + "”");
    }

    /**
     * 处理 {@link #allTaskList} 中保存着的所有任务清单中的数据
     * 将任务清单的数据变为 ListView 可以直接显示的数据
     */
    private void handleTaskList() {
        List<Map<String, Object>> taskList = getAllTaskList();
        String nickName = SP.getString(this, "nickName", "手机");
        for (int i = 0, len = taskList.size(); i < len; i++) {
            Map<String, Object> map = taskList.get(i);
            int id = (int) map.get("id");
            String title = handleNickName((String) map.get("title"), nickName);
            String content = handleNickName((String) map.get("content"), nickName);
            String description = "";
            if (isTaskCompleted(id)) {
                if (title != null && title.length() > 0) {
                    description = "<big><font color=\"#00229bff\">" + title + "</font></big><br></br>";
                }
                description += "<font color=\"#00229bff\">" + content + "</font>";
                map.put("finish", R.mipmap.task_finish);
                if ("2".equals(map.get("type"))) {
                    map.put("more", R.mipmap.task_finish_more);
                } else {
                    map.put("more", R.drawable.task_none);
                }
            } else {
                if (title != null && title.length() > 0) {
                    description = "<big><font color=\"#000000\">" + title + "</font></big><br></br>";
                }
                description += content;
                map.put("finish", R.mipmap.task_not_finish);
                if ("2".equals(map.get("type"))) {
                    map.put("more", R.mipmap.task_not_finish_more);
                    int status = getTaskStatus(id);
                    if (status == 0) {
                        map.put("finish", R.mipmap.task_checking);
                    } else if (status == 2) {
                        map.put("finish", R.mipmap.task_reject);
                    }
                } else {
                    map.put("more", R.drawable.task_none);
                }
            }
            map.put("description", Html.fromHtml(description));
        }
    }

    /**
     * 将通过{@link #handleTaskList()}处理过的数据显示出来
     */
    private void show() {
        ListView listView = (ListView) findViewById(R.id.task_list);
        if (listView.getTag() == null) {
            List<Map<String, Object>> list = new ArrayList<>();
            listView.setTag(list);
            SimpleAdapter simpleAdapter = new SimpleAdapter(this, list, R.layout.fragment_task_item, new String[]{"id", "description", "finish", "more"}, new int[]{R.id.task_item_id, R.id.task_item_description, R.id.task_item_finish, R.id.task_item_more});
            simpleAdapter.setViewBinder(new SimpleAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view, Object data, String textRepresentation) {
                    if (view.getId() == R.id.task_item_description) {
                        try {
                            ((TextView) view).setText((CharSequence) data);
                            return true;
                        } catch (Exception e) {
                            return false;
                        }
                    }
                    return false;
                }
            });
            listView.setAdapter(simpleAdapter);
            listView.setOnItemClickListener(this);
        }
        List<Map<String, Object>> list = (List<Map<String, Object>>) listView.getTag();
        list.clear();
        int finishCount = 0;
        for (Map<String, Object> map : getAllTaskList()) {
            if (map.get("groupId") != null && type == (int) map.get("groupId")) {
                if ((int) map.get("finish") == R.mipmap.task_finish) {
                    list.add(finishCount++, map);
                } else {
                    list.add(map);
                }
            }
        }
        ((SimpleAdapter) listView.getAdapter()).notifyDataSetChanged();
        taskTitleTv.setText(Html.fromHtml("<span>" + title + "</span><span>(已完成 " + finishCount + " 个)</span>"));
    }

    private static final int CHECK_TASK_FINISH_ACTIVITY = 10;

    private static final int CHECK_TASK_UPLOAD_ACTIVITY = 11;

    /**
     * 列表点击时触发该事件
     * 通过父控件所带数据，取得被点击的View的数据，再跳转到 {@link TaskUploadActivity}
     *
     * @param parent   父控件
     * @param view     被点击的控件
     * @param position 位置
     * @param id       ID
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            boolean stopAllTaskComplete = SP.getBoolean(this, SP.getUserIdKey(this, SP.STOP_ALL_TASK_COMPLETE), false);
            if (stopAllTaskComplete) {
                TaskTools.showStagetTask(this);
                return;
            }
            Object tag = parent.getTag();
            if (tag == null) {
                return;
            }
            List<Map<String, Object>> list = (List<Map<String, Object>>) tag;
            Map<String, Object> map = list.get(position);
            String nickName = SP.getString(this, "nickName", "手机");
            String title2 = handleNickName((String) map.get("title"), nickName);
            String content = handleNickName((String) map.get("content"), nickName);
            if ("1".equals(map.get("type"))) {
                Intent intent = new Intent(this, TaskFinishActivity.class);
                intent.putExtra("id", (int) map.get("id"));
                intent.putExtra("title", title);
                intent.putExtra("taskTitle", title2);
                intent.putExtra("content", (String) map.get("content"));
                intent.putExtra("finish", isTaskCompleted((int) map.get("id")));
                startActivityForResult(intent, CHECK_TASK_FINISH_ACTIVITY);
            } else if ("2".equals(map.get("type"))) {
                Intent intent = new Intent(this, TaskUploadActivity.class);
                intent.putExtra("content", content);
                intent.putExtra("id", (int) map.get("id"));
                intent.putExtra("camera", (int) map.get("camera"));
                startActivityForResult(intent, CHECK_TASK_UPLOAD_ACTIVITY);
            }
        } catch (Exception e) {
            Log.e("test", "TaskActivity onItemClick cause an Exception=>:", e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_back:
                finish();
                break;
            case R.id.task_bulb:
                Intent intent = new Intent(this, WebHtmlActivity.class);
                intent.putExtra("url", "raiders.html");
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode > 0) {
            for (Map<String, Object> map : getAllTaskList()) {
                if ((int) map.get("id") == resultCode) {
                    if (requestCode == CHECK_TASK_FINISH_ACTIVITY) {
                        writeTaskCompletedStr(resultCode);
                    } else if (requestCode == CHECK_TASK_UPLOAD_ACTIVITY) {
                        setTaskStatus(resultCode, 0);
                    }
                    handleTaskList();
                    show();
                    break;
                }
            }
        }
    }

}