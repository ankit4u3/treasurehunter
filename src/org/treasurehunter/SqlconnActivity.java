
package org.treasurehunter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBAdapter {

    private static final String TAG = "DBAdapter";

    // The Android's default system name of your application database and
    // tables.
    private static final String DATABASE_NAME = "gps";

    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String tblocation = "create table tblocation (_id integer primary key autoincrement, "
            + "latitude text not null, longitude text not null, lock text not null);";

    private static final String tbgps = "create table tbgps (_id integer primary key autoincrement, "
            + "P_GID text not null, P_UserLocationLatitude text not null ,P_UserLocationLongitude  text not null,"
            + "P_Point text not null , P_Video text not null , P_Image text not null , P_Description text not null , P_QuestionTpye text not null ,"
            + "P_Question text not null , P_Answer text not null , P_AnsOption1 text not null , P_AnsOption2 text not null ,"
            + "P_AnsOption3 text not null , P_AnsOption4 text not null , P_LocationClue text not null , P_LID text not null , P_PointIcon text not null , P_isDelete text not null);";

    private final Context context;

    // singleton/ single instance reference of database instance
    private DatabaseHelper1 DBHelper;

    // reference of database

    private SQLiteDatabase db;

    public DBAdapter(Context ctx) {
        this.context = ctx;
        DBHelper = new DatabaseHelper1(context);
    }

    private static class DatabaseHelper1 extends SQLiteOpenHelper {
        DatabaseHelper1(Context context) {
            // Create Data Base
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {

                db.execSQL(tbgps);
                db.execSQL(tblocation);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // do update - if database already exist
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, oldVersion + " to " + newVersion + ", which will destroy all old data");
            // Drop old table on update time
            db.execSQL("DROP TABLE IF EXISTS tbgps ");

            // Re-Create DataBase
            onCreate(db);
        }
    }

    public DBAdapter open() throws SQLException {
        // Open Data Base Connection
        db = DBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        // Close Data Base Connection
        DBHelper.close();
    }

    //
    // public long insertLocation(String latitude,String longitude)
    // {
    // ContentValues values=new ContentValues();
    // values.put("latitude", latitude);
    // values.put("longitude", longitude);
    // return db.insert(tblocation, null, values);
    // }
    // insertAccount Method ,Account info insert into Account table
    public long insertAccount(String Gid, String Latitude, String Longitude, String point,
            String video, String image, String desc, String Qtype, String qestion, String Answer,
            String AnsOption1, String AnsOption2, String AnsOption3, String AnsOption4,
            String locationClue, String Lid, String pointicon, String isDelete) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("P_GID", Gid);
        initialValues.put("P_UserLocationLatitude", Latitude);
        initialValues.put("P_UserLocationLongitude", Longitude);
        initialValues.put("P_Point", point);
        initialValues.put("P_Video", video);
        initialValues.put("P_Image", image);
        initialValues.put("P_Description", desc);
        initialValues.put("P_QuestionTpye", Qtype);
        initialValues.put("P_Question", qestion);
        initialValues.put("P_Answer", Answer);
        initialValues.put("P_AnsOption1", AnsOption1);
        initialValues.put("P_AnsOption2", AnsOption2);
        initialValues.put("P_AnsOption3", AnsOption3);
        initialValues.put("P_AnsOption4", AnsOption4);
        initialValues.put("P_LocationClue", locationClue);
        initialValues.put("P_LID", Lid);
        initialValues.put("P_PointIcon", pointicon);
        initialValues.put("P_isDelete", isDelete);

        return db.insert("tbgps", null, initialValues);
    }

    public Cursor get_treasure(String id) {

        String query = null;
        query = "select * from tbgps where P_GID=" + id + " and P_isDelete=0";
        Cursor cur = db.rawQuery(query, null);
        return cur;

    }

    public Cursor M_GetGID(String dbquery) {

        // String query=null;
        // String dbquery1="select * from tbinfo where _id='"+_ID+"'";
        Cursor cur = db.rawQuery(dbquery, null);

        return cur;
    }

    public Cursor M_CurLocatoin() {
        String query = "select * from tblocation";
        Cursor cur = db.rawQuery(query, null);
        return cur;
    }

    public Cursor M_chklocation(double _lat, double _lng, String Gid) {

        // String query=null;

        // String
        // dbquery1="select * from tbgps where P_isDelete='0' and   substr(P_UserLocationLatitude,0,6)='"+String.valueOf(_lat).substring(0,6)+"' and substr(P_UserLocationLongitude ,0,6 )='"+String.valueOf(_lng).substring(0,6)+"'  and P_Gid='"+Gid+"'";
        String dbquery1 = "select  *  from tbgps where  P_isDelete='0' and  P_Gid='" + Gid + "'";
        //
        return db.rawQuery(dbquery1, null);
    }

    // select SUBSTR(P_UserLocationLatitude,0,8 ) as P_UserLocationLatitude ,
    // SUBSTR(P_UserLocationLongitude ,0,8 ) as P_UserLocationLongitude from
    // tbgps
    // where P_isDelete='0' and SUBSTR(P_UserLocationLatitude,0,8 ) ='41.39648'
    // and SUBSTR(P_UserLocationLongitude ,0,8 )='2.168959' and P_Gid
    // insertAccount Method ,Account info insert into Account table

    public void updatelocStatus(String _qry) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("P_isDelete", "1");

        db.update("tbgps", initialValues, _qry, null);

    }

    public Cursor GetMyTestRsult() {

        String query = null;
        query = "select * from tbgps";
        Cursor cur = db.rawQuery(query, null);

        return cur;

    }

    public Cursor GetCurrentLocation() {

        String query = null;
        query = "select * from tblocation";
        Cursor cur = db.rawQuery(query, null);

        return cur;

    }

    // deleteContact Method ,Delete record from Contact tabe on contactid base
    public boolean deleteHistory() {

        return db.delete("tbgps", null, null) > 0;
    }

    public long insertLocation(String valueOf_lat, String valueOf2_lng, String string) {
        // TODO Auto-generated method stub
        ContentValues values = new ContentValues();
        values.put("latitude", valueOf_lat);
        values.put("longitude", valueOf2_lng);
        values.put("lock", string);
        return db.insert(tblocation, null, values);
    }

}
