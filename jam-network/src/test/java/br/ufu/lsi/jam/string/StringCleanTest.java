package br.ufu.lsi.jam.string;

import org.junit.Test;

import br.ufu.lsi.jam.utils.StringClean;

public class StringCleanTest {
    
    @Test
    public void testClean() {
        String str = "\"ddkd\"";
        System.out.println( StringClean.clean( str ) );
        
        String s = "\"Weird Al\" Yankovic";
        System.out.println( s.contains( "Yankovic" ));
            
    }

}
