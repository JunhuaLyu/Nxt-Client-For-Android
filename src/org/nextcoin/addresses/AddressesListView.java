package org.nextcoin.addresses;

import java.util.LinkedList;

import org.nextcoin.accounts.Account;
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

public class AddressesListView extends ListView{
    
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
                TextView emptyMsg = new  TextView(AddressesListView.this.getContext());
                emptyMsg.setText(R.string.address_list_empty_msg);
                emptyMsg.setTextColor(Color.GRAY);
                emptyMsg.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                emptyMsg.setPadding(20, 20, 20, 20);
                emptyMsg.setTag(null);
                return emptyMsg;
            }

            if (convertView == null || null == convertView.getTag() ) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.address_list_item, null);
                holder.img = (ImageView) convertView.findViewById(R.id.img_send);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.info = (TextView) convertView.findViewById(R.id.info);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            
            Account acct = mAccountList.get(position);
            holder.title.setText(acct.mTag);
            holder.info.setText(acct.mId);
            holder.img.setId(position);
            holder.img.setOnClickListener(mSendOnClickListener);
            holder.img.setImageResource(R.drawable.receive_nxt);
//            if ( null != acct.mImg && acct.mImg.length() > 3 ){
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

            return convertView;
        }
    }
    
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
    }
    
    private MyViewAdapter mMyViewAdapter;
    private void init(){
        this.setCacheColorHint(Color.TRANSPARENT);
        mMyViewAdapter = new MyViewAdapter(this.getContext());
        this.setAdapter(mMyViewAdapter);
    }

    public AddressesListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public AddressesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public AddressesListView(Context context) {
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
