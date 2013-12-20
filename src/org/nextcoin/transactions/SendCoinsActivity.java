package org.nextcoin.transactions;

import java.util.LinkedList;

import org.nextcoin.accounts.Account;
import org.nextcoin.accounts.AccountsManager;
import org.nextcoin.accounts.AccountsSelectDialog;
import org.nextcoin.addresses.AddressesManager;
import org.nextcoin.nxtclient.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class SendCoinsActivity extends Activity {
    
    static public void open(Context context, String sendAccount, String receiveAccount){
        Intent intent = new Intent(context, SendCoinsActivity.class);
        intent.putExtra("receive", receiveAccount);
        intent.putExtra("send", sendAccount);
        context.startActivity(intent);
    }

    private EditText mEditTextSecretPhrase;
    private EditText mEditTextSend;
    private EditText mEditTextReceive;
    private EditText mEditTextAmount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_coins);
        
        Intent intent = this.getIntent();

        mEditTextSecretPhrase = (EditText)this.findViewById(R.id.edittext_secret_input);
        mEditTextSend = (EditText)this.findViewById(R.id.edittext_send_account);
        mEditTextSend.setText(intent.getStringExtra("send"));
        mEditTextReceive = (EditText)this.findViewById(R.id.edittext_receive_account);
        mEditTextReceive.setText(intent.getStringExtra("receive"));
        mEditTextAmount = (EditText)this.findViewById(R.id.edittext_amount);

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
        
        Button btnSend = (Button)this.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String src = mEditTextSend.getText().toString();
                String secret = mEditTextSecretPhrase.getText().toString();
                String recipient = mEditTextReceive.getText().toString();
                float amount = Float.parseFloat(mEditTextAmount.getText().toString());
                if ( secret.length() < 1 || recipient.length() < 1 || amount <= 1 )
                    return;
                
                SendCoin.sendCoin(secret, recipient, amount, mResponseListener);
            }
        });
        
        Button btnChooseSender = (Button)this.findViewById(R.id.btn_choose_src);
        btnChooseSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinkedList<Account> accountList = AccountsManager.sharedInstance().getAccountList();
                if ( null == accountList || 0 == accountList.size() )
                    return;
                
                AccountsSelectDialog dialog = new AccountsSelectDialog(SendCoinsActivity.this,
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
                
                AccountsSelectDialog dialog = new AccountsSelectDialog(SendCoinsActivity.this,
                        mReceiverAccountListerner, AccountsSelectDialog.TYPE_SELECT_RECEIVER_ACCOUNT);
                dialog.setAccountList(accountList);
                dialog.show();
            }
        });
    }
    
    private SendCoin.ResponseListener mResponseListener = new SendCoin.ResponseListener() {
        @Override
        public void onResponse(boolean success, String info) {
            if ( success ){
                Toast.makeText(SendCoinsActivity.this,
                        SendCoinsActivity.this.getText(R.string.success),
                        Toast.LENGTH_LONG).show();
                finish();
            }else{
                if ( null == info )
                    info = (String) SendCoinsActivity.this.getText(R.string.failed);
                Toast.makeText(SendCoinsActivity.this, info, Toast.LENGTH_LONG).show();
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}
