package org.nextcoin.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.nextcoin.nxtclient.SafeBox;
import org.nextcoin.util.Crypto;
import org.nextcoin.util.NxtUtil;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.other.util.MyApplication;


public class MessagesData {
    static public class NxtMessage{
        public String mId;
        public String mSender;
        public String mRecipient;
        public String mHex;
        public int mTimestamp;
        public boolean mIsEncrypted;
        public String mText;
        
        public NxtMessage(String id, String sender, String recipient, String hex, int timestamp){
            mId = id;
            mHex = hex;
            mSender = sender;
            mRecipient = recipient;
            mTimestamp = timestamp;
            
            if ( hex.length() > 16 ){
                ByteBuffer buffer = ByteBuffer.wrap(NxtUtil.convert(hex));
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                byte[] head = new byte[4];
                buffer.get(head);
                int len = buffer.getInt();
                if ( EncryptHead.equals(new String(head)) &&  len == hex.length() / 2 )
                    mIsEncrypted  = true;
            }else
                mIsEncrypted  = false;
        }
        
        public String getText(){
            if ( null != mText )
                return mText;
            
            if ( mIsEncrypted )
                return "**Encrypted**";
            
            String text = null;
            try {
                text = new String(NxtUtil.convert(mHex), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return text;
        }
        
        static final private String EncryptHead = "NE*#";
        static public byte[] encryptMessage(String message, String senderSecret, byte[] recipientKey){
            byte[] data = Crypto.encodeMessage(message, senderSecret, recipientKey);
            ByteBuffer buff = ByteBuffer.allocate(data.length + 8);
            buff.order(ByteOrder.LITTLE_ENDIAN);
            buff.put(EncryptHead.getBytes());
            buff.putInt(data.length + 8);
            buff.put(data);
            
            return buff.array();
        }
    }
    
    static public void decodeMessage(NxtMessage message, String myAccountID){
        
    }
    
    static public void decodeMessage(LinkedList<NxtMessage> list, String myAccountID){
        String secret = SafeBox.sharedInstance().getSecret(myAccountID);
        if ( null == secret )
            return;
        
        for ( NxtMessage message : list ){
            if ( message.mIsEncrypted && null == message.mText){
                byte[] data  = NxtUtil.convert(message.mHex.substring(16));
                String othersAccount;
                if ( message.mSender.equals(myAccountID) ){
                    othersAccount = message.mRecipient;
                }else if ( message.mRecipient.equals(myAccountID) ){
                    othersAccount = message.mSender;
                }
                else continue;
                
                byte[] othersKey = SafeBox.sharedInstance().getPublicKey(othersAccount);
                if ( othersKey == null )
                    continue;
                
                message.mText = Crypto.decodeMessage(data, secret, othersKey);
            }
        }
    }
    
    static public void sortByTimestamp(LinkedList<NxtMessage> List){
        Collections.sort(List, new Comparator<NxtMessage>(){
            @Override
            public int compare(NxtMessage lhs, NxtMessage rhs) {
                return rhs.mTimestamp - lhs.mTimestamp;
            }});
    }

    private Context mContext;
    private SQLiteDatabase mMessageDB;
    
    public LinkedList<NxtMessage> getMessages(String accId){
        LinkedList<NxtMessage> messages = new LinkedList<NxtMessage>();
        final String[] columns  = {"id", "sender", "recipient", "hex", "time"};
        String[] selectionArgs = new String[1];
        selectionArgs[0] = accId;
        Cursor cursor = mMessageDB.query(MESSAGES_TABLE_NAME, columns, "sender=?", selectionArgs, null, null, null);
        if (cursor.moveToFirst()){
            for(int i = 0; i < cursor.getCount(); i ++){
                messages.add(new NxtMessage(cursor.getString(0), cursor.getString(1), 
                        cursor.getString(2), cursor.getString(3), cursor.getInt(4)));
                cursor.moveToNext();
            }
        }

        cursor = mMessageDB.query(MESSAGES_TABLE_NAME, columns, "recipient=?", selectionArgs, null, null, null);
        if (cursor.moveToFirst()){
            for(int i = 0; i < cursor.getCount(); i ++){
                messages.add(new NxtMessage(cursor.getString(0), cursor.getString(1), 
                        cursor.getString(2), cursor.getString(3), cursor.getInt(4)));
                cursor.moveToNext();
            }
        }
        
        sortByTimestamp(messages);
        return messages;
    }
    
    public long saveMessage(NxtMessage msg){
        final String[] columns  = {"id"};
        String[] selectionArgs = new String[1];
        selectionArgs[0] = msg.mId;
        Cursor cursor = mMessageDB.query(MESSAGES_TABLE_NAME, columns, "id=?", selectionArgs, null, null, null);
        if (cursor.moveToFirst())
            return -1;
        
        ContentValues values = new ContentValues();
        values.put("id", msg.mId);
        values.put("sender", msg.mSender);
        values.put("recipient", msg.mRecipient);
        values.put("hex", msg.mHex);
        values.put("time", msg.mTimestamp);
        return mMessageDB.insert(MESSAGES_TABLE_NAME, null, values);
    }
    
    public int getLastTimestamp(String accId){
        SharedPreferences prefer = mContext.getSharedPreferences(mPrefFileName, 0);
        //return 0;
        return prefer.getInt(mLastTimestampSaveKey + accId, 0);
    }
    
    public void saveTimestamp(String accId, int timestamp){
        SharedPreferences prefer = mContext.getSharedPreferences(mPrefFileName, 0);
        SharedPreferences.Editor editor = prefer.edit();
        editor.putInt(mLastTimestampSaveKey + accId, timestamp);
        editor.commit();
    }

    final static private String mPrefFileName = "NxtMessagesDataPrefFile";
    final static private String mLastTimestampSaveKey = "LastTimestampSaveKey";
    
    private static final String MESSAGES_TABLE_NAME = "messages";
    private void init(){
        mContext = MyApplication.getAppContext();
        mMessageDB = mContext.openOrCreateDatabase("message.db", Context.MODE_PRIVATE, null);
        //mMessageDB.execSQL("DROP TABLE messages");
        mMessageDB.execSQL(
                "CREATE TABLE IF NOT EXISTS messages (" +
                "id VARCHAR PRIMARY KEY, " +
                "sender CHAR(24), recipient CHAR(24), " +
                "hex TEXT, time INT)");
    }

    /**
     * Singleton
     */
    public static MessagesData sharedInstance(){
        if ( null == mMessagesData )
            mMessagesData = new MessagesData();

        return mMessagesData;
    }
    
    private static MessagesData mMessagesData;
    private MessagesData(){
        init();
    }
}
