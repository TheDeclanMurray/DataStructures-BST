/**
 * @author Brian Sea
 * @classdesc An iterable sigularly linked list data structure
 * @class
 */

class LinkedList{

    /**
     * @classdesc Nodes in a linked list
     * @class
     */
    static Node = class {
        constructor(data){
            this.data = data;
            this.next = null;
        }
    }

    /**
     * Initializes an empty linked list
     * @constructor
     */
    constructor(){
        this.head = null;
        this._size = 0;
    }

    /**
     * Prohibits setting the size of the linked list
     */
    set size(newSize) {
        throw "Size is a read-only attribute and shouldn't be set externally."
    }

    /**
     * Gets the size of the linked list
     */
    get size() {
        return this._size;
    }

    /**
     * Provides an iterator for the linked list
     * @return an JS5 standard iterator
     */
    [Symbol.iterator](){
        let currNode = this.head;

        return {
            next: () => {
                if( currNode === null ){
                    return { value: undefined, done: true};
                }

                let result = {
                    value: currNode.data,
                    done: false
                }
                currNode = currNode.next;
                return result;
            }
        }
    }

    /**
     * Add to the end of the linked list 
     * @param data the data to add to the list
     */
    add(data){
        this.insert(data, this.size);
    }

    /**
     * Inserts data into the list after the index provided.
     * @param data the data to insert into the linked list
     * @param place the index to insert after. -1 indicates before head, > size indicates at the end of the list
     */
    insert( data, place = -1 ){
        let node = new LinkedList.Node(data);
        if( this.head === null ){
            this.head = node;
        }
        else {
            let currNode = this.head;
            // Stop one before addition
            while( currNode.next !== null && place > 0 ){
                currNode = currNode.next;
                place = place - 1;
            }

            node.next = currNode.next;
            currNode.next = node;
        }
        
        this._size = this._size + 1;
    }

    /**
     * Removes an element from the list at the index provided.
     * @param place index to remove; <= 0 indicates removal of first element; > size indicates removal of last element
     * @return the data that was removed
     */
    remove( place = -1 ){
        if( this.size === 0 ){
            return null;
        }

        let currNode = this.head;
        let prevNode = null;
        while(currNode !== null && place > 1 ){
            prevNode = currNode;
            currNode = currNode.next;
            place = place -1;
        }

        if( prevNode === null ){
            this.head = currNode.next;
        }
        else {
            prevNode.next = currNode.next;
        }


        this._size = this._size - 1;
		return currNode.data;
    }

    /**
     * Gets the data from a provided index (stating at index zero)
     * @param place the index to retreive data from
     * @return the data at index {place} or null if doesn't exist
     */
    get( place = 0 ){

        // place isn't a valid index
        if( place >= this.size || place < 0 ){ 
            let error = new Error(`Access Out Of Bounds: Attempt: ${place} Size: ${this.size}`);
            throw  error;
        }

        let current = this.head;
        for( let spot = 0; current.next !== null && spot < place && spot < this.size; spot++ ){
            current = current.next;
        }
        return current.data;
    }

    /**
     * Convert the Linked List into a String with format [E1, E2, E3, ...]
     */

     toString() {
         let current = this.head;

         let rtn = "[";
         while( current !== null ){
            rtn += current.data.toString()+', ';
            current = current.next;
         }
         rtn = rtn.substring(0, rtn.length-2);
         rtn += "]";

         return rtn;
     }

    /**
     * print the linked list to the console
     */
    print() {
        let current = this.head;
        let spot = 0;
        while( current !== null ){
            console.log( `${spot}: ${current.data}`);
            spot = spot + 1;
            current = current.next;
        }
    }
}


export {LinkedList}
