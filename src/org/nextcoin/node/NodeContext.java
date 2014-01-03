package org.nextcoin.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class NodeContext {
    /**
     *  node IP
     */
    private String mIPStr;
    public void setIP(String ipStr){
        mIPStr = ipStr;
        mActive = false;
    }
    
    public String getIP(){
        return mIPStr;
    }

    /**
     * node information
     */
    private boolean mActive;
    private String mVersion;
    private int mBlocks;
    public boolean isActive(){
        return mActive;
    }
    
    public String getVersion(){
        return mVersion;
    }

    public int getBlocks(){
        return mBlocks;
    }

    /**
     * node update notify
     */
    public interface NodeUpdateListener{
        public void onUpdate(NodeContext node);
    }
    
    private NodeUpdateListener mNodeUpdateListener;
    public void setNodeUpdateListener(NodeUpdateListener l){
        mNodeUpdateListener = l;
    }
    
    public void update(){
        if ( null == mIPStr )
            return;

        String base_url = "http://" + mIPStr + ":7874";
        String httpUrl = String.format("%s/nxt?requestType=getState", base_url);
        AsyncHttpClient client = new AsyncHttpClient();
        try{
            client.get(httpUrl, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(String response) {
                    String strResult = response;
                    JSONObject jsonObj;
                    try {
                        jsonObj = new JSONObject(strResult);
                        mVersion = jsonObj.getString("version");
                        String lastBlock = jsonObj.getString("lastBlock");
                        mBlocks = getBlockHeight(lastBlock);
                        mActive = true;
                        //Log.v("NodeContext", mIPStr);
                        if ( null != mNodeUpdateListener )
                            mNodeUpdateListener.onUpdate(NodeContext.this);
                        return;
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    mActive = false;
                    if ( null != mNodeUpdateListener )
                        mNodeUpdateListener.onUpdate(NodeContext.this);
                }

                @Override
                public void onFailure(Throwable error, String content) {
                    mActive = false;
                    if ( null != mNodeUpdateListener )
                        mNodeUpdateListener.onUpdate(NodeContext.this);
                }
            });
        }catch(Exception e){
            e.printStackTrace();
            mActive = false;
            if ( null != mNodeUpdateListener )
                mNodeUpdateListener.onUpdate(NodeContext.this);
        }
    }
    
    private int getBlockHeight(String blockId){
        String base_url = "http://" + mIPStr + ":7874";
        String httpUrl = String.format(
                "%s/nxt?requestType=getBlock&&block=%s", 
                base_url, blockId);

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
                int height = jsonObj.getInt("height");

                return height;
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        

        return 0;
    }
    
    public NodeContext(){
        
    }
}
