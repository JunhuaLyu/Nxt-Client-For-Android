package org.nextcoin.accounts;

import java.util.LinkedList;

import org.nextcoin.nxtclient.R;
import org.nextcoin.transactions.SendCoinsActivity;
import org.nextcoin.transactions.TransactionsActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

public class AccountPage {
    private Context mContext;
    private View mAccountsPage;
    private AccountListView mAccountListView;
    public AccountPage(View accountsPage){
        mAccountsPage = accountsPage;
        mContext = mAccountsPage.getContext();
        mAccountListView = (AccountListView)mAccountsPage.findViewById(R.id.listview_accounts);
        mAccountListView.setAccountList(AccountsManager.sharedInstance().getAccountList());
        mAccountListView.setOnItemClickListener(new AccountListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                openItemMenu(arg2);
            }});
        mAccountListView.setIconOnClickListener(new AccountListView.IconOnClickListener() {
            @Override
            public void onClick(int iconType, int pos) {
                if ( AccountListView.ICON_TYPE_UNLOCK == iconType )
                    unlockAccount(pos);
                else if ( AccountListView.ICON_TYPE_SEND == iconType )
                    sendNxt(pos);
            }
        });

        Button btnAdd = (Button)mAccountsPage.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccountInputDialog();
            }
        });
        
        Button btnCreate = (Button)mAccountsPage.findViewById(R.id.btn_create);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(mContext, AccountCreateActivity.class));
            }
        });
        
        mItemOptions = new CharSequence[2];
        mItemOptions[0] = mContext.getText(R.string.transactions);
        mItemOptions[1] = mContext.getText(R.string.remove);
        
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mAccountInputView = inflater.inflate(R.layout.account_input, null);
        mEditTextId = (EditText)mAccountInputView.findViewById(R.id.edittext_accid_input);
        mEditTextTag = (EditText)mAccountInputView.findViewById(R.id.edittext_acctag_input);
    }
    
    private void unlockAccount(int pos){
        new AlertDialog.Builder(mContext)
        .setTitle(R.string.unlock)
        .setMessage(R.string.in_development)
        .setNegativeButton(R.string.back, null)
        .show();
    }

    private void sendNxt(int pos){
        Account account = AccountsManager.sharedInstance().getAccountList().get(pos);
        SendCoinsActivity.open(mContext, account.mId, "");
    }

    /**
     * items menu --- transactions, remove, etc
     */
    private CharSequence[] mItemOptions;
    private int mCurrentItemPos;
    private void openItemMenu(int pos){
        LinkedList<Account> accountList = AccountsManager.sharedInstance().getAccountList();
        if ( null == accountList || 0 == accountList.size() )
            return;
        
        mCurrentItemPos = pos;
        new AlertDialog.Builder(mContext)
        .setTitle(accountList.get(pos).mId)
        .setItems(mItemOptions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if ( 1 == which ){
                    AccountsManager.sharedInstance().removeAccount(mContext, mCurrentItemPos);
                    LinkedList<Account> accList = AccountsManager.sharedInstance().getAccountList();
                    mAccountListView.setAccountList(accList);
                }else if ( 0 == which ){
                    TransactionsActivity.open(mContext, mCurrentItemPos);
                }
            }
        })
        .show();
    }
    
    private View mAccountInputView;
    private AlertDialog mAccountInputDialog;
    private EditText mEditTextId;
    private EditText mEditTextTag;
    private void openAccountInputDialog(){
        mEditTextId.setText("");
        mEditTextTag.setText("");
        
        if ( null == mAccountInputDialog ){
            mAccountInputDialog = new AlertDialog.Builder(mContext)
            .setTitle(R.string.add)
            .setView(mAccountInputView)
            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    String accid = mEditTextId.getText().toString();
                    if ( accid.length() <= 0 )
                        return;
                    
                    String tag = mEditTextTag.getText().toString();
                    if ( tag.length() <= 0 )
                        tag = "null";
                    
                    AccountsManager.sharedInstance().addAccount(
                            mAccountsPage.getContext(), accid, tag);
                    mAccountListView.setAccountList(
                            AccountsManager.sharedInstance().getAccountList());
                    update(AccountsManager.sharedInstance().getAccountList().getLast());
                }})
            .setNegativeButton(R.string.back, null)
            .create();
        }
        mAccountInputDialog.show();
    }
    
    private AccountsInfoHelper.ResponseListener mResponseListener = 
            new AccountsInfoHelper.ResponseListener() {
        @Override
        public void onResponse(boolean success, Account account, String info) {
            if ( success )
                mAccountListView.notifyDataSetInvalidated();
        }
    };

    public void update(Account account){
        new AccountsInfoHelper().requestAccountInfo(account, mResponseListener);
    }
    
    public void update(){
        new AccountsInfoHelper().requestAccountsInfo(
                AccountsManager.sharedInstance().getAccountList(), mResponseListener);
    }
    
    public void release(){
        
    }
}
