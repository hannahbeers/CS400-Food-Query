package application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Implementation of a B+ tree to allow efficient access to
 * many different indexes of a large data set. 
 * BPTree objects are created for each type of index
 * needed by the program.  BPTrees provide an efficient
 * range search as compared to other types of data structures
 * due to the ability to perform log_m N lookups and
 * linear in-order traversals of the data items.
 * 
 * @author sapan (sapan@cs.wisc.edu)
 *
 * @param <K> key - expect a string that is the type of id for each item
 * @param <V> value - expect a user-defined type that stores all data for a food item
 */
public class BPTree<K extends Comparable<K>, V> implements BPTreeADT<K, V> {

    // Root of the tree
    private Node root;
    
    // Branching factor is the number of children nodes 
    // for internal nodes of the tree
    private int branchingFactor;
    
    
    /**
     * Public constructor
     * 
     * @param branchingFactor 
     */
    public BPTree(int branchingFactor) {
        if (branchingFactor <= 2) {
            throw new IllegalArgumentException(
               "Illegal branching factor: " + branchingFactor);
        }
        this.branchingFactor = branchingFactor;
        root = new LeafNode();
    }
    
    
    /*
     * (non-Javadoc)
     * @see BPTreeADT#insert(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void insert(K key, V value) {
        
        /* Initial adds to a LeafNode */
        if(root instanceof BPTree.LeafNode) {
            root.insert(key, value);
            if(root.isOverflow()) {
                LeafNode sibling = (BPTree<K, V>.LeafNode) root.split();
                InternalNode parent = new InternalNode();
                parent.keys.add(sibling.getFirstLeafKey());
                parent.children.add(sibling.previous);
                parent.children.add(sibling);
                root = parent;
            }
            return;
        }
        
        /* Add the key, value pair */
        root = insert(root, key, value);
        
        /* Fix any overflow that occurs on the root */
        if(root.isOverflow()) {
            InternalNode newRoot = new InternalNode();
            int rootSize = root.keys.size();
            
            //Find the key to pull up
            newRoot.keys.add(root.keys.get((int) Math.floor(rootSize / 2)));
            
            //Set new children
            newRoot.children.add(root);
            newRoot.children.add(root.split());
            
            //Set new root
            root = newRoot;

        }
    }
    
    
    /*
     * (non-Javadoc)
     * @see BPTreeADT#rangeSearch(java.lang.Object, java.lang.String)
     */
    @Override
    public List<V> rangeSearch(K key, String comparator) {
        if (!comparator.contentEquals(">=") && 
            !comparator.contentEquals("==") && 
            !comparator.contentEquals("<=") )
            return new ArrayList<V>();

        ArrayList<V> vals = new ArrayList<V>();
        vals.addAll(root.rangeSearch(key, comparator));
        return vals;
    }
    
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        Queue<List<Node>> queue = new LinkedList<List<Node>>();
        queue.add(Arrays.asList(root));
        StringBuilder sb = new StringBuilder();
        while (!queue.isEmpty()) {
            Queue<List<Node>> nextQueue = new LinkedList<List<Node>>();
            while (!queue.isEmpty()) {
                List<Node> nodes = queue.remove();
                sb.append('{');
                Iterator<Node> it = nodes.iterator();
                while (it.hasNext()) {
                    Node node = it.next();
                    sb.append(node.toString());
                    if (it.hasNext())
                        sb.append(", ");
                    if (node instanceof BPTree.InternalNode)
                        nextQueue.add(((InternalNode) node).children);
                }
                sb.append('}');
                if (!queue.isEmpty())
                    sb.append(", ");
                else {
                    sb.append('\n');
                }
            }
            queue = nextQueue;
        }
        return sb.toString();
    }
    
    
    @SuppressWarnings("unchecked")
    private Node insert(Node n, K key, V value) {
        if(n instanceof BPTree.InternalNode) {
            //Cast to an InternalNode
            InternalNode node = (BPTree<K, V>.InternalNode) n;
            
            //Find the child of interest
            int childIndex = node.childIndex(key);
            Node child = node.children.get(childIndex);
            
            child = insert(child, key, value);
            
            //Fix ALL overflow from here
            if(child.isOverflow()) {
                int childSize = child.keys.size();
                
                //pull up the propper key
                node.keys.add(childIndex, child.keys.get((int) Math.floor(childSize / 2)));
                
                //set new child pointer
                node.children.add(childIndex + 1, child.split());
            }
        }
        
        if(n instanceof BPTree.LeafNode) {
            //just insert, may return an overflown node.
            n.insert(key, value);
        }
        
        return n;
    }
    
    
    /**
     * This abstract class represents any type of node in the tree
     * This class is a super class of the LeafNode and InternalNode types.
     * 
     * @author sapan
     */
    private abstract class Node {
        
        // List of keys
        List<K> keys;
        
        /**
         * Package constructor
         */
        Node() {
            keys = new ArrayList<K>();
        }
        
        /**
         * Inserts key and value in the appropriate leaf node 
         * and balances the tree if required by splitting
         *  
         * @param key
         * @param value
         */
        abstract void insert(K key, V value);

        /**
         * Gets the first leaf key of the tree
         * 
         * @return key
         */
        abstract K getFirstLeafKey();
        
        /**
         * Gets the new sibling created after splitting the node
         * 
         * @return Node
         */
        abstract Node split();
        
        /*
         * (non-Javadoc)
         * @see BPTree#rangeSearch(java.lang.Object, java.lang.String)
         */
        abstract List<V> rangeSearch(K key, String comparator);

        /**
         * 
         * @return boolean
         */
        abstract boolean isOverflow();
        
        public String toString() {
            return keys.toString();
        }
    
    } // End of abstract class Node
    
    /**
     * This class represents an internal node of the tree.
     * This class is a concrete sub class of the abstract Node class
     * and provides implementation of the operations
     * required for internal (non-leaf) nodes.
     * 
     * @author sapan
     */
    private class InternalNode extends Node {

        // List of children nodes
        List<Node> children;
        
        /**
         * Package constructor
         */
        InternalNode() {
            super();
            children = new ArrayList<Node>();
        }
        
        /**
         * (non-Javadoc)
         * @see BPTree.Node#getFirstLeafKey()
         */
        K getFirstLeafKey() {
            return children.get(0).getFirstLeafKey();
        }
        
        /**
         * (non-Javadoc)
         * @see BPTree.Node#isOverflow()
         */
        boolean isOverflow() {
            if(this.keys.size() >= branchingFactor) {
                return true;
            }
            return false;
        }
        
        /**
         * (non-Javadoc)
         * @see BPTree.Node#insert(java.lang.Comparable, java.lang.Object)
         */
        void insert(K key, V value) {
            
            /* INSERT NORMAL BPTREE */
            if(keys.isEmpty()) {
                /* Naive add */
                keys.add(key);
            }
            else {
                /* Insert in a sorted order so that this never runs into trouble */
                int index = Collections.binarySearch(keys, key); 
                
                /* index not found; must convert index to usable value */
                if(index < 0) { 
                    index++;
                    index *= -1;
                }
                
                /* Insert to LeafNode */
                keys.add(index, key);
            }
            
        }
        
        /**
         * (non-Javadoc)
         * @see BPTree.Node#split()
         */
        Node split() {
            
            InternalNode Rchild = new InternalNode(); // the node on the right side of the 
            InternalNode Lchild = new InternalNode(); // the node on the left side of the 
                        
            int keys_to = keys.size()/2; //Lchild takes from keys(0) -> 'keys_to'
            int keys_from = keys.size()/2+1; //Rchild takes from 'keys_from' -> keys.size() 
            
            int children_to = children.size()/2;
            int children_from = children.size()/2;
            
            /* Populate new nodes */
            Lchild.children.addAll(children.subList(0, children_to));
            Rchild.children.addAll(children.subList(children_from, children.size()));
            
            Lchild.keys.addAll(keys.subList(0, keys_to));
            Rchild.keys.addAll(keys.subList(keys_from, keys.size()));
            
            // set current node to be the Lchild
            this.keys = Lchild.keys;
            this.children = Lchild.children;

            return Rchild;
        }
        
        /**
         * (non-Javadoc)
         * @see BPTree.Node#rangeSearch(java.lang.Comparable, java.lang.String)
         */
        List<V> rangeSearch(K key, String comparator) {
            ArrayList<V> vals = new ArrayList<V>();
            switch(comparator) {
            case ">=":
                for(int i=0;i<keys.size();i++) {
                    /* Must go right */
                    if(i == keys.size()-1 && keys.get(i).compareTo(key) < 0) {
                        vals.addAll(children.get(i + 1).rangeSearch(key, comparator));
                        break;
                    }
                    /* Found the right key */
                    if(keys.get(i).compareTo(key) >= 0) {
                        vals.addAll(children.get(i).rangeSearch(key, comparator));
                        break;
                    }
                }
                break;
                
            case "==":
                for(int i=0;i<keys.size();i++) {
                    /* Search right */
                    if(i == keys.size()-1 && keys.get(i).compareTo(key) < 0) {
                        vals.addAll(children.get(i + 1).rangeSearch(key, comparator));
                        break;
                    }
                    /* Search the middle */
                    if(keys.get(i).compareTo(key) == 0) {
                        vals.addAll(children.get(i).rangeSearch(key, comparator));
                        break;
                    }
                    /* Search left */
                    if(keys.get(i).compareTo(key) > 0) {
                        vals.addAll(children.get(i).rangeSearch(key, comparator));
                        break;
                    }
                }
                break;
                
            case "<=":
                /* Search left */
                vals.addAll(children.get(0).rangeSearch(key, comparator));
                break;
            }
            return vals;
        }
                
        /**
         * helper method to get the index of a child to insert
         * @param key
         * @return the propper index
         */
        private int childIndex(K key) {
            for(int i=0;i<keys.size();i++) {
                if(key.compareTo(keys.get(i)) <= 0) {
                    return i;
                }
            }
            return keys.size();
        }
        
    } // End of class InternalNode
    
    
    /**
     * This class represents a leaf node of the tree.
     * This class is a concrete sub class of the abstract Node class
     * and provides implementation of the operations that
     * required for leaf nodes.
     * 
     * @author sapan
     */
    private class LeafNode extends Node {
        
        // List of values
        List<V> values;
        
        // Reference to the next leaf node
        LeafNode next;
        
        // Reference to the previous leaf node
        LeafNode previous;
        
        /**
         * Package constructor
         */
        LeafNode() {
            super();
            values = new ArrayList<V>();
        }
        
        
        /**
         * (non-Javadoc)
         * @see BPTree.Node#getFirstLeafKey()
         */
        K getFirstLeafKey() {
            return keys.get(0);
        }
        
        /**
         * (non-Javadoc)
         * @see BPTree.Node#isOverflow()
         */
        boolean isOverflow() {
            if(keys.size() >= branchingFactor) {
                return true;
            }
            return false;
        }
        
        /**
         * (non-Javadoc)
         * @see BPTree.Node#insert(Comparable, Object)
         */
        void insert(K key, V value) {
            /* INSERT NORMAL BPTREE */
            if(keys.isEmpty()) {
                /* Naive add */
                keys.add(key);
                values.add(value);
            }
            else {
                /* Insert in a sorted order so that this never runs into trouble */
                int index = Collections.binarySearch(keys, key); 
                
                /* index not found; must convert index to usable value */
                if(index < 0) { 
                    index++;
                    index *= -1;
                }
                
                /* Insert to LeafNode */
                keys.add(index, key);
                values.add(index, value);
            }
                        
        }
        
        /** TODO this works so far.
         * (non-Javadoc)
         * @see BPTree.Node#split()
         */
        Node split() {
            /* Transfer right half of overflown node to a new leaf node, 'sibling'.
             * If the BF is an odd number, the larger half of the overflown node will go to 'sibling' */
            LeafNode sibling = new LeafNode(); 
            int from = keys.size()/2;
            int to = keys.size();
            
            /* Populate new sibling LeafNode */
            sibling.keys.addAll(keys.subList(from, to)); //subList follows format (from (inclusive), to (exclusive))
            sibling.values.addAll(values.subList(from, to));
            
            /* remove excess data from original node */
            keys.subList(from, to).clear();
            values.subList(from, to).clear();
            
            /* update pointers */
            sibling.next = next; //sibling is the rightmost of the two nodes
            sibling.previous = this; //overflown node is now the leftmost of the two nodes
            this.next = sibling;
            //this.previous remains unchanged
            
            return sibling; //use LeafNode.getFirstLeftKey to find the new InternalNode that must be created.
        }
        
        /**
         * (non-Javadoc)
         * @see BPTree.Node#rangeSearch(Comparable, String)
         */
        List<V> rangeSearch(K key, String comparator) {
            ArrayList<V> vals = new ArrayList<V>();
            
            /* BASE CASE */
            switch(comparator) {
            case ">=":
                /* Base case */
                for(int i=0;i<keys.size();i++) {
                    if(keys.get(i).compareTo(key) >= 0) {
                        vals.add(values.get(i));
                    }
                }
                /* Recursive case */
                if(next != null && next.keys.size() > 0) {
                    if(next.keys.get(0).compareTo(key) >= 0) {
                        vals.addAll(next.rangeSearch(key, comparator));
                    }
                }
                break;
                
            case "==":
                /* Base case */
                for(int i=0;i<keys.size();i++) {
                    if(keys.get(i).compareTo(key) == 0) {
                        vals.add(values.get(i));
                    }
                }
                /* Recursive case */
                if(next != null && next.keys.size() > 0) {
                    if(next.keys.get(0).compareTo(key) == 0) {
                        vals.addAll(next.rangeSearch(key, comparator));
                    }
                }
                break;
                
            case "<=":
                /* Base case */
                for(int i=0;i<keys.size();i++) {
                    if(keys.get(i).compareTo(key) <= 0) {
                        vals.add(values.get(i));
                    }
                }
                /* Recursive case */
                if(next != null && next.keys.size() > 0) {
                    if(next.keys.get(0).compareTo(key) <= 0) {
                        vals.addAll(next.rangeSearch(key, comparator));
                    }
                }
                break;
            }
            
            return vals;
        }
        
    } // End of class LeafNode
    
    
    /**
     * Contains a basic test scenario for a BPTree instance.
     * It shows a simple example of the use of this class
     * and its related types.
     * 
     * @param args
     */
    public static void main(String[] args) {
                
        // create empty BPTree with branching factor of 3
        BPTree<Double, Double> bpTree = new BPTree<>(3);

        // create a pseudo random number generator
        Random rnd1 = new Random();

        // some value to add to the BPTree
        Double[] dd = {0.0d, 0.5d, 0.2d, 0.8d};

        // build an ArrayList of those value and add to BPTree also
        // allows for comparing the contents of the ArrayList 
        // against the contents and functionality of the BPTree
        // does not ensure BPTree is implemented correctly
        // just that it functions as a data structure with
        // insert, rangeSearch, and toString() working.
        List<Double> list = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            Double j = dd[rnd1.nextInt(4)];
            list.add(j);
            bpTree.insert(j, j);
            System.out.println("\n\nTree structure:\n" + bpTree.toString());
        }
        List<Double> filteredValues = bpTree.rangeSearch(0.2d, ">=");
        System.out.println("Filtered values: " + filteredValues.toString());
    }

} // End of class BPTree
