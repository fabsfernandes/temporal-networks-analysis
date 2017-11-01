package br.ufu.lsi.preference.utils;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.Period;


public class DateUtil {
    
    public static boolean changeDay( Long t1, Long t2 ) {
        
        int nDays = DateUtil.checkNumberOfDaysInInterval( t1, t2 );
            
        return nDays > 0;
    }
    
    public static int checkNumberOfDaysInInterval( Long timestamp1, Long timestamp2 ) {
        
        TimeZone tz = TimeZone.getTimeZone( "America/Sao_Paulo" );
        Calendar ca = GregorianCalendar.getInstance( tz );

        Date date1 = Date.from( Instant.ofEpochMilli( timestamp1 ) );
        ca.setTime( date1 );
        ca.set(Calendar.HOUR_OF_DAY, 0);
        ca.set(Calendar.MINUTE, 0);
        ca.set(Calendar.SECOND, 0);
        ca.set(Calendar.MILLISECOND, 0);
        DateTime dateTime1 = new DateTime( ca.getTime() );
        
        Date date2 = Date.from( Instant.ofEpochMilli( timestamp2 ) );
        ca.setTime( date2 );
        ca.set(Calendar.HOUR_OF_DAY, 1);
        ca.set(Calendar.MINUTE, 1);
        ca.set(Calendar.SECOND, 1);
        ca.set(Calendar.MILLISECOND, 1);
        DateTime dateTime2 = new DateTime( ca.getTime() );
        
        Period p = new Period( dateTime1, dateTime2);
        int days = p.toStandardDays().getDays();
        
        return days;      
    }
    
public static long nextDay( long timestamp ) {
        
        TimeZone tz = TimeZone.getTimeZone( "America/Sao_Paulo" );
        Calendar ca = GregorianCalendar.getInstance( tz );

        Date date1 = Date.from( Instant.ofEpochMilli( timestamp ) );
        ca.setTime( date1 );
        ca.add(Calendar.DATE, 1);  
        
        return ca.getTimeInMillis();
    }

    public static String readableDate( long timestamp ) {
        TimeZone tz = TimeZone.getTimeZone( "America/Sao_Paulo" );
        Calendar ca = GregorianCalendar.getInstance( tz );

        Date date1 = Date.from( Instant.ofEpochMilli( timestamp ) );
        ca.setTime( date1 );
        
        String month = "" + (1 + ca.get( Calendar.MONTH ));
        String day = "" + ( ca.get( Calendar.DAY_OF_MONTH ));
        
        return month + "-" + day;
    }

}
