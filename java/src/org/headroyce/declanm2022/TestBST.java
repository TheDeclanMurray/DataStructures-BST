package org.headroyce.declanm2022;

import java.util.List;

public class TestBST {

    public static void main(String[] args) {
        int[] data = { 18, 30, 14, 12, 90};

        BST<Integer> test = new BST<>();
        for( Integer i : data ){
            test.add(i);
        }

        test.remove( 18 );

        List<Integer> order = test.inOrder();
        for( Integer i : order ){
            System.err.println( i );
        }
    }
}
