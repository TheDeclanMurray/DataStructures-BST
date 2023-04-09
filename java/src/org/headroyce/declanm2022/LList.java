package org.headroyce.declanm2022;

import java.util.Iterator;

/**
 * @author Brian Sea
 * An iterable sigularly linked list data structure
 * @param <T> the type of data stored in the list
 */
public class LList<T> implements Iterable<T>{
    private Node<T> head;
    private Node<T> tail; //I added this for the time complexity of levelOrder()
    private int size;

    /**
     * Initializes an empty list
     */
    public LList(){
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Gets the size of the linked list.  Size is a read-only attribute.
     * @return the number of elements in the list
     */
    public int size(){
        return this.size;
    }

    /**
     * Add to the end of the linked list
     * @param data the data to add to the list
     */
    public void add( T data ){
        this.insert(data, size);
    }

    /**
     * Inserts data into the list after the index provided.
     * @param data the data to insert into the linked list
     * @param place the index to insert after. -1 indicates before head, > size indicates at the end of the list
     */
    public void insert( T data, int place ){
        Node<T> node = new Node<>(data);
        if(place >= size){
            if(tail != null){
                tail.next = node;
                tail = node;
            }
        }
        if( this.head == null ){
            this.head = node;
        }
        else {
            Node<T> currNode = this.head;
            // Stop one before addition
            while( currNode.next != null && place > 0 ){
                currNode = currNode.next;
                place = place - 1;
            }

            node.next = currNode.next;
            currNode.next = node;
        }

        this.size = this.size + 1;
    }

    /**
     * Removes an element from the list at the index provided.
     * @param place index to remove; <= 0 indicates removal of first element; > size indicates removal of last element
     * @return the data that was removed
     */
    public T remove( int place ){
        if( this.size == 0 ){
            return null;
        }

        Node<T> currNode = this.head;
        Node<T> prevNode = null;
        while(currNode != null && place > 1 ){
            prevNode = currNode;
            currNode = currNode.next;
            place = place -1;
        }

        if( prevNode == null ){
            this.head = currNode.next;
        }
        else {
            prevNode.next = currNode.next;
        }

        this.size = this.size - 1;
        return currNode.data;

    }

    /**
     * Gets the data from a provided index (stating at index zero)
     * @param place the index to retreive data from
     * @return the data at index place
     * @throws ArrayIndexOutOfBoundsException if place is outside the list
     */
    public T get( int place ){

        // place isn't a valid index
        if( place >= this.size() || place < 0 ){
            String errorMsg = "Access: " + place + " List Size: "+ this.size();
            throw new ArrayIndexOutOfBoundsException(errorMsg);
        }

        Node<T> current = this.head;
        for( int spot = 0; current.next != null && spot < place && spot < this.size; spot++ ){
            current = current.next;
        }
        return current.data;
    }

    /**
     * Convert the LList into a String
     * @return a String in format [E0, E1, E2, ...]
     */
    public String toString(){
        StringBuilder rtn = new StringBuilder("[");

        Node<T> currNode = this.head;
        while( currNode != null ){
            rtn.append( currNode.data.toString() );
            rtn.append(", ");
            currNode = currNode.next;
        }
        rtn = rtn.delete(rtn.length()-2, rtn.length());
        rtn.append("]");

        return rtn.toString();
    }

    /**
     * Provides an iterator for the list
     * @return a new iterator starting at the first element of the list
     */
    @Override
    public Iterator<T> iterator() {
        return new LListIterator<T>();
    }

    /**
     * Prints the linked list to the console
     */
    public void print(){
        Node<T> current = this.head;
        int spot = 0;
        while( current != null ){
            System.out.println(spot+": " + current.data.toString());
            spot = spot + 1;
            current = current.next;
        }
    }

    /**
     * Nodes that specific to the linked list
     * @param <E> the type of the Node. It must by T or extend it.
     */
    private class Node<E extends T>{
        public E data;
        public Node<E> next;

        public Node( E data ){
            this.data = data;
        }
    }

    /**
     * The iterator that is used for our list.
     */
    private class LListIterator<E extends T> implements Iterator<E>{

        private Node<E> currNode;

		@SuppressWarnings("unchecked")
        public LListIterator(){
			// This cast will always work
            currNode = (Node<E>)head;
        }

        @Override
        public boolean hasNext() {
            return currNode != null;
        }

        @Override
        public E next() {
            E data = currNode.data;
            currNode = currNode.next;
            return data;
        }
    }
}
