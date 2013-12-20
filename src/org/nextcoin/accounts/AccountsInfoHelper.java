package org.nextcoin.accounts;

import java.util.LinkedList;

import org.json.JSONObject;
import org.nextcoin.node.NodeContext;
import org.nextcoin.node.NodesManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class AccountsInfoHelper {
    
    public interface ResponseListener{
        public void onResponse(boolean success, Account account, String info);
    }

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
    
    public AccountsInfoHelper(){
    }
}
