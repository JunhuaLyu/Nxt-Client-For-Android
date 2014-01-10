package org.nextcoin.transactions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nextcoin.alias.Alias;
import org.nextcoin.node.NodeContext;
import org.nextcoin.node.NodesManager;

public class Transaction {
    public boolean mLoaded = false;
    public String mId;
    public int mType;
    public int mSubType;
    public int mConfirmations;
    public int mTimestamp = 0;
    public String mRecipient;
    public String mSender;
    public float mAmount;
    public float mFee;
    public Object mAttachment;
    
    static public boolean loadTransaction(Transaction transaction){
        NodeContext nodeContext = NodesManager.sharedInstance().getCurrentNode();
        if ( !nodeContext.isActive()){
            return false;
        }
        
        String ip = nodeContext.getIP();
        String base_url = "http://" + ip + ":7874";
        String httpUrl = String.format(
                "%s/nxt?requestType=getTransaction&&transaction=%s", 
                base_url, transaction.mId);

        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(httpUrl).openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while((line = br.readLine()) != null)
                sb.append(line);

            String strResult = sb.toString();
            JSONObject jsonObj;
            try {
                jsonObj = new JSONObject(strResult);
                transaction.mLoaded = true; 
                transaction.mType = jsonObj.getInt("type");
                transaction.mTimestamp = jsonObj.getInt("timestamp");
                transaction.mConfirmations = jsonObj.getInt("confirmations");
                double amount = jsonObj.getDouble("amount");
                transaction.mAmount = (float)amount;
                double fee = jsonObj.getDouble("fee");
                transaction.mFee = (float)fee;
                if ( jsonObj.has("subtype") )
                    transaction.mSubType = jsonObj.getInt("subtype");

                if ( NxtTransaction.TYPE_PAYMENT == transaction.mType ){
                    if ( NxtTransaction.SUBTYPE_PAYMENT_ORDINARY_PAYMENT == transaction.mSubType ){
                        transaction.mRecipient = jsonObj.getString("recipient");
                        transaction.mSender = jsonObj.getString("sender");
                    }
                }else if ( NxtTransaction.TYPE_MESSAGING == transaction.mType ){
                    if ( NxtTransaction.SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT == transaction.mSubType ){
                        JSONObject jsonAlias = jsonObj.getJSONObject("attachment");
                        if ( jsonAlias.has("alias") ){
                            Alias alias = new Alias();
                            alias.mName = jsonAlias.getString("alias");
                            if ( jsonAlias.has("uri") )
                                alias.mUrl = jsonAlias.getString("uri");
                            
                            transaction.mAttachment  = alias;
                        }
                    }
                }

                return true;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
        return false;
    }
    
    static public LinkedList<Transaction> getTransactionList(
            NodeContext nodeContext, String accId, int timestamp){
        String ip = null;
        if ( nodeContext.isActive() )
            ip = nodeContext.getIP();
        else 
            return null;
        
        String base_url = "http://" + ip + ":7874";
        String httpUrl = String.format(
                "%s/nxt?requestType=getAccountTransactionIds&account=%s&timestamp=%d", 
                base_url, accId, timestamp);
        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(httpUrl).openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while((line = br.readLine()) != null)
                sb.append(line);

            String strResult = sb.toString();
            JSONObject jsonObj;
            try {
                jsonObj = new JSONObject(strResult);
                JSONArray jarray = jsonObj.getJSONArray("transactionIds");

                LinkedList<Transaction> transactionList = new LinkedList<Transaction>();
                for ( int i = 0; i < jarray.length(); ++ i ){
                    String transactionId = jarray.getString(i);
                    Transaction transaction = new Transaction();
                    transaction.mId = transactionId;
                    transactionList.add(transaction);
                }

                return transactionList;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    static public void sortByTimestamp(LinkedList<Transaction> List){
        Collections.sort(List, new Comparator<Transaction>(){
            @Override
            public int compare(Transaction lhs, Transaction rhs) {
                return rhs.mTimestamp - lhs.mTimestamp;
            }});
    }
}
