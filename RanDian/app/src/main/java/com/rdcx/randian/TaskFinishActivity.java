package com.rdcx.randian;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.rdcx.tools.TaskTools;

/**
 * Created by Administrator on 2016/5/9 0009.
 * <p>
 * 显示用户完成程度
 */
public class TaskFinishActivity extends Activity implements View.OnClickListener {

    private String title;
    private int id;
    private String taskTitle;
    private String content;
    private boolean finish;
    private String finishStr;
    private String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_finish);

        initParams();
        initViews();
    }

    private void initParams() {
        Intent intent = getIntent();
        if (intent == null) {
            title = "平凡的世界";
            id = 0;
            taskTitle = "";
            content = "";
            finish = false;
        } else {
            title = intent.getStringExtra("title");
            id = intent.getIntExtra("id", 0);
            taskTitle = intent.getStringExtra("taskTitle");
            content = intent.getStringExtra("content");
            finish = intent.getBooleanExtra("finish", false);
        }
        if (!finish) {
            msg = TaskTools.checkTask(this, id);
        }
        if (msg == null) {
            finishStr = "当前任务已完成";
            msg = content;
            setResult(id);
        } else {
            finishStr = "当前任务尚未完成";
        }
    }

    private void initViews() {

        ((TextView) findViewById(R.id.task_title_tv)).setText(title);
        ((TextView) findViewById(R.id.task_finish_title)).setText(taskTitle);
        ((TextView) findViewById(R.id.task_finish_content)).setText(content);
        ((TextView) findViewById(R.id.task_finish_state)).setText(finishStr);
        ((TextView) findViewById(R.id.task_finish_msg)).setText(msg);

        findViewById(R.id.set_back).setOnClickListener(this);
        
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_back:
                finish();
                break;
            default:
                break;
        }
    }

}
