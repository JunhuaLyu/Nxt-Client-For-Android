package org.nextcoin.accounts;


import org.nextcoin.nxtclient.R;
import org.nextcoin.nxtclient.SafeBox;
import org.nextcoin.util.Vanity;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class AccountUnlockDialog {
    
    public interface ResponseListener{
        public void onResponse(boolean success, String info);
    }

    Context mContext;
    String mAccountID;
    ResponseListener mResponseListener;
    EditText mEditTextSecret;
    AlertDialog mAlertDialog;
    public void openUnlockDialog(Context context, String accountID, ResponseListener listener){
        mContext = context;
        mAccountID = accountID;
        mResponseListener = listener;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.account_unlock, null);
        mEditTextSecret = (EditText)view.findViewById(R.id.edittext_secret_input);
        
        CheckBox checkVisable = (CheckBox)view.findViewById(R.id.check_visable);
        checkVisable.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if ( isChecked ){
                    mEditTextSecret
                            .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                    | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else{
                    mEditTextSecret
                    .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }});
        
        Button btnUnlock = (Button)view.findViewById(R.id.btn_account_unlock);
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String secret = mEditTextSecret.getText().toString();
                if ( null == secret || secret.length() < 1 )
                    return ;
                
                if ( Vanity.generateAccount(secret).equals(mAccountID) ){
                    SafeBox.sharedInstance().unlock(mAccountID, secret);
                    mResponseListener.onResponse(true, null);
                    mAlertDialog.dismiss();
                }else
                    mResponseListener.onResponse(false, 
                            (String)mContext.getText(R.string.acc_secret_unmatch));
            }
        });
        mAlertDialog = new AlertDialog.Builder(mContext)
                .setTitle(R.string.unlock_account)
                .setView(view)
                .show();
    }

}
