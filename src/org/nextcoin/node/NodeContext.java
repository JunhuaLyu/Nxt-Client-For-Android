package org.nextcoin.node;

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
    public boolean isActive(){
        return mActive;
    }
    
    public String getVersion(){
        return mVersion;
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
                        mActive = true;
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
    
    public NodeContext(){
        
    }
}
