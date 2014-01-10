package org.nextcoin.alias;

import org.nextcoin.accounts.Account;
import org.nextcoin.accounts.AccountsInfoHelper;
import org.nextcoin.node.NodesManager;
import org.nextcoin.nxtclient.R;
import org.nextcoin.transactions.NxtTransaction;
import org.nextcoin.util.NxtApi;
import org.nextcoin.util.Vanity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class AliasAssignActivity extends Activity {

    private EditText mEditTextSecretPhrase;
    private EditText mEditTextAlias;
    private EditText mEditTextUri;
    private EditText mEditTextFee;
    private EditText mEditTextHour;

    private Button mBtnAssign;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alias_assign);

        mEditTextAlias = (EditText)this.findViewById(R.id.edittext_alias_input);
        mEditTextUri = (EditText)this.findViewById(R.id.edittext_uri_input);
        mEditTextFee = (EditText)this.findViewById(R.id.edittext_fee_input);
        mEditTextHour = (EditText)this.findViewById(R.id.edittext_deadline_input);
        
        mEditTextSecretPhrase = (EditText)this.findViewById(R.id.edittext_secret_input);
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
        
        mBtnAssign = (Button)this.findViewById(R.id.btn_account_create);
        mBtnAssign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String secretPhrase = mEditTextSecretPhrase.getText().toString();
                
                String alias = mEditTextAlias.getText().toString();
                if ( alias.length() < 1 )
                    return;
                
                String uri = mEditTextUri.getText().toString();
                
                String feeStr = mEditTextFee.getText().toString();
                int fee = Integer.parseInt(feeStr);
                if ( fee < 1 )
                    return;
                
                String deadlineStr = mEditTextHour.getText().toString();
                int deadline = Integer.parseInt(deadlineStr) * 60;
                if ( deadline < 60 )
                    return;
                
                mBtnAssign.setText(R.string.waiting);
                mBtnAssign.setEnabled(false);
                assign(secretPhrase, alias, uri, fee, deadline);
            }
        });        
    }

    private String mSecret;
    private String mAlias;
    private String mUri;
    private int mFee;
    private int mDeadline;
    private void assign(String secret, String alias, String uri, int fee, int deadline){
        mSecret = secret;
        mAlias = alias;
        mUri = uri;
        mFee = fee;
        mDeadline = deadline;
        
        // check account balance
        String accountId = Vanity.generateAccount(mSecret);
        Account acct = new Account();
        acct.mId = accountId;
        new AccountsInfoHelper().requestAccountInfo(acct, new AccountsInfoHelper.ResponseListener() {
            @Override
            public void onResponse(boolean success, Account account, String info) {
                if ( success ){
                    if ( account.mBalance >= mFee ){
                        // sign transaction
                        NxtTransaction transaction = 
                                NxtApi.makeAliasTransaction(mSecret, mAlias, mUri, mFee, (short) mDeadline);
                        
                        // broadcast transaction
                        String addr = NodesManager.sharedInstance().getCurrentNode().getIP();
                        new NxtApi().broadcastTransaction(addr, transaction, new NxtApi.ResponseListener() {
                            @Override
                            public void onResponse(boolean success, String info) {
                                if ( success )
                                    assignSuccess();
                                else
                                    assignFailed((String)AliasAssignActivity.this.getText(R.string.failed));
                            }
                        });
                    }else
                        assignFailed((String)AliasAssignActivity.this.getText(R.string.not_enough_funds));
                }else{
                    assignFailed((String)AliasAssignActivity.this.getText(R.string.connection_failed));
                }
            }
        });
    }
    
    private void assignSuccess(){
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SEND_SUCCESS;
        mHandler.sendMessage(msg);
    }
    
    private void assignFailed(String info){
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SEND_FAILED;
        
        Bundle data = new Bundle();
        data.putString("info", info);
        msg.setData(data);
        mHandler.sendMessage(msg);
    }
    
    /**
     * handle msg to update UI
     * @param msg
     */
    private static final int MSG_SEND_SUCCESS = 0;
    private static final int MSG_SEND_FAILED = 1;
    public void handleMessage(Message msg) {
        mBtnAssign.setText(R.string.assign);
        mBtnAssign.setEnabled(true);

        if ( MSG_SEND_SUCCESS == msg.what ){
            Toast.makeText(AliasAssignActivity.this,
                    AliasAssignActivity.this.getText(R.string.success),
                    Toast.LENGTH_LONG).show();
            finish();
        }else{
            Bundle data = msg.getData();
            Toast.makeText(AliasAssignActivity.this, data.getString("info"), Toast.LENGTH_LONG).show();
        }
    }

    static private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ( msg.obj instanceof AliasAssignActivity ){
                AliasAssignActivity instance = (AliasAssignActivity)msg.obj;
                instance.handleMessage(msg);
            }
        }
    };
}
