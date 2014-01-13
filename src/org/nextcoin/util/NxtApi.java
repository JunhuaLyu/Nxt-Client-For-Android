package org.nextcoin.util;

import java.math.BigInteger;

import org.nextcoin.node.NodesManager;
import org.nextcoin.transactions.NxtTransaction;
import org.nextcoin.transactions.NxtTransaction.MessagingAliasAssignmentAttachment;

import com.other.util.HttpUtil;

public class NxtApi {
    public interface ResponseListener{
        public void onResponse(boolean success, String info);
    }

    private ResponseListener mResponseListener;
    
    static public NxtTransaction makeAliasTransaction(String secret, String alias, String uri, int fee, short deadline){
        byte type = NxtTransaction.TYPE_MESSAGING;
        byte subtype = NxtTransaction.SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT;
        //int timestamp = NxtUtil.getTimestamp() - 15;
        int timestamp = NodesManager.sharedInstance().getCurrentNode().getTimestamp();
        //short deadline = 1500;
        byte[] senderPublicKey = Crypto.getPublicKey(secret);
        long recipient = new BigInteger("1739068987193023818").longValue();;
        int amount = 0;
        //int fee = 1;
        long referencedTransaction = 0;;
        byte[] signature = new byte[64];

        NxtTransaction orgTransaction = new NxtTransaction(type, subtype, timestamp, deadline, 
                senderPublicKey, recipient, amount, fee, referencedTransaction, signature);
        
        MessagingAliasAssignmentAttachment attach = new MessagingAliasAssignmentAttachment(alias, uri);
        orgTransaction.setAttachment(attach);
        
        orgTransaction.sign(secret);
        
        return orgTransaction;
    }

    private String mAddr;
    private String mBytes;
    public void broadcastTransaction(String addr, NxtTransaction transaction, ResponseListener listener){
        mAddr = addr;
        mBytes = NxtUtil.convert(transaction.getBytes());
        mResponseListener = listener;
        new Thread(new Runnable(){
            @Override
            public void run() {
                String base_url = "http://" + mAddr + ":7874";
                String httpUrl = String.format(
                        "%s/nxt?requestType=broadcastTransaction&&transactionBytes=%s", 
                        base_url, mBytes);
                
                try {
                    String result = HttpUtil.getHttp(httpUrl);
                    mResponseListener.onResponse(true, result);
//                    JSONObject jsonObj = new JSONObject(result);
//                    String transaction = null;
//                    String errInfo = null;
//                    if (!jsonObj.isNull("transaction")){
//                        transaction = jsonObj.getString("transaction");
//                    }
//                    else
//                        errInfo = jsonObj.getString("errorDescription");
//
//                    if ( null != transaction )
//                        mResponseListener.onResponse(true, transaction);
//                    else
//                        mResponseListener.onResponse(false, errInfo);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                mResponseListener.onResponse(false, null);
            }}).start();
    }
}
