package org.nextcoin.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class NxtUtil {
    
    static private long mCalendarGenMs;
    static {
        Calendar calendarGen = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        calendarGen.set(Calendar.YEAR, 2013);
        calendarGen.set(Calendar.MONTH, Calendar.NOVEMBER);
        calendarGen.set(Calendar.DAY_OF_MONTH, 24);
        calendarGen.set(Calendar.HOUR_OF_DAY, 12);
        calendarGen.set(Calendar.MINUTE, 0);
        calendarGen.set(Calendar.SECOND, 0);
        calendarGen.set(Calendar.MILLISECOND, 0);
        mCalendarGenMs = calendarGen.getTimeInMillis();
    }
    
    static public long getStartTime(){
        return mCalendarGenMs;
    }

    static public int getTimestamp(){
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
        long millis = calendar.getTimeInMillis();
        long stamp = millis - mCalendarGenMs;

        stamp = ( stamp + 500 ) / 1000;
        return (int)stamp;
    }

    static public String convert(byte[] paramArrayOfByte)
    {
      StringBuilder localStringBuilder = new StringBuilder();
      for (int k : paramArrayOfByte)
      {
        int m;
        localStringBuilder.append("0123456789abcdefghijklmnopqrstuvwxyz".charAt((m = k & 0xFF) >> 4)).append("0123456789abcdefghijklmnopqrstuvwxyz".charAt(m & 0xF));
      }

      return localStringBuilder.toString();
    }

    static public byte[] convert(String paramString) {
        byte[] arrayOfByte = new byte[paramString.length() / 2];
        for (int i = 0; i < arrayOfByte.length; i++)
            arrayOfByte[i] = ((byte) Integer.parseInt(
                    paramString.substring(i * 2, i * 2 + 2), 16));
        return arrayOfByte;
    }
}
