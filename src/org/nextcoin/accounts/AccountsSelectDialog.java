package org.nextcoin.accounts;

import java.util.LinkedList;

import org.nextcoin.nxtclient.R;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AccountsSelectDialog {
    public final static int TYPE_SELECT_SENDER_ACCOUNT = 0;
    public final static int TYPE_SELECT_RECEIVER_ACCOUNT = 1;
    
    public interface AccountReceiver{
        public void OnSelect(Account account);
    }
    private AccountReceiver mAccountReceiver;
    
    private Context mContext;
    private int mType;
    public AccountsSelectDialog(Context context, AccountReceiver receiver, int type){
        mContext = context;
        mAccountReceiver = receiver;
        mMyViewAdapter = new MyViewAdapter(mContext);
        mType = type;
    }
    
    private AlertDialog mAlertDialog;
    public void show(){
        ListView listView = new ListView(mContext);
        listView.setBackgroundColor(Color.WHITE);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setAdapter(mMyViewAdapter);
        listView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                mAccountReceiver.OnSelect(mAccountList.get(arg2));
                mAlertDialog.dismiss();
            }});
        
        mAlertDialog = new AlertDialog.Builder(mContext)
        .setTitle(R.string.select_account)
        .setView(listView)
        .setNegativeButton(R.string.back, null)
        .show();
    }

    private LinkedList<Account> mAccountList;
    public void setAccountList(LinkedList<Account> list){
        mAccountList = list;
        mMyViewAdapter.notifyDataSetChanged();
    }
    
    public void notifyDataSetInvalidated(){
        mMyViewAdapter.notifyDataSetInvalidated();
    }
    
    private class MyViewAdapter extends BaseAdapter{
        private LayoutInflater mInflater;
        public MyViewAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            if ( null == mAccountList || 0 == mAccountList.size() )
                return 1;
            
            return mAccountList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        private View getSenderView(int position, View convertView) {
            MyAccountViewHolder holder = null;
            
            if (convertView == null || null == convertView.getTag() ) {
                holder = new MyAccountViewHolder();
                convertView = mInflater.inflate(R.layout.account_list_item, null);
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.img.setVisibility(View.INVISIBLE);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.info = (TextView) convertView.findViewById(R.id.info);
                holder.balance = (TextView) convertView.findViewById(R.id.balance);
                holder.img2 = (ImageView) convertView.findViewById(R.id.img_send);
                convertView.setTag(holder);
            } else {
                holder = (MyAccountViewHolder) convertView.getTag();
            }
            
            Account acct = mAccountList.get(position);
            holder.title.setText(acct.mTag);
            holder.info.setText(acct.mId);
            holder.balance.setText(acct.getBalanceText());

            return convertView;
        }
        
        private View getReceiverView(int position, View convertView) {
            OtherAccountViewHolder holder = null;
            
            if (convertView == null || null == convertView.getTag() ) {
                holder = new OtherAccountViewHolder();
                convertView = mInflater.inflate(R.layout.address_list_item, null);
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.info = (TextView) convertView.findViewById(R.id.info);
                convertView.setTag(holder);
            } else {
                holder = (OtherAccountViewHolder) convertView.getTag();
            }
            
            Account acct = mAccountList.get(position);
            holder.title.setText(acct.mTag);
            holder.info.setText(acct.mId);

            return convertView;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if ( TYPE_SELECT_SENDER_ACCOUNT == mType )
                return getSenderView(position, convertView);
            else
                return getReceiverView(position, convertView);
        }
    }
    
    public final class MyAccountViewHolder {
        public ImageView img;
        public TextView title;
        public TextView info;
        public TextView balance;
        public ImageView img2;
    }
    
    public final class OtherAccountViewHolder {
        public ImageView img;
        public TextView title;
        public TextView info;
    }
    
    private MyViewAdapter mMyViewAdapter;

//    public static final int ICON_TYPE_RECEIVE = 0;
//    public static final int ICON_TYPE_SEND = 1;
//    public interface IconOnClickListener{
//        public void onClick(int iconType, int pos);
//    }
}
