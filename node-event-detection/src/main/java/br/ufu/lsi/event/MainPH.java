
package br.ufu.lsi.event;

import java.util.ArrayList;
import java.util.List;

import br.ufu.lsi.event.methods.MovingWindowAverage;
import br.ufu.lsi.event.methods.PageHinckleyTestMain;

public class MainPH {

    public static void main( String... args ) throws Exception {

        String[] nodeIds = new String[] {
                "14594813", "334345564", "3145222787", "343820098", "122757872", "28958495",
                "260856271", "636368737", "279635698", "58488491"
        };

      //for ( String nodeId : nodeIds ) {
        
        String nodeId = "334345564";
        //System.out.println( nodeId );
            List< List< Integer >> allChanges = new ArrayList< List< Integer >>();

            double threshold;
            double alpha = 0.1;

            PageHinckleyTestMain ph;

            threshold = 0.01;
            ph = new PageHinckleyTestMain( nodeId, alpha, threshold );
            List< Integer > changes1 = ph.run();
            allChanges.add( changes1 );
            
            threshold = 0.1;
            ph = new PageHinckleyTestMain( nodeId, alpha, threshold );
            List< Integer > changes2 = ph.run();
            allChanges.add( changes2 );
            
            threshold = 0.2;
            ph = new PageHinckleyTestMain( nodeId, alpha, threshold );
            List< Integer > changes3 = ph.run();
            allChanges.add( changes3 );
            
            threshold = 0.04;
            ph = new PageHinckleyTestMain( nodeId, alpha, threshold );
            List< Integer > changes4 = ph.run();
            allChanges.add( changes4 );
            
            threshold = 0.06;
            ph = new PageHinckleyTestMain( nodeId, alpha, threshold );
            List< Integer > changes5 = ph.run();
            allChanges.add( changes5 );

            

            // print all
            for ( int k = 0; k < changes5.size(); k++ ) {
                for ( int i = 0; i < allChanges.size(); i++ ) {
                    List< Integer > change = allChanges.get( i );
                    System.out.print( change.get( k ) + "," );
                }
                System.out.println();
            }

        //}

    }

}
