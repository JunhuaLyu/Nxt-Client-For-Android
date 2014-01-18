package org.nextcoin.transactions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;

import org.nextcoin.node.NodeContext;
import org.nextcoin.node.NodesManager;
import org.nextcoin.util.Crypto;
import org.nextcoin.util.NxtUtil;


public class SendCoin {
// https://localhost:7875/nxt?requestType=broadcastTransaction&&transactionBytes=%s",
// https://localhost:7875/nxt?requestType=sendMoney
//&secretPhrase=%s&recipient=%s&amount=%s&fee=1&deadline=900
    static private class SendRunnale implements Runnable{
        String mSecret;
        String mRecipient;
        float mAmount;
        ResponseListener mListener;
        public SendRunnale(String secret, String recipient, float amount, ResponseListener listener){
            mSecret = secret;
            mRecipient = recipient;
            mAmount = amount;
            mListener = listener;
        }
        
        @Override
        public void run() {
            SendCoin sender = new SendCoin(mListener);
            //sender.sendHttps(mSecret, mRecipient, mAmount);
            sender.send(mSecret, mRecipient, mAmount);
        }
    }

    static public void sendCoin(String secret, String recipient, float amount, ResponseListener listener){
        //SendCoin sender = new SendCoin(mListener);
        //sender.send(secret, recipient, amount);
        new Thread(new SendRunnale(secret, recipient, amount, listener)).start();
    }

    public interface ResponseListener{
        public void onResponse(boolean success, String info);
    }

    private ResponseListener mResponseListener;
    private SendCoin(ResponseListener listener){
        mResponseListener = listener;
    }

    private void send(String secret, String recipientStr, float amountF){
        NodeContext nodeContext = NodesManager.sharedInstance().getCurrentNode();
        if ( !nodeContext.isActive()){
            mResponseListener.onResponse(false, null);
            return;
        }
        
        byte type = NxtTransaction.TYPE_PAYMENT;
        byte subtype = NxtTransaction.SUBTYPE_PAYMENT_ORDINARY_PAYMENT;
        //int timestamp = NxtUtil.getTimestamp() - 15;
        int timestamp = nodeContext.getTimestamp();
        short deadline = 1500;
        byte[] senderPublicKey = Crypto.getPublicKey(secret);
        long recipient = new BigInteger(recipientStr).longValue();;
        int amount = (int)amountF;
        int fee = 1;
        long referencedTransaction = 0;;
        byte[] signature = new byte[64];
        
        NxtTransaction orgTransaction = new NxtTransaction(type, subtype, timestamp, deadline, 
                senderPublicKey, recipient, amount, fee, referencedTransaction, signature);
        orgTransaction.sign(secret);
        String transactionBytes = NxtUtil.convert(orgTransaction.getBytes());

        String ip = nodeContext.getIP();
        String base_url = "http://" + ip + ":7874";
        String httpUrl = String.format(
                "%s/nxt?requestType=broadcastTransaction&transactionBytes=%s", 
                base_url, transactionBytes);

        try {
            HttpURLConnection conn = (HttpURLConnection)new URL(httpUrl).openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while((line = br.readLine()) != null)
                sb.append(line);

            String strResult = sb.toString();
            mResponseListener.onResponse(true, strResult);
            return;
//            JSONObject jsonObj;
//            try {
//                jsonObj = new JSONObject(strResult);
//                String transaction = null;
//                String errInfo = null;
//                if (!jsonObj.isNull("transaction")){
//                    transaction = jsonObj.getString("transaction");
//                }
//                else
//                    errInfo = jsonObj.getString("errorDescription");
//
//                if ( null != transaction )
//                    mResponseListener.onResponse(true, transaction);
//                else
//                    mResponseListener.onResponse(false, errInfo);
//                return;
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
        mResponseListener.onResponse(false, null);
    }

//    private void sendHttps(String secret, String recipient, float amount){
//        NodeContext nodeContext = NodesManager.sharedInstance().getCurrentNode();
//        if ( !nodeContext.isActive()){
//            mResponseListener.onResponse(false, null);
//            return;
//        }
//        int amountInt = (int)amount;
//        
//        secret = URLEncoder.encode(secret);
//        String ip = nodeContext.getIP();
//        String base_url = "https://" + ip + ":7875";
//        String httpUrl = String.format(
//                "%s/nxt?requestType=sendMoney&secretPhrase=%s&recipient=%s&amount=%d&fee=1&deadline=900", 
//                base_url, secret, recipient, amountInt);
//
//        try {
//            SSLContext sc = SSLContext.getInstance("TLS");
//            sc.init(null, new TrustManager[]{new MyTrustManager()}, new SecureRandom());
//            
//            HttpsURLConnection conn = (HttpsURLConnection)new URL(httpUrl).openConnection();
//            conn.setSSLSocketFactory(sc.getSocketFactory());
//            conn.setHostnameVerifier(new MyHostnameVerifier());
//
//            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            StringBuffer sb = new StringBuffer();
//            String line;
//            while((line = br.readLine()) != null)
//                sb.append(line);
//
//            String strResult = sb.toString();
//            JSONObject jsonObj;
//            try {
//                jsonObj = new JSONObject(strResult);
//                String transaction = null;
//                String errInfo = null;
//                if (!jsonObj.isNull("transaction"))
//                    transaction = jsonObj.getString("transaction");
//                else
//                    errInfo = jsonObj.getString("errorDescription");
//
//                if ( null != transaction )
//                    mResponseListener.onResponse(true, transaction);
//                else
//                    mResponseListener.onResponse(false, errInfo);
//                return;
//            } catch (Exception e1) {
//                e1.printStackTrace();
//            }
//
//            mResponseListener.onResponse(false, strResult);  
//            return;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mResponseListener.onResponse(false, null);
//    }

//    private class MyHostnameVerifier implements HostnameVerifier {
//        @Override
//        public boolean verify(String hostname, SSLSession session) {
//            return true;
//        }
//    }
//
//    private class MyTrustManager implements X509TrustManager{
//
//        @Override
//        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
//                throws CertificateException {
//        }
//
//        @Override
//        public void checkServerTrusted(X509Certificate[] chain, String authType)
//                throws CertificateException {
//        }
//
//        @Override
//        public X509Certificate[] getAcceptedIssuers() {
//            return null;
//        }
//    }
}
