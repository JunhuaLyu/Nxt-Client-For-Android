package org.nextcoin.alias;

import org.json.JSONObject;
import org.nextcoin.node.NodeContext;
import org.nextcoin.node.NodesManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class Alias {
    public String mName;
    public String mUrl;
    public String mAccountId;
    public String mImg;
    
    public void loadAsyn(AliasResponse response){
        mAliasResponse = response;

        NodeContext nodeContext = NodesManager.sharedInstance().getCurrentNode();
        if ( !nodeContext.isActive()){
            mAliasResponse.onResult(RESULT_FAILED, null);
            return;
        }
        String ip = nodeContext.getIP();
        if ( null == ip ){
            mAliasResponse.onResult(RESULT_FAILED, null);
            return;
        }

        String base_url = "http://" + ip + ":7874";
        String httpUrl = String.format("%s/nxt?requestType=getAliasURI&alias=%s", base_url, mName);
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(httpUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                String strResult = response;
                
                if ( strResult.startsWith("{}") ){
                    mUrl = "";
                    mAliasResponse.onResult(RESULT_NO_ACC, Alias.this);
                    return;
                }
                JSONObject jsonObj;
                try {
                    jsonObj = new JSONObject(strResult);
                    if ( jsonObj.has("uri") ){
                        mUrl = jsonObj.getString("uri");
                        String content = mUrl.toLowerCase();
                        if ( content.startsWith("nxter:") )
                            content = parseNxter(content.substring(6));
                        else if ( content.startsWith("nacc:") )
                            content = content.substring(5);
                        else if ( content.startsWith("nxt:") )
                            content = content.substring(4);
                        
                        if (content.length() < 22 && content.matches("\\d+")){
                            mAccountId = content;
                            mAliasResponse.onResult(RESULT_SUCCESS, Alias.this);
                        }else
                            mAliasResponse.onResult(RESULT_NO_ACC, Alias.this);
                    }else{
                        mAliasResponse.onResult(RESULT_NOT_EXIST, Alias.this);
                    }
                    return;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                
                mAliasResponse.onResult(RESULT_NOT_EXIST, Alias.this);
            }

            @Override
            public void onFailure(Throwable error, String content) {
                mAliasResponse.onResult(RESULT_FAILED, null);
            }
        });
    }
    
    // {"web":"http://www.notsoshifty.de/","fb":"notsoshifty","twitter":"notsoshifty","nxtacct":"11111125831111116332","smtp":"notsoshifty111@example.com","skype":"notsoshifty1971"}
    // nxter:acc=5693933960808456307&img=bbyk.sinaapp.com/snake.png
    private String parseNxter(String content){
        String acc = "";
        String pairs[] = content.split("&");
        for ( int i = 0; i < pairs.length; ++ i ){
            String pair[] = pairs[i].split("=");
            if ( 2 == pair.length ){
                if ( pair[0].equals("acc") )
                    acc = pair[1];
                else if ( pair[0].equals("img") )
                    mImg = pair[1];
            }
        }

        return acc;
    }
    
    private AliasResponse mAliasResponse;
    public interface AliasResponse{
        public void onResult(int result, Alias alias);
    }
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_FAILED = 1;
    public static final int RESULT_NOT_EXIST = 2;
    public static final int RESULT_NO_ACC = 3;
}
