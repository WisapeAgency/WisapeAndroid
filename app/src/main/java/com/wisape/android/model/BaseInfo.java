package com.wisape.android.model;

import android.os.Parcelable;

import com.google.gson.Gson;

/**
 * BaseEntity
 * Created by Xugm on 15/6/16.
 */
public abstract class BaseInfo implements Parcelable {

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}