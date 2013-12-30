package org.nextcoin.news;

import java.util.LinkedList;

import org.nextcoin.nxtclient.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class NewsPage {

    private Context mContext;
    private NewsListView mNewsListView;
    public NewsPage(View newsPageView){
        mContext = newsPageView.getContext();
        mNewsListView = (NewsListView)newsPageView.findViewById(R.id.listview_news);
        mIsInit = false;
        mNewsListView.setOnItemClickListener(new ListView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                LinkedList<News> list = mNewsListView.getList();
                if ( list != null && list.size() > arg2 ){
                    Uri uri = Uri.parse(list.get(arg2).mUrl);  
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
                    mContext.startActivity(intent);                     
                }
            }});
    }
    
    private boolean mIsInit;
    public boolean isInit(){
        return mIsInit;
    }
    
    public void update(){
        News.requestNews(new News.NewsReciever() {
            @Override
            public void onRecieve(LinkedList<News> list) {
                if ( list.size() > 0 ){
                    mNewsListView.setNewsList(list);
                    Message msg = new Message();
                    msg.what = MSG_NEWS_UPDATE;
                    msg.obj = NewsPage.this;
                    mHandler.sendMessage(msg);
                }
            }
        });
    }
    
    /**
     * handle msg to update UI
     * @param msg
     */
    private static final int MSG_NEWS_UPDATE = 0;
    public void handleMessage(Message msg) {
        if ( MSG_NEWS_UPDATE == msg.what ){
            mNewsListView.notifyDataSetChanged();
            mIsInit = true;
        }
    }

    static private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ( msg.obj instanceof NewsPage ){
                NewsPage instance = (NewsPage)msg.obj;
                instance.handleMessage(msg);
            }
        }
    };
    
    public void release(){
        
    }
}
