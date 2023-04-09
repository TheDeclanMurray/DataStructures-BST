package org.headroyce.declanm2022;

import java.util.ArrayList;
import java.util.List;

/**
 * A Binary Search Tree (BST)
 *O(h) means height, h, is the explanatory variable
 * Data inside the BST must have a compareTo method
 */

             //O(h) means height, h, is the explanatory variable
             //O(n) means total nodes in the tree
public class BST<T extends Comparable<T>> {
    private Node<T> root;
    private Node<T> curr; //used in multiple methods, saves space

    /**
     * Constructs an empty BST
     */
    public BST(){
        root = null;
        curr = null;
    }

    // O(2^h)  O(n)
    public int height(){
        if(root != null){
            return depth(root, 0)+1;
        }
        return 0;
    }
    // indeterminable O(2^h) O(2^n)
    private int depth(Node<T> pres, int depth){
        int maxDepth = depth;
        if(pres.left != null){
            maxDepth = depth(pres.left, depth+1);
        }
        if(pres.right != null){
            int R = depth(pres.right, depth+1);
            if(R > maxDepth){
                maxDepth = R;
            }
        }
        return maxDepth;
    }
    // O(h)   O(log n)
    public T largestBST(){
        curr = root;
        while(curr.right != null){
            curr = curr.right;
        }
        return curr.data;
    }

    //O(2^h)    O(n)
    public T largest(){
        LList<Node<T>> Q = new LList<>();
        T rtn = null;
        if(root != null){
            Q.add(root);
            rtn = root.data;
            while(Q.size() > 0){
                curr = Q.remove(0);
                //if k tree then go through all children for here
                if(curr.left != null){
                    Q.add(curr.left);
                }
                if(curr.right != null) {
                    Q.add(curr.right);
                }
                //to here
                if(rtn.compareTo(curr.data)<0){
                    rtn = curr.data;
                }
            }
        }
        return rtn;
    }

    /**
     * Add data the the Binary Search tree via the ordering done
     * by the comparator
     * @param data the data to add
     */
    // O(h)    O(log n)
    public void add( T data ){
        Node<T> Baby = new Node<T>(data);
        if(root == null){
            root = Baby;
            return;
        }
        curr = root;
        while(true){
            if(curr.data.compareTo(Baby.data)>=0){
                if(curr.left == null){
                    curr.left = Baby;
                    return;
                }
                curr = curr.left;
            }else{
                if(curr.right == null){
                    curr.right = Baby;
                    return;
                }
                curr = curr.right;
            }
        }
    }

    /**
     * Removes a Node from the BST
     * @param curr the node to remove
     * @param parent the parent of curr
     * @return the exact data removed
     */
    //O(h)   O(log n)
    private T removeNode( Node<T> curr, Node<T> parent ){
        T rtnData = null;
        if(curr.left != null && curr.right != null){
            Node<T> RMLparent = curr;
            Node<T> RML = curr.left;
            while(RML.right != null){
                RMLparent = RML;
                RML = RML.right;
            }
            rtnData = curr.data;
            curr.data = RML.data;
            removeNode(RML,RMLparent);
            return rtnData;
        }else{
            Node<T> here = curr.left;
            if(here == null){
                here = curr.right;
            }
            if(parent != null){
                 if(curr == parent.left){
                     parent.left = here;
                 }else{
                     parent.right = here;
                 }
            }else{
                root = here;
            }
        }
        return curr.data;
    }

    /**
     * Removes the first element equal to data when using the comparator function
     * @param data the element to compare with
     * @return the exact data removed from the BST
     */
    //O(h)  O(log n)
    public T remove( T data ){
        T rtnData = null;
        if(root == null){
            return null;
        }
        Node<T> parent = null;
        curr = this.root;
        while(curr != null){
            if(curr.data.compareTo(data)==0){
                rtnData = removeNode(curr, parent);
                return rtnData;
            }else{
                parent = curr;
            }
            if(curr.data.compareTo(data)<0){
                curr = curr.right;
            }else{
                curr = curr.left;
            }
        }
        return null;
    }

    /**
     * Completes an inOrder traversal of the BST
     * @return Starting from the root, a list of the resulting inOrder traversal
     */
    // O(2^h)    O(n log n)
    public List<T> inOrder(){
        List<T> inOrder = new ArrayList<>();
        if(root != null){
            LCR(root, inOrder);
        }
        return inOrder;
    }
    // indeterminable O(2^n)
    private void LCR(Node<T> pres, List<T> Order){
        if(pres.left != null){
            LCR(pres.left, Order);
        }
        Order.add(pres.data); //O(n)
        if(pres.right != null){
            LCR(pres.right, Order);
        }
    }

    // O(2^h)   O(n log n)
    public List<T> preOrder(){
        List<T> preOrder = new ArrayList<>();
        if(root != null){
            CLR(root, preOrder);
        }
        return preOrder;
    }
    // indeterminable O(2^n)
    private void CLR(Node<T> pres, List<T> Order){
        Order.add(pres.data); //O(n)
        if(pres.left != null){
            CLR(pres.left, Order);
        }
        if(pres.right != null){
            CLR(pres.right, Order);
        }
    }

    // O(2^h)    O(n log n)
    public List<T> postOrder(){
        List<T> postOrder = new ArrayList<>();
        if(root != null){
            LRC(root, postOrder);
        }
        return postOrder;
    }
    // indeterminable O(2^n)
    private void LRC(Node<T> pres, List<T> Order){
        if(pres.left != null){
            LRC(pres.left, Order);
        }
        if(pres.right != null){
            LRC(pres.right, Order);
        }
        Order.add(pres.data); //O(n)
    }

    // O(2^h)   O(n log n)
    public List<T> levelOrder(){
        List<T> Order = new ArrayList<>();
        LList<Node<T>> queue = new LList<Node<T>>();
        if(root != null){
            queue.add(root); //O(1)
            while(queue.size() > 0){
                curr = queue.remove(0);
                Order.add(curr.data); //O(n)
                if(curr.left != null){
                    queue.add(curr.left);
                }
                if(curr.right != null){
                    queue.add(curr.right);
                }
            }
        }
        return Order;
    }

    /**
     * Completes an inOrder traversal of the BST
     * @param curr the node start at (null indicates stoppage)
     * @param list the list to add to
     * @return Starting at curr, a list of the resulting inOrder traversal
     */
    // O(h)   O(n log n)
    private List<T> inOrder(Node<T> curr, List<T> list ){
        if(curr != null){
            LCR(root, list);
        }
        return list;
    }

    /**
     * Each element of the BST
     * @param <E> the type of data stored
     */
    private class Node<E extends Comparable<E>> {
        public E data;
        public Node<E> left;
        public Node<E> right;

        public Node(E data) {
            this.data = data;
            left = null;
            right = null;
        }
    }
}