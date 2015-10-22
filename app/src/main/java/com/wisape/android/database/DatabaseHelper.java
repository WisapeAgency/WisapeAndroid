package com.wisape.android.database;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * @author LeiGuoting
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "data.db";

    private static final int VERSION_1_0_0 = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION_1_0_0);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource source) {
        try{
            TableUtils.createTableIfNotExists(source, StoryEntity.class);
            TableUtils.createTableIfNotExists(source, UserMessageEntity.class);
            TableUtils.createTableIfNotExists(source, UserActivityEntity.class);
            TableUtils.createTableIfNotExists(source, StoryTemplateEntity.class);
            TableUtils.createTableIfNotExists(source, StoryMusicEntity.class);
            TableUtils.createTableIfNotExists(source, StoryMusicTypeEntity.class);
        }catch (SQLException e){
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        //do nothing
    }
}
