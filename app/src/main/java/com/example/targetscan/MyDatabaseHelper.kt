package com.example.targetscan

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDatabaseHelper (val context: Context,name:String,version:Int):SQLiteOpenHelper(context,name,null,version) {
    private val create = "create table ShootingRecords (" +
            "filename tinytext primary key,"+
            "targetNum tinyint,"+
            "scores tinytext,"+
            "discipline tinyint,"+
            "year tinyint,"+
            "month tinyint,"+
            "day tinyint,"+
            "comment tinytext,"+
            "vectors tinytext)"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(create)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVer: Int, newVer: Int) {
        db?.execSQL("drop table if exists ShootingRecords")
        onCreate(db)
    }
}