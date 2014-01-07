package com.other.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class ProgressDialogExt extends ProgressDialog{

    
    static private Handler mProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ( msg.obj instanceof ProgressDialogExt ){
                ProgressDialogExt instance = (ProgressDialogExt)msg.obj;
                instance.handleMessage(msg);
            }
        }
    };

    public final static int MGS_SET_PROGRESS = 0;
    public final static int MGS_DIALOG_DISMISS = 1;
    public void handleMessage(Message msg) {
        switch(msg.what){
        case MGS_SET_PROGRESS:
            int value = msg.arg1;
            this.setProgress(value);
            break;
        case MGS_DIALOG_DISMISS:
            this.dismiss();
            break;
        default:
            break;
        }
    }
    
    public void postSetProgress(int value){
        Message msg = new Message();
        msg.obj = this;
        msg.what = MGS_SET_PROGRESS;
        msg.arg1 = value;
        mProgressHandler.sendMessage(msg);
    }
    
    public void postDismiss(){
        Message msg = new Message();
        msg.obj = this;
        msg.what = MGS_DIALOG_DISMISS;
        mProgressHandler.sendMessage(msg);
    }
    
    public ProgressDialogExt(Context context, int theme) {
        super(context, theme);
    }

    public ProgressDialogExt(Context context) {
        super(context);
    }

}
