package org.nextcoin.nxtclient;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;


public class Settings {
    
    public interface SettingsChangeListener{
        public void onChange(Settings settings);
    }
    private Set<SettingsChangeListener> mSettingsChangeListenerSet = new HashSet<SettingsChangeListener>();
    public void registerSettingsChangeListener(SettingsChangeListener l){
        mSettingsChangeListenerSet.add(l);
    }
    
    public void unregisterSettingsChangeListener(SettingsChangeListener l){
        mSettingsChangeListenerSet.remove(l);
    }
    
    // show nxt price in the bottom
    public boolean isShowingPrice(Context context){
        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        return prefer.getBoolean(mShowingPriceSaveKey, true);
    }

    public void setShowingPrice(Context context, boolean show){
        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        prefer.edit().putBoolean(mShowingPriceSaveKey, show).commit();
        if ( mSettingsChangeListenerSet.size() > 0 ){
            for ( SettingsChangeListener lstener : mSettingsChangeListenerSet ){
                lstener.onChange(Settings.this);
            }
        }
    }
    
    public boolean isNotificationEnable(Context context){
        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        return prefer.getBoolean(mNotificationSaveKey, true);
    }

    public void setNotificationEnable(Context context, boolean enable){
        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        prefer.edit().putBoolean(mNotificationSaveKey, enable).commit();
        if ( mSettingsChangeListenerSet.size() > 0 ){
            for ( SettingsChangeListener lstener : mSettingsChangeListenerSet ){
                lstener.onChange(Settings.this);
            }
        }
    }
    
    public boolean isNotificationVibrateEnable(Context context){
        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        return prefer.getBoolean(mNotificationVibrateSaveKey, false);
    }

    public void setNotificationVibrateEnable(Context context, boolean enable){
        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        prefer.edit().putBoolean(mNotificationVibrateSaveKey, enable).commit();
        if ( mSettingsChangeListenerSet.size() > 0 ){
            for ( SettingsChangeListener lstener : mSettingsChangeListenerSet ){
                lstener.onChange(Settings.this);
            }
        }
    }
    
    public int getNotificationInterval(Context context){
        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        return prefer.getInt(mNotificationIntervalSaveKey, 1);
    }

    public void setNotificationInterval(Context context, int index){
        SharedPreferences prefer = context.getSharedPreferences(mPrefFileName, 0);
        prefer.edit().putInt(mNotificationIntervalSaveKey, index).commit();
        if ( mSettingsChangeListenerSet.size() > 0 ){
            for ( SettingsChangeListener lstener : mSettingsChangeListenerSet ){
                lstener.onChange(Settings.this);
            }
        }
    }
    
    final static private String mPrefFileName = "NxtClientSettingsPrefFile";
    final static private String mShowingPriceSaveKey = "ShowingPriceSaveKey";
    final static private String mNotificationSaveKey = "NotificationSaveKey";
    final static private String mNotificationVibrateSaveKey = "NotificationVibrateSaveKey";
    final static private String mNotificationIntervalSaveKey = "NotificationIntervalSaveKey";
    
    /**
     * Singleton
     */
    public static Settings sharedInstance(){
        if ( null == mSettings )
            mSettings = new Settings();

        return mSettings;
    }
    
    private static Settings mSettings;
    private Settings(){
        
    }
}
