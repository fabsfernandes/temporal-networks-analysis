
package br.ufu.lsi.jam.centralities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

import br.ufu.lsi.event.NetworkHandler;
import br.ufu.lsi.jam.model.TimestampedEdge;
import br.ufu.lsi.jam.utils.Centrality;
import br.ufu.lsi.jam.utils.DateUtils;
import br.ufu.lsi.jam.utils.FileUtil;
import br.ufu.lsi.jam.utils.Granularity;
import br.ufu.lsi.jam.utils.Score;

public class StaticCentralities {

    private static String NODES_FILE = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/nodes.csv";

    private static String EDGES_FILE = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/edges.csv";
    
    private static final String INIT = "2011-08-26";
    
    private static final String END = "2015-09-26";
    
    private static Granularity GRANULARITY = Granularity.MONTH;
    
    private static Score SCORE = Score.AVERAGE;
    
    private static Centrality CENTRALITY = Centrality.CLOSENESS;
    
    private static int W = 4;
    
    private static BigDecimal THRESHOLD = new BigDecimal( 0.01 ).setScale( 2, RoundingMode.HALF_UP );

    public static GraphModel graphModel;
    
    public static NetworkHandler networkHandler;

    public static Map< String, Node > nodes = new HashMap< String, Node >();
    
    public static Map< String, List<BigDecimal> > nodesCentrality = new HashMap< String, List<BigDecimal> >();
    
    public static Map< String, List<Integer> > nodesChanges = new HashMap< String, List<Integer> >();
    
    public static List< String > runningTimes = new ArrayList<String>();

    public static Map< String, Edge > edges = new HashMap< String, Edge >();
    
    public static List<TimestampedEdge> orderedEdges = new ArrayList<TimestampedEdge>();

    public static void main( String... args ) throws Exception {
        
        
        if( args.length > 0 ) {
            
            NODES_FILE = args[0];
            
            EDGES_FILE = args[1];
            
            String granularity = args[2];
            switch( granularity ) {
                case "day": GRANULARITY = Granularity.DAY;
                    break;
                case "month": GRANULARITY = Granularity.MONTH;
                    break;
                case "semester": GRANULARITY = Granularity.SEMESTER;
                    break;
                case "year": GRANULARITY = Granularity.YEAR;
                    break;
            }
            
            String score = args[3];
            switch( score ) {
                case "average": SCORE = Score.AVERAGE;
                    break;
                case "ranking": SCORE = Score.RANKING;
                    break;
                case "z": SCORE = Score.Z;
                    break;
            }
            
            String centrality = args[4];
            switch( centrality ) {
                case "closeness": CENTRALITY = Centrality.CLOSENESS;
                    break;
                case "betweenness": CENTRALITY = Centrality.BETWEENNESS;
                    break;
            }
            
            W = Integer.parseInt( args[5] );
            
            
            Double d = Double.parseDouble( args[6] );
            THRESHOLD = new BigDecimal( d ).setScale( 2, RoundingMode.HALF_UP );
        }
        
        
        System.out.println( "Initializing workspace..." );
        initializeWorkspace();
        
        System.out.println( "Loading network to main memory..." );
        loadNetworkToMainMemory();
        
        System.out.println( "Update network..." );
        updateNetwork();
        
        System.out.println( "Printing result..." );
        printResults();
        
    }
    
    public static void printResults() throws Exception {
        
        String setup = "-W" + W + "-T" + THRESHOLD + "-" + CENTRALITY.toString() + "-" + SCORE.toString() + "-" + GRANULARITY.toString() + "-STATIC";
        BufferedWriter bwCentrality = FileUtil.openOutputFile( NODES_FILE.replace( ".csv", "-CENTRALITY" + setup + ".csv" ) );
        BufferedWriter bwChanges = FileUtil.openOutputFile( NODES_FILE.replace( ".csv", "-CHANGES" + setup + ".csv" ) );
        BufferedWriter bwRunningTimes = FileUtil.openOutputFile( NODES_FILE.replace( ".csv", "-RUNTIMES" + setup + ".csv" ) );
        
        // centralities
        for( Entry<String,Node> entry : nodes.entrySet() ) {
            Node node = entry.getValue();
            bwCentrality.write( node.getId()  + ";" );
            List<BigDecimal> centralityValues = nodesCentrality.get( node.getId() );
            
            for( int i = 0; i<centralityValues.size(); i++ ) {
                BigDecimal centrality = centralityValues.get( i );
                bwCentrality.write( centrality + ";" );
            }
            bwCentrality.write( "\n" );
        }
        
        bwCentrality.close();
        
        // changes
        for( Entry<String,List<Integer>> entry : nodesChanges.entrySet() ) {
            bwChanges.write( entry.getKey() );
            for( Integer i : entry.getValue() ) {
                bwChanges.write( ";" + i );
            }
            bwChanges.write( "\n" );
        }
        bwChanges.close();
        
        
        // running times
        for( String line : runningTimes ) {
            bwRunningTimes.write( line + "\n" );
        }
        bwRunningTimes.close();
    }
    
    
    public static void updateNetwork() throws Exception {
        
        
        String time;
        for( time = INIT; DateUtils.checkBefore( time, END ); time = DateUtils.nextPeriod( time, GRANULARITY ) ) {
            
            long init = System.currentTimeMillis();
            
            // load edges of the period
            List<TimestampedEdge> currentEdges = new ArrayList<TimestampedEdge>();
            networkHandler = new NetworkHandler( graphModel.getDirectedGraph() );
            
            for( TimestampedEdge timestampedEdge : orderedEdges ) {
                String inferiorLimit = time;
                String superiorLimit = DateUtils.nextPeriod( time, GRANULARITY );
                if( DateUtils.checkInside( timestampedEdge.getTime(), inferiorLimit, superiorLimit ) ) {
                    
                    currentEdges.add( timestampedEdge );
                    networkHandler.updateNetwork( timestampedEdge.getEdge() );
                    
                }
            }
            
            // compute centralities
            computeStaticCentralities( time );
            
            // compute nodes changes
            computeNodesChanges();
            
            // clear current network
            for( TimestampedEdge te : currentEdges ) {
                networkHandler.removeOldEdge( te.getEdge() );
            }
            
            long end = System.currentTimeMillis();
            System.out.println( time + ";" + (end - init) );
            runningTimes.add( time + ";" + (end - init) );
        }
        
        // last period
        long init = System.currentTimeMillis();
        
        List<TimestampedEdge> currentEdges = new ArrayList<TimestampedEdge>();
        networkHandler = new NetworkHandler( graphModel.getDirectedGraph() );
        
        for( TimestampedEdge timestampedEdge : orderedEdges ) {
            String inferiorLimit = time;
            String superiorLimit = END;
            if( DateUtils.checkInside( timestampedEdge.getTime(), inferiorLimit, superiorLimit ) ) {
                currentEdges.add( timestampedEdge );
                networkHandler.updateNetwork( timestampedEdge.getEdge() );
            }
        }
        
        // compute static centralities
        computeStaticCentralities( time );
        
        // compute nodes changes
        computeNodesChanges();
        
        long end = System.currentTimeMillis();
        System.out.println( time + ";" + (end - init) );
        runningTimes.add( time + ";" + (end - init) );
        
    }
    
    public static void computeNodesChanges() {
        
        // for each node
        for( Entry<String,List<BigDecimal>> entry : nodesCentrality.entrySet() ) {
            computeScore( entry.getKey(), entry.getValue() );
        }
        
    }
    
    public static void computeScore( String node, List<BigDecimal> nodesCentralities ) {
        
        BigDecimal current = nodesCentralities.get( nodesCentralities.size()-1 );
        int pivot = nodesCentralities.size()-2;
        BigDecimal score = BigDecimal.ZERO;
        
        switch( SCORE ) {
            case AVERAGE:
                BigDecimal sum = BigDecimal.ZERO;
                for( int i = 0; i < W; i++ ) {
                    if( pivot-i < 0 ) {
                        nodesChanges.get( node ).add( 0 );
                        return;
                    }
                    sum = sum.add( nodesCentralities.get( pivot-i ) );
                }
                BigDecimal avg = sum.divide( new BigDecimal( W ) );
                
                if( avg.max( current ).compareTo( BigDecimal.ZERO ) == 0 ) {
                    nodesChanges.get( node ).add( 0 );
                    return;
                }
                score = avg.subtract( current ).abs().divide( avg.max( current ), RoundingMode.HALF_UP );
                break;
                
            case RANKING:
                break;
                
            case Z:
                break;
        }
        
        if( score.compareTo( THRESHOLD ) > 0 )
            nodesChanges.get( node ).add( 1 );
        else
            nodesChanges.get( node ).add( 0 );
    }
    
    
    public static void computeStaticCentralities( String time ) {
        
        //System.out.println( "Computing static centralities... " + time );
        
        networkHandler.computeDistances();
       
        for( Entry<String,Node> entry : nodes.entrySet() ) {
            
            BigDecimal centrality = BigDecimal.ZERO;
            
            switch( CENTRALITY ) {
                case BETWEENNESS:
                    centrality = networkHandler.getBetweennessDirect( (String) entry.getValue().getId() );
                    break;
                case CLOSENESS:
                    centrality = networkHandler.getClosenessDirect( (String) entry.getValue().getId() );
                    break;
            }
            
            
            Node node = entry.getValue();
            
            List<BigDecimal> centralityValues = nodesCentrality.get( node.getId() );
            centralityValues.add( centrality );
            
        }
        
    }
    
    

    
    public static void initializeWorkspace() throws Exception {

        ProjectController pc = Lookup.getDefault().lookup( ProjectController.class );
        pc.newProject();
        graphModel = Lookup.getDefault().lookup( GraphController.class ).getGraphModel();
        
        graphModel.getEdgeTable().addColumn( "timeInit", String.class );
        graphModel.getEdgeTable().addColumn( "timeEnd", String.class );
        graphModel.getEdgeTable().addColumn( "genre", String.class );
    }
    
    
    public static void loadNetworkToMainMemory(  ) throws Exception {
        
        loadNodes();
        loadEdges();
        
        for ( Entry< String, Edge > entry : edges.entrySet() ) {
            TimestampedEdge timestampedEdge = new TimestampedEdge();
            timestampedEdge.setEdge( entry.getValue() );
            timestampedEdge.setTime( ( String ) entry.getValue().getAttribute( "timeInit" ) );
            orderedEdges.add( timestampedEdge );
        }
        Collections.sort( orderedEdges );
    }
    
    public static void loadNodes() throws Exception {

        BufferedReader br = FileUtil.openInputFile( NODES_FILE );

        // with header
        String line = br.readLine();

        while ( ( line = br.readLine() ) != null ) {
            String id = line.split( ";" )[ 0 ];

            Node node = graphModel.factory().newNode( id );
            node.setLabel( id );

            List<BigDecimal> centralitiesValues = new ArrayList<BigDecimal>();
            nodesCentrality.put( id, centralitiesValues );
            
            List<Integer> nodeChangesList = new ArrayList<Integer>();
            nodesChanges.put( id, nodeChangesList );
            
            nodes.put( id, node );
        }

        br.close();

        System.out.println( "Finish nodes: " + nodes.size() );

    }

    public static void loadEdges() throws Exception {

        BufferedReader br2 = FileUtil.openInputFile( EDGES_FILE );
       
        // with header
        String line2 = br2.readLine();
        
        while ( ( line2 = br2.readLine() ) != null ) {
            String source = line2.split( ";" )[ 0 ];
            String target = line2.split( ";" )[ 1 ];
            String genre = line2.split( ";" )[ 2 ];
            String timeset = line2.split( ";" )[ 3 ];

            Node sourceNode = nodes.get( source );
            Node targetNode = nodes.get( target );
            String timeInit = timeset.substring( 2, 12 );
            
            Edge edge = graphModel.factory().newEdge( sourceNode, targetNode, true );
            edge.setAttribute( "timeInit", timeInit );
            edge.setAttribute( "timeEnd", timeInit );
            edge.setAttribute( "genre", genre );
            
            edges.put( source + "-" + target + "-" + timeInit, edge );
        }

        br2.close();

        System.out.println( "Finish edges: " + edges.size() );

    }


}