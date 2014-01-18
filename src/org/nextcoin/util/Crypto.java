package org.nextcoin.util;

import java.security.MessageDigest;
import java.util.Arrays;

public class Crypto {
//    static public void test2() throws Exception{
//        String senderSecret = "12334";
//        byte[] senderPrivate = MessageDigest.getInstance("SHA-256").digest(senderSecret.getBytes("UTF-8"));
//        //Log.v("Crypto", "senderPrivate = " + NxtUtil.convert(senderPrivate));
//        byte[] senderKey = new byte[32];
//        Curve25519.keygen(senderKey, null, senderPrivate);
//
//        String recipientSecret = "123345";
//        byte[] recipientPrivate = MessageDigest.getInstance("SHA-256")
//                .digest(recipientSecret.getBytes("UTF-8"));
//        byte[] recipientKey = new byte[32];
//        Curve25519.keygen(recipientKey, null, recipientPrivate);
//
//        byte[] sharedSecret = new byte[32];
//        //Log.v("Crypto", "senderKey = " + NxtUtil.convert(senderKey));
//        //Log.v("Crypto", "recipientKey = " + NxtUtil.convert(recipientKey));
//        Curve25519.curve(sharedSecret, senderPrivate, recipientKey);
//        //Log.v("Crypto", "sharedSecret = " + NxtUtil.convert(sharedSecret));
//        byte[] sharedSecret1 = new byte[32];
//        Curve25519.curve(sharedSecret1, recipientPrivate, senderKey);
//        //Log.v("Crypto", NxtUtil.convert(sharedSecret1));
//    }

//    static public void test(){
//        try {
//            test2();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        String senderSecret = "12334";
//        byte[] senderKey = Crypto.getPublicKey(senderSecret);
//        //Log.v("Crypto", "senderKey = " + NxtUtil.convert(senderKey));
//        
//        String recipientSecret = "123345";
//        byte[] recipientKey = Crypto.getPublicKey(recipientSecret);
//        
//        String text = "my secret";
//        byte[] encode = encodeMessage(text, senderSecret, recipientKey);
//        
//        String decode = decodeMessage(encode, recipientSecret, senderKey);
//        Log.v("Crypto", decode);
//    }
    
    static public byte[] getPublicKey(String secretPhrase) {
        
        try {
            
            byte[] publicKey = new byte[32];
            Curve25519.keygen(publicKey, null, MessageDigest.getInstance("SHA-256").digest(secretPhrase.getBytes("UTF-8")));
            
            return publicKey;
            
        } catch (Exception e) {
            
            return null;
            
        }
        
    }
    
    static public byte[] sign(byte[] message, String secretPhrase) {
        
        try {
            
            byte[] P = new byte[32];
            byte[] s = new byte[32];
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            Curve25519.keygen(P, s, digest.digest(secretPhrase.getBytes("UTF-8")));
            
            byte[] m = digest.digest(message);
            
            digest.update(m);
            byte[] x = digest.digest(s);
            
            byte[] Y = new byte[32];
            Curve25519.keygen(Y, null, x);
            
            digest.update(m);
            byte[] h = digest.digest(Y);
            
            byte[] v = new byte[32];
            Curve25519.sign(v, h, x, s);
            
            byte[] signature = new byte[64];
            System.arraycopy(v, 0, signature, 0, 32);
            System.arraycopy(h, 0, signature, 32, 32);
            
            return signature;
        } catch (Exception e) {
            return null;
        }
    }
    
    static public boolean verify(byte[] signature, byte[] message, byte[] publicKey) {
        try {
            byte[] Y = new byte[32];
            byte[] v = new byte[32];
            System.arraycopy(signature, 0, v, 0, 32);
            byte[] h = new byte[32];
            System.arraycopy(signature, 32, h, 0, 32);
            Curve25519.verify(Y, v, h, publicKey);
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] m = digest.digest(message);
            digest.update(m);
            byte[] h2 = digest.digest(Y);
            
            return Arrays.equals(h, h2);
        } catch (Exception e) {
            return false;
        }
    }

    static public byte[] encodeMessage(String message, String senderSecret, byte[] recipientKey){
        try {
            byte[] senderPrivate = MessageDigest.getInstance("SHA-256").digest(senderSecret.getBytes("UTF-8"));
            byte[] senderKey = new byte[32];
            Curve25519.keygen(senderKey, null, senderPrivate);

            byte[] sharedSecret = new byte[32];
            Curve25519.curve(sharedSecret, senderPrivate, recipientKey);

            byte[] plainText = message.getBytes("UTF-8");
            byte[] cipherText = new byte[plainText.length];
            byte[] seed = MessageDigest.getInstance("SHA-256").digest(sharedSecret);
            int n = plainText.length;
            int index = 0;
            while( n > 0 ){
                int len = n > 32 ? 32 : n;
                for ( int i = 0; i < len; ++ i ){
                    cipherText[index + i] = (byte) (plainText[index + i] ^ seed[i]);
                }
                n -= len;
                index += len;
                seed = MessageDigest.getInstance("SHA-256").digest(seed);
            }

            return cipherText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        return null;
    }
    
    static public String decodeMessage(byte[] encodeData,  String recipientSecret, byte[] senderKey){
        try {
            byte[] recipientPrivate = MessageDigest.getInstance("SHA-256")
                    .digest(recipientSecret.getBytes("UTF-8"));
            byte[] recipientKey = new byte[32];
            Curve25519.keygen(recipientKey, null, recipientPrivate);

            byte[] sharedSecret = new byte[32];
            Curve25519.curve(sharedSecret, recipientPrivate, senderKey);

            byte[] decodeText = new byte[encodeData.length];
            byte[] seed = MessageDigest.getInstance("SHA-256").digest(sharedSecret);
            int n = encodeData.length;
            int index = 0;
            while( n > 0 ){
                int len = n > 32 ? 32 : n;
                for ( int i = 0; i < len; ++ i ){
                    decodeText[index + i] = (byte) (encodeData[index + i] ^ seed[i]);
                }
                n -= len;
                index += len;
                seed = MessageDigest.getInstance("SHA-256").digest(seed);
            }

            return new String(decodeText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

}

