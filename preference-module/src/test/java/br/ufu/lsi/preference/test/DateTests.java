package br.ufu.lsi.preference.test;

import org.junit.Assert;
import org.junit.Test;

import br.ufu.lsi.preference.utils.DateUtil;

public class DateTests {
    
    
    @Test
    public void changeDay(){
        
        long t1 = 1476669585000L;
        long t2 = 1476710505000L;
        System.out.println( DateUtil.changeDay( t1, t2 ) );
        
        
        
    }
    
}
