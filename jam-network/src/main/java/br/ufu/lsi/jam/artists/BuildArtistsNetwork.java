package br.ufu.lsi.jam.artists;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import br.ufu.lsi.jam.utils.DateUtils;
import br.ufu.lsi.jam.utils.FileUtil;
import br.ufu.lsi.jam.utils.Granularity;
import br.ufu.lsi.jam.utils.StringClean;

public class BuildArtistsNetwork {
    
    private static final String JAMS = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/jams.tsv";
    private static final String EDGES_ARTIST = "/Users/fabiola/Desktop/BraSNAM2018/dataset/edges.csv";
    private static final String MAP_ARTISTS = "/Users/fabiola/Desktop/BraSNAM2018/dataset/dynetvis/mapArtists.csv";
    private static final String MAP_TIMES = "/Users/fabiola/Desktop/BraSNAM2018/dataset/dynetvis/mapTimes.csv";
    private static final String EDGES_ARTIST_DYNETVIS = "/Users/fabiola/Desktop/BraSNAM2018/dataset/dynetvis/edges.csv";
    
    private static final String INFERIOR_LIMIT = "2011-08-26";
    private static final String SUPERIOR_LIMIT = "2015-09-26";
    private static final Granularity GRANULARITY = Granularity.DAY;

    private static final double WEIGHT_THRESHOLD = 0.0;
    
    private static Set<String> uniqueArtists = new HashSet<String>();
    
    private static Set<String> uniqueUsers = new HashSet<String>();
    
    private static Map<Long,String> uniqueTimes = new HashMap<Long,String>();
    
    // for each time, a set of users with respective listened artists
    private static Map<Long,Map<String,Set<String>>> times = new HashMap<Long,Map<String,Set<String>>>();
    
    //private static List<Map<String,Edge>> edgesListByTime = new ArrayList<Map<String,Edge>>();
    
    private static Map<String,Edge> [] edgesListByTime;// = new HashMap[size];
    
    public static void main( String ... args ) throws Exception {
        
        int size = DateUtils.getNumberOfTimeSteps( INFERIOR_LIMIT, SUPERIOR_LIMIT, GRANULARITY );
        edgesListByTime = new HashMap[size+1];
        
        loadJams();
        buildEdges();
        printEdges();
        
        printDyNetVisFormat();
    }
    
    public static void printDyNetVisFormat() throws Exception {
        System.out.println( "Print dynetvis format" );
        
        // map and print artists
        Map<String,Integer> mapArtists = new HashMap<String,Integer>();
        BufferedWriter bwMap = FileUtil.openOutputFile( MAP_ARTISTS );
        int id = 1;
        for( String artist : uniqueArtists ) {
            bwMap.write( artist + "," + id + "\n" );
            mapArtists.put( artist, id++ );
        }
        bwMap.close();
        
        // map times
        BufferedWriter bwMapTimes = FileUtil.openOutputFile( MAP_TIMES );
        for( Entry<Long,String> entry : uniqueTimes.entrySet() ) {
            bwMapTimes.write( entry.getKey() + "," + entry.getValue() + "\n" );
        }
        bwMapTimes.close();
        
        BufferedWriter bw = FileUtil.openOutputFile( EDGES_ARTIST_DYNETVIS.replace( ".csv", "-" + GRANULARITY + ".csv" ) );
        bw.write( "idArt1;idArt2;time;weight\n" );
        for( int time = 1; time < edgesListByTime.length; time++ ) {
            Map<String,Edge> edges = edgesListByTime[time];
            if( edges != null ) {
                for( Entry<String,Edge> entry : edges.entrySet() ) {
                    Edge edge = entry.getValue();
                    if( edge.weight > WEIGHT_THRESHOLD ) {
                        Integer artist1Id = mapArtists.get( edge.source );
                        Integer artist2Id = mapArtists.get( edge.target );
                        bw.write( artist1Id + " " + artist2Id + " " + edge.time + " " + edge.weight + "\n" );
                    }
                }
            }
        }
        bw.close();
    }
    
    
    public static void printEdges() throws Exception {
        
        double i = 1.0;
        BufferedWriter bw = FileUtil.openOutputFile( EDGES_ARTIST.replace( ".csv", "-" + GRANULARITY + ".csv" ) );
        bw.write( "Id;Source;Target;Type;weight;timeset\n" );
        for( int time = 1; time < edgesListByTime.length; time++ ) {
            Map<String,Edge> edges = edgesListByTime[time];
            //BufferedWriter bw = FileUtil.openOutputFile( EDGES_ARTIST.replace( ".csv", "-" + GRANULARITY + "-" + time + ".csv" ) );
            //bw.write( "Id;Source;Target;weight;timeset\n" );
            if( edges != null ) {
                for( Entry<String,Edge> entry : edges.entrySet() ) {
                    Edge edge = entry.getValue();
                    if( edge.weight > WEIGHT_THRESHOLD )
                        bw.write( i++ + ";" + edge.source + ";" + edge.target + ";undirected;" + edge.weight + ";<[" + edge.time + "," + edge.time + "]>\n" );
                }
            }
            //bw.close();
        }
        bw.close();
    }
    
    public static void buildEdges() throws Exception {
        
        for( Entry<Long,Map<String,Set<String>>> entry : times.entrySet() ) {
            
            long time = entry.getKey();
            
            System.out.println( time );
            
            Map<String,Edge> edges = new HashMap<String,Edge>();
            edgesListByTime[(int) (long) time] = edges;
            
            Map<String,Set<String>> users = entry.getValue();
            for( Entry<String,Set<String>> entryUser : users.entrySet() ) {
                String user = entryUser.getKey();
                //System.out.println( user );
                
                Set<String> artists = entryUser.getValue();
                
                String [] artistsArray = artists.stream().toArray( String[]::new );

                for( int i = 0; i < artistsArray.length-1; i++ ) {
                    String artist1 = artistsArray[i];
                    for( int j = i+1; j < artistsArray.length; j++ ) {
                        String artist2 = artistsArray[j];
                        
                        // always lexicograph order for edge id
                        if( artist2.compareTo( artist1 ) == -1 ) {
                            String temp = artist1;
                            artist1 = artist2;
                            artist2 = temp;
                        }
                        
                        String edgeId = artist1 + "-" + artist2 + "-" + time;
                        Edge edge = edges.get( edgeId );
                        if( edge == null ) {
                            edge = new Edge();
                            edge.source = artist1;
                            edge.target = artist2;
                            edge.time = time;
                            edge.weight = 1;
                            edges.put( edgeId, edge );
                        } else {
                            edge.weight++;
                        }
                        
                    }
                }
                
            }
            System.out.println( "Time " + time + " - number of edges: " + edges.size() );
        }
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
                    
                    temp = StringClean.clean( temp );
                    temp = StringClean.removeStopWords( temp );
                    
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
                    
                    //if( temp.contains( "Yankovic" ))
                      //  System.out.println( temp );
                    
                    //if( temp.contains( "Yankovic" ))
                      //  System.out.println( temp );
                    
                    cont++;
                    temp = "";
                }
                else {
                    temp += Character.toString( line.charAt( j ) );
                }
            }
            

            if( !artist.isEmpty() && artist.length() > 2) {
                // get time
                long time = DateUtils.getPeriod( creationDate, GRANULARITY, INFERIOR_LIMIT );
                
                // get users
                Map<String,Set<String>> users = times.get( time );
                if( users == null ) {
                    users = new HashMap<String,Set<String>>();
                    times.put( time, users );
                }
                
                // get artists
                Set<String> artists = users.get( userId );
                if( artists == null ) {
                    artists = new HashSet<String>();
                    users.put( userId, artists );
                }
                
                artists.add( artist );
                
                // statistics only
                uniqueArtists.add( artist );
                uniqueUsers.add( userId );
                uniqueTimes.put( time, creationDate );
            }
        }        
        br.close();
        
        System.out.println( "Unique artists = " + uniqueArtists.size() );
        System.out.println( "Unique users = " + uniqueUsers.size()  );
        System.out.println( "Number of timestamps = " + times.size() );
    }

}

class Edge {
    
    long time;
    String source;
    String target;
    double weight;
    
    String getEdgeId() {
        return source + "-" + target + "-" + time;
    }
}

