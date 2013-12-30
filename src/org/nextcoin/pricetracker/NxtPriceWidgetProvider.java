package org.nextcoin.pricetracker;

import org.nextcoin.nxtclient.R;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

public class NxtPriceWidgetProvider extends AppWidgetProvider {
    
    static private final String mBroadCastString = "org.nextcoin.appWidgetUpdate";
    
    private static PriceTracker mPriceTracker;
    private static Context mContext;
    private static boolean mEnable = true;
    
    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        if ( null != mPriceTracker )
            mPriceTracker.stop();
        
        mEnable = false;
    }

    void startTracker(Context context){
        mContext = context;
        if ( null != mPriceTracker )
            mPriceTracker.stop();
        mPriceTracker = new PriceTracker();
        mPriceTracker.setPriceReciever(new PriceTracker.PriceReciever() {
            @Override
            public void onPriceUpdate(PriceTracker priceTracker) {
                Intent intent = new Intent();
                intent.setAction(mBroadCastString);
                mContext.sendBroadcast(intent);
            }
        });
        mPriceTracker.start();
    }
    
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        mEnable = true;
        startTracker(context);
    }
    
    @Override  
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        //if (intent.getAction().equals(mBroadCastString)) {

            float newPrice = 0;
            float oldPrice = 0;
            if ( null != mPriceTracker ){
                newPrice = (float)mPriceTracker.mNewPrice;
                oldPrice = (float)mPriceTracker.mOldPrice;
            }else if ( mEnable ){
                startTracker(context);
            }
            
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.nxt_price_widget_layout);
            if ( oldPrice > 0 && newPrice > 0 ){
                float change = (newPrice - oldPrice) * 100 / oldPrice;
                rv.setTextViewText(R.id.price, String.format("%.7f B", newPrice));
                if ( change >= 0 ){
                    rv.setTextViewText(R.id.change, String.format("+%.2f%%", change));
                    rv.setTextColor(R.id.change, Color.GREEN);
                    rv.setImageViewResource(R.id.img, R.drawable.up);
                }else{
                    rv.setTextViewText(R.id.change, String.format("%.2f%%", change));
                    rv.setTextColor(R.id.change, Color.RED);
                    rv.setImageViewResource(R.id.img, R.drawable.down);
                }
            }else{
                rv.setTextViewText(R.id.price, "-");
                rv.setTextViewText(R.id.change, "");
            }

             AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
             ComponentName componentName = new ComponentName(context,NxtPriceWidgetProvider.class);
             appWidgetManager.updateAppWidget(componentName, rv);
        //}
    }
      
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
