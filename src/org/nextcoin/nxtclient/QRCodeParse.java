package org.nextcoin.nxtclient;

import java.util.HashMap;

import org.nextcoin.accounts.Account;

public class QRCodeParse {
    static private String getAddress(String code){
        code = code.toLowerCase();
        if ( code.startsWith("nxtacct:") ){
            int index = code.indexOf('?');
            if ( index < 0 )
                return code.substring("nxtacct:".length());
            else
                return code.substring("nxtacct:".length(), index);
        }
        return null;
    }
    
    static private HashMap<String, String> getParams(String code){
        code = code.toLowerCase();
        int index = code.indexOf('?');
        if ( index < 0 )
            return null;
        
        code = code.substring(index + 1);
        HashMap<String, String> map = new HashMap<String, String>();
        String[] array = code.split("&");
        for ( int i = 0; i < array.length; ++ i ){
            String[] pair = array[i].split("=");
            if ( 2 == pair.length ){
                map.put(pair[0], pair[1]);
            }
        }

        return map;
    }
    
    static public Account genAccount(String code){
        String addr = getAddress(code);
        if ( null == addr )
            return null;
        
        if (addr.length() > 21 || !addr.matches("\\d+"))
            return null;
        
        Account acc = new Account();
        acc.mId = addr;
        
        HashMap<String, String> params = getParams(code);
        if ( null != params ){
            String label = params.get("label");
            if ( null != label ){
                acc.mTag = label;
            }
        }
        
        return acc;
    }
}
