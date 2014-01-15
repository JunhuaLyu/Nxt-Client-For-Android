
package org.nextcoin.message;

import org.nextcoin.message.MessagesData.NxtMessage;
import org.nextcoin.util.NxtUtil;

public class ChatMsgEntity {
//    private static final String TAG = ChatMsgEntity.class.getSimpleName();

    //private String text;
    private NxtMessage mMessage;

    private int layoutID;

    public String getText() {
        return mMessage.getText();
    }
    
    public String getDate(){
        return NxtUtil.getDate(mMessage.mTimestamp);
    }

//    public void setText(String text) {
//        this.text = text;
//    }

    public int getLayoutID() {
        return layoutID;
    }

    public void setLayoutID(int layoutID) {
        this.layoutID = layoutID;
    }

    public ChatMsgEntity() {
    }

    public ChatMsgEntity(NxtMessage msg, int layoutID) {
        super();
        this.mMessage = msg;
        this.layoutID = layoutID;
    }

}
