package br.ufu.lsi.event.test;

import org.junit.Assert;
import org.junit.Test;

import br.ufu.lsi.event.utils.DateUtil;

public class DateTests {
    
    long aug10 = 1470831599000L;
    long aug11 = 1470944635000L;
    long aug12 = 1470981674000L;
    long aug13 = 1471050926000L;
    long aug14 = 1471192022000L;
    long aug15 = 1471233035000L;
    
    @Test
    public void intervalBetweenDates(){
        
        
        
        int interval1 = DateUtil.checkNumberOfDaysInInterval( aug10, aug11 );
        System.out.println( interval1 );
        Assert.assertEquals( 1, interval1 );
        
        int interval2 = DateUtil.checkNumberOfDaysInInterval( aug10, aug12 );
        System.out.println( interval2 );
        Assert.assertEquals( 2, interval2 );
        
        int interval3 = DateUtil.checkNumberOfDaysInInterval( aug11, aug14 );
        System.out.println( interval3 );
        Assert.assertEquals( 3, interval3 );
        
        int interval0 = DateUtil.checkNumberOfDaysInInterval( aug13, aug13 );
        System.out.println( interval0 );
        Assert.assertEquals( 0, interval0 );
        
        int interval5 = DateUtil.checkNumberOfDaysInInterval( aug10, aug15 );
        System.out.println( interval5 );
        Assert.assertEquals( 5, interval5 );
        
        long aug7 = 1502076864000L;
        long aug14 = 1502681664000L;
        int interval6 = DateUtil.checkNumberOfDaysInInterval( aug7, aug14 );
        System.out.println( interval6 );
        
    }

    @Test
    public void nextDay() {
        
        long nextDay = DateUtil.nextDay( aug11 );
        System.out.println( nextDay );
        
        
    }
    
}
