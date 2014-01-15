
package org.nextcoin.message;

import java.util.LinkedList;

import org.nextcoin.accounts.Account;
import org.nextcoin.accounts.AccountsManager;
import org.nextcoin.addresses.AddressesManager;
import org.nextcoin.message.MessagesData.NxtMessage;
import org.nextcoin.nxtclient.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class ChatActivity extends Activity {

    static public void open(Context context, String myAcctId, String othersAcctId){
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("MyAcctId", myAcctId);
        intent.putExtra("OthersAcctId", othersAcctId);
        context.startActivity(intent);
    }

    private ListView talkView;
    private LinkedList<ChatMsgEntity> list = new LinkedList<ChatMsgEntity>();
    private Account mMyAccount;
    private Account mOthersAccount;
    private TextView mTextViewLabel;
    private TextView mTextViewAccountId;
    private Button mBtnSend;
    private EditText mEditMessage;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        
        Intent intent = this.getIntent();
        String myAcctId = intent.getStringExtra("MyAcctId");
        String othersAcctId = intent.getStringExtra("OthersAcctId");
        Account acct = AccountsManager.sharedInstance().getAccount(myAcctId);
        if ( null == acct ){
            this.finish();
            return;
        }
        mMyAccount = acct;
        mOthersAccount = AddressesManager.sharedInstance().getAccount(othersAcctId);
        if ( null == mOthersAccount ){
            mOthersAccount = new Account();
            mOthersAccount.mId = othersAcctId;
        }

        talkView = (ListView) findViewById(R.id.list);
        talkView.setCacheColorHint(Color.TRANSPARENT);
        
        mTextViewLabel = (TextView) findViewById(R.id.textview_label);
        mTextViewAccountId = (TextView) findViewById(R.id.textview_account_id);
        mTextViewLabel.setText(mOthersAccount.mTag);
        mTextViewAccountId.setText(mOthersAccount.mId);
        
        mEditMessage = (EditText) findViewById(R.id.edit_message);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageActivity.open(ChatActivity.this, 
                        mMyAccount.mId, mOthersAccount.mId, mEditMessage.getText().toString());
            }
        });
    }

    public void onResume() {
        super.onResume();
        loadMessages();
    }
    
    public void onDestroy() {
        super.onDestroy();
    }
    
    private LinkedList<NxtMessage> mMessageList;
    private void loadMessages(){
        mMessageList = MessagesData.sharedInstance().getMessages(mMyAccount.mId);
        list.clear();
        for ( int i = 0; i < mMessageList.size(); ++ i ){
            NxtMessage message = mMessageList.get(i);
            if (message.mSender.equals(mMyAccount.mId) && message.mRecipient.equals(mOthersAccount.mId)){
                chatAsk(message);
            }else if (message.mSender.equals(mOthersAccount.mId) && message.mRecipient.equals(mMyAccount.mId)){
                chatAnswer(message);
            }
        }
        updateUI();
    }
    
    private void updateUI(){
        talkView.setAdapter(new ChatMsgViewAdapter(ChatActivity.this, list));
    }
    
    private void chatAsk(NxtMessage message){
        ChatMsgEntity newMessage = new ChatMsgEntity(message, R.layout.list_say_me_item);
        list.addFirst(newMessage);
        //list.add(0, newMessage);
    }
    
    private void chatAnswer(NxtMessage message){
        ChatMsgEntity newMessage = new ChatMsgEntity(message, R.layout.list_say_he_item);
        list.addFirst(newMessage);
        //list.add(0, newMessage);
    }

}
