package br.ufu.lsi.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import moa.classifiers.rules.driftdetection.PageHinkleyFading;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;

import br.ufu.lsi.event.methods.PageHinckleyTest;
import br.ufu.lsi.event.model.EdgeStreamObject;
import br.ufu.lsi.event.model.FeatureVector;
import br.ufu.lsi.event.utils.DateUtil;

public class NodeEventDetection {
    
    
    NetworkHandler networkHandler;
    
    // temporal extension in days (progression step)
    private static final int WINDOW = 1;
    
    // PH test params
    private static  double THRESHOLD = 0.1;
    private static  double ALPHA = 0.05;
    private static  long MIN_INSTANCES_SEEN = 1;
    
    // temp params
    private static String NODEID = "14594813";
    
    
    public NodeEventDetection( DirectedGraph directedGraph, Map< String, Node > nodes ) {
    
        networkHandler = new NetworkHandler( directedGraph );
    }
    
    /*public NodeEventDetection( UndirectedGraph undirectedGraph, Map< String, Node > nodes ) {
        
        networkHandler = new NetworkHandler( undirectedGraph );
    }*/
    
    public void setParams(double threshold, double alpha, String nodeID ){
        THRESHOLD = threshold;
        ALPHA = alpha;
        NODEID = nodeID;
        
        System.out.println("***************************");
        System.out.println( NODEID + "," + THRESHOLD + "," + ALPHA );
        System.out.println("***************************");
    }
    
    public void sketchAlgorithm( List<EdgeStreamObject> edgeStream ) {
        
        long oldestTimestamp = edgeStream.get( 0 ).getInitTimestamp();
        long previousTimestamp = oldestTimestamp;
        
        PageHinckleyTest phTestCloseness = new PageHinckleyTest( THRESHOLD, ALPHA, MIN_INSTANCES_SEEN );
        PageHinckleyTest phTestBetweenness = new PageHinckleyTest( THRESHOLD, ALPHA, MIN_INSTANCES_SEEN );
        PageHinkleyFading phTest = new PageHinkleyFading( THRESHOLD, ALPHA );
        
        BigDecimal closenessPast = BigDecimal.ZERO;
        for( EdgeStreamObject edgeObject : edgeStream ) {
            
            Long currentTimestamp = edgeObject.getInitTimestamp();
            
            if( changeDay( previousTimestamp, currentTimestamp ) ) {
                
                // update PH
                //int centrality = networkHandler.getDegree( NODEID );
                //double centrality = networkHandler.getWeightedDegree( NODEID );
                
                BigDecimal closeness = BigDecimal.ZERO;//networkHandler.getBetweenness( NODEID );
                System.out.print( closeness.toPlainString() + ",");
                
                /*// initial case
                if( !(closeness.doubleValue()==0.0) &&  closenessPast.doubleValue()==0.0 ) {
                    phTestCloseness.incrementPHInstancesSeen();
                    //System.out.println( "Case 1" );
                    System.out.println( "1" );
                    
                } 
                else if( closeness.doubleValue()==0.0 && closenessPast.doubleValue()==0.0 ) {
                    phTestCloseness.incrementPHInstancesSeen();
                    //System.out.println("Case 2");
                    System.out.println( "0" );
                    
                }
                else {
                
                    // copy previous value
                    if(  closeness.doubleValue()==0.0 ) {
                        closeness = new BigDecimal( closenessPast.doubleValue() );
                    }
                    //System.out.println("Case 3");
                */    
//                    if( phTestCloseness.update( closeness.doubleValue() ) ) {
//                        System.out.println( "1" );
//                    } else {
//                        System.out.println( "0" );
//                    }
               /* }
                
                closenessPast = new BigDecimal( closeness.doubleValue() );
                */
                /*BigDecimal betweenness = networkHandler.getBetweenness( NODEID );
                System.out.print( betweenness.toPlainString() + ",");
                if( phTestBetweenness.update( betweenness.doubleValue() ) ) {
                    System.out.println( "1" );
                } else {
                    System.out.println( "0" );
                }*/
            }
            
            if ( windowFull( oldestTimestamp, currentTimestamp ) ) {  
                
                oldestTimestamp = DateUtil.nextDay( oldestTimestamp );
                
                System.out.println( networkHandler.getNodeCount() + "," + networkHandler.getEdgeCount() );
                
                // refresh network N
                networkHandler.removeOldEdges( oldestTimestamp );
            }
            
            // update network N
            networkHandler.updateNetwork( edgeObject.getEdge() );
            
            previousTimestamp = currentTimestamp;
        } 
        
        System.out.println( networkHandler.getNodeCount() + "," + networkHandler.getEdgeCount() );
        
        // last day
        BigDecimal closeness = networkHandler.getBetweenness( NODEID );
        System.out.print( closeness.toPlainString() + ",");
        if( phTestCloseness.update( closeness.doubleValue() ) ) {
            System.out.print( "1," );
        } else {
            System.out.print( "0," );
        }
        
        BigDecimal betweenness = networkHandler.getBetweenness( NODEID );
        System.out.print( betweenness.toPlainString() + ",");
        if( phTestBetweenness.update( betweenness.doubleValue() ) ) {
            System.out.println( "1" );
        } else {
            System.out.println( "0" );
        }
    }
    
    
    public boolean changeDay( Long t1, Long t2 ) {
        
        int nDays = DateUtil.checkNumberOfDaysInInterval( t1, t2 );
            
        return nDays > 0;
    }
    
    public boolean windowFull( Long oldestTimestamp, Long currentTimestamp ){
        
        int nDays = DateUtil.checkNumberOfDaysInInterval( oldestTimestamp, currentTimestamp );
        //nDays = Math.abs( nDays );
        return nDays >= WINDOW;
    }
    
    
    
    
    
    public boolean featureScore( FeatureVector currentFeatures, List<FeatureVector> pastFeatures, Double threshold ) {
        
        FeatureVector sum = new FeatureVector();
        for( FeatureVector featureVector : pastFeatures ) {
            sum = sum.sumVector( featureVector );
        }
        
        FeatureVector average = sum.average( new Double( pastFeatures.size() ) );
        
        
        Double dotProduct = currentFeatures.dotProduct( average );
        
        if( (1 - dotProduct ) > threshold )
            return true;
        
        return false;
    }
    
    
    public boolean rankingScore( Integer currentPosition, List<Integer> pastPositions, Double threshold ) {
        
        Double sum = 0.0;
        for( Integer position : pastPositions ) {
            sum += position;
        }
        
        Double average = sum / pastPositions.size();
        
        if( Math.abs( currentPosition - average ) > threshold )
            return true;
        
        return false;
    }
    
    
    public boolean averageScore( Double currentCentrality, List<Double> pastCentralities, Double threshold  ) {
        
        Double sum = 0.0;
        for( Double centrality : pastCentralities ) {
            sum += centrality;
        }
        
        Double average = sum / pastCentralities.size();
        
        if( Math.abs( currentCentrality - average ) > threshold )
            return true;
        
        return false;
    }

}
