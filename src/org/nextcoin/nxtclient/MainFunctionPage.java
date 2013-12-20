package org.nextcoin.nxtclient;

import java.util.ArrayList;
import java.util.List;

import org.nextcoin.accounts.AccountPage;
import org.nextcoin.addresses.AddressesPage;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

public class MainFunctionPage {
    private static final int TAB_ID_ACCOUNTS = 0;
    private static final int TAB_ID_ADDRESSES = 1;
    private static final int TAB_ID_SETTINGS = 2;
    
    private View.OnClickListener mTabOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            int id;
            switch(v.getId()){
            case R.id.tab_accounts:
                id = TAB_ID_ACCOUNTS;
                break;
            case R.id.tab_address:
                id = TAB_ID_ADDRESSES;
                break;
            case R.id.tab_settings:
                id = TAB_ID_SETTINGS;
                break;
            default:
                return;
            }
            
            selectPage(id);
        }
    };

    private Activity mMainActivity;
    public MainFunctionPage(Activity mainActivity){
        mMainActivity = mainActivity;
        TextView tabTextView = (TextView)mMainActivity.findViewById(R.id.tab_accounts);
        tabTextView.setOnClickListener(mTabOnClickListener);
        tabTextView = (TextView)mMainActivity.findViewById(R.id.tab_address);
        tabTextView.setOnClickListener(mTabOnClickListener);
        tabTextView = (TextView)mMainActivity.findViewById(R.id.tab_settings);
        tabTextView.setOnClickListener(mTabOnClickListener);
        
        viewPagerInit();
    }
    
    public void update(){
        switch(currIndex){
        case TAB_ID_ACCOUNTS:
            mAccountPage.update();
            break;
        case TAB_ID_ADDRESSES:
            mAddressesPage.update();
            break;
        case TAB_ID_SETTINGS:
            break;
        default:
            break;
        }
    }
    
    public void release(){
        mAccountPage.release();
        mAddressesPage.release();
    }
    
    /**
     * function pages manage
     */
    private ViewPager mViewPager;
    private List<View> mViewList;
    private View imageView;
    private int offset = 0; // animation offset
    private int currIndex = 0; // current page
    private AccountPage mAccountPage;
    private AddressesPage mAddressesPage;
    
    private void viewPagerInit(){
        LayoutInflater inflater = mMainActivity.getLayoutInflater();
        View accountsPage = inflater.inflate(R.layout.page_accounts, null);
        View addressPage = inflater.inflate(R.layout.page_addresses, null);
        View settingPage = inflater.inflate(R.layout.page_settings, null);
        mAccountPage = new AccountPage(accountsPage);
        mAddressesPage = new AddressesPage(addressPage);
        
        mViewPager = (ViewPager)mMainActivity.findViewById(R.id.viewpager);
        
        mViewList = new ArrayList<View>();
        mViewList.add(accountsPage);
        mViewList.add(addressPage);
        mViewList.add(settingPage);
        
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        
        imageView= (View)mMainActivity.findViewById(R.id.cursor);
        DisplayMetrics dm = new DisplayMetrics();  
        mMainActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);  
        offset = dm.widthPixels / 3;
    }

    private void selectPage(int id){
        mViewPager.setCurrentItem(id);
    }
    
    private PagerAdapter mPagerAdapter = new PagerAdapter() {  
        
        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {  
            return arg0 == arg1;  
        }  

        @Override  
        public int getCount() {  
            return mViewList.size();  
        }  

        @Override  
        public void destroyItem(ViewGroup container, int position,  
                Object object) {  
            container.removeView(mViewList.get(position));  
        }  

        @Override  
        public int getItemPosition(Object object) {  
            return super.getItemPosition(object);  
        }  

        @Override  
        public CharSequence getPageTitle(int position) {  
            return " ";  
        }  

        @Override  
        public Object instantiateItem(ViewGroup container, int position) {  
            container.addView(mViewList.get(position));  
            return mViewList.get(position);  
        }  

    };       

    private class MyOnPageChangeListener implements OnPageChangeListener{  

        public void onPageScrollStateChanged(int arg0) {  
        }  

        public void onPageScrolled(int arg0, float arg1, int arg2) {  
        }  
  
        public void onPageSelected(int arg0) {  
            Animation animation = new TranslateAnimation(offset * currIndex, offset * arg0, 0, 0);
            currIndex = arg0;
            animation.setFillAfter(true);  
            animation.setDuration(300);  
            imageView.startAnimation(animation);
            //mCurrentPageId = arg0;
        }

    } 
}
