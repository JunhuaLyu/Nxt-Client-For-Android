package org.nextcoin.transactions;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nextcoin.accounts.Account;
import org.nextcoin.accounts.AccountsInfoHelper;
import org.nextcoin.accounts.AccountsManager;
import org.nextcoin.addresses.AddressesManager;
import org.nextcoin.nxtclient.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.other.util.ProgressDialogExt;

public class TransactionsActivity extends Activity {

    static public void open(Context context, int accountPos){
        Intent intent = new Intent(context, TransactionsActivity.class);
        intent.putExtra("AccountPos", accountPos);
        context.startActivity(intent);
    }

    private AccountsInfoHelper.ResponseListener mResponseListener = 
            new AccountsInfoHelper.ResponseListener() {
        @Override
        public void onResponse(boolean success, Account account, String info) {
            if ( success ){
                loadTransactionsData();
            }else{
                dismissProgressDialog();
            }
        }
    };
    
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

                LinkedList<Transaction> listTotal = new LinkedList<Transaction>();
                LinkedList<Transaction> listIn = new LinkedList<Transaction>();
                LinkedList<Transaction> listOut = new LinkedList<Transaction>();
                LinkedList<Transaction> listOther = new LinkedList<Transaction>();
                for ( Transaction transaction : account.mTransactionList ){
                    if ( NxtTransaction.TYPE_PAYMENT == transaction.mType 
                            && NxtTransaction.SUBTYPE_PAYMENT_ORDINARY_PAYMENT == transaction.mSubType ){
                        listTotal.add(transaction);
                        if ( transaction.mSender.equals(account.mId) )
                            listOut.add(transaction);
                        else if ( transaction.mRecipient.equals(account.mId) )
                            listIn.add(transaction);
                    }
                    else if ( NxtTransaction.TYPE_MESSAGING == transaction.mType 
                        && NxtTransaction.SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT == transaction.mSubType ){
                        listTotal.add(transaction);
                        listOther.add(transaction);
                    }
                }

                account.mTransactionList = listTotal;
                Transaction.sortByTimestamp(account.mTransactionList);
                Transaction.sortByTimestamp(listIn);
                Transaction.sortByTimestamp(listOut);
                Transaction.sortByTimestamp(listOther);
                mTransactionListView.setTransactionList(account.mTransactionList);
                mTransactionListViewIn.setTransactionList(listIn);
                mTransactionListViewOut.setTransactionList(listOut);
                mTransactionListViewOther.setTransactionList(listOther);
                dismissProgressDialog();
            }}).start();
    }
    
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
                mTransactionListView.notifyDataSetChanged();
                mTransactionListViewIn.notifyDataSetChanged();
                mTransactionListViewOut.notifyDataSetChanged();
                mTransactionListViewOther.notifyDataSetChanged();
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
    
    private int mLastRequestDays = -1;
    private void transactionsRequest(int days){
        if ( mLastRequestDays != days ){
            mLastRequestDays = days;
            new AccountsInfoHelper().requestTransactionHistory(mAccount, mResponseListener, days);
            showProgressDialog();
        }
    }
    
    private Account mAccount;
    private TransactionListView mTransactionListView;
    private TransactionListView mTransactionListViewIn;
    private TransactionListView mTransactionListViewOut;
    private TransactionListView mTransactionListViewOther;
    private ViewPager mViewPager;
    private List<View> mViewList;
    private View imageView;
    private int offset = 0; // animation offset
    private int currIndex = 0; // current page
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transactions_activity);
        int accountPos = this.getIntent().getIntExtra("AccountPos", -1);
        int days = 30;
        if ( -1 == accountPos ){
            String accId = this.getIntent().getStringExtra("AccountId");
            mAccount = AccountsManager.sharedInstance().getAccount(accId);
            days = 7;
        }
        else
            mAccount = AccountsManager.sharedInstance().getAccountList().get(accountPos);
        
        if ( null == mAccount ){
            this.finish();
            return;
        }

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
        
        mTransactionListView = new TransactionListView(this);
        mTransactionListView.setAccount(mAccount);
        mTransactionListViewIn = new TransactionListView(this);
        mTransactionListViewIn.setAccount(mAccount);
        mTransactionListViewOut = new TransactionListView(this);
        mTransactionListViewOut.setAccount(mAccount);
        mTransactionListViewOther = new TransactionListView(this);
        mTransactionListViewOther.setAccount(mAccount);
        
        mViewPager = (ViewPager)this.findViewById(R.id.viewpager);
        mViewList = new ArrayList<View>();
        mViewList.add(mTransactionListView);
        mViewList.add(mTransactionListViewIn);
        mViewList.add(mTransactionListViewOut);
        mViewList.add(mTransactionListViewOther);
        mViewPager.setAdapter(mPagerAdapter);
//        mTransactionListView = 
//                (TransactionListView)this.findViewById(R.id.listview_transaction);

        mItemOptions = new CharSequence[1];
        mItemOptions[0] = this.getText(R.string.save_to_address);
        mTransactionListView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                openItemMenu(arg2);
            }});
        
        Spinner spinnerPeriod = (Spinner)this.findViewById(R.id.spinner_period);
        String options[] = new String[3];
        options[0] = (String)this.getText(R.string.week1);
        options[1] = (String)this.getText(R.string.month1);
        options[2] = (String)this.getText(R.string.total);
        ArrayAdapter<String> spinnerAdapter = 
                new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, options);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(spinnerAdapter);
        if ( 30 == days )
            spinnerPeriod.setSelection(1);
        spinnerPeriod.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                    int arg2, long arg3) {
                if ( 0 == arg2 )
                    transactionsRequest(7);
                else if ( 1 == arg2 )
                    transactionsRequest(30);
                else if ( 2 == arg2 )
                    transactionsRequest(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }});
        
        TextView tabTextView = (TextView)this.findViewById(R.id.tab_total);
        tabTextView.setOnClickListener(mTabOnClickListener);
        tabTextView = (TextView)this.findViewById(R.id.tab_in);
        tabTextView.setOnClickListener(mTabOnClickListener);
        tabTextView = (TextView)this.findViewById(R.id.tab_out);
        tabTextView.setOnClickListener(mTabOnClickListener);
        tabTextView = (TextView)this.findViewById(R.id.tab_other);
        tabTextView.setOnClickListener(mTabOnClickListener);
        
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        
        imageView= (View)this.findViewById(R.id.cursor);
        DisplayMetrics dm = new DisplayMetrics();  
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);  
        offset = dm.widthPixels / 4;
        
        transactionsRequest(days);
    }

    private static final int TAB_ID_TOTAL = 0;
    private static final int TAB_ID_IN = 1;
    private static final int TAB_ID_OUT = 2;
    private static final int TAB_ID_OTHER = 3;
    
    private View.OnClickListener mTabOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            int id;
            switch(v.getId()){
            case R.id.tab_total:
                id = TAB_ID_TOTAL;
                break;
            case R.id.tab_in:
                id = TAB_ID_IN;
                break;
            case R.id.tab_out:
                id = TAB_ID_OUT;
                break;
            case R.id.tab_other:
                id = TAB_ID_OTHER;
                break;
            default:
                return;
            }
            
            selectPage(id);
        }
    };

    private void selectPage(int id){
        mViewPager.setCurrentItem(id);
    }
    
    private PagerAdapter mPagerAdapter = new PagerAdapter() {  
        
        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {  
            return arg0 == arg1;  
        }  

        @Override  
        public int getCount() {  
            return mViewList.size();  
        }  

        @Override  
        public void destroyItem(ViewGroup container, int position,  
                Object object) {  
            container.removeView(mViewList.get(position));  
        }  

        @Override  
        public int getItemPosition(Object object) {  
            return super.getItemPosition(object);  
        }  

        @Override  
        public CharSequence getPageTitle(int position) {  
            return " ";  
        }  

        @Override  
        public Object instantiateItem(ViewGroup container, int position) {  
            container.addView(mViewList.get(position));  
            return mViewList.get(position);  
        }  

    };

    private class MyOnPageChangeListener implements OnPageChangeListener{  

        public void onPageScrollStateChanged(int arg0) {  
        }  

        public void onPageScrolled(int arg0, float arg1, int arg2) {  
        }  
  
        public void onPageSelected(int arg0) {  
            Animation animation = new TranslateAnimation(offset * currIndex, offset * arg0, 0, 0);
            currIndex = arg0;
            animation.setFillAfter(true);  
            animation.setDuration(300);  
            imageView.startAnimation(animation);
        }
    }     
    
    private CharSequence[] mItemOptions;
    private int mCurrentItemPos;
    private void openItemMenu(int pos){
        LinkedList<Transaction> transactionList = mAccount.mTransactionList;
        if ( null == transactionList || 0 == transactionList.size() )
            return;
        
        if ( NxtTransaction.TYPE_PAYMENT != transactionList.get(pos).mType )
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
