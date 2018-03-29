package br.ufu.lsi.jam.users;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
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

public class BuildUsersNetwork {
    
    private static final String JAMS = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/jams.tsv";
    private static final String JAMS_FOLLOWERS = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/original-dataset/followers.tsv";
    private static final String EDGES_USER = "/Users/fabiola/Desktop/BraSNAM2018/dataset/edges-users.csv";
    private static final String EDGES_USER_DYNETVIS = "/Users/fabiola/Desktop/BraSNAM2018/dataset/dynetvis/edges-users.csv";
    private static final String MAP_USERS_DYNETVIS = "/Users/fabiola/Desktop/BraSNAM2018/dataset/dynetvis/mapUsers-POP200.csv";
    
    private static final String INFERIOR_LIMIT = "2011-08-26";
    private static final String SUPERIOR_LIMIT = "2015-09-26";
    //private static final Granularity GRANULARITY = Granularity.QUARTER_YEAR;
    private static final Granularity GRANULARITY = Granularity.DAY;
    //private static final double WEIGHT_THRESHOLD = 100.0;
    private static final double WEIGHT_THRESHOLD = 0.0;
    private static final int POPULARITY_THRESHOLD = 200;
    
    private static Map<String,Integer> uniqueArtists = new HashMap<String,Integer>();
    
    private static Set<String> uniqueUsers = new HashSet<String>();
    
    private static Map<Long,String> uniqueTimes = new HashMap<Long,String>();
    
    // for each time, a set of artists with respective users that listened them
    private static Map<Long,Map<String,Set<String>>> times = new HashMap<Long,Map<String,Set<String>>>();
    
    private static Map<String,Edge> [] edgesListByTime;
    
    // popularity
    private static Map<String,Integer> popularUsers = new HashMap<String,Integer>();
    private static Map<String,Integer> mapPopularUsers = new HashMap<String,Integer>();
    
    public static void main( String ... args ) throws Exception {
        
        int size = DateUtils.getNumberOfTimeSteps( INFERIOR_LIMIT, SUPERIOR_LIMIT, GRANULARITY );
        edgesListByTime = new HashMap[size+1];
        
        loadJams();
        buildEdges();
        
        //printEdges();
        getMostPopular();
        printEdgesDyNetVis();
    }
    
    
    public static void getMostPopular() throws Exception {
        
        BufferedReader br = FileUtil.openInputFile( JAMS_FOLLOWERS );
        
        String line = br.readLine();
        while( (line = br.readLine() ) != null ) {
            String followed = line.split( "\t" )[0];
                        
            if( popularUsers.get( followed ) == null ) {
                popularUsers.put( followed, 1 );
            } else {
                Integer cont = popularUsers.get( followed );
                popularUsers.put( followed, cont+1 );
            }
        }
        br.close();
        
        // sort popular users
        List<User> users = new ArrayList<User>();
        for( Entry<String,Integer> entry : popularUsers.entrySet() ) {
            User u = new User( entry.getKey(), entry.getValue() );
            users.add( u );
        }
        Collections.sort( users );
        
        // keep only top popular
        for( int i = POPULARITY_THRESHOLD; i < users.size(); i++ ) {
            popularUsers.remove( users.get( i ).user );
        }
        
        // map
        BufferedWriter bw = FileUtil.openOutputFile( MAP_USERS_DYNETVIS );
        int i = 1;
        for( Entry<String,Integer> entry : popularUsers.entrySet() ) {
            mapPopularUsers.put( entry.getKey(), i );
            bw.write( entry.getKey() + " " + i + "\n" );
            i++;   
        }
        bw.close();
    }
    
    public static void printEdgesDyNetVis() throws Exception {
        
        double i = 1.0;
        BufferedWriter bw = FileUtil.openOutputFile( EDGES_USER_DYNETVIS.replace( ".csv", "-" + GRANULARITY + ".csv" ) );
        bw.write( "Source Target time weight\n" );
        for( int time = 1; time < edgesListByTime.length; time++ ) {
            Map<String,Edge> edges = edgesListByTime[time];
            if( edges != null ) {
                for( Entry<String,Edge> entry : edges.entrySet() ) {
                    Edge edge = entry.getValue();
                    if( edge.weight > WEIGHT_THRESHOLD ) {
                        if( popularUsers.get( edge.source ) != null && popularUsers.get( edge.target ) != null ) {
                            Integer source = mapPopularUsers.get( edge.source );
                            Integer target = mapPopularUsers.get( edge.target );
                            bw.write( source + " " + target + " " + edge.time + " " + edge.weight + "\n" );
                        }
                    }
                        
                }
            }
        }
        bw.close();
    }
    
    public static void printEdges() throws Exception {
        
        double i = 1.0;
        BufferedWriter bw = FileUtil.openOutputFile( EDGES_USER.replace( ".csv", "-" + GRANULARITY + ".csv" ) );
        bw.write( "Id;Source;Target;Type;weight;timeset\n" );
        for( int time = 1; time < edgesListByTime.length; time++ ) {
            Map<String,Edge> edges = edgesListByTime[time];
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
            
            Map<String,Set<String>> artists = entry.getValue();
            for( Entry<String,Set<String>> entryArtist : artists.entrySet() ) {
                String artist = entryArtist.getKey();
                //System.out.println( user );
                
                Set<String> users = entryArtist.getValue();
                
                String [] usersArray = users.stream().toArray( String[]::new );

                for( int i = 0; i < usersArray.length-1; i++ ) {
                    String u1 = usersArray[i];
                    for( int j = i+1; j < usersArray.length; j++ ) {
                        String u2 = usersArray[j];
                        
                        // always lexicograph order for edge id
                        if( u2.compareTo( u1 ) == -1 ) {
                            String temp = u1;
                            u1 = u2;
                            u2 = temp;
                        }
                        
                        String edgeId = u1 + "-" + u2 + "-" + time;
                        Edge edge = edges.get( edgeId );
                        if( edge == null ) {
                            edge = new Edge();
                            edge.source = u1;
                            edge.target = u2;
                            edge.time = time;
                            edge.weight = 1;
                            edges.put( edgeId, edge );
                        } else {
                            edge.weight++;
                        }
                    }
                }
            }
            
            List<String> toRemove = new ArrayList<String>();
            for( Entry<String,Edge> entryEdge : edges.entrySet() ) {
                Edge edge = entryEdge.getValue();
                if( edge.weight < WEIGHT_THRESHOLD ) {
                    toRemove.add( entryEdge.getKey() );
                }
            }
            for( String rem : toRemove ) {
                edges.remove( rem );
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
                
                // get artists
                Map<String,Set<String>> artists = times.get( time );
                if( artists == null ) {
                    artists = new HashMap<String,Set<String>>();
                    times.put( time, artists );
                }
                
                // get users
                Set<String> users = artists.get( artist );
                if( users == null ) {
                    users = new HashSet<String>();
                    artists.put( artist, users );
                }
                
                users.add( userId );
                
                // statistics only
                Integer popularity;
                if( (popularity = uniqueArtists.get( artist )) == null ) {
                    popularity = 0;
                    uniqueArtists.put( artist, popularity );
                }
                uniqueArtists.put( artist, popularity+1 );
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

class User implements Comparable<User> {
    
    String user;
    Integer popularity;

    public User( String user, Integer popularity ) {
        this.user = user;
        this.popularity = popularity;
    }
    
    @Override
    public int compareTo( User o ) {
        if( popularity < o.popularity )
            return 1;
        if( popularity > o.popularity )
            return -1;                   
        return 0;
    }
    
}


