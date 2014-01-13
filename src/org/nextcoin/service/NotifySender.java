package org.nextcoin.service;

import java.util.Random;

import org.nextcoin.addresses.AddressesManager;
import org.nextcoin.alias.Alias;
import org.nextcoin.nxtclient.R;
import org.nextcoin.nxtclient.Settings;
import org.nextcoin.transactions.NxtTransaction;
import org.nextcoin.transactions.Transaction;
import org.nextcoin.transactions.TransactionsActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotifySender {
    static Random mRandGen = new Random();
    static public void send(Context context, Transaction transaction, InfoCenter.AccountInfo myAccInfo){
        NotificationInfo info = getNotificationInfo(context, transaction, myAccInfo);
        if ( null == info )
            return;

        CharSequence contentTitle = info.mTitle;
        CharSequence contentText = info.mContent;
        
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(ns);
        
        CharSequence tickerText = contentTitle;
        int icon = R.drawable.logo2;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_SOUND;
        if ( Settings.sharedInstance().isNotificationVibrateEnable(context) ){
            notification.defaults = Notification.DEFAULT_VIBRATE;
        }
        
        Intent notificationIntent = new Intent(context, TransactionsActivity.class);
        notificationIntent.putExtra("AccountId", info.mAccountId);
        notificationIntent.putExtra("TransactionId", transaction.mId);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        notification.setLatestEventInfo(context, contentTitle, contentText,
                contentIntent);
        
        notificationManager.notify(mRandGen.nextInt(), notification);
        Log.v("Nxt Notification", info.mAccountId);
    }
    
    static private NotificationInfo getNotificationInfo(Context context, 
            Transaction transaction, InfoCenter.AccountInfo myAccInfo){
        // ORDINARY_PAYMENT
        if ( NxtTransaction.TYPE_PAYMENT == transaction.mType 
                && NxtTransaction.SUBTYPE_PAYMENT_ORDINARY_PAYMENT == transaction.mSubType ){
            // receive money
            if ( transaction.mRecipient.equals(myAccInfo.mId) ){
                NotificationInfo notificationInfo = new NotificationInfo();
                
                String sender = AddressesManager.sharedInstance().getTag(transaction.mSender);
                if ( null == sender || sender.equals(" ") )
                    sender = transaction.mSender;
                int amount = (int)transaction.mAmount;
                notificationInfo.mTitle = (String) context.getText(R.string.new_transaction);
                notificationInfo.mContent = String.format("%s %d Nxt, %s %s", 
                        context.getText(R.string.received), amount, 
                        context.getText(R.string.from), sender);
                notificationInfo.mAccountId = myAccInfo.mId;
                notificationInfo.mType = "ReceiveMoney";

                return notificationInfo;
            }
            // send money
            else{
                NotificationInfo notificationInfo = new NotificationInfo();
                
                String recipient = AddressesManager.sharedInstance().getTag(transaction.mRecipient);
                if ( null == recipient || recipient.equals(" ") )
                    recipient = transaction.mRecipient;
                int amount = (int)transaction.mAmount;
                notificationInfo.mTitle = (String) context.getText(R.string.transaction_confirm);
                notificationInfo.mContent = String.format("%s %d Nxt, %s %s", 
                        context.getText(R.string.sent), amount, 
                        context.getText(R.string.to), recipient);
                notificationInfo.mAccountId = myAccInfo.mId;
                notificationInfo.mType = "SendMoney";

                return notificationInfo;
            }
        }
        // alias assign
        else if ( NxtTransaction.TYPE_MESSAGING == transaction.mType 
                && NxtTransaction.SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT == transaction.mSubType ){
            NotificationInfo notificationInfo = new NotificationInfo();
            
            Alias alias = (Alias)transaction.mAttachment;
            
            notificationInfo.mTitle = (String) context.getText(R.string.alias_confirm);
            notificationInfo.mContent = String.format("Alias %s assigned success", alias.mName);
            notificationInfo.mAccountId = myAccInfo.mId;
            notificationInfo.mType = "AliasAssign";

            return notificationInfo;
        }
        return null;
    }

    private static class NotificationInfo{
        public String mTitle;
        public String mContent;
        public String mAccountId;
        public String mType;
    }
}
