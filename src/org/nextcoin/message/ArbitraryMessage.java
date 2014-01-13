package org.nextcoin.message;


public class ArbitraryMessage {
    public byte[] mData;
    public String mText;

    public ArbitraryMessage(byte[] data){
        mData = data;
        try {
            mText = new String(mData, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
