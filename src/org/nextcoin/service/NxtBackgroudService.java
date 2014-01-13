package org.nextcoin.service;

import org.nextcoin.nxtclient.Settings;
import org.nextcoin.nxtclient.ToolsPage;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class NxtBackgroudService extends IntentService{

    public NxtBackgroudService() {
        super("NxtBackgroudService");
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        //Log.v("NxtBackgroudService", "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("NxtBackgroudService", "onDestroy");
        if ( null != mTicker ){
            mTicker.stop();
            mTicker = null;
        }
    }
    
    private Ticker mTicker;
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("NxtBackgroudService", "onHandleIntent");
        if ( null != mTicker )
            return;

        InfoCenter.shardInstance().init();
        mTicker = new Ticker();
        //mTicker.start(60 * 10 * 1000, new Ticker.TickerResponse() {
        int interval = ToolsPage.getRefreshInterval(NxtBackgroudService.this)
                * 60 * 1000;
        mTicker.start(interval, new Ticker.TickerResponse() {
            @Override
            public void onTick() {
                if ( !Settings.sharedInstance().isNotificationEnable(NxtBackgroudService.this) ){
                    if ( null != mTicker ){
                        mTicker.stop();
                        mTicker = null;
                    }
                    NxtBackgroudService.this.stopSelf();
                    return;
                }

                if ( null == mTicker )
                    return;

                Log.v("NxtBackgroudService", "onTick");
                InfoCenter.shardInstance().refresh(NxtBackgroudService.this);
                mTicker.setInterval(ToolsPage.getRefreshInterval(NxtBackgroudService.this)
                        * 60 * 1000);
            }
        });
        
        try {
            while(Settings.sharedInstance().isNotificationEnable(NxtBackgroudService.this)
                    && null != mTicker){
                    Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final IBinder mBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        LOG("Service onBind");
        return mBinder;
    }

    public class LocalBinder extends Binder {
        public NxtBackgroudService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NxtBackgroudService.this;
        }
    }
    
    private void LOG(String log){
        Log.v("NxtBackgroudService", log);
    }
}
