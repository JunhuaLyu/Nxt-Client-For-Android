package org.nextcoin.pricetracker;

import org.json.JSONArray;
import org.json.JSONObject;

import com.other.util.HttpUtil;


/**
 * get NXT price from dgex.com
 */
public class PriceTracker {
    public double mNewPrice;
    public double mOldPrice;
    
    public interface PriceReciever{
        public void onPriceUpdate(PriceTracker priceTracker);
    }
    
    private PriceReciever mPriceReciever;
    public void setPriceReciever(PriceReciever reciever){
        mPriceReciever = reciever;
    }

    public boolean isRunning(){
        return mGetPriceRunnable != null;
    }

    public void start(){
        synchronized(PriceTracker.this){
            if ( null == mGetPriceRunnable ){
                mGetPriceRunnable = new GetPriceRunnable(mPriceReciever, this);
                new Thread(mGetPriceRunnable).start();
            }
        }
    }
    
    public void stop(){
        synchronized(PriceTracker.this){
            if ( null != mGetPriceRunnable ){
                mGetPriceRunnable.mRunning = false;
                mGetPriceRunnable = null;
            }
        }
    }
    
    private GetPriceRunnable mGetPriceRunnable;
    private class GetPriceRunnable implements Runnable{
        public boolean mRunning;
        private PriceReciever mPriceReciever;
        private PriceTracker mPriceTracker;
        public GetPriceRunnable(PriceReciever reciever, PriceTracker priceTracker){
            mPriceReciever = reciever;
            mPriceTracker = priceTracker;
            mRunning = true;
        }
        
        @Override
        public void run() {
            while (mRunning){
                try {
                    String httpsUrl = "https://dgex.com/API/trades3h.json";
                    String result = HttpUtil.getHttps(httpsUrl);
                    JSONObject jsonObj;
                    jsonObj = new JSONObject(result);
                    JSONArray jarray = jsonObj.getJSONArray("ticker");
                    JSONObject newTicker = jarray.getJSONObject(0);
                    JSONObject oldTicker = jarray.getJSONObject(jarray.length() - 1);
                    int timestamp0 = newTicker.getInt("timestamp");
                    int timestampN = oldTicker.getInt("timestamp");
                    double newPrice = 0;
                    double oldPrice = 0;
                    
                    int i;
                    int volume = 0;
                    for ( i = 0; i < jarray.length(); ++ i){
                        int units = jarray.getJSONObject(i).getInt("units");
                        newPrice += jarray.getJSONObject(i).getDouble("unitprice") * units;
                        volume += units;
                        if ( volume > 1000 )
                            break;
                    }
                    newPrice = newPrice / volume;
                    
                    volume = 0;
                    for ( i = jarray.length() - 1; i >= 0; -- i){
                        int units = jarray.getJSONObject(i).getInt("units");
                        oldPrice += jarray.getJSONObject(i).getDouble("unitprice") * units;
                        volume += units;
                        if ( volume > 1000 )
                            break;
                    }
                    oldPrice = oldPrice / volume;

                    if ( timestamp0 > timestampN ){
                        mPriceTracker.mNewPrice = newPrice;
                        mPriceTracker.mOldPrice = oldPrice;
                    }else{
                        mPriceTracker.mNewPrice = oldPrice;
                        mPriceTracker.mOldPrice = newPrice;
                    }

                    if ( null != mPriceReciever )
                        mPriceReciever.onPriceUpdate(mPriceTracker);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                try {
                    Thread.sleep(60 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }};
    
}
