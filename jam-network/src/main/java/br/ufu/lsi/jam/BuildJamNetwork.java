package br.ufu.lsi.jam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import br.ufu.lsi.jam.utils.FileUtil;


public class BuildJamNetwork {
    
    private static final String JAMS = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/jams.tsv";
    
    private static final String JAM_TO_MSD_TO_GENRE = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/jam_to_msd_to_genre.tsv";

    private static final String LIKES = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/likes.tsv";
    
    private static final String EDGES = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/edges.csv";
    
    private static final String NODES = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/nodes.csv";
    
    private static final String FILTER1 = "2014-12-31";
    
    private static final String FILTER2 = "2015-02-01";
    
    private static Map<String,User> jams = new HashMap<String,User>();
    
    private static Map<String,String> jamGenre = new HashMap<String,String>();
    
    private static Map<String,Edge> edges = new HashMap<String,Edge>();
    
    private static Set<String> users = new HashSet<String>();
    
    
    public static void main( String ... args ) throws Exception {
        
        System.out.println( "Loading genres..." );
        loadGenres();
        
        System.out.println( "Loading jams..." );
        loadJams();
        
        System.out.println( "Loading likes..." );
        loadLikes();
        
        System.out.println( "Writing network..." );
        writeNetwork();
    }
    
    
    public static void loadGenres() throws Exception {
        
        BufferedReader br = FileUtil.openInputFile( JAM_TO_MSD_TO_GENRE );
        
        String line = br.readLine();
        
        int i = 0;
        
        while( (line = br.readLine()) != null ) {
            
            if( ++i % 100000 == 0 )
                System.out.println( i );
            
            String jamId = line.split( "\t" )[0];
            String genre = line.split( "\t" )[2];
            
            jamGenre.put( jamId, genre );
            
        }
        br.close();
        System.out.println( "Jam genres size = " + jamGenre.size() );
    }
    
    
    public static void writeNetwork() throws Exception {
        
        // edges
        BufferedWriter bw = FileUtil.openOutputFile( EDGES.replace( ".csv", FILTER1 + "-" + FILTER2 + ".csv" ) );
        bw.write( "Source;Target;genre;timeset\n");
        for( Entry<String,Edge> edge : edges.entrySet() ) {
            StringBuilder line = new StringBuilder(); 
            line.append( edge.getValue().source );
            line.append( ";" );
            line.append( edge.getValue().target );
            line.append( ";" );
            line.append( edge.getValue().genre );
            line.append( ";" );
            line.append( "<[" + edge.getValue().timestamp + "," + edge.getValue().timestamp + "]>");
            bw.write( line.toString() + "\n" );
        }
        
        bw.close();
        
        // nodes
        BufferedWriter bw2 = FileUtil.openOutputFile( NODES.replace( ".csv", FILTER1 + "-" + FILTER2 + ".csv" ) );
        bw2.write( "id;label\n");
        for( String user : users ) {
            bw2.write( user + ";" + user + "\n" );
        }
        
        bw2.close();
    }
    
    
    public static void loadLikes() throws Exception {
        
        BufferedReader br = FileUtil.openInputFile( LIKES );
        
        String line = br.readLine();
        
        int i = 0;
        
        while( (line = br.readLine()) != null ) {
            
            if( ++i % 100000 == 0 )
                System.out.println( i );
            
            String userId = line.split( "\t" )[0];
            String jamId = line.split( "\t" )[1];
            
            User target = jams.get( jamId );
            
            if( target != null && !target.timestamp.isEmpty() ) {
                
                // filter by timestamp
                Date dateFilter1 = new SimpleDateFormat("yyyy-MM-dd").parse( FILTER1 );
                Date dateFilter2 = new SimpleDateFormat("yyyy-MM-dd").parse( FILTER2 );
                Date date = new SimpleDateFormat("yyyy-MM-dd").parse( target.timestamp );
                
                if( date.after( dateFilter1 ) && date.before( dateFilter2 ) ) {
                
                    Edge edge = new Edge();
                    edge.target = target.userId;
                    edge.source = userId;
                    edge.timestamp = target.timestamp;
                    edge.genre = target.genre;
                
                    String edgeId = edge.getEdgeId();
                    
                    if( edges.get( edgeId ) != null ) {
                        //System.out.println("Deu zica: " + edgeId );
                        
                    } else {
                        edges.put( edgeId, edge );
                        users.add( edge.source );
                        users.add( edge.target );
                    }
                }
            }
        }
        
        br.close();
        System.out.println( "#nodes = " + users.size() );
        System.out.println( "#edges = " + edges.size() );
    }
    
    
    public static void loadJams() throws Exception {
        
        BufferedReader br = FileUtil.openInputFile( JAMS );
        
        String line = br.readLine();
        
        String jamId = "";
        String userId = "";
        String artist = "";
        String title = "";
        String creationDate = "";
        String link = "";
        String spotifyUri = "";
        
        int i = 0;
        int jamWithoutGenre = 0;
        
        while( (line = br.readLine()) != null ) {
            
            if( ++i % 100000 == 0 )
                System.out.println( i );
            
            int cont = 0;
            String temp = "";
            for( int j = 0; j <= line.length(); j++ ) {
                
                if( j == line.length() || line.charAt( j ) == '\t' ) {
                    
                    switch( cont ) {
                        case 0:
                            jamId = temp; 
                            break;
                        case 1:
                            userId = temp;
                            break;
                        case 2:
                            artist = temp;
                            break;
                        case 3:
                            title = temp;
                            break;
                        case 4:
                            creationDate = temp;
                            break;
                        case 5:
                            link = temp;
                            break;
                        case 6:
                            spotifyUri = temp;
                            break;
                                
                    }
                    cont++;
                    temp = "";
                }
                else {
                    temp += Character.toString( line.charAt( j ) );
                }
            }
           
            
            User user = new User();
            user.jamId = jamId;
            user.timestamp = creationDate;
            user.userId = userId;
            
            if( jams.get( jamId ) != null ) {
                System.out.println( "Jams can be duplicated: " + jamId );
            } else if ( jamGenre.get( jamId ) == null ) {
                jamWithoutGenre++;
            } else {
                user.genre = jamGenre.get( jamId );
                jams.put( jamId, user );
            }                 
        }        
        br.close();
        System.out.println( "Jams size = " + jams.size() );
        System.out.println( "Jams without genre = " + jamWithoutGenre );
    }
}


class User implements Comparable< User >{
    String userId;
    String timestamp;
    String jamId;
    String genre;
    
    public int compareTo( User o ) {
        int compare = userId.compareTo( o.userId );
        if( compare == 0 ) {
            return timestamp.compareTo( o.timestamp );
        }
        return compare; 
    }
}


class Edge {
    
    String timestamp;
    String source;
    String target;
    String genre;
    
    String getEdgeId() {
        return source + "-" + target + "-" + timestamp;
    }
}
