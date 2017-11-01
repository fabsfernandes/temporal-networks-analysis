package br.ufu.lsi.jam.preferences;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.ufu.lsi.jam.utils.FileUtil;


public class BuildUserPreferences {
    
    private static final String JAMS = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/jams.tsv";
    
    private static final String JAM_TO_MSD_TO_GENRE = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/jam_to_msd_to_genre.tsv";

    private static final String USERS_ORDER = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/users_ordered.csv";
    
    private static Map<String,User> jams = new HashMap<String,User>();
    
    private static Map<String,String> jamGenre = new HashMap<String,String>();
    
    public static List<User> users = new ArrayList<User>();
    
    public static void main( String ... args ) throws Exception {
        
        System.out.println( "Loading genres..." );
        loadGenres();
        
        System.out.println( "Loading jams..." );
        loadJams();
        
        System.out.println( "Ordering users..." );
        orderUsers();
        
        System.out.println( "Writing users..." );
        writeUsers();
    }
    
    public static void writeUsers() throws Exception {
        
        BufferedWriter bw = FileUtil.openOutputFile( USERS_ORDER );
        bw.write( "userId;timestamp;jamId;genre\n" );
        
        for( User u : users ) {
            StringBuilder sb = new StringBuilder();
            sb.append( u.userId + ";" + u.timestamp + ";" + u.jamId + ";" + u.genre );
            bw.write( sb.toString() + "\n" );
        }
        bw.close();
    }
    
    public static void orderUsers() throws Exception {
        
        for( Entry<String,User> jamEntry : jams.entrySet() ) {
            users.add( jamEntry.getValue() );
        }
        
        Collections.sort( users );
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



