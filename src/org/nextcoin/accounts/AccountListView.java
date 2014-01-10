package org.nextcoin.accounts;

import java.util.LinkedList;

import org.nextcoin.nxtclient.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AccountListView extends ListView{
    
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            
            if ( null == mAccountList || 0 == mAccountList.size() ){
                TextView emptyMsg = new  TextView(AccountListView.this.getContext());
                emptyMsg.setText(R.string.account_list_empty_msg);
                emptyMsg.setTextColor(Color.GRAY);
                emptyMsg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                emptyMsg.setPadding(20, 20, 20, 20);
                emptyMsg.setTag(null);
                return emptyMsg;
            }

            if (convertView == null || null == convertView.getTag() ) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.account_list_item, null);
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.info = (TextView) convertView.findViewById(R.id.info);
                holder.balance = (TextView) convertView.findViewById(R.id.balance);
                holder.img2 = (ImageView) convertView.findViewById(R.id.img_send);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            Account acct = mAccountList.get(position);
            holder.title.setText(acct.mTag);
            holder.info.setText(acct.mId);
            holder.balance.setText(acct.getBalanceText());
            
            holder.img.setId(position);
//            holder.img.setImageResource(R.drawable.lock);
//            if ( null != acct.mImg ){
//                try {
//                    URL picUrl;
//                    if ( acct.mImg.startsWith("http://") )
//                        picUrl = new URL(acct.mImg);
//                    else
//                        picUrl = new URL("http://" + acct.mImg);
//                    Bitmap pngBM = BitmapFactory.decodeStream(picUrl.openStream()); 
//                    holder.img.setImageBitmap(pngBM);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            
            holder.img.setOnClickListener(mUnlockOnClickListener);
            holder.img2.setId(position);
            holder.img2.setOnClickListener(mSendOnClickListener);

            return convertView;
        }
    }
    
    private View.OnClickListener mUnlockOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if ( null != mIconOnClickListener )
                mIconOnClickListener.onClick(ICON_TYPE_UNLOCK, v.getId());
        }
    };

    private View.OnClickListener mSendOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            if ( null != mIconOnClickListener )
                mIconOnClickListener.onClick(ICON_TYPE_SEND, v.getId());
        }
    };

    public final class ViewHolder {
        public ImageView img;
        public TextView title;
        public TextView info;
        public TextView balance;
        public ImageView img2;
    }
    
    private MyViewAdapter mMyViewAdapter;
    private void init(){
        this.setCacheColorHint(Color.TRANSPARENT);
        mMyViewAdapter = new MyViewAdapter(this.getContext());
        this.setAdapter(mMyViewAdapter);
    }

    public AccountListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public AccountListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public AccountListView(Context context) {
        super(context);
        init();
    }
    
    public static final int ICON_TYPE_UNLOCK = 0;
    public static final int ICON_TYPE_SEND = 1;
    public interface IconOnClickListener{
        public void onClick(int iconType, int pos);
    }
    
    private IconOnClickListener mIconOnClickListener;
    public void setIconOnClickListener(IconOnClickListener l){
        mIconOnClickListener = l;
    }
}
