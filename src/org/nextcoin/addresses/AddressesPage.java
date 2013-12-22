package org.nextcoin.addresses;

import java.util.LinkedList;

import org.nextcoin.accounts.Account;
import org.nextcoin.nxtclient.R;
import org.nextcoin.transactions.SendCoinsActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

public class AddressesPage {
    private Context mContext;
    private View mAddressesPage;
    private AddressesListView mAddressesListView;
    public AddressesPage(View addressesPage){
        mAddressesPage = addressesPage;
        mContext = mAddressesPage.getContext();
        mAddressesListView = (AddressesListView)mAddressesPage.findViewById(R.id.listview_addresses);
        mAddressesListView.setAccountList(AddressesManager.sharedInstance().getAccountList());
        mAddressesListView.setOnItemClickListener(new AddressesListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                openItemMenu(arg2);
            }});
        mAddressesListView.setIconOnClickListener(new AddressesListView.IconOnClickListener() {
            @Override
            public void onClick(int iconType, int pos) {
                if ( AddressesListView.ICON_TYPE_SEND == iconType )
                    sendNxt(pos);
            }
        });

        Button btnAdd = (Button)mAddressesPage.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccountInputDialog(false, 0);
            }
        });
        
        mItemOptions = new CharSequence[3];
        mItemOptions[0] = mContext.getText(R.string.send_nxt);
        mItemOptions[1] = mContext.getText(R.string.edit);
        mItemOptions[2] = mContext.getText(R.string.remove);
        
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mAccountInputView = inflater.inflate(R.layout.account_input, null);
        mEditTextId = (EditText)mAccountInputView.findViewById(R.id.edittext_accid_input);
        mEditTextTag = (EditText)mAccountInputView.findViewById(R.id.edittext_acctag_input);
    }
    
    private void sendNxt(int pos){
        Account account = AddressesManager.sharedInstance().getAccountList().get(pos);
        SendCoinsActivity.open(mContext, "", account.mId);
    }

    /**
     * items menu --- transactions, remove, etc
     */
    private CharSequence[] mItemOptions;
    private int mCurrentItemPos;
    private void openItemMenu(int pos){
        LinkedList<Account> accountList = AddressesManager.sharedInstance().getAccountList();
        if ( null == accountList || 0 == accountList.size() )
            return;
        
        mCurrentItemPos = pos;
        new AlertDialog.Builder(mContext)
        .setTitle(accountList.get(pos).mId)
        .setItems(mItemOptions, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if ( 2 == which ){
                    AddressesManager.sharedInstance().removeAccount(mContext, mCurrentItemPos);
                    LinkedList<Account> accList = AddressesManager.sharedInstance().getAccountList();
                    mAddressesListView.setAccountList(accList);
                }else if ( 1 == which ){
                    openAccountInputDialog(true, mCurrentItemPos);
                }else if ( 0 == which ){
                    sendNxt(mCurrentItemPos);
                }
            }
        })
        .show();
    }
    
    private View mAccountInputView;
    private AlertDialog mAccountInputDialog;
    private EditText mEditTextId;
    private EditText mEditTextTag;
    private boolean mIsEdit;
    private Account mEditAccount;
    private void openAccountInputDialog(boolean isEdit, int pos){
        mIsEdit = isEdit;
        
        if ( mIsEdit ){
            mEditAccount = AddressesManager.sharedInstance().getAccountList().get(pos);
            mEditTextId.setText(mEditAccount.mId);
            mEditTextTag.setText(mEditAccount.mTag);
        }else{
            mEditTextId.setText("");
            mEditTextTag.setText("");
        }
        
        
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
                    
                    if ( mIsEdit ){
                        mEditAccount.mId = accid;
                        mEditAccount.mTag = tag;
                        AddressesManager.sharedInstance().saveAccountList(mContext);
                        mAddressesListView.notifyDataSetInvalidated();
                    }else{
                        AddressesManager.sharedInstance().addAccount(
                                mContext, accid, tag);
                        mAddressesListView.setAccountList(
                                AddressesManager.sharedInstance().getAccountList());
                    }
                }})
            .setNegativeButton(R.string.back, null)
            .create();
        }
        mAccountInputDialog.show();
    }
    
    public void update(){
    }
    
    public void release(){
        
    }
}
