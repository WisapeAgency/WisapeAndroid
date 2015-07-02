package com.wisape.android.model;

import android.os.Parcelable;

import java.util.Map;

/**
 * Created by LeiGuoting on 2/7/15.
 */
public abstract class AttributeInfo extends BaseInfo implements Parcelable{
    public abstract Map<String, String> convert();
}
