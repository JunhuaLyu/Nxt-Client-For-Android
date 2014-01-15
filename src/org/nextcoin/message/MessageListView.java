package org.nextcoin.message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import org.nextcoin.accounts.Account;
import org.nextcoin.addresses.AddressesManager;
import org.nextcoin.message.MessagesData.NxtMessage;
import org.nextcoin.nxtclient.R;
import org.nextcoin.util.NxtUtil;

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

public class MessageListView extends ListView{
    private Account mAccount;
    public void setAccount(Account acct){
        mAccount = acct;
    }

    private HashMap<String, Integer> mMsgCountMap;
    public void setMessageCounts(HashMap<String, Integer> msgCountMap){
        mMsgCountMap = msgCountMap;
    }
    
    private LinkedList<NxtMessage> mNxtMessageList;
    public void setMessageList(LinkedList<NxtMessage> list){
        mNxtMessageList = list;
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
            if ( null == mNxtMessageList || 0 == mNxtMessageList.size() )
                return 1;
            
            return mNxtMessageList.size();
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
            
            if ( null == mNxtMessageList || 0 == mNxtMessageList.size() ){
                TextView emptyMsg = new  TextView(MessageListView.this.getContext());
                emptyMsg.setText("");
                emptyMsg.setTag(null);
                return emptyMsg;
            }

            if (convertView == null || null == convertView.getTag() ) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.message_list_item, null);
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.name = (TextView) convertView.findViewById(R.id.name);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            NxtMessage msg = mNxtMessageList.get(position);
            String label;
            Context context = convertView.getContext();
            int count = 0;
            if ( mAccount.mId.equals(msg.mSender) ){
                count = mMsgCountMap.get(msg.mRecipient).intValue();
                label = AddressesManager.sharedInstance().getTag(msg.mRecipient);
                if ( null == label || label.equals(" ") )
                    label = msg.mRecipient;
                
                label = context.getText(R.string.to) + ": " + label;
            }else{
                count = mMsgCountMap.get(msg.mSender).intValue();
                label = AddressesManager.sharedInstance().getTag(msg.mSender);
                if ( null == label || label.equals(" ") )
                    label = msg.mSender;
                
                label = context.getText(R.string.from) + ": " + label;
            }
            holder.name.setText(label + " (" + count + ")");
            holder.content.setText(msg.getText());

            long start = NxtUtil.getStartTime() + ((long)msg.mTimestamp) * 1000;
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date date = new Date(start);
            holder.date.setText(df.format(date));
            
            return convertView;
        }
    }

    public final class ViewHolder {
        public ImageView img;
        public TextView name;
        public TextView content;
        public TextView date;
    }
    
    private MyViewAdapter mMyViewAdapter;
    private void init(){
        this.setCacheColorHint(Color.TRANSPARENT);
        mMyViewAdapter = new MyViewAdapter(this.getContext());
        this.setAdapter(mMyViewAdapter);
    }

    public MessageListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public MessageListView(Context context) {
        super(context);
        init();
    }
}
