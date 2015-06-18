package com.example.bsauzet.testnfc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.LinkedList;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

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
    private static final String KEY_MESSAGES_TIMEOUT ="timeout";
    private static final String KEY_MESSAGES_SENT = "sent";
    private static final String KEY_MESSAGES_DATE = "date";


    private static final String TABLE_CHAT = "chat";
    private static final String KEY_CHAT_ID = "id";
    private static final String KEY_CHAT_UUID = "uuid";
    private static final String KEY_CHAT_IDSOURCE = "idSource";
    private static final String KEY_CHAT_IDDEST = "idDest";
    private static final String KEY_CHAT_CONTENT = "content";
    private static final String KEY_CHAT_DATE = "date";

    private static final String TABLE_SIGNAL = "signal";
    private static final String KEY_SIGNAL_UUID = "uuid";
    private static final String KEY_SIGNAL_DATE = "date";

    private static final String[] COLUMNS_USERS = {KEY_USERS_ID, KEY_USERS_PUBLICKEY, KEY_USERS_NAME};
    private static final String[] COLUMNS_MESSAGES = {KEY_MESSAGES_ID, KEY_MESSAGES_UUID, KEY_MESSAGES_CONTENT, KEY_MESSAGES_IDSOURCE, KEY_MESSAGES_IDDEST, KEY_MESSAGES_TIMEOUT, KEY_MESSAGES_SENT, KEY_MESSAGES_DATE};
    private static final String[] COLUMNS_CHAT = {KEY_CHAT_ID, KEY_CHAT_UUID, KEY_CHAT_CONTENT, KEY_CHAT_IDSOURCE, KEY_CHAT_IDDEST, KEY_CHAT_DATE};
    private static final String[] COLUMNS_SIGNAL = {KEY_SIGNAL_UUID, KEY_SIGNAL_DATE};

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
                "content BLOB, " +
                "idSource TEXT, " +
                "idDest TEXT, " +
                "timeout REAL, " +
                "sent INTEGER, " +
                "date REAL)";

        String CREATE_CHAT_TABLE = "CREATE TABLE chat ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid TEXT, " +
                "content BLOB, " +
                "idSource TEXT, " +
                "idDest TEXT, " +
                "date REAL) " ;

        String CREATE_SIGNAL_TABLE = "CREATE TABLE signal ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uuid TEXT, " +
                "date REAL )";

        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_MESSAGES_TABLE);
        db.execSQL(CREATE_CHAT_TABLE);
        db.execSQL(CREATE_SIGNAL_TABLE);
    }

    public int numberOfUsers(){
        SQLiteDatabase db =this.getReadableDatabase();
        return (int)DatabaseUtils.queryNumEntries(db, "users");

    }

    public void addUser(User user){

        SQLiteDatabase db =this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USERS_NAME, user.getName());
        values.put(KEY_USERS_PUBLICKEY, user.getPublicKey());

        db.insert(TABLE_USERS, null, values);

        db.close();
    }

    public User getUserByName(String name){
        SQLiteDatabase db =this.getReadableDatabase();

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
                user = new User(cursor.getString(2), cursor.getString(1));

        cursor.close();
        db.close();
        return user;
    }

    public User getUserByPublicKey(String publicKey){
        SQLiteDatabase db =this.getReadableDatabase();
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
                user = new User(cursor.getString(2), cursor.getString(1));

        cursor.close();
        db.close();
        return user;
    }

    public List<User> getAllUsers(){

        SQLiteDatabase db =this.getReadableDatabase();
        List<User> users = new LinkedList<User>();

        String query = "SELECT * FROM " + TABLE_USERS;

         Cursor cursor = db.rawQuery(query, null);

        User user = null;

        if(cursor.moveToFirst())
            do{
                user = new User(cursor.getString(1), cursor.getString(2));
                users.add(user);
            }while(cursor.moveToNext());

        cursor.close();
        db.close();
        return users;
    }

    public void updateUserName(User u, String name){
        SQLiteDatabase db =this.getWritableDatabase();
         ContentValues values = new ContentValues();
        values.put(KEY_USERS_NAME, name);
        db.update(TABLE_USERS, values, KEY_USERS_NAME + "=?", new String[]{u.getName()});
        db.close();
    }

    public void deleteUser(User user){
        SQLiteDatabase db =this.getWritableDatabase();
        db.delete(TABLE_USERS,
                KEY_USERS_PUBLICKEY + " = ?",
                new String[]{String.valueOf(user.getPublicKey())});

        db.close();
    }

    public boolean userExists(String publicKey, String name){
         if(getUserByName(name) != null || getUserByPublicKey(publicKey) != null)
            return true;
        else
            return false;
    }

    public void addMessage(Message message){
        SQLiteDatabase db =this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGES_UUID, message.getUuid());
        values.put(KEY_MESSAGES_IDSOURCE, message.getPublicKeySource());
        values.put(KEY_MESSAGES_IDDEST, message.getPublicKeyDest());
        values.put(KEY_MESSAGES_CONTENT, message.getContent());
        values.put(KEY_MESSAGES_TIMEOUT, message.getTimeout());
        values.put(KEY_MESSAGES_SENT, (message.getSent() ? 1 : 0));
        values.put(KEY_MESSAGES_DATE, message.getDate());
        db.insert(TABLE_MESSAGES, null, values);

        db.close();

    }

    public Message getMessage(String uuid){
        SQLiteDatabase db =this.getReadableDatabase();
        Cursor cursor =
                db.query(TABLE_MESSAGES,
                        COLUMNS_MESSAGES,
                        "uuid = ?",
                        new String[]{uuid},
                        null,
                        null,
                        null,
                        null);

        db.close();
        if(cursor != null && cursor.getCount()>0) {
            cursor.moveToFirst();

            Message message = new Message(cursor.getString(1), cursor.getBlob(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5), (cursor.getInt(6) == 1 ? true : false), cursor.getDouble(7));

            cursor.close();
            return message;
        }
        else {
            cursor.close();
            return null;
        }

    }

    public List<Message> getMessagesFromPublicKeyDest(String pbk){
        SQLiteDatabase db =this.getReadableDatabase();
        List<Message> messages = new LinkedList<Message>();
         Cursor cursor = db.query(TABLE_MESSAGES,
                 COLUMNS_MESSAGES,
                 "idDest = ?",
                 new String[]{pbk},
                 null,
                 null,
                 null,
                 null);


        Message message = null;
        if(cursor.moveToFirst())
            do{
                message = new Message(cursor.getString(1), cursor.getBlob(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5), (cursor.getInt(6) == 1 ? true : false), cursor.getDouble(7));
                messages.add(message);
            }while(cursor.moveToNext());

        cursor.close();
        db.close();
        return messages;
    }


    public List<Message> getAllMessages(){
        SQLiteDatabase db =this.getReadableDatabase();
        List<Message> messages = new LinkedList<Message>();

        String query = "SELECT * FROM " + TABLE_MESSAGES;

         Cursor cursor = db.rawQuery(query, null);

        Message message = null;

        if(cursor.moveToFirst())
            do{
                message = new Message(cursor.getString(1), cursor.getBlob(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5), (cursor.getInt(6) == 1 ? true : false), cursor.getDouble(7));
                messages.add(message);
            }while(cursor.moveToNext());

        cursor.close();
        db.close();
        return messages;
    }
//
//    public List<Message> getMessagesFromContentAndSender(String content, String sender){
//        List<Message> messages = new LinkedList<Message>();
//
//        String userPk = getUserByName(sender).getPublicKey();
//
//        Cursor cursor = db.query(TABLE_MESSAGES,
//                COLUMNS_MESSAGES,
//                "idSource = ? ",
//                new String[]{userPk},
//                null,
//                null,
//                null,
//                null);
//
//
//        Message message = null;
//        if(cursor.moveToFirst())
//            do{
//                message = new Message(cursor.getString(1), cursor.getBlob(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5), (cursor.getInt(6) == 1 ? true : false), cursor.getDouble(7));
//                messages.add(message);
//            }while(cursor.moveToNext());
//
//        List<Message> messagesToReturn = new LinkedList<Message>();
//        for(int i = 0 ; i < messages.size() ; i++) {
//            String content1 = null;
//            try {
//                content1 = CryptoHelper.RSADecrypt(messages.get(i).getContent(), KeysHelper.getMyPrivateKey());
//            } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchProviderException | InvalidKeyException e) {
//                e.printStackTrace();
//            }
//
//            if (content1.equals(content))
//                messagesToReturn.add(messages.get(i));
//        }
//
//
//
//        return messagesToReturn;
//    }

    public void updateSent(Message message){
        SQLiteDatabase db =this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_MESSAGES_SENT, 1);
        db.update(TABLE_MESSAGES, values, KEY_MESSAGES_UUID + "=?", new String[]{message.getUuid()});
        db.close();
    }

    public void deleteMessage(Message message){
        SQLiteDatabase db =this.getWritableDatabase();
        db.delete(TABLE_MESSAGES,
                KEY_MESSAGES_UUID + " = ?",
                new String[]{String.valueOf(message.getUuid())});
        db.close();
    }

    public void addMessageToChat(Message message){

        SQLiteDatabase db =this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_CHAT_UUID, message.getUuid());
        values.put(KEY_CHAT_IDSOURCE, message.getPublicKeySource());
        values.put(KEY_CHAT_IDDEST, message.getPublicKeyDest());
        values.put(KEY_CHAT_CONTENT, message.getContent());
        values.put(KEY_CHAT_DATE, message.getDate());

        db.insert(TABLE_CHAT, null, values);
        db.close();
    }


    public List<Message> getMessagesChatFromContentAndSender(String content, String sender){

        SQLiteDatabase db =this.getReadableDatabase();

        List<Message> messages = new LinkedList<Message>();

        String userPk = getUserByName(sender).getPublicKey();

        Cursor cursor = db.query(TABLE_CHAT,
                COLUMNS_CHAT,
                "idSource = ? OR idSource = ? ",
                new String[]{userPk, KeysHelper.getMyPublicKey()},
                null,
                null,
                null,
                null);


        Message message = null;
        if(cursor.moveToFirst())
            do{
                message = new Message(cursor.getString(1), cursor.getBlob(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5));
                messages.add(message);
            }while(cursor.moveToNext());

        List<Message> messagesToReturn = new LinkedList<Message>();
        for(int i = 0 ; i < messages.size() ; i++) {
            String content1 = null;
            content1 = new String(messages.get(i).getContent());

            if (content1.equals(content))
                messagesToReturn.add(messages.get(i));
        }

        cursor.close();
        db.close();

        return messagesToReturn;
    }

    public List<Message> getMessagesChatConcerningUser(String pbk){
        SQLiteDatabase db =this.getReadableDatabase();
        String myPbk = KeysHelper.getMyPublicKey();

        List<Message> messages = new LinkedList<Message>();
         Cursor cursor = db.query(TABLE_CHAT,
                COLUMNS_CHAT,
                "idSource = ? OR (idDest = ? AND idSource = ? )",
                new String[]{pbk, pbk, myPbk},
                null,
                null,
                null,
                null);


        Message message = null;
        if(cursor.moveToFirst())
            do{
                message = new Message(cursor.getString(1), cursor.getBlob(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5));
                messages.add(message);
            }while(cursor.moveToNext());

        cursor.close();
        db.close();

        return messages;
    }

    public void deleteMessageChat(Message message){

        SQLiteDatabase db =this.getWritableDatabase();

        db.delete(TABLE_CHAT,
                KEY_CHAT_UUID + " = ?",
                new String[]{String.valueOf(message.getUuid())});
        db.close();
    }

    public List<Message> getMessagesChatFromPublicKeyDestAndSource(String pbk){

        SQLiteDatabase db =this.getReadableDatabase();

        List<Message> messages = new LinkedList<Message>();
         Cursor cursor = db.query(TABLE_CHAT,
                COLUMNS_CHAT,
                "idDest = ? OR idSource = ?",
                new String[]{pbk, pbk},
                null,
                null,
                null,
                null);


        Message message = null;
        if(cursor.moveToFirst())
            do{
                message = new Message(cursor.getString(1), cursor.getBlob(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5));
                messages.add(message);
            }while(cursor.moveToNext());

        cursor.close();
        db.close();

        return messages;
    }

    public List<Message> getAllMessagesChat(){

        SQLiteDatabase db =this.getReadableDatabase();

        List<Message> messages = new LinkedList<Message>();

        String query = "SELECT * FROM " + TABLE_CHAT;

         Cursor cursor = db.rawQuery(query, null);

        Message message = null;

        if(cursor.moveToFirst())
            do{
                message = new Message(cursor.getString(1), cursor.getBlob(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5));
                messages.add(message);
            }while(cursor.moveToNext());

        cursor.close();
        db.close();
        return messages;
    }


    public Message getMessageChat(String uuid){

        SQLiteDatabase db =this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_CHAT,
                        COLUMNS_CHAT,
                        "uuid = ?",
                        new String[] {uuid},
                        null,
                        null,
                        null,
                        null);

        db.close();

        if(cursor != null && cursor.getCount()>0) {
            cursor.moveToFirst();

            Message message = new Message(cursor.getString(1), cursor.getBlob(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5));

            cursor.close();
            return message;
        }
        else {
            cursor.close();
            return null;
        }
    }

    public void addSignal(Signal s){

        SQLiteDatabase db =this.getWritableDatabase();

        if(!signalExists(s)) {
            if (numberOfSignals() >= Global.MAX_SIGNALS) {
                replaceOldestSignal(s);
            } else {//insert new signal
                ContentValues values = new ContentValues();
                values.put(KEY_SIGNAL_UUID, s.getUuid());
                values.put(KEY_SIGNAL_DATE, s.getDate());

                db.insert(TABLE_SIGNAL, null, values);
            }
        }

        db.close();
    }

    public Signal getSignal(String uuid){

        SQLiteDatabase db =this.getReadableDatabase();

        Cursor cursor =
                db.query(TABLE_SIGNAL,
                        COLUMNS_SIGNAL,
                        "uuid = ?",
                        new String[]{uuid},
                        null,
                        null,
                        null,
                        null);

        Signal s = null;
        if(cursor != null)
            if(cursor.moveToFirst())
                s = new Signal(cursor.getString(1));

        cursor.close();
        db.close();

        return s;
    }

    public int numberOfSignals(){
        SQLiteDatabase db =this.getReadableDatabase();
         int ret = (int)DatabaseUtils.queryNumEntries(db, "signal");
        db.close();
        return ret;
    }

    public void replaceOldestSignal(Signal s){

        List<Signal> signals = getAllSignals();

        Signal oldest = new Signal("null");
        oldest.setDate(Double.MAX_VALUE);
        for(int i = 0 ; i < signals.size() ; i++)
            if(signals.get(i).getDate()<oldest.getDate()) {
                oldest = signals.get(i);
            }

        updateSignal(oldest, s);


    }

    public void updateSignal(Signal o, Signal n){
        SQLiteDatabase db =this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_SIGNAL_UUID, n.getUuid());
        values.put(KEY_SIGNAL_DATE, n.getDate());
        db.update(TABLE_SIGNAL, values, KEY_SIGNAL_UUID + "=?", new String[]{o.getUuid()});
        db.close();
    }

    public List<Signal> getAllSignals(){
        SQLiteDatabase db =this.getReadableDatabase();

        List<Signal> signals = new LinkedList<Signal>();

        String query = "SELECT * FROM " + TABLE_SIGNAL;

         Cursor cursor = db.rawQuery(query, null);

        Signal s = null;

        if(cursor.moveToFirst())
            do{
                s = new Signal(cursor.getString(1));
                signals.add(s);
            }while(cursor.moveToNext());
        cursor.close();
        db.close();

        return signals;
    }

    public boolean signalExists(Signal s){
         if(getSignal(s.getUuid()) != null)
            return true;
        else
            return false;
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
