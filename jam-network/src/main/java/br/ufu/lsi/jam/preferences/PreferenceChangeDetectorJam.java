package br.ufu.lsi.jam.preferences;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;

import br.ufu.lsi.event.utils.FileUtil;
import br.ufu.lsi.jam.utils.DateUtils;
import br.ufu.lsi.jam.utils.Granularity;
import br.ufu.lsi.preference.BTG;

public class PreferenceChangeDetectorJam {
    
    private static final String INIT = "2011-08-26";
    
    private static final String END = "2015-09-26";
    
    private static final Granularity GRANULARITY = Granularity.YEAR;
    
    private static String USERS_PREF_CHANGES_FILE = "/Users/fabiola/Desktop/MLJournal/this-is-my-jam/users-CHANGES.csv";
    
    
    public static void main( String ... args ) throws Exception {
        
        System.out.println( "Loading users..." );
        loadUsers();
        
        System.out.println( "Detecting users' preference changes..." );
        engine();
    }
    
    // load users
    public static void loadUsers() throws Exception {
        BuildUserPreferences.loadGenres();
        BuildUserPreferences.loadJams();
        BuildUserPreferences.orderUsers();
    }
    
    // main engine to process each user
    public static void engine() throws Exception {
        
        StringBuilder sb = new StringBuilder();
        
        // list of users' jams grouped by users, timestamp 
        List<User> users = BuildUserPreferences.users;
        
        User previousUser = new User();
        List<User> currentUserJams = new ArrayList<User>();
        
        long init = System.currentTimeMillis();
        
        for( User user : users ) {
            
            // end of user registers
            if( !user.userId.equals( previousUser.userId ) ) {
                
                // process
                if( previousUser.userId != null ) {
                    sb.append( previousUser.userId );
                    //bw.write( previousUser.userId );
                    processUser( currentUserJams, sb );
                    sb.append( "\n" );
                    //bw.write( "\n" );
                }
                currentUserJams.clear();
                

            } else {
                currentUserJams.add( user );
            }
            
            previousUser.genre = user.genre;
            previousUser.jamId = user.jamId;
            previousUser.timestamp = user.timestamp;
            previousUser.userId = user.userId;
            
        }
        
        long end = System.currentTimeMillis() - init;
        System.out.println( end );
        
        BufferedWriter bw = FileUtil.openOutputFile( USERS_PREF_CHANGES_FILE.replace( ".csv", "-" + GRANULARITY.toString() + ".csv" ) );
        bw.write( sb.toString() );
        bw.close();
    }
    
    
    // process preference changes based on granularity
    public static void processUser( List<User> userJams, StringBuilder sb ) throws Exception {
        
        BTGJam previousBTG = null;
        BTGJam currentBTG = null;
        
        for( String time = INIT; DateUtils.checkBefore( time, END ); time = DateUtils.nextPeriod( time, GRANULARITY ) ) {
            
            // load jams of the period
            List<User> currentUserJams = new ArrayList<User>();
            
            for( User u : userJams ) {
                String inferiorLimit = time;
                String superiorLimit = DateUtils.nextPeriod( time, GRANULARITY );
                if( DateUtils.checkInside( u.timestamp, inferiorLimit, superiorLimit ) ) {
                    currentUserJams.add( u );
                }
            }
            
            // change period
            currentBTG = new BTGJam();
            currentBTG.buildBTGJam( currentUserJams );
            currentBTG.execute();
                    
            //compare with previousBTG
            BTG aggregateBTG = BTG.aggregateBTG( previousBTG, currentBTG );
            aggregateBTG.execute();
            
            if( aggregateBTG.hasCycle() ) {
                //System.out.print( ";1" );
                sb.append( ";1" );
                //bw.write( ";1" );
                 //changes.add( 1 );
             } else {
                 //System.out.print( ";0" );
                 sb.append( ";0" );
                 //bw.write( ";0" );
                 // changes.add( 0 );
             }
             
             // slides
             previousBTG = currentBTG;
        }
    }
    
}











