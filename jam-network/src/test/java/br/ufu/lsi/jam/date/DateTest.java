package br.ufu.lsi.jam.date;

import org.junit.Test;

import br.ufu.lsi.jam.utils.DateUtils;
import br.ufu.lsi.jam.utils.Granularity;

public class DateTest {
    
    String INFERIOR_LIMIT = "2011-08-26";
    String SUPERIOR_LIMIT = "2015-09-26";
    
    @Test
    public void test() {
        
        System.out.println( DateUtils.checkInside( "2015-09-25", "2011-08-26", "2015-09-26" ) ) ;
    }
    
    @Test
    public void testNext() {
        String next = DateUtils.nextPeriod( "2011-08-26", Granularity.YEAR );
        System.out.println(next);
    }
    
    @Test
    public void testGetPeriod() {
        long time = DateUtils.getPeriod( "2012-02-26", Granularity.SEMESTER, INFERIOR_LIMIT );
        System.out.println( "Get period = " + time );
    }
    
    @Test
    public void testGetNumberOfTimesteps() {
        int time = DateUtils.getNumberOfTimeSteps( INFERIOR_LIMIT, SUPERIOR_LIMIT, Granularity.QUARTER_YEAR );
        System.out.println( "Number of timesteps = " + time );
    }

}
