package com.rdcx.randian;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by Administrator on 2016/2/18 0018.
 * 空 Activity
 */
public class BlankActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MyApplication) getApplication()).actFinisih();
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
