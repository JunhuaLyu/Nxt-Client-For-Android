package org.nextcoin.transactions;

import java.util.LinkedList;

import org.nextcoin.accounts.Account;
import org.nextcoin.accounts.AccountsInfoHelper;
import org.nextcoin.accounts.AccountsManager;
import org.nextcoin.addresses.AddressesManager;
import org.nextcoin.nxtclient.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class TransactionsActivity extends Activity {

    private AccountsInfoHelper.ResponseListener mResponseListener = 
            new AccountsInfoHelper.ResponseListener() {
        @Override
        public void onResponse(boolean success, Account account, String info) {
            if ( success ){
                for ( Transaction transaction : account.mTransactionList ){
                    Transaction.loadTransaction(transaction);
                }
                Transaction.sortByTimestamp(account.mTransactionList);
                mTransactionListView.setTransactionList(account.mTransactionList);
                mTransactionListView.notifyDataSetChanged();
            }
        }
    };
    
    static public void open(Context context, int accountPos){
        Intent intent = new Intent(context, TransactionsActivity.class);
        intent.putExtra("AccountPos", accountPos);
        context.startActivity(intent);
    }

    private Account mAccount;
    private TransactionListView mTransactionListView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transactions_activity);
        int accountPos = this.getIntent().getIntExtra("AccountPos", 0);
        mAccount = AccountsManager.sharedInstance().getAccountList().get(accountPos);
        
        TextView textViewAccountId = (TextView)this.findViewById(R.id.textview_account_id);
        textViewAccountId.setText(mAccount.mId);
        
        TextView textViewAccountBalance = (TextView)this.findViewById(R.id.textview_account_balance);
        textViewAccountBalance.setText("Balance:  " + mAccount.getBalanceText());
        
        Button btnSend = (Button)this.findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendCoinsActivity.open(TransactionsActivity.this, mAccount.mId, "");
            }
        });
        
        mTransactionListView = 
                (TransactionListView)this.findViewById(R.id.listview_transaction);
        mTransactionListView.setAccount(mAccount);
        new AccountsInfoHelper().requestTransactionHistory(mAccount, mResponseListener);
        
        mItemOptions = new CharSequence[1];
        mItemOptions[0] = this.getText(R.string.save_to_address);
        mTransactionListView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                openItemMenu(arg2);
            }});
    }
    
    private CharSequence[] mItemOptions;
    private int mCurrentItemPos;
    private void openItemMenu(int pos){
        LinkedList<Transaction> transactionList = mAccount.mTransactionList;
        if ( null == transactionList || 0 == transactionList.size() )
            return;
        
        String title;
        if ( mAccount.mId.equals(transactionList.get(pos).mSender) )
            title = transactionList.get(pos).mRecipient;
        else
            title = transactionList.get(pos).mSender;
        mCurrentItemPos = pos;
        new AlertDialog.Builder(TransactionsActivity.this)
        .setTitle(title)
        .setItems(mItemOptions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if ( 0 == which ){
                    saveToAddressBook(mCurrentItemPos);
                }
            }
        })
        .show();
    }
    
    private void saveToAddressBook(int pos){
        LinkedList<Transaction> transactionList = mAccount.mTransactionList;
        String accountID;
        Transaction transaction = transactionList.get(pos);
        if ( mAccount.mId.equals(transaction.mSender) )
            accountID = transaction.mRecipient;
        else
            accountID = transaction.mSender;
        AddressesManager.sharedInstance().addAccount(this, accountID, null);
    }
    
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
