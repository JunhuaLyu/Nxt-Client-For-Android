package org.nextcoin.transactions;

import java.util.LinkedList;

import org.nextcoin.accounts.Account;
import org.nextcoin.addresses.AddressesManager;
import org.nextcoin.nxtclient.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TransactionListView extends ListView{
    private Account mAccount;
    public void setAccount(Account acct){
        mAccount = acct;
    }

    private LinkedList<Transaction> mTransactionList;
    public void setTransactionList(LinkedList<Transaction> list){
        mTransactionList = list;
        //mMyViewAdapter.notifyDataSetChanged();
    }
    
    public void notifyDataSetChanged(){
        mMyViewAdapter.notifyDataSetChanged();
    }

    private class MyViewAdapter extends BaseAdapter{
        private LayoutInflater mInflater;
        public MyViewAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if ( null == mTransactionList || 0 == mTransactionList.size() )
                return 1;
            
            return mTransactionList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            
            if ( null == mTransactionList || 0 == mTransactionList.size() ){
                TextView emptyMsg = new  TextView(TransactionListView.this.getContext());
                emptyMsg.setText("");
                emptyMsg.setTag(null);
                return emptyMsg;
            }

            if (convertView == null || null == convertView.getTag() ) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.transaction_list_item, null);
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.amount = (TextView) convertView.findViewById(R.id.amount);
                holder.fee = (TextView) convertView.findViewById(R.id.fee);
                holder.confirm = (TextView) convertView.findViewById(R.id.confirm);
                holder.account = (TextView) convertView.findViewById(R.id.related_account);
                holder.tag = (TextView) convertView.findViewById(R.id.tag);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            Transaction transaction = mTransactionList.get(position);
            holder.amount.setText("" + transaction.mAmount);
            holder.fee.setText("" + transaction.mFee);
                
            if ( transaction.mConfirmations <= 10 )
                holder.confirm.setText("" + transaction.mConfirmations);
            else
                holder.confirm.setText("10+");

            if ( mAccount.mId.equals(transaction.mSender) ){
                holder.account.setText(transaction.mRecipient);
                holder.img.setImageResource(R.drawable.out);
                holder.tag.setText(AddressesManager.sharedInstance().getTag(transaction.mRecipient));
            }else{
                holder.account.setText(transaction.mSender);
                holder.img.setImageResource(R.drawable.in);
                holder.tag.setText(AddressesManager.sharedInstance().getTag(transaction.mSender));
            }

            return convertView;
        }
    }

    public final class ViewHolder {
        public ImageView img;
        public TextView amount;
        public TextView fee;
        public TextView confirm;
        public TextView account;
        public TextView tag;
    }
    
    private MyViewAdapter mMyViewAdapter;
    private void init(){
        this.setCacheColorHint(Color.TRANSPARENT);
        mMyViewAdapter = new MyViewAdapter(this.getContext());
        this.setAdapter(mMyViewAdapter);
    }

    public TransactionListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public TransactionListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public TransactionListView(Context context) {
        super(context);
        init();
    }
}
