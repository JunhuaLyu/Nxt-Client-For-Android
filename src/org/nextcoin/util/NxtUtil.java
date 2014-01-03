package org.nextcoin.util;

import java.util.Calendar;

public class NxtUtil {
    
    static private long mCalendarGenMs;
    static {
        Calendar calendarGen = Calendar.getInstance();
        //calendarGen.set(Calendar.ZONE_OFFSET, 0);
        calendarGen.set(Calendar.YEAR, 2013);
        calendarGen.set(Calendar.MONTH, Calendar.NOVEMBER);
        calendarGen.set(Calendar.DAY_OF_MONTH, 24);
        calendarGen.set(Calendar.HOUR_OF_DAY, 12);
        calendarGen.set(Calendar.MINUTE, 0);
        calendarGen.set(Calendar.SECOND, 0);
        calendarGen.set(Calendar.MILLISECOND, 0);
        mCalendarGenMs = calendarGen.getTimeInMillis();
    }

    static public int getTimestamp(){
        //long stamp = System.currentTimeMillis() - mCalendarGenMs;
        Calendar calendar = Calendar.getInstance();
        //calendarGen.set(Calendar.ZONE_OFFSET, 0);
        int offset = calendar.getTimeZone().getRawOffset();
        long stamp = calendar.getTimeInMillis() - offset - mCalendarGenMs;

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

}
