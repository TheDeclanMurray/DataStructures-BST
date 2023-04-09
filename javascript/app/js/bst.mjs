/**
 * A Binary Search Tree 
 * 
 * Uses a comparator function to determine data equality
 */
class BST{

    static Node = class {
        constructor(data = null){
            this.data = data;
            this.left = null;
            this.right = null;
        }
    }

    /**
     * Build a BST using a comparing function if the form:
     *   comparator(A,B)
     * @param comparator a function which returns -1 if A < B, 0 if A === B, or 1 if A > B
     */
    constructor(comparator){
        this._root = null;
        this._comparator = comparator;
    }

    /**
     * Add data the the Binary Search tree via the ordering done
     * by the comparator
     * @param data the data to add
     */
    add(data){
        return false;
    }

    /**
     * Removes the first element equal to data when using the comparator function
     * @param data the element to compare with
     * @return the exact data removed from the BST
     */
    remove(data){
        return null;
    }

    /**
     * Completes an inOrder traversal of the BST
     * @param currNode the node to start at (null = root)
     * @param startOver true if we should start at the root, false otherwise
     * 
     * @return Starting at currNode, a list of the resulting inOrder traversal 
     */
    inOrder(currNode = null, startOver = true){
        return null;
    }
}

export {BST}
