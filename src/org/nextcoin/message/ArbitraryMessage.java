package org.nextcoin.message;

import org.nextcoin.util.NxtUtil;


public class ArbitraryMessage {
    public byte[] mData;
    public String mHex;
    public String mText;

    public ArbitraryMessage(String hex){
        mHex = hex;
        mData = NxtUtil.convert(mHex);
        try {
            mText = new String(mData, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
