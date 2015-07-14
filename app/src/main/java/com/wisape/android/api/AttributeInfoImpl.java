package com.wisape.android.api;

import android.os.Parcel;

import com.wisape.android.model.AttributeInfo;

import java.util.Map;

/**
 * Created by LeiGuoting on 14/7/15.
 */
public class AttributeInfoImpl extends AttributeInfo {

    @Override
    protected void onConvert(Map<String, String> params) {
        //do nothing
    }

    @Override
    protected int acquireAttributeNumber() {
        return 0;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public AttributeInfoImpl() {
    }

    protected AttributeInfoImpl(Parcel in) {
    }

    public static final Creator<AttributeInfoImpl> CREATOR = new Creator<AttributeInfoImpl>() {
        public AttributeInfoImpl createFromParcel(Parcel source) {
            return new AttributeInfoImpl(source);
        }

        public AttributeInfoImpl[] newArray(int size) {
            return new AttributeInfoImpl[size];
        }
    };
}
