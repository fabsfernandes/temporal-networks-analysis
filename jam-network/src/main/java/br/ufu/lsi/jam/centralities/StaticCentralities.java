
package br.ufu.lsi.jam.centralities;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

import br.ufu.lsi.event.NetworkHandler;
import br.ufu.lsi.event.utils.FileUtil;

public class StaticCentralities {

    private static String NODES_FILE = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/nodes.csv";

    private static String EDGES_FILE = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/edges.csv";

    public static GraphModel graphModel;
    
    public static NetworkHandler networkHandler;

    public static Map< String, Node > nodes = new HashMap< String, Node >();

    public static Map< String, Edge > edges = new HashMap< String, Edge >();

    public static void main( String... args ) throws Exception {
        
        
        if( args.length > 0 ) {
            NODES_FILE = args[0];
            EDGES_FILE = args[1];
        }
        
        
        System.out.println( "Initializing workspace..." );
        initializeWorkspace();
        
        System.out.println( "Loading network to main memory..." );
        loadNetworkToMainMemory();
        
        System.out.println( "Update network..." );
        updateNetwork();
        
        System.out.println( "Computing static centralities..." );
        computeStaticCentralities();
    }
    
    public static void computeStaticCentralities() {
        
        
        networkHandler.computeDistances();
        
        
        for( Entry<String,Node> entry : nodes.entrySet() ) {
            BigDecimal betweenness = networkHandler.getBetweennessDirect( (String) entry.getValue().getId() );
            BigDecimal closeness = networkHandler.getClosenessDirect( (String) entry.getValue().getId() );
            System.out.println(  entry.getKey() + ";" + betweenness + ";" + closeness );
        }
    }
    
    public static void updateNetwork() throws Exception {
        
        networkHandler = new NetworkHandler( graphModel.getDirectedGraph() );
        for( Entry<String,Edge> entry : edges.entrySet() ) {
            
            networkHandler.updateNetwork( entry.getValue() );
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
        
        /*for ( Entry< String, Edge > entry : edges.entrySet() ) {
            EdgeStreamObject edgeStreamObject = new EdgeStreamObject();
            edgeStreamObject.setEdge( entry.getValue() );
            edgeStreamObject
                    .setInitTimestamp( ( Long ) entry.getValue().getAttribute( "timeInit" ) );
            edgesList.add( edgeStreamObject );
        }
        Collections.sort( edgesList );*/
    }
    
    public static void loadNodes() throws Exception {

        BufferedReader br = FileUtil.openInputFile( NODES_FILE );

        // with header
        String line = br.readLine();

        while ( ( line = br.readLine() ) != null ) {
            String id = line.split( ";" )[ 0 ];

            Node node = graphModel.factory().newNode( id );
            node.setLabel( id );
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
