package org.nextcoin.accounts;

public class Account {
    public String mId;
    public String mTag;
    public float mBalance;

    public Account(){
        mBalance = -1;
    }
    
    public String getBalanceText(){
        if ( mBalance < 0 )
            return "";

        return String.valueOf(mBalance);
    }
}
