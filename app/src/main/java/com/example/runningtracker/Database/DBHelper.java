package com.example.runningtracker.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
  public DBHelper(Context context, String name, CursorFactory factory, int version) {
    super(context, name, factory, version);
    Log.d("runningTracker", "DBHelper");
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    Log.d("runningTracker", "onCreate: db");
  }

  @Override
  public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    Log.d("runningTracker", "onUpgrade: db");
  }
}
