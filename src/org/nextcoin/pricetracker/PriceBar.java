package org.nextcoin.pricetracker;

import org.nextcoin.nxtclient.R;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class PriceBar {

    //private Context mContext;
    private TextView mTextViewPrice;
    private TextView mTextViewChange;
    private ImageView mImageViewChange;
    private View mBar;
    
    public PriceBar(View bar){
        mBar = bar;
        //mContext = bar.getContext();
        mTextViewPrice = (TextView)mBar.findViewById(R.id.price);
        mTextViewChange = (TextView)mBar.findViewById(R.id.change);
        mImageViewChange = (ImageView)mBar.findViewById(R.id.img);
        mTextViewPrice.setText("-");
        mTextViewChange.setText("");
        mImageViewChange.setVisibility(View.INVISIBLE);
    }
    
    public void hide(){
        mBar.setVisibility(View.GONE);
    }
    
    public void show(){
        mBar.setVisibility(View.VISIBLE);
    }
    
    public void setPrice(float newPrice, float oldPrice){
        mTextViewPrice.setText(String.format("%.7f B", newPrice));
        mImageViewChange.setVisibility(View.VISIBLE);
        float change = (newPrice - oldPrice) * 100 / oldPrice;
        if ( change >= 0 ){
            mTextViewChange.setText(String.format("+%.2f%%", change));
            mTextViewChange.setTextColor(Color.GREEN);
            mImageViewChange.setImageResource(R.drawable.up);
        }else{
            mTextViewChange.setText(String.format("%.2f%%", change));
            mTextViewChange.setTextColor(Color.RED);
            mImageViewChange.setImageResource(R.drawable.down);
        }
    }
}
