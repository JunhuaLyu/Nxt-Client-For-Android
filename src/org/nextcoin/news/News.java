package org.nextcoin.news;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONObject;

public class News {
    public String mTitle;
    public String mExcerpt;
    public String mDate;
    public String mUrl;
    public int mId;

    public interface NewsReciever{
        public void onRecieve(LinkedList<News> list);
    }

    static public void requestNews(NewsReciever recieve){
        new Thread(new NewsRunnable(recieve)).start();
    }
    
    static class NewsRunnable implements Runnable {
        private NewsReciever mNewsReciever;
        public NewsRunnable(NewsReciever reciever){
            mNewsReciever = reciever;
        }
        
        @Override
        public void run() {
            try {
                String httpUrl = "http://info.nxtcrypto.org/?wpapi=get_posts&dev=1";
                HttpURLConnection conn;
                conn = (HttpURLConnection) new URL(httpUrl).openConnection();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null)
                    sb.append(line);

                JSONObject jsonObj;
                jsonObj = new JSONObject(sb.toString());
                JSONArray jarray = jsonObj.getJSONArray("posts");
                LinkedList<News> list = new LinkedList<News>();
                for ( int i = 0; i < 20 && i < jarray.length(); ++ i ){
                    JSONObject newsObj = jarray.getJSONObject(i);
                    String type = newsObj.getString("type");
                    if ( type.equals("post") ){
                        News news = new News();
                        news.mId = newsObj.getInt("id");
                        news.mTitle = newsObj.getString("title");
                        news.mUrl = newsObj.getString("url");
                        news.mExcerpt = newsObj.getString("excerpt");
                        news.mDate = newsObj.getString("date");
                        list.addLast(news);
                    }
                }
                mNewsReciever.onRecieve(list);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
