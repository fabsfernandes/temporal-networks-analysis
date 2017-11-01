package br.ufu.lsi.preference;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Node;

import br.ufu.lsi.event.Main;
import br.ufu.lsi.event.NetworkHandler;
import br.ufu.lsi.event.model.EdgeStreamObject;
import br.ufu.lsi.preference.utils.DateUtil;

public class PreferenceChangeDetectorZeroCases {
    
    private static String NODES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/bipartite/nodes";
    private static String EDGES_FILE = "/Users/fabiola/Desktop/LearnStream/dataset/bipartite/edges";
    
    private NetworkHandler networkHandler;
    
    private int WINDOW = 2;
    
    
    //private static String NODEID = "14594813"; // folha
    
    //private static String NODEID = "14594813";
    //private static String NODEID = "334345564";
    //private static String NODEID = "3145222787";
    //private static String NODEID = "343820098";
    //private static String NODEID = "122757872";
    //private static String NODEID = "28958495";
    //private static String NODEID = "260856271";
    //private static String NODEID = "636368737";
    //private static String NODEID = "279635698";
    //private static String NODEID = "58488491";
    
    //private static String NODEID = "136766642";
    private static String NODEID = "19126035";
    
    
    public PreferenceChangeDetectorZeroCases( DirectedGraph directedGraph, Map< String, Node > nodes ) {
        
        networkHandler = new NetworkHandler( directedGraph );
        networkHandler.insertNodes( nodes );
    }
    
    public static void main( String ... args ) throws Exception {
        Main main = new Main();
        
        main.initializeWorkspace( Main.HOMOGENOUS );
        main.loadStreamToMainMemory( EDGES_FILE, NODES_FILE, Main.HOMOGENOUS );
        PreferenceChangeDetectorZeroCases preferenceChangeDetector = new PreferenceChangeDetectorZeroCases( main.graphModel.getDirectedGraph(), main.nodes );
        preferenceChangeDetector.preferenceDetector( main.edgesList, NODEID );
    }
    
    public void preferenceDetector( List<EdgeStreamObject> edgeStream, String nodeId ) {
        
        long oldestTimestamp = edgeStream.get( 0 ).getInitTimestamp();
        long previousTimestamp = oldestTimestamp;
        List<EdgeStreamObject> nodeObjects = new ArrayList< EdgeStreamObject >();
        int day = 1;
        
        BTG previousBTG = null;
        BTG currentBTG = null;
        
        for( EdgeStreamObject edgeObject : edgeStream ) {
            
            Long currentTimestamp = edgeObject.getInitTimestamp();
            
            if( DateUtil.changeDay( previousTimestamp, currentTimestamp ) ) {
                
                //System.out.println( day++ + ":" + DateUtil.readableDate(previousTimestamp) + " - " + DateUtil.readableDate(currentTimestamp) );
                //System.out.println( day++ );
                
                currentBTG = new BTG();
                currentBTG.buildBTG( nodeObjects );
                currentBTG.execute();
                
                
                // initial case
                if( !currentBTG.isEmpty() && (previousBTG == null || previousBTG.isEmpty()) ) {
                    System.out.println( "1" );
                    
                } 
                else if( currentBTG.isEmpty() && (previousBTG == null || previousBTG.isEmpty()) ) {
                    System.out.println("0");
                    
                }
                else {
                
                    // copy previous value
                    if( currentBTG.isEmpty() ) {
                        currentBTG = previousBTG;
                    }
                        
                    //compare with previousBTG
                    BTG aggregateBTG = BTG.aggregateBTG( previousBTG, currentBTG );
                    aggregateBTG.execute();
                    
                    /*System.out.println( "==> PREVIOUS" );
                    if( previousBTG != null )
                        previousBTG.printGraph();
                    
                    System.out.println( "==> CURRENT" );
                    currentBTG.printGraph();
                    
                    System.out.println( "==> AGGREGATE" );
                    aggregateBTG.printGraph();*/
                    
                    if( aggregateBTG.hasCycle() ) {
                        System.out.println( "1" );
                    } else {
                        System.out.println( "0" );
                    }
                
                }
                
                // slides
                previousBTG = currentBTG;
                nodeObjects.clear();
                
            }
            
            if( edgeObject.getEdge().getTarget().getId().equals( nodeId ) ) {
                nodeObjects.add( edgeObject );
            }
            previousTimestamp = currentTimestamp;
            
        }
        
        
        //System.out.print( day++ );
        
        currentBTG = new BTG();
        currentBTG.buildBTG( nodeObjects );
        currentBTG.execute();
        
        // initial case
        if( !currentBTG.isEmpty() && (previousBTG == null || previousBTG.isEmpty()) ) {
            System.out.println( "1" );
            
        } 
        else if( currentBTG.isEmpty() && (previousBTG == null || previousBTG.isEmpty()) ) {
            System.out.println("0");
            
        }
        else {
        
            // copy previous value
            if( currentBTG.isEmpty() ) {
                currentBTG = previousBTG;
            }
                
            //compare with previousBTG
            BTG aggregateBTG = BTG.aggregateBTG( previousBTG, currentBTG );
            aggregateBTG.execute();
            
            /*System.out.println( "==> PREVIOUS" );
            if( previousBTG != null )
                previousBTG.printGraph();
            System.out.println( "==> CURRENT" );
            currentBTG.printGraph();
            System.out.println( "==> AGGREGATE" );
            aggregateBTG.printGraph();*/
            
            
            if( aggregateBTG.hasCycle() ) {
                System.out.println( "1" );
            } else {
                System.out.println( "0" );
            }
        }
        
    }
    
    public void slidesNodeObjects( List<EdgeStreamObject> nodeObjects, Long timestamp ) {
        for( int i = 0; i<nodeObjects.size(); i++ ) {
            EdgeStreamObject object = nodeObjects.get( i );
            if( object.getInitTimestamp() < timestamp ) {
                nodeObjects.remove( i );
                i--;
            }
        }
    }
    
    public boolean windowFull( Long oldestTimestamp, Long currentTimestamp ){
        
        int nDays = DateUtil.checkNumberOfDaysInInterval( oldestTimestamp, currentTimestamp );
        nDays = Math.abs( nDays );
        return nDays > WINDOW;
    }

    

}
