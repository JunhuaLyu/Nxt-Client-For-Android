package org.nextcoin.alias;

import org.nextcoin.nxtclient.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class AliasInputDialog {
    
    private View mAliasInputView;
    private EditText mEditTextAlias;
    private Alias.AliasResponse mAliasResponse;
    private Context mContext;
    public void open(Context context, Alias.AliasResponse receiver){
        mContext = context;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mAliasInputView = inflater.inflate(R.layout.alias_input, null);
        mEditTextAlias = (EditText)mAliasInputView.findViewById(R.id.edittext_alias_input);
        
        mAliasResponse = receiver;
        new AlertDialog.Builder(mContext)
        .setTitle(R.string.add)
        .setView(mAliasInputView)
        .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                String name = mEditTextAlias.getText().toString();
                if ( name.length() > 0 ){
                    Alias alias = new Alias();
                    alias.mName = name;
                    //Toast.makeText(mContext, "Checking alias:" + name, Toast.LENGTH_SHORT).show();
                    alias.loadAsyn(mAliasResponse);
                }
            }})
        .setNegativeButton(R.string.back, null)
        .show();
    }
}
