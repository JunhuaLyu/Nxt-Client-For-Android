package org.nextcoin.news;

import java.util.LinkedList;

import org.nextcoin.nxtclient.R;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NewsListView extends ListView{

    private LinkedList<News> mNewsList;
    public void setNewsList(LinkedList<News> list){
        mNewsList = list;
    }
    
    public LinkedList<News> getList(){
        return mNewsList;
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
            if ( null == mNewsList || 0 == mNewsList.size() )
                return 1;
            
            return mNewsList.size();
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
            
            if ( null == mNewsList || 0 == mNewsList.size() ){
                TextView emptyMsg = new  TextView(NewsListView.this.getContext());
                emptyMsg.setText("");
                emptyMsg.setTag(null);
                return emptyMsg;
            }

            if (convertView == null || null == convertView.getTag() ) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.news_list_item, null);
                holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.excerpt = (TextView) convertView.findViewById(R.id.excerpt);
                holder.date = (TextView) convertView.findViewById(R.id.date);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            News news = mNewsList.get(position);
            holder.title.setText(news.mTitle);
            holder.excerpt.setText(Html.fromHtml(news.mExcerpt));
            holder.date.setText(news.mDate);

            return convertView;
        }
    }

    public final class ViewHolder {
        public ImageView img;
        public TextView title;
        public TextView excerpt;
        public TextView date;
    }
    
    private MyViewAdapter mMyViewAdapter;
    private void init(){
        this.setCacheColorHint(Color.TRANSPARENT);
        mMyViewAdapter = new MyViewAdapter(this.getContext());
        this.setAdapter(mMyViewAdapter);
    }

    public NewsListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public NewsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public NewsListView(Context context) {
        super(context);
        init();
    }
}
