package org.nextcoin.accounts;

import org.nextcoin.nxtclient.R;
import org.nextcoin.util.Vanity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class AccountCreateActivity extends Activity {
    
    private EditText mEditTextSecretPhrase;
    private EditText mEditTextTag;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_create);
        
        mEditTextSecretPhrase = (EditText)this.findViewById(R.id.edittext_secret_input);
        mEditTextTag = (EditText)this.findViewById(R.id.edittext_acctag_input);
        
        CheckBox checkVisable = (CheckBox)this.findViewById(R.id.check_visable);
        checkVisable.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if ( isChecked ){
                    mEditTextSecretPhrase
                            .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                                    | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }else{
                    mEditTextSecretPhrase
                    .setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }});
        
        Button btnCreate = (Button)this.findViewById(R.id.btn_account_create);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String secretPhrase = mEditTextSecretPhrase.getText().toString();
                if ( secretPhrase.length() < 30 ){
                    new AlertDialog.Builder(AccountCreateActivity.this)
                    .setTitle(R.string.create_account)
                    .setMessage(R.string.secret_phrase_refuse)
                    .setNegativeButton(R.string.back, null)
                    .show();
                    return;
                }
                
                String tag = mEditTextTag.getText().toString();
                if ( 0 == tag.length() )
                    tag = "null";
                
                String accountId = Vanity.generateAccount(secretPhrase);
                AccountsManager.sharedInstance().addAccount(AccountCreateActivity.this, accountId, tag);
                AccountCreateActivity.this.finish();
            }
        });
    }

}
