package org.nextcoin.alias;

import java.util.LinkedList;

import org.nextcoin.accounts.Account;
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

public class AliasListView extends ListView{
    //private Account mAccount;
    public void setAccount(Account acct){
        //mAccount = acct;
    }

    private LinkedList<Alias> mAliasList;
    public void setAliasList(LinkedList<Alias> list){
        mAliasList = list;
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
            if ( null == mAliasList || 0 == mAliasList.size() )
                return 1;
            
            return mAliasList.size();
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
            
            if ( null == mAliasList || 0 == mAliasList.size() ){
                TextView emptyMsg = new  TextView(AliasListView.this.getContext());
                emptyMsg.setText("");
                emptyMsg.setTag(null);
                return emptyMsg;
            }

            if (convertView == null || null == convertView.getTag() ) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.alias_list_item, null);
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.account = (TextView) convertView.findViewById(R.id.related_account);
                holder.alias = (TextView) convertView.findViewById(R.id.alias_name);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Alias alias = mAliasList.get(position);
            holder.alias.setText(alias.mName);
            holder.account.setText(alias.mUrl);

            return convertView;
        }
    }

    public final class ViewHolder {
        public ImageView img;
        public TextView account;
        public TextView alias;
    }
    
    private MyViewAdapter mMyViewAdapter;
    private void init(){
        this.setCacheColorHint(Color.TRANSPARENT);
        mMyViewAdapter = new MyViewAdapter(this.getContext());
        this.setAdapter(mMyViewAdapter);
    }

    public AliasListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public AliasListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public AliasListView(Context context) {
        super(context);
        init();
    }
}
