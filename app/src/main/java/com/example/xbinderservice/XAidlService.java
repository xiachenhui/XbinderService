package com.example.xbinderservice;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

public class XAidlService extends Service {
    private static final String TAG = "XAidlService";
    private static final String TABLE_NAME = "User";


    private DataBaseHelper dataBaseHelper;
    SQLiteDatabase database;


    @Override
    public IBinder onBind(Intent intent) {
        dataBaseHelper = new DataBaseHelper(this, "User.db", null, 1);
        database = dataBaseHelper.getWritableDatabase();
        return iBinder;
    }

    private IBinder iBinder = new XAidl.Stub() {
        @Override
        public User login(User user) throws RemoteException {
            if (user != null && database != null) {
                User queryUser = null;
                String QUERY_USER = "select * from User where User.name = ? and User.password =?";
                Cursor cursor = database.rawQuery(QUERY_USER, new String[]{user.getName(), user.getPassword()});
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String password = cursor.getString(cursor.getColumnIndex("password"));
                    queryUser = new User(name, password);
                }
                cursor.close();
                return queryUser;
            }
            return null;
        }

        @Override
        public long register(String name, String password) throws RemoteException {
            String QUERY_USER = "select * from User where User.name = ? ";
            //String INSERT_USER = "insert  into User(name,password) values(" + name + "," + password + ")";
            Cursor cursor = database.rawQuery(QUERY_USER, new String[]{name});
            //已经有了，就提示注册失败
            if (cursor.moveToNext()) {
                cursor.close();
                return -1;
            } else {
                ContentValues values = new ContentValues();
                values.put("name", name);
                values.put("password", password);
                long insert = database.insert(TABLE_NAME, null, values);
                return insert;
            }

        }

        @Override
        public int resetPwd(String name, String oldPwd, String newPwd) throws RemoteException {
            String QUERY_USER = "select * from User where User.name = ?";
           // Cursor query = database.query(TABLE_NAME, new String[]{"name", "password"}, "name =? and passowrd =?", new String[]{name, oldPwd}, null, null, null);
            Cursor cursor = database.rawQuery(QUERY_USER, new String[]{name});
            //已经有了，就提示注册失败
            if (cursor.moveToNext()) {
                String userPassword = cursor.getString(cursor.getColumnIndex("password"));
                if (oldPwd.equals(userPassword)) {
                    //更新
                    ContentValues values = new ContentValues();
                    values.put("name", name);
                    values.put("password", newPwd);
                    int update = database.update(TABLE_NAME, values, "name = ?", new String[]{name});
                    return update;
                } else {
                    return 0;
                }

            } else {
                return 0;
            }

        }
    };

}
