package com.rdcx.style;

import android.graphics.Color;
import android.os.Parcel;
import android.text.TextPaint;
import android.text.style.StyleSpan;

import com.rdcx.randian.R;

public class BoldStyleSpan extends StyleSpan {

    public BoldStyleSpan(int style) {
        super(style);
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public int getSpanTypeId() {
        return super.getSpanTypeId();
    }

    @Override
    public int getStyle() {
        return super.getStyle();
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setFakeBoldText(true);
        ds.setColor(Color.WHITE);
        ds.setTextSize(ds.getTextSize() * 1.1F);
        super.updateDrawState(ds);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        paint.setFakeBoldText(true);
        super.updateMeasureState(paint);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }
}