
package br.ufu.lsi.event.test;

import java.util.List;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Node;

import br.ufu.lsi.event.NetworkHandler;
import br.ufu.lsi.event.model.EdgeStreamObject;
import br.ufu.lsi.event.utils.DateUtil;

public class SketchAlgorithmBackup {

    DirectedGraph directedGraph;

    NetworkHandler networkHandler;

    // temporal extension in days (progression step)
    private static final int WINDOW = 3;

    public void sketchAlgorithm( List< EdgeStreamObject > edgeStream, Node v ) {

        long oldestTimestamp = edgeStream.get( 0 ).getInitTimestamp();

        // start stream 
        for ( EdgeStreamObject edgeObject : edgeStream ) {

            // update network N
            networkHandler.updateNetwork( edgeObject.getEdge() );

            // compute centrality values/position values/feature vectors for all nodes in network N (incremental algorithm)

            // summary centrality values/position values/feature vectors for node v in memory (avg) (incremental algorithm)

            // check timestamp
            Long currentTimestamp = edgeObject.getInitTimestamp();
            if ( windowFull( oldestTimestamp, currentTimestamp ) ) {

                // check event detected for node v (very simple algorithm. Batch over memory data.)

                // slide time-based window with temporal extent W and progression step delta = 1
                // same window for network N and centrality values summary

                // refresh network N
                networkHandler.removeOldEdges( oldestTimestamp );

                // refresh centrality values in memory 

                oldestTimestamp = DateUtil.nextDay( oldestTimestamp );
            }
        }
    }

    public boolean windowFull( Long oldestTimestamp, Long currentTimestamp ) {

        int nDays = DateUtil.checkNumberOfDaysInInterval( oldestTimestamp, currentTimestamp );

        return nDays > WINDOW;
    }

}
