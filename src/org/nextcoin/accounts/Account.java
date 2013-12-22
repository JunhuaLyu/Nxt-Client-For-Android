package org.nextcoin.accounts;

import java.util.LinkedList;

import org.nextcoin.transactions.Transaction;

public class Account {
    public String mId;
    public String mTag;
    public float mBalance;
    public float mUnconfirmedBalance;
    public LinkedList<Transaction> mTransactionList;

    public Account(){
        mBalance = -1;
    }
    
    public String getBalanceText(){
        if ( mBalance < 0 )
            return "";

        if ( mBalance == mUnconfirmedBalance )
            return String.valueOf(mBalance);
        else
            return String.valueOf(mBalance) + "/" + String.valueOf(mUnconfirmedBalance);
    }
}
