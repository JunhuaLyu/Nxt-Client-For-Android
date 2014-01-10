package org.nextcoin.alias;

import org.nextcoin.nxtclient.R;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.TextView;

public class AliasCheck {

    private Context mContext;
    public void open(Context context){
        mContext = context;
        new AliasInputDialog().open(context, new Alias.AliasResponse(){
            @Override
            public void onResult(int result, Alias alias) {
                StringBuffer strBuff = new StringBuffer();
                if ( Alias.RESULT_FAILED == result ){
                    strBuff.append("Node connection is failed.");
                }else if ( Alias.RESULT_NOT_EXIST == result ){
                    strBuff.append("Alias:");
                    strBuff.append(alias.mName);
                    strBuff.append(" doesn't exist.");
                }else{
                    strBuff.append("Alias:");
                    strBuff.append("\r\n");
                    strBuff.append(alias.mName);
                    strBuff.append("\r\n");
                    strBuff.append("\r\n");
                    strBuff.append("Uri:");
                    strBuff.append("\r\n");
                    strBuff.append(alias.mUrl);
                    strBuff.append("\r\n");
                }
                TextView tv = new TextView(mContext);
                tv.setPadding(20, 20, 20, 20);
                tv.setText(strBuff.toString());

                new AlertDialog.Builder(mContext)
                        // .setTitle(R.string.unlock)
                        .setView(tv)
                        //.setMessage(R.string.in_development)
                        .setNegativeButton(R.string.back, null).show();
            }
        });
    }
}
