package br.ufu.lsi.jam.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;



public class DateUtils {
    
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "yyyy-MM-dd" );
    
    
    public static boolean changePeriod( String t1, String t2, Granularity granularity ) {
        
        
        return false;
    }
    
    
    
    public static boolean checkBefore( String current, String limit ) {
        
        LocalDate ldCurrent = LocalDate.parse( current, formatter );
        LocalDate ldLimit = LocalDate.parse( limit, formatter );
        
        int compare = ldCurrent.compareTo( ldLimit );
        
        if( compare < 0 )
            return true;
        return false;
    }
    
    public static boolean checkInside( String current, String inferiorLimit, String superiorLimit ) {
        
        LocalDate ldCurrent = LocalDate.parse( current, formatter );
        LocalDate ldInferiorLimit = LocalDate.parse( inferiorLimit, formatter );
        LocalDate ldSuperiorLimit = LocalDate.parse( superiorLimit, formatter );
        
        int compareInf = ldCurrent.compareTo( ldInferiorLimit );
        int compareSup = ldCurrent.compareTo( ldSuperiorLimit );
        
        if( compareInf < 0 || compareSup == 0 || compareSup > 0 ) 
            return false;
        
        return true;
    }
    
    public static String nextPeriod( String currentPeriod, Granularity granularity ) {
        
        LocalDate ld = LocalDate.parse( currentPeriod, formatter );
        
        LocalDate newLd = null;
        
        switch( granularity ) {
            
            case DAY:
                newLd = ld.plusDays( 1 );
                break;
            case MONTH:
                newLd = ld.plusMonths( 1 );
                break;
            case SEMESTER:
                newLd = ld.plusMonths( 6 );
                break;
            case YEAR:
                newLd = ld.plusYears( 1 );
                break;
        }
         
        return newLd.toString();
    }

}
