package org.nextcoin.node;

import java.util.LinkedList;

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

public class NodesListView extends ListView{

    private LinkedList<NodeContext> mNodeList;
    public void setNodesList(LinkedList<NodeContext> list){
        mNodeList = list;
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
            if ( null == mNodeList || 0 == mNodeList.size() )
                return 1;
            
            return mNodeList.size();
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
            
            if ( null == mNodeList || 0 == mNodeList.size() ){
                TextView emptyMsg = new  TextView(NodesListView.this.getContext());
                emptyMsg.setText("");
                emptyMsg.setTag(null);
                return emptyMsg;
            }

            if (convertView == null || null == convertView.getTag() ) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.node_list_item, null);
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.addr = (TextView) convertView.findViewById(R.id.textview_addr);
                holder.blocks = (TextView) convertView.findViewById(R.id.textview_blocks);
                holder.version = (TextView) convertView.findViewById(R.id.textview_version);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            NodeContext node = mNodeList.get(position);
            
            if ( node.isActive() ){
                holder.img.setImageResource(R.drawable.flag_green);
                holder.addr.setText(node.getIP());
                holder.blocks.setText("Blocks:" + node.getBlocks());
                holder.version.setText("NRS(" + node.getVersion() + ")");
            }else{
                holder.img.setImageResource(R.drawable.flag_red);
                holder.addr.setText(node.getIP());
                holder.blocks.setText("Blocks: -- ");
                holder.version.setText("NRS(--)");
            }

            return convertView;
        }
    }

    public final class ViewHolder {
        public ImageView img;
        public TextView addr;
        public TextView blocks;
        public TextView version;
    }
    
    private MyViewAdapter mMyViewAdapter;
    private void init(){
        this.setCacheColorHint(Color.TRANSPARENT);
        mMyViewAdapter = new MyViewAdapter(this.getContext());
        this.setAdapter(mMyViewAdapter);
    }

    public NodesListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public NodesListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public NodesListView(Context context) {
        super(context);
        init();
    }
}
