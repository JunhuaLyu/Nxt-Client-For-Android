package org.nextcoin.transactions;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;

import org.json.JSONException;
import org.json.JSONObject;
import org.nextcoin.util.Crypto;
import org.nextcoin.util.NxtUtil;

public class NxtTransaction {

    static final long serialVersionUID = 0L;
    public static final byte TYPE_PAYMENT = 0;
    public static final byte TYPE_MESSAGING = 1;
    public static final byte TYPE_COLORED_COINS = 2;
    public static final byte SUBTYPE_PAYMENT_ORDINARY_PAYMENT = 0;
    public static final byte SUBTYPE_MESSAGING_ARBITRARY_MESSAGE = 0;
    public static final byte SUBTYPE_MESSAGING_ALIAS_ASSIGNMENT = 1;
    public static final byte SUBTYPE_COLORED_COINS_ASSET_ISSUANCE = 0;
    public static final byte SUBTYPE_COLORED_COINS_ASSET_TRANSFER = 1;
    public static final byte SUBTYPE_COLORED_COINS_ASK_ORDER_PLACEMENT = 2;
    public static final byte SUBTYPE_COLORED_COINS_BID_ORDER_PLACEMENT = 3;
    public static final byte SUBTYPE_COLORED_COINS_ASK_ORDER_CANCELLATION = 4;
    public static final byte SUBTYPE_COLORED_COINS_BID_ORDER_CANCELLATION = 5;
    public static final int ASSET_ISSUANCE_FEE = 1000;

    byte type;
    byte subtype;
    int timestamp;
    short deadline;
    byte[] senderPublicKey;
    long recipient;
    int amount;
    int fee;
    long referencedTransaction;
    byte[] signature;
    NxtTransaction.Attachment attachment;
    //int index;
    //long block;
    //int height;

    public NxtTransaction(byte type, byte subtype, int timestamp, short deadline, byte[] senderPublicKey, long recipient, int amount, int fee, long referencedTransaction, byte[] signature)
    {
      this.type = type;
      this.subtype = subtype;
      this.timestamp = timestamp;
      this.deadline = deadline;
      this.senderPublicKey = senderPublicKey;
      this.recipient = recipient;
      this.amount = amount;
      this.fee = fee;
      this.referencedTransaction = referencedTransaction;
      this.signature = signature;

      //this.height = 2147483647;
    }
    
    public void setAttachment(NxtTransaction.Attachment attach){
        attachment = attach;
    }

    public byte[] getBytes()
    {
      ByteBuffer buffer = ByteBuffer.allocate(128 + (this.attachment == null ? 0 : this.attachment.getBytes().length));
      buffer.order(ByteOrder.LITTLE_ENDIAN);
      buffer.put(this.type);
      buffer.put(this.subtype);
      buffer.putInt(this.timestamp);
      buffer.putShort(this.deadline);
      buffer.put(this.senderPublicKey);
      buffer.putLong(this.recipient);
      buffer.putInt(this.amount);
      buffer.putInt(this.fee);
      buffer.putLong(this.referencedTransaction);
      buffer.put(this.signature);
      if (this.attachment != null)
      {
        buffer.put(this.attachment.getBytes());
      }

      return buffer.array();
    }

    long getId() throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(getBytes());
        BigInteger bigInteger = new BigInteger(1, new byte[] { hash[7], hash[6], hash[5], hash[4], hash[3], hash[2], hash[1], hash[0]});
        return bigInteger.longValue();
    }

    public void sign(String secretPhrase) {
        this.signature = Crypto.sign(getBytes(), secretPhrase);
        try {
            while (!verify())
            {
                this.timestamp += 1;
                this.signature = new byte[64];
                this.signature = Crypto.sign(getBytes(), secretPhrase);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    boolean verify() throws Exception {

        byte[] data = getBytes();
        for (int i = 64; i < 128; i++) {
            data[i] = 0;
        }

        return Crypto.verify(this.signature, data, this.senderPublicKey);
    }

    static public class MessagingAliasAssignmentAttachment implements
            NxtTransaction.Attachment, Serializable {
        static final long serialVersionUID = 0L;
        final String alias;
        final String uri;

        public MessagingAliasAssignmentAttachment(String paramString1,
                String paramString2) {
            this.alias = paramString1;
            this.uri = paramString2;
        }

        public byte[] getBytes() {
            try {
                byte[] arrayOfByte1 = this.alias.getBytes("UTF-8");
                byte[] arrayOfByte2 = this.uri.getBytes("UTF-8");
                ByteBuffer localByteBuffer = ByteBuffer.allocate(1
                        + arrayOfByte1.length + 2 + arrayOfByte2.length);
                localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                localByteBuffer.put((byte) arrayOfByte1.length);
                localByteBuffer.put(arrayOfByte1);
                localByteBuffer.putShort((short) arrayOfByte2.length);
                localByteBuffer.put(arrayOfByte2);
                return localByteBuffer.array();
            } catch (Exception localException) {
            }
            return null;
        }

        public JSONObject getJSONObject() {
            JSONObject localJSONObject = new JSONObject();
            try {
                localJSONObject.put("alias", this.alias);
                localJSONObject.put("uri", this.uri);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return localJSONObject;
        }
    }


    public static class MessagingArbitraryMessageAttachment implements
            NxtTransaction.Attachment, Serializable {
        //static final long serialVersionUID = 0L;
        final byte[] message;

        public MessagingArbitraryMessageAttachment(byte[] paramArrayOfByte) {
            this.message = paramArrayOfByte;
        }

        public int getSize() {
            return 4 + this.message.length;
        }

        public byte[] getBytes() {
            try {
                ByteBuffer localByteBuffer = ByteBuffer.allocate(getSize());
                localByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                localByteBuffer.putInt(this.message.length);
                localByteBuffer.put(this.message);
                return localByteBuffer.array();
            } catch (Exception localException) {
            }
            return null;
        }

        public JSONObject getJSONObject() {
            JSONObject localJSONObject = new JSONObject();
            try {
                localJSONObject.put("message", NxtUtil.convert(this.message));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return localJSONObject;
        }

        public long getRecipientDeltaBalance() {
            return 0L;
        }

        public long getSenderDeltaBalance() {
            return 0L;
        }
    }

    static abstract interface Attachment
    {
      public abstract byte[] getBytes();

      public abstract JSONObject getJSONObject();
    }
}
