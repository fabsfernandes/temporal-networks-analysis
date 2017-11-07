
package br.ufu.lsi.jam.centralities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.GraphDistance;
import org.openide.util.Lookup;

import br.ufu.lsi.event.NetworkHandler;
import br.ufu.lsi.jam.model.TimestampedEdge;
import br.ufu.lsi.jam.utils.DateUtils;
import br.ufu.lsi.jam.utils.FileUtil;
import br.ufu.lsi.jam.utils.Granularity;

public class StaticCentralities {

    private static String NODES_FILE = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/nodes.csv";

    private static String EDGES_FILE = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/edges.csv";
    
    private static final String INIT = "2012-08-26";
    
    private static final String END = "2015-09-26";
    
    private static Granularity GRANULARITY = Granularity.MONTH;

    public static GraphModel graphModel;
    
    public static NetworkHandler networkHandler;

    public static Map< String, Node > nodes = new HashMap< String, Node >();
    
    public static Map< String, List<BigDecimal> > nodesCloseness = new HashMap< String, List<BigDecimal> >();
    
    public static Map< String, List<BigDecimal> > nodesBetweenness = new HashMap< String, List<BigDecimal> >();

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
        
        BufferedWriter bwClo = FileUtil.openOutputFile( NODES_FILE.replace( ".csv", GRANULARITY.toString() + "-CLO.csv" ) );
        BufferedWriter bwBet = FileUtil.openOutputFile( NODES_FILE.replace( ".csv", GRANULARITY.toString() + "-BET.csv" ) );
        
        for( Entry<String,Node> entry : nodes.entrySet() ) {
            Node node = entry.getValue();
            bwClo.write( node.getId()  + ";" );
            bwBet.write( node.getId()  + ";" );
            List<BigDecimal> closenessValues = nodesCloseness.get( node.getId() );
            List<BigDecimal> betweennessValues = nodesBetweenness.get( node.getId() );
            
            for( int i = 0; i<closenessValues.size(); i++ ) {
                BigDecimal clo = closenessValues.get( i );
                BigDecimal bet = betweennessValues.get( i );
                bwClo.write( clo + ";" );
                bwBet.write( bet + ";" );
            }
            bwClo.write( "\n" );
            bwBet.write( "\n" );
        }
        
        bwClo.close();
        bwBet.close();
    }
    
    
    public static void updateNetwork() throws Exception {
        
        
        String time;
        for( time = INIT; DateUtils.checkBefore( time, END ); time = DateUtils.nextPeriod( time, GRANULARITY ) ) {
            
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
            
            // clear current network
            for( TimestampedEdge te : currentEdges ) {
                networkHandler.removeOldEdge( te.getEdge() );
            }
        }
        
        // last period
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
        
        computeStaticCentralities( time );
        
    }
    
    
    public static void computeStaticCentralities( String time ) {
        
        System.out.println( "Computing static centralities... " + time );
        
        networkHandler.computeDistances();
       
        for( Entry<String,Node> entry : nodes.entrySet() ) {
            
            BigDecimal betweenness = networkHandler.getBetweennessDirect( (String) entry.getValue().getId() );
            BigDecimal closeness = networkHandler.getClosenessDirect( (String) entry.getValue().getId() );
            
            Node node = entry.getValue();
            
            List<BigDecimal> closenessValues = nodesCloseness.get( node.getId() );
            closenessValues.add( closeness );
            
            List<BigDecimal> betweennessValues = nodesBetweenness.get( node.getId() );
            betweennessValues.add( betweenness );
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

            List<BigDecimal> closenessValues = new ArrayList<BigDecimal>();
            nodesCloseness.put( id, closenessValues );
            
            List<BigDecimal> betweennessValues = new ArrayList<BigDecimal>();
            nodesBetweenness.put( id, betweennessValues );
            
            nodes.put( id, node );
        }

        br.close();

        System.out.println( "Finish nodes: " + nodes.size() );

    }

    public static void loadEdges() throws Exception {

        BufferedReader br2 = FileUtil.openInputFile( EDGES_FILE );
       
        // with header
        String line2 = br2.readLine();
        
        int cont = 0;
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

            if( ++cont % 100000 == 0 )
                System.out.println( cont );
        }

        br2.close();

        System.out.println( "Finish edges: " + edges.size() );

    }


}
