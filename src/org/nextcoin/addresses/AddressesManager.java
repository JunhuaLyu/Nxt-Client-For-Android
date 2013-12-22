package org.nextcoin.addresses;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nextcoin.accounts.Account;

import android.content.Context;
import android.content.SharedPreferences;


public class AddressesManager {

    /**
     * the account list
     */
    private LinkedList<Account> mAccountList;
    public LinkedList<Account> getAccountList(){
        return mAccountList;
    }
    
    public String getTag(String accountID){
        if ( null != mAccountList && mAccountList.size() > 0 ){
            for ( Account acc : mAccountList ){
                if ( accountID.equals(acc.mId) ){
                    if ( null != acc.mTag && !acc.mTag.equals("null") )
                        return acc.mTag;
                }
            }
        }
        return " ";
    }
    
    public void addAccount(Context context, String id, String tag){
        if ( null == tag )
            tag = "null";

        Account acct = new Account();
        acct.mId = id;
        acct.mTag = tag;
        mAccountList.addLast(acct);
        saveAccountList(context);
    }
    
    public void removeAccount(Context context, int index){
        mAccountList.remove(index);
        saveAccountList(context);
    }
    
    public void removeAccount(String id, String tag){
        if ( null == tag )
            tag = "null";

        for ( Account acct : mAccountList ){
            if ( acct.mId.equals(id) && acct.mTag.equals(tag) ){
                mAccountList.remove(acct);
                break;
            }
        }
    }

    private void loadAccountList(Context context){
        mAccountList = new LinkedList<Account>();

        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        String accountListJson = prefer.getString(mAccountListSaveKey, null); 

        if ( null != accountListJson ){
            try {
                JSONObject  json = new JSONObject(accountListJson);
                JSONArray jarray = json.getJSONArray("AccountList");

                for ( int i = 0; i < jarray.length(); ++ i ){
                    JSONObject  jso = jarray.getJSONObject(i);
                    String strId = jso.getString("ID");
                    String strTag = jso.getString("TAG");

                    if ( null != strId ){
                        addAccount(context, strId, strTag);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveAccountList(Context context){
        if ( null != mAccountList && mAccountList.size() >= 0 ){
            StringBuffer strbuff = new StringBuffer();
            strbuff.append("{\"AccountList\":[");
            for ( int i = 0; i < mAccountList.size(); ++ i ){
                Account acct = mAccountList.get(i);
                strbuff.append("{\"ID\":\"");
                strbuff.append(acct.mId);
                strbuff.append("\", \"TAG\":\"");
                strbuff.append(acct.mTag);
                if ( i == mAccountList.size() - 1 )
                    strbuff.append("\"}");
                else
                    strbuff.append("\"},");
            }
            strbuff.append("]}");
            
            String jsonStr = strbuff.toString();
            SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
            SharedPreferences.Editor editor = prefer.edit();
            editor.putString(mAccountListSaveKey, jsonStr);
            editor.commit();
        }
    }
    
    /**
     * init and release
     */
    final static private String mPrefFileName = "AddressesManagerPrefFile";
    final static private String mAccountListSaveKey = "AddressesListSaveKey";
    public void init(Context context){
        loadAccountList(context);
    }

    public void release(Context context){
    }
    
    /**
     * Singleton
     */
    public static AddressesManager sharedInstance(){
        if ( null == mAddressesManager )
            mAddressesManager = new AddressesManager();

        return mAddressesManager;
    }
    
    private static AddressesManager mAddressesManager;
    private AddressesManager(){
        
    }
}
