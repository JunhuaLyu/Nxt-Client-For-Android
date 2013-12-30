package org.nextcoin.nxtclient;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class ToolsPage {

    private CheckBox mCheckBoxShowPrice;
    public ToolsPage(View page){
        mCheckBoxShowPrice = (CheckBox)page.findViewById(R.id.check_show_price);
        mCheckBoxShowPrice.setChecked(Settings.sharedInstance().isShowingPrice(
                mCheckBoxShowPrice.getContext()));
        mCheckBoxShowPrice.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Settings.sharedInstance().setShowingPrice(mCheckBoxShowPrice.getContext(), isChecked);
            }});
    }

    public void update(){
        
    }
    
    public void release(){
        
    }
}
