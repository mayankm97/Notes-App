package com.example.notesappsql.ui.theme

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.StringBuilder

class DBHelper(context: Context): SQLiteOpenHelper(context, "Notedetails", null, 1) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table Notedetails(id INTEGER primary key, title TEXT, subtitle TEXT, content TEXT)")
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("drop table if exists Notedetails")
    }
    override fun onDowngrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("drop Table if exists Notedetails")
    }

    // create
    fun insertNote(id: Int, title: String, subtitle: String, content: String): Boolean {
        val db: SQLiteDatabase = writableDatabase
        val contentValues = ContentValues()
        contentValues.apply {
            put("id", id)
            put("title", title)
            put("subtitle", subtitle)
            put("content", content)
        }

        val result = db.insert("Notedetails", null, contentValues)
        if (result == (-1).toLong()) return false
        return true
    }

    fun deleteNote(id: Int): Boolean {
        val db: SQLiteDatabase = writableDatabase
        val cursor = db.query("Notedetails", null, "id=?", arrayOf(id.toString()), null, null, null)
        if (cursor.count > 0) {
            val result = db.delete("Notedetails", "id=?", arrayOf(id.toString()))
            if (result == -1) return false
            else return true
        } else return false
    }

    // update
    fun updateNote(id: Int, content: String): Boolean {
        val db: SQLiteDatabase = writableDatabase
        val contentValues = ContentValues()
        contentValues.apply {
            put("id", id)
            put("content", content)
        }
        val cursor = db.rawQuery("select * from Notedetails where id = ?", arrayOf(id.toString()))
        if (cursor.count > 0) {
            val result = db.update("Notedetails", contentValues, "id = ?", arrayOf(id.toString()))
            if (result == -1) return false
            return true
        } else {
            return false
        }
    }
    fun getDataByCustomId(id: Int): Cursor? {
        val db = this.readableDatabase
        return db.rawQuery("select * from Notedetails where id = ?", arrayOf(id.toString()))
    }

    // read
    fun getData(): Cursor? {
        val db: SQLiteDatabase = readableDatabase
        val cursor: Cursor = db.rawQuery("select * from Notedetails", null)
        return cursor
    }
    fun getMaxId(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT MAX(id) FROM Notedetails", null)
        var maxId = 0
        if (cursor.moveToFirst()) {
            maxId = cursor.getInt(0)
        }
        cursor.close()
        return maxId
    }

}