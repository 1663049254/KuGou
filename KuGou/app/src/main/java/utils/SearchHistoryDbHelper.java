package utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SearchHistoryDbHelper {
    private Context mContext;
    public SearchHistoryDbHelper(Context context){this.mContext=context;}
    /*public void openOrCreateDatabase(String path){
        new MySQLiteOpenHelper(mContext,"history.db",null,SQLiteDatabase.OPEN_READONLY);
    }*/
    public void insertDatabase(String str){
        //如果已存在则不插入
        List<String> list=queryDatabase();
        if(list.contains(str)==false){
            //如果历史记录大于10则删除
            if(list.size()>=10)
                deleteDatabase();
            MySQLiteOpenHelper helper = new MySQLiteOpenHelper(mContext,"history.db",null,1);
            SQLiteDatabase db=helper.getWritableDatabase();
            String sql = "insert into history values(null,'" + str + "')";
            db.execSQL(sql);
            db.close();
        }
    }
    public void deleteDatabase(){
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(mContext,"history.db",null,1);
        SQLiteDatabase db=helper.getWritableDatabase();
        String sql="delete from history";
        db.execSQL(sql);
        db.close();
    }
    public List<String> queryDatabase(){
        MySQLiteOpenHelper helper = new MySQLiteOpenHelper(mContext,"history.db",null,1);
        SQLiteDatabase db=helper.getReadableDatabase();
        String sql="select * from history order by id desc";
        Cursor cursor=db.rawQuery(sql,null);
        List<String> list=new ArrayList<String>();
        while(cursor.moveToNext()){
            //System.out.println(cursor.getString(1));
            list.add(cursor.getString(1));
        }
        return list;
    }
}
