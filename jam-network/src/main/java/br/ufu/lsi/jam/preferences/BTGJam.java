package br.ufu.lsi.jam.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

import br.ufu.lsi.preference.BTG;

public class BTGJam extends BTG {
    
    public void buildBTGJam( List<User> userJams ) {
        
        ProjectController pc = Lookup.getDefault().lookup( ProjectController.class );
        pc.newProject();
        gm = Lookup.getDefault().lookup( GraphController.class ).getGraphModel();

        Map<String,Integer> topicNodes = new HashMap<String,Integer>();
        populateJamTopicNodes( topicNodes );
        
        for( User user : userJams ) {
            String topicName = user.genre;
            Integer count = topicNodes.get( topicName );
            topicNodes.put( topicName, ++count );
        }
        
        List<MyTopic> myTopics = new ArrayList<MyTopic>();
        for( Entry<String,Integer> entry : topicNodes.entrySet() ) {
            MyTopic myTopic = new MyTopic( entry.getKey(), entry.getValue() );
            myTopics.add( myTopic );
        }
        Collections.sort( myTopics );
        
        for( int i = 0; i<myTopics.size(); i++ ) {
            MyTopic t1 = myTopics.get(i);
            Node n1 = gm.getDirectedGraph().getNode( t1.topicName );
            for( int j = i+1; j<myTopics.size(); j++ ) {
                MyTopic t2 = myTopics.get( j );
                if( t1.count > t2.count ) {
                    Node n2 = gm.getDirectedGraph().getNode( t2.topicName );
                    Edge edge = gm.factory().newEdge( n1, n2, true );
                    gm.getDirectedGraph().addEdge( edge );
                }
                
            }
        }
        
    }
    
    public void populateJamTopicNodes( Map<String,Integer> topicNodes ) {
        
        topicNodes.put( "Blues", 0 );
        topicNodes.put( "Country", 0 );
        topicNodes.put( "Electronic", 0 );
        topicNodes.put( "Folk", 0 );
        topicNodes.put( "Jazz", 0 );
        topicNodes.put( "Latin", 0 );
        topicNodes.put( "Metal", 0 );
        topicNodes.put( "New Age", 0 );
        topicNodes.put( "Pop", 0 );
        topicNodes.put( "Punk", 0 );
        topicNodes.put( "Rap", 0 );
        topicNodes.put( "Reggae", 0 );
        topicNodes.put( "RnB", 0 );
        topicNodes.put( "Rock", 0 );
        topicNodes.put( "World", 0 );
        
        Node Blues = gm.factory().newNode( "Blues" );
        gm.getDirectedGraph().addNode( Blues );
        
        Node Country = gm.factory().newNode( "Country" );
        gm.getDirectedGraph().addNode( Country );
        
        Node Electronic = gm.factory().newNode( "Electronic" );
        gm.getDirectedGraph().addNode( Electronic );
        
        Node Folk = gm.factory().newNode( "Folk" );
        gm.getDirectedGraph().addNode( Folk );
        
        Node Jazz = gm.factory().newNode( "Jazz" );
        gm.getDirectedGraph().addNode( Jazz );
        
        Node Latin = gm.factory().newNode( "Latin" );
        gm.getDirectedGraph().addNode( Latin );
        
        Node Metal = gm.factory().newNode( "Metal" );
        gm.getDirectedGraph().addNode( Metal );
        
        Node NewAge = gm.factory().newNode( "New Age" );
        gm.getDirectedGraph().addNode( NewAge );
        
        Node Pop = gm.factory().newNode( "Pop" );
        gm.getDirectedGraph().addNode( Pop );
        
        Node Punk = gm.factory().newNode( "Punk" );
        gm.getDirectedGraph().addNode( Punk );
        
        Node Rap = gm.factory().newNode( "Rap" );
        gm.getDirectedGraph().addNode( Rap );
        
        Node Reggae = gm.factory().newNode( "Reggae" );
        gm.getDirectedGraph().addNode( Reggae );
        
        Node RnB = gm.factory().newNode( "RnB" );
        gm.getDirectedGraph().addNode( RnB );
        
        Node Rock = gm.factory().newNode( "Rock" );
        gm.getDirectedGraph().addNode( Rock );
        
        Node World = gm.factory().newNode( "World" );
        gm.getDirectedGraph().addNode( World );
    }

}

class MyTopic implements Comparable< MyTopic >{
    
    String topicName;
    Integer count;
    
    public MyTopic( String topicName, Integer count ) {
        this.topicName = topicName;
        this.count = count;
    }
    
    public int compareTo( MyTopic o ) {
        if( o.count > this.count )
            return 1;
        if( o.count < this.count )
            return -1;
        return 0;
    }
}