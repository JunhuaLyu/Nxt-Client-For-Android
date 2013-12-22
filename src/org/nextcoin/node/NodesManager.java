package org.nextcoin.node;

import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;


public class NodesManager {
    
    /**
     * select a note , connect and get information
     */
    private NodeContext mNodeContext;
    
    public NodeContext getCurrentNode(){
        return mNodeContext;
    }
    
    public NodeContext selectNode(int index){
        if ( null == mNodeContext )
            mNodeContext = new NodeContext();
        
        String ipStr = mNodeIPList.get(index);
        mNodeContext.setIP(ipStr);
        
        return mNodeContext;
    }
    
    private void selectRecentlyNode(Context context){
        selectNode(0);
    }
    
    private void saveRecentlyNode(Context context){
        
    }
    
    public void changeNodeIP(String ipStr){
        mNodeIPList.remove(0);
        mNodeIPList.add(0, ipStr);
    }
    
    /**
     * the nodes ip list
     */
    private LinkedList<String> mNodeIPList;
    private void loadNodeIPList(Context context){
        mNodeIPList = new LinkedList<String>();
        //mNodeIPList.addLast("121.199.12.227");
        
        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        String nodeListJson = prefer.getString(mNodeListSaveKey, null); 
                //"{\"NodeList\":[{\"IP\":\"121.199.12.227\"}]}"
        if ( null != nodeListJson ){
            try {
                JSONObject  json = new JSONObject(nodeListJson);
                JSONArray jarray = json.getJSONArray("NodeList");

                for ( int i = 0; i < jarray.length(); ++ i ){
                    JSONObject  jso = jarray.getJSONObject(i);
                    String strIP = jso.getString("IP");
                    mNodeIPList.addLast(strIP);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        
        if ( 0 == mNodeIPList.size() )
            mNodeIPList.add("121.199.12.227");
    }

    private void saveNodeIPList(Context context){
        if ( null != mNodeIPList && mNodeIPList.size() > 0 ){
            StringBuffer strbuff = new StringBuffer();
            strbuff.append("{\"NodeList\":[");
            
            for ( int i = 0; i < mNodeIPList.size(); ++ i ){
                String ipStr = mNodeIPList.get(i);
                strbuff.append("{\"IP\":\"");
                strbuff.append(ipStr);
                if ( i == mNodeIPList.size() - 1 )
                    strbuff.append("\"}");
                else
                    strbuff.append("\"},");
            }
            strbuff.append("]}");
            
            String jsonStr = strbuff.toString();
            SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
            SharedPreferences.Editor editor = prefer.edit();
            editor.putString(mNodeListSaveKey, jsonStr);
            editor.commit();
        }
    }
    
    /**
     * init and release
     */
    final static private String mPrefFileName = "NodesManagerPrefFile";
    final static private String mNodeListSaveKey = "NodeListSaveKey";
    public void init(Context context){
        loadNodeIPList(context);
        selectRecentlyNode(context);
    }
    
    public void save(Context context){
        saveRecentlyNode(context);
        saveNodeIPList(context);
    }

    public void release(Context context){
        save(context);
    }

    /**
     * Singleton
     */
    public static NodesManager sharedInstance(){
        if ( null == mNodesManager )
            mNodesManager = new NodesManager();

        return mNodesManager;
    }
    
    private static NodesManager mNodesManager;
    private NodesManager(){
        
    }
}
