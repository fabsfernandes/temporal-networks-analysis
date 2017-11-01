package br.ufu.lsi.jam.date;

import org.junit.Test;

import br.ufu.lsi.jam.utils.DateUtils;
import br.ufu.lsi.jam.utils.Granularity;

public class DateTest {
    
    @Test
    public void test() {
        
        System.out.println( DateUtils.checkInside( "2015-09-25", "2011-08-26", "2015-09-26" ) ) ;
    }
    
    @Test
    public void testNext() {
        String next = DateUtils.nextPeriod( "2011-08-26", Granularity.YEAR );
        System.out.println(next);
    }

}
