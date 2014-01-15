package org.nextcoin.message;

import java.util.LinkedList;

import org.nextcoin.accounts.Account;
import org.nextcoin.accounts.AccountsManager;
import org.nextcoin.accounts.AccountsSelectDialog;
import org.nextcoin.addresses.AddressesManager;
import org.nextcoin.node.NodesManager;
import org.nextcoin.nxtclient.R;
import org.nextcoin.transactions.NxtTransaction;
import org.nextcoin.util.NxtApi;
import org.nextcoin.util.Vanity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class SendMessageActivity extends Activity {
    
    static public void open(Context context, String sendAccount, String receiveAccount, String content){
        Intent intent = new Intent(context, SendMessageActivity.class);
        intent.putExtra("receive", receiveAccount);
        intent.putExtra("send", sendAccount);
        intent.putExtra("content", content);
        context.startActivity(intent);
    }
    
    private boolean checkTransaction(String secret, String recipient, float amount){
        if ( secret.length() < 1 || recipient.length() < 1 || amount < 1 )
            return false;
        
        String senderId = mEditTextSend.getText().toString();
        String secretToId = Vanity.generateAccount(secret);
        if ( !secretToId.equals(senderId) ){
            Toast.makeText(this, R.string.acc_secret_unmatch, Toast.LENGTH_LONG).show();
            return false;
        }

        LinkedList<Account> accountList = AccountsManager.sharedInstance().getAccountList();
        if ( null != accountList && accountList.size() > 0 ){
            for ( Account acc : accountList ){
                if ( acc.mId.equals(secretToId) && acc.mBalance < amount - 1 ){
                    Toast.makeText(this, R.string.not_enough_funds, Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }
        
        return true;
    }

    private EditText mEditTextSecretPhrase;
    private EditText mEditTextSend;
    private EditText mEditTextReceive;
    private EditText mEditTextMessage;
    private Button mBtnSend;
    private CheckBox mCheckEncrypt;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_message);
        
        Intent intent = this.getIntent();

        mEditTextSecretPhrase = (EditText)this.findViewById(R.id.edittext_secret_input);
        mEditTextSend = (EditText)this.findViewById(R.id.edittext_send_account);
        mEditTextSend.setText(intent.getStringExtra("send"));
        mEditTextReceive = (EditText)this.findViewById(R.id.edittext_receive_account);
        mEditTextReceive.setText(intent.getStringExtra("receive"));
        mEditTextMessage = (EditText)this.findViewById(R.id.edittext_message);
        mEditTextMessage.setText(intent.getStringExtra("content"));

        mCheckEncrypt = (CheckBox)this.findViewById(R.id.check_encrypt);
        mCheckEncrypt.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                if ( isChecked ){
                    new AlertDialog.Builder(SendMessageActivity.this)
                            .setTitle(R.string.encrypt_message)
                            .setMessage(R.string.in_development)
                            .setNegativeButton(R.string.back, null)
                            .show();
                    mCheckEncrypt.setChecked(false);
                }
            }});

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
        
        mBtnSend = (Button)this.findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String secret = mEditTextSecretPhrase.getText().toString();
                String recipient = mEditTextReceive.getText().toString();
                String message = mEditTextMessage.getText().toString();
                
                if ( checkTransaction(secret, recipient, (float)1.0) ){
                    // sign transaction
                    NxtTransaction transaction = NxtApi.makeArbitraryMessageTransaction(
                            secret, recipient, message, (short)1440);
                    
                    // broadcast transaction
                    String addr = NodesManager.sharedInstance().getCurrentNode().getIP();
                    new NxtApi().broadcastTransaction(addr, transaction, mResponseListener);
                    mBtnSend.setText(R.string.waiting);
                    mBtnSend.setEnabled(false);
                }
            }
        });
        
        Button btnChooseSender = (Button)this.findViewById(R.id.btn_choose_src);
        btnChooseSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkedList<Account> accountList = AccountsManager.sharedInstance().getAccountList();
                if ( null == accountList || 0 == accountList.size() )
                    return;
                
                AccountsSelectDialog dialog = new AccountsSelectDialog(SendMessageActivity.this,
                        mSenderAccountListerner, AccountsSelectDialog.TYPE_SELECT_SENDER_ACCOUNT);
                dialog.setAccountList(accountList);
                dialog.show();
            }
        });
        Button btnChooseReceiver = (Button)this.findViewById(R.id.btn_choose_des);
        btnChooseReceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkedList<Account> accountList = AddressesManager.sharedInstance().getAccountList();
                if ( null == accountList || 0 == accountList.size() )
                    return;
                
                AccountsSelectDialog dialog = new AccountsSelectDialog(SendMessageActivity.this,
                        mReceiverAccountListerner, AccountsSelectDialog.TYPE_SELECT_RECEIVER_ACCOUNT);
                dialog.setAccountList(accountList);
                dialog.show();
            }
        });
    }
    
    private NxtApi.ResponseListener mResponseListener = new NxtApi.ResponseListener() {
        @Override
        public void onResponse(boolean success, String info) {
            Message msg = new Message();
            msg.obj = SendMessageActivity.this;
            if ( success )
                msg.what = MSG_SEND_SUCCESS;
            else
                msg.what = MSG_SEND_FAILED;
            mHandler.sendMessage(msg);
        }
    };
    
    /**
     * handle msg to update UI
     * @param msg
     */
    private static final int MSG_SEND_SUCCESS = 0;
    private static final int MSG_SEND_FAILED = 1;
    public void handleMessage(Message msg) {
        mBtnSend.setText(R.string.send);
        mBtnSend.setEnabled(true);

        if ( MSG_SEND_SUCCESS == msg.what ){
            Toast.makeText(SendMessageActivity.this,
                    SendMessageActivity.this.getText(R.string.success),
                    Toast.LENGTH_LONG).show();
            finish();
        }else{
            Toast.makeText(SendMessageActivity.this, R.string.failed, Toast.LENGTH_LONG).show();
        }
    }

    static private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ( msg.obj instanceof SendMessageActivity ){
                SendMessageActivity instance = (SendMessageActivity)msg.obj;
                instance.handleMessage(msg);
            }
        }
    };
    
    private AccountsSelectDialog.AccountReceiver mSenderAccountListerner = 
            new AccountsSelectDialog.AccountReceiver() {
        @Override
        public void OnSelect(Account account) {
            mEditTextSend.setText(account.mId);
        }
    };
    
    private AccountsSelectDialog.AccountReceiver mReceiverAccountListerner = 
            new AccountsSelectDialog.AccountReceiver() {
        @Override
        public void OnSelect(Account account) {
            mEditTextReceive.setText(account.mId);
        }
    };
    
    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();
        NodesManager.sharedInstance().getCurrentNode().updateAsync();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
