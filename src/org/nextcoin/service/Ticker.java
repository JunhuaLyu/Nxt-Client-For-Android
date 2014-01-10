package org.nextcoin.service;

public class Ticker {
    public interface TickerResponse{
        public void onTick();
    }
    
    private TickerResponse mTickerResponse;
    public Ticker(){
        mRunning = false;
    }
    
    private boolean mRunning;
    private int mInterval;
    public void setInterval(int interval){
        mInterval = interval;
    }

    public void start(int interval, TickerResponse response){
        mTickerResponse = response;
        mInterval = interval;
        if ( !mRunning ){
            mRunning = true;
            new Thread(new Runnable(){
                @Override
                public void run() {
                    while( mRunning ){
                        try {
                            Thread.sleep(mInterval);
                            mTickerResponse.onTick();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }}).start();
        }
    }
    
    public void stop(){
        mRunning = false;
    }
}
