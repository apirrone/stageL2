package com.example.bsauzet.testnfc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.PublicKey;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Antoine on 29/05/2015.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABSE_NAME = "database";

    public static final String TABLE_USERS = "users";
    private static final String KEY_USERS_ID = "id";
    private static final String KEY_USERS_PUBLICKEY = "publicKey";
    private static final String KEY_USERS_NAME = "name";


    private static final String TABLE_MESSAGES = "messages";
    private static final String KEY_MESSAGES_ID = "id";
    private static final String KEY_MESSAGES_UUID = "uuid";
    private static final String KEY_MESSAGES_IDSOURCE = "idSource";
    private static final String KEY_MESSAGES_IDDEST = "idDest";
    private static final String KEY_MESSAGES_CONTENT = "content";

    private static final String[] COLUMNS_USERS = {KEY_USERS_ID, KEY_USERS_PUBLICKEY, KEY_USERS_NAME};
    private static final String[] COLUMNS_MESSAGES = {KEY_MESSAGES_ID, KEY_MESSAGES_UUID, KEY_MESSAGES_IDSOURCE, KEY_MESSAGES_IDDEST, KEY_MESSAGES_CONTENT};


    public SQLiteHelper(Context context){
        super(context, DATABSE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(null, "Create base");
        String CREATE_USERS_TABLE = "CREATE TABLE users ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "publicKey TEXT )";

        String CREATE_MESSAGES_TABLE = "CREATE TABLE messages ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid TEXT, " +
                "idSource TEXT, " +
                "idDest TEXT, " +
                "content TEXT )";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    public int numberOfUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        return (int)DatabaseUtils.queryNumEntries(db, "users");

    }

    public void addUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USERS_NAME, user.getName());
        values.put(KEY_USERS_PUBLICKEY, user.getPublicKey());

        db.insert(TABLE_USERS, null, values);

        db.close();
    }

    public User getUserByName(String name){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_USERS,
                        COLUMNS_USERS,
                        "name = ?",
                        new String[]{name},
                        null,
                        null,
                        null,
                        null);

        User user = null;
        if(cursor != null)
            if(cursor.moveToFirst())
                user = new User(cursor.getString(1), cursor.getString(2));

        return user;
    }

    public User getUserByPublicKey(String publicKey){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_USERS,
                        COLUMNS_USERS,
                        "publicKey = ?",
                        new String[]{publicKey},
                        null,
                        null,
                        null,
                        null);

        User user = null;
        if(cursor != null)
            if(cursor.moveToFirst())
                user = new User(cursor.getString(1), cursor.getString(2));

        return user;
    }

    public List<User> getAllUsers(){
        List<User> users = new LinkedList<User>();

        String query = "SELECT * FROM " + TABLE_USERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        User user = null;

        if(cursor.moveToFirst())
            do{
                user = new User(cursor.getString(1), cursor.getString(2));
                users.add(user);
            }while(cursor.moveToNext());


        return users;
    }

    public void deleteUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_USERS,
                KEY_USERS_PUBLICKEY + " = ?",
                new String[]{String.valueOf(user.getPublicKey())});

        db.close();
    }

    public boolean userExists(String publicKey, String name){
        SQLiteDatabase db = this.getReadableDatabase();
        if(getUserByName(name) != null || getUserByPublicKey(publicKey) != null)
            return true;
        else
            return false;
    }

    public void addMessage(Message message){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGES_UUID, message.getUuid());
        values.put(KEY_MESSAGES_IDSOURCE, message.getPublicKeySource());
        values.put(KEY_MESSAGES_IDDEST, message.getPublicKeyDest());
        values.put(KEY_MESSAGES_CONTENT, message.getContent());

        db.insert(TABLE_MESSAGES, null, values);

        db.close();
    }

    public Message getMessage(String uuid){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_MESSAGES,
                        COLUMNS_MESSAGES,
                        "uuid = ?",
                        new String[] {uuid},
                        null,
                        null,
                        null,
                        null);

        if(cursor != null)
            cursor.moveToFirst();

        Message message = new Message(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        return message;
    }


    public List<Message> getAllMessages(){
        List<Message> messages = new LinkedList<Message>();

        String query = "SELECT * FROM " + TABLE_MESSAGES;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        Message message = null;

        if(cursor.moveToFirst())
            do{
                message = new Message(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
                messages.add(message);
            }while(cursor.moveToNext());


        return messages;
    }

    public void deleteMessage(Message message){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_MESSAGES,
                KEY_MESSAGES_UUID+" = ?",
                new String[] {String.valueOf(message.getUuid())});

        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS database");

        this.onCreate(db);
    }

    public void deleteAllMessages(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS messages");
    }

    public void deleteAllUsers(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS users");
    }
}
