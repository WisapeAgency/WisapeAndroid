package com.wisape.android.database;

import com.j256.ormlite.field.DatabaseField;

/**
 * Created by LeiGuoting on 9/7/15.
 */

public abstract class BaseEntity {
    @DatabaseField(generatedId = true, id=true)
    public long id;

    @DatabaseField()
    public String reserved;

    @DatabaseField()
    public int reservedInt;

    @DatabaseField()
    public long createAt;

    @DatabaseField()
    public long updateAt;
}
