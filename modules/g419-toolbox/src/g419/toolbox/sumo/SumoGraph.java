package g419.toolbox.sumo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by michal on 6/2/15.
 */
public class SumoGraph {

    private HashMap<String, Node> nodes;

    public SumoGraph(){
        nodes = new HashMap<String, Node>();
    }

    public Node getNode(String label) throws Exception {
        if(containsClass(label)){
            return nodes.get(label);
        }
        else{
            throw new Exception("SumoGraph not contain class: " + label);
        }
    }

    public boolean containsClass(String label){
        return nodes.containsKey(label);
    }

    public boolean isSubclassOf(Node subNode, Node upperNode){
        if(upperNode.isUpperClass(subNode)){
            return true;
        }
        else{
            for(Node node: upperNode.subClasses){
                if(isSubclassOf(subNode, node)){
                    return true;
                }
            }
        }
        return false;

    }

    public void addConnection(String subClass, String upperClass){
        Node upperNode = getOrCreateNode(upperClass);
        Node subNode = getOrCreateNode(subClass);
        upperNode.addSubClass(subNode);
    }

    private Node getOrCreateNode(String label){
        Node node;
        if(nodes.containsKey(label)){
            node = nodes.get(label);
        }
        else{
            node = new Node(label);
            nodes.put(label, node);
        }
        return node;
    }

    private class Node{
        String label;
        HashSet<Node> subClasses;

        public Node (String label){
            this.label = label;
            subClasses = new HashSet<>();
        }

        public void addSubClass(Node subClass){
            subClasses.add(subClass);
        }

        public boolean isUpperClass(Node subNode){
            return subClasses.contains(subNode);
        }

    }
}
