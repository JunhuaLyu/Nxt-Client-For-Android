package org.nextcoin.message;

import java.util.HashMap;
import java.util.LinkedList;

import org.nextcoin.accounts.Account;
import org.nextcoin.accounts.AccountUnlockDialog;
import org.nextcoin.accounts.AccountsInfoHelper;
import org.nextcoin.accounts.AccountsManager;
import org.nextcoin.message.MessagesData.NxtMessage;
import org.nextcoin.nxtclient.R;
import org.nextcoin.nxtclient.SafeBox;
import org.nextcoin.transactions.NxtTransaction;
import org.nextcoin.transactions.Transaction;
import org.nextcoin.util.NxtUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.other.util.ProgressDialogExt;

public class MessageActivity extends Activity {

    static public void open(Context context, String accountId){
        Intent intent = new Intent(context, MessageActivity.class);
        intent.putExtra("AccountId", accountId);
        context.startActivity(intent);
    }
    
    //private LinkedList<Transaction> mMessageList;
    private void loadTransactionsData(){
        new Thread(new Runnable(){
            @Override
            public void run() {
                Account account = mAccount;
                int size = account.mTransactionList.size();
                int i;
                for ( i = 0; i < size; i ++ ){
                    if ( null == mLoadingProgressDialogExt )
                        return;
                    setProgressDialog((i + 1) * 100 / size);
                    Transaction.loadTransaction(account.mTransactionList.get(i));
                }

                for ( Transaction transaction : account.mTransactionList ){
                    if ( NxtTransaction.TYPE_MESSAGING == transaction.mType 
                        && NxtTransaction.SUBTYPE_MESSAGING_ARBITRARY_MESSAGE == transaction.mSubType ){
                        MessagesData.sharedInstance().saveMessage(new MessagesData.NxtMessage(
                                transaction.mId, transaction.mSender, transaction.mRecipient, 
                                ((ArbitraryMessage)transaction.mAttachment).mHex, transaction.mTimestamp));
                    }
                }

                //Transaction.sortByTimestamp(mMessageList);
                //mTransactionListViewOther.setTransactionList(mMessageList);
                dismissProgressDialog();
            }}).start();
    }

    private AccountsInfoHelper.ResponseListener mResponseListener = 
            new AccountsInfoHelper.ResponseListener() {
        @Override
        public void onResponse(boolean success, Account account, String info) {
            if ( success ){
                loadTransactionsData();
                MessagesData.sharedInstance().saveTimestamp(mAccount.mId, NxtUtil.getTimestamp());
            }else{
                dismissProgressDialog();
            }
        }
    };
    
    private void transactionsRequest(int timestamp){
        new AccountsInfoHelper().requestTransactionHistoryWithTimestamp(
                mAccount, mResponseListener, timestamp);
        showProgressDialog();
    }
    
    private LinkedList<MessagesData.NxtMessage> mMessageList;
    private void loadMessageList(){
        LinkedList<MessagesData.NxtMessage> allList = 
                MessagesData.sharedInstance().getMessages(mAccount.mId);
        
        HashMap<String, MessagesData.NxtMessage> msgMap = new HashMap<String, MessagesData.NxtMessage>();
        HashMap<String, Integer> msgCountMap = new HashMap<String, Integer>();
        
        for ( MessagesData.NxtMessage msg : allList ){
            String key;
            if ( msg.mSender.equals(mAccount.mId) )
                key = msg.mRecipient;
            else
                key = msg.mSender;
            
            NxtMessage message = msgMap.get(key);
            if ( null == message ){
                msgMap.put(key, msg);
                msgCountMap.put(key, Integer.valueOf(1));
            }else{
                Integer count = msgCountMap.get(key);
                int countInt = count.intValue() + 1;
                msgCountMap.put(key, Integer.valueOf(countInt));
            }
        }
        
        mMessageList = new LinkedList<MessagesData.NxtMessage>();
        for (HashMap.Entry<String, NxtMessage> msgEntry : msgMap.entrySet()) {
            mMessageList.addLast(msgEntry.getValue());
        }

        MessagesData.sortByTimestamp(mMessageList);
        decodeMessages();
        mMessageListView.setMessageList(mMessageList);
        mMessageListView.setMessageCounts(msgCountMap);
        mMessageListView.notifyDataSetChanged();
    }
    
    private void decodeMessages(){
        if ( null != mMessageList )
            MessagesData.decodeMessage(mMessageList, mAccount.mId);
    }

    private MessageListView mMessageListView;
    private Account mAccount;
    private ImageView mImgUnlock;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messages_activity);

        String accId = this.getIntent().getStringExtra("AccountId");
        Account acct = AccountsManager.sharedInstance().getAccount(accId);
        
        if ( null == acct ){
            this.finish();
            return;
        }
        
        mAccount = acct.clone();
        TextView textViewAccountId = (TextView)this.findViewById(R.id.textview_account_id);
        textViewAccountId.setText(mAccount.mId);
        
        TextView textViewAccountBalance = (TextView)this.findViewById(R.id.textview_account_balance);
        textViewAccountBalance.setText("Balance:  " + mAccount.getBalanceText());
        
        Button btnSend = (Button)this.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageActivity.open(MessageActivity.this, mAccount.mId, "", "");
            }
        });
        
        mImgUnlock = (ImageView) this.findViewById(R.id.img_lock);
        mImgUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !SafeBox.sharedInstance().isUnlock(mAccount.mId) ){
                    new AccountUnlockDialog().openUnlockDialog(MessageActivity.this, mAccount.mId, 
                            new AccountUnlockDialog.ResponseListener() {
                        @Override
                        public void onResponse(boolean success, String info) {
                            if ( success ){
                                updateUI();
                            }else{
                                Toast.makeText(MessageActivity.this, info, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }else{
                    SafeBox.sharedInstance().lock(mAccount.mId);
                    updateUI();
                }
            }
        });
        
        mMessageListView = (MessageListView)this.findViewById(R.id.listview_message);
        mMessageListView.setAccount(mAccount);
        mMessageListView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                if ( null == mMessageList || mMessageList.size() <= arg2 )
                    return;

                MessagesData.NxtMessage message = mMessageList.get(arg2);
                String othersId;
                if ( mAccount.mId.equals(message.mSender) )
                    othersId = message.mRecipient;
                else
                    othersId = message.mSender;
                ChatActivity.open(MessageActivity.this, mAccount.mId, othersId);
            }});
        
        int timestamp = MessagesData.sharedInstance().getLastTimestamp(mAccount.mId) - 1440 * 60;
        if ( timestamp < 0 )
            timestamp = 0;
        transactionsRequest(timestamp);
        updateUI();
    }
    
    private void updateUI(){
        decodeMessages();
        if ( SafeBox.sharedInstance().isUnlock(mAccount.mId) )
            mImgUnlock.setImageResource(R.drawable.unlock);
        else
            mImgUnlock.setImageResource(R.drawable.lock);
        mMessageListView.notifyDataSetChanged();
    }

    /**
     *  Progress Dialog
     */
    private ProgressDialogExt mLoadingProgressDialogExt;
    public void showProgressDialog(){
        if ( null == mLoadingProgressDialogExt ){
            mLoadingProgressDialogExt = new ProgressDialogExt(this);
            mLoadingProgressDialogExt.setTitle(R.string.loading);
            mLoadingProgressDialogExt.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mLoadingProgressDialogExt.setMax(100);
            mLoadingProgressDialogExt.setCancelable(false);
            mLoadingProgressDialogExt.setButton(DialogInterface.BUTTON_NEGATIVE, 
                    this.getText(R.string.back), new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog,
                        int which) {
                    mLoadingProgressDialogExt.dismiss();
                    mLoadingProgressDialogExt = null;
                }
            });
        }
        mLoadingProgressDialogExt.setProgress(0);
        mLoadingProgressDialogExt.show();
        mLoadingProgressDialogExt.setOnDismissListener(new ProgressDialog.OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
                loadMessageList();
            }});
    }
    
    public void dismissProgressDialog(){
        if ( null != mLoadingProgressDialogExt ){
            mLoadingProgressDialogExt.postDismiss();
            mLoadingProgressDialogExt = null;
        }
    }
    
    public void setProgressDialog(int progress){
        if ( null != mLoadingProgressDialogExt ){
            mLoadingProgressDialogExt.postSetProgress(progress);
        }
    }
}
