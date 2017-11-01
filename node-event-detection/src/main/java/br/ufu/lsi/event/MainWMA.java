
package br.ufu.lsi.event;

import java.util.ArrayList;
import java.util.List;

import br.ufu.lsi.event.methods.MovingWindowAverage;

public class MainWMA {

    public static void main( String... args ) throws Exception {

        String[] nodeIds = new String[] {
                "14594813", "334345564", "3145222787", "343820098", "122757872", "28958495",
                "260856271", "636368737", "279635698", "58488491"
        };

        //for ( String nodeId : nodeIds ) {
            
            String nodeId = "334345564";
            //System.out.println( nodeId );
            List< List< Integer >> allChanges = new ArrayList< List< Integer >>();

            
            int window;
            double threshold;

            MovingWindowAverage mwa;

            window = 2;
            threshold = 0.01;
            mwa = new MovingWindowAverage( nodeId, window, threshold );
            List< Integer > changes1 = mwa.run();
            allChanges.add( changes1 );

            window = 2;
            threshold = 0.1;
            mwa = new MovingWindowAverage( nodeId, window, threshold );
            List< Integer > changes2 = mwa.run();
            allChanges.add( changes2 );

            window = 2;
            threshold = 0.2;
            mwa = new MovingWindowAverage( nodeId, window, threshold );
            List< Integer > changes3 = mwa.run();
            allChanges.add( changes3 );

            window = 2;
            threshold = 0.4;
            mwa = new MovingWindowAverage( nodeId, window, threshold );
            List< Integer > changes4 = mwa.run();
            allChanges.add( changes4 );

            window = 2;
            threshold = 0.6;
            mwa = new MovingWindowAverage( nodeId, window, threshold );
            List< Integer > changes5 = mwa.run();
            allChanges.add( changes5 );

            window = 5;
            threshold = 0.01;
            mwa = new MovingWindowAverage( nodeId, window, threshold );
            List< Integer > changes6 = mwa.run();
            allChanges.add( changes6 );

            window = 5;
            threshold = 0.1;
            mwa = new MovingWindowAverage( nodeId, window, threshold );
            List< Integer > changes7 = mwa.run();
            allChanges.add( changes7 );

            window = 5;
            threshold = 0.2;
            mwa = new MovingWindowAverage( nodeId, window, threshold );
            List< Integer > changes8 = mwa.run();
            allChanges.add( changes8 );

            window = 5;
            threshold = 0.4;
            mwa = new MovingWindowAverage( nodeId, window, threshold );
            List< Integer > changes9 = mwa.run();
            allChanges.add( changes9 );

            window = 5;
            threshold = 0.6;
            mwa = new MovingWindowAverage( nodeId, window, threshold );
            List< Integer > changes10 = mwa.run();
            allChanges.add( changes10 );

            // print all
            for ( int k = 0; k < changes10.size(); k++ ) {
                for ( int i = 0; i < allChanges.size(); i++ ) {
                    List< Integer > change = allChanges.get( i );
                    System.out.print( change.get( k ) + "," );
                }
                System.out.println();
            }

        //}

    }

}
