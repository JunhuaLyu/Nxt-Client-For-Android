package org.nextcoin.accounts;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.nextcoin.alias.Alias;
import org.nextcoin.node.NodeContext;
import org.nextcoin.node.NodesManager;
import org.nextcoin.transactions.Transaction;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class AccountsInfoHelper {
    
    public interface ResponseListener{
        public void onResponse(boolean success, Account account, String info);
    }

    //
    // request balance
    //
    private ResponseListener mResponseListener;
    public void requestAccountInfo(Account account, ResponseListener listener){
        mResponseListener = listener;
        NodeContext nodeContext = NodesManager.sharedInstance().getCurrentNode();
        String ip = null;
        if ( nodeContext.isActive() )
            ip = nodeContext.getIP();
        
        getAccountInfo(ip, account, mResponseListener);
    }
    
    private String mIP;
    private LinkedList<Account> mAccountList;
    private int mRequestIndex;
    private AccountsInfoHelper.ResponseListener mLoopResponseListener = 
            new AccountsInfoHelper.ResponseListener() {
        @Override
        public void onResponse(boolean success, Account account, String info) {
            mResponseListener.onResponse(success, account, info);
            mRequestIndex += 1;
            if ( mRequestIndex < mAccountList.size() )
                getAccountInfo(mIP, mAccountList.get(mRequestIndex), mLoopResponseListener);
        }
    };

    public void requestAccountsInfo(LinkedList<Account> accountList, ResponseListener listener){
        NodeContext nodeContext = NodesManager.sharedInstance().getCurrentNode();
        if ( null == accountList || accountList.size() <= 0
                || !nodeContext.isActive())
            return;

        mAccountList = accountList;
        mResponseListener = listener;
        mIP = nodeContext.getIP();

        mRequestIndex = 0;
        getAccountInfo(mIP, mAccountList.get(mRequestIndex), mLoopResponseListener);
    }
    
    private ResponseListener mResponse;
    private Account mAccount;
    private void getAccountInfo(String ip, Account account, ResponseListener listener){
        mResponse = listener;
        mAccount = account;
        if ( null == ip )
            mResponse.onResponse(false, account, null);

        String base_url = "http://" + ip + ":7874";
        String httpUrl = String.format("%s/nxt?requestType=getBalance&account=%s", base_url, mAccount.mId);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(httpUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                String strResult = response;
                JSONObject jsonObj;
                try {
                    jsonObj = new JSONObject(strResult);
                    String balance = jsonObj.getString("balance");
                    float balanceF = Float.parseFloat(balance);
                    mAccount.mBalance = balanceF / 100;
                    
                    String unconfirmedBalance = jsonObj.getString("unconfirmedBalance");
                    float unconfirmedBalanceF = Float.parseFloat(unconfirmedBalance);
                    mAccount.mUnconfirmedBalance = unconfirmedBalanceF / 100;

                    mResponse.onResponse(true, mAccount, null);
                    return;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                
                mResponse.onResponse(false, mAccount, null);
            }

            @Override
            public void onFailure(Throwable error, String content) {
                mResponse.onResponse(false, mAccount, null);
            }
        });
    }

    //
    // request transaction history
    //
    private ResponseListener mTransactionResponseListener;
    public void requestTransactionHistory(Account account, ResponseListener listener){
        mTransactionResponseListener = listener;
        NodeContext nodeContext = NodesManager.sharedInstance().getCurrentNode();
        String ip = null;
        if ( nodeContext.isActive() )
            ip = nodeContext.getIP();
        
        getTransactionHistory(ip, account, mTransactionResponseListener);
    }

    private ResponseListener mTransactionResponse;
    private Account mTransactionAccount;
    private void getTransactionHistory(String ip, Account account, ResponseListener listener){
        mTransactionResponse = listener;
        mTransactionAccount = account;
        if ( null == ip )
            mTransactionResponse.onResponse(false, account, null);

        String base_url = "http://" + ip + ":7874";
        String httpUrl = String.format(
                "%s/nxt?requestType=getAccountTransactionIds&account=%s&timestamp=0", 
                base_url, mTransactionAccount.mId);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(httpUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                String strResult = response;
                JSONObject jsonObj;
                try {
                    jsonObj = new JSONObject(strResult);
                    JSONArray jarray = jsonObj.getJSONArray("transactionIds");

                    mTransactionAccount.mTransactionList = new LinkedList<Transaction>();
                    for ( int i = 0; i < jarray.length(); ++ i ){
                        String transactionId = jarray.getString(i);
                        Transaction transaction = new Transaction();
                        transaction.mId = transactionId;
                        mTransactionAccount.mTransactionList.add(transaction);
                    }
                    mTransactionResponse.onResponse(true, mTransactionAccount, null);
                    return;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                
                mTransactionResponse.onResponse(false, mTransactionAccount, null);
            }

            @Override
            public void onFailure(Throwable error, String content) {
                mTransactionResponse.onResponse(false, mTransactionAccount, null);
            }
        });
    }
    
    //
    //  request aliases list
    // 
    private ResponseListener mAliasResponseListener;
    public void requestAliasList(Account account, ResponseListener listener){
        mAliasResponseListener = listener;
        NodeContext nodeContext = NodesManager.sharedInstance().getCurrentNode();
        String ip = null;
        if ( nodeContext.isActive() )
            ip = nodeContext.getIP();
        
        getAliasList(ip, account, mAliasResponseListener);
    }

    private ResponseListener mAliasResponse;
    private Account mAliasAccount;
    private void getAliasList(String ip, Account account, ResponseListener listener){
        mAliasResponse = listener;
        mAliasAccount = account;
        if ( null == ip )
            mAliasResponse.onResponse(false, account, null);

        String base_url = "http://" + ip + ":7874";
        String httpUrl = String.format(
                "%s/nxt?requestType=listAccountAliases&account=%s", 
                base_url, mAliasAccount.mId);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(httpUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                String strResult = response;
                JSONObject jsonObj;
                try {
                    jsonObj = new JSONObject(strResult);
                    JSONArray jarray = jsonObj.getJSONArray("aliases");

                    mAliasAccount.mAliasList = new LinkedList<Alias>();
                    for ( int i = 0; i < jarray.length(); ++ i ){
                        JSONObject  jso = jarray.getJSONObject(i);
                        Alias alias = new Alias();
                        alias.mName = jso.getString("alias");
                        alias.mUrl = jso.getString("uri");
                        mAliasAccount.mAliasList.add(alias);
                    }
                    mAliasResponse.onResponse(true, mAliasAccount, null);
                    return;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                
                mAliasResponse.onResponse(false, mAliasAccount, null);
            }

            @Override
            public void onFailure(Throwable error, String content) {
                mAliasResponse.onResponse(false, mAliasAccount, null);
            }
        });
    }
    
    public AccountsInfoHelper(){
    }
}
