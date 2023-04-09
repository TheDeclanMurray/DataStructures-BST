/**
 * @author Brian Sea
 * @classdesc Represents the Tool parent. All tools should extends this class.
 * @class
 */
class Tool{

    /** 
     *  Static elements must be implemented by subclasses 
     */
    
    // Name of the tool. Should be all lowercase
    static toolName = 'generic tool';
    /**
     * Creates a new GUI element to display tool selection
     */
    static renderTool() {
        throw "Tool:renderTool:Method not Implemented";
    }


    constructor(id){
        this.id = id;
        this._selected = false;
        this._mode = '';
    }

    // Getter and Setters for the private attributes
    // If you override a setter, you must override the getter
    get selected() {
        return this._selected;
    }

    set selected(newSelect){
        this._selected = newSelect;
        this.render();
    }

    get mode(){
        return this._mode;
    }
    set mode(newMode){
        let rtn = false;
        if( newMode !== null ){
            this._mode = newMode;
            this.render();
            rtn = true;
        }

        return rtn;
    }

    // Methods which must be implemented by subclasses
    
    
    /**
     * Check to see if point is inside the shape
     * @param point a PIXI.Point to test
     * @return {boolean} true if point is inside the shape, false otherwise
     */
    contains(point){
        throw "Tool:contains(point):Method Not Implemented";
    }

    /**
     * Handler for pressing the mouse
     * @param event the event caused by pressing the mouse
     * @return {boolean} true if event is handled, false otherwise
     */
    mouseDown(event){
        throw "Tool:mouseDown(event):Method Not Implemented";
    }

    /**
     * Handler for moving the mouse
     * @param event the event caused by moving the mouse (button status does not matter)
     * @return {boolean} true if event is handled, false otherwise
     */
    mouseMove(event){
        throw "Tool:mouseMove(event):Method Not Implemented";
    }

    /**
     * Handler for releasing the mouse
     * @param event the event caused by releasing the mouse
     * @return {boolean} true if event is handled, false otherwise
     */
    mouseUp(event){
        throw "Tool:mouseUp(event):Method Not Implemented";
    }

    /**
     * Render the shape to the canvas
     */
    render(){
        throw "Tool:render:Method Not Implemented";
    }
    /**
     * Render graphics when the shape is selected
     */
    renderWidgets(){
        throw "Tool:renderWidgets:Method Not Implemented";
    }

    /**
     * Creates or returns the options palette
     * Recommanded that you keep a reference to the palette
     * @return the parent DOM Node of this tools's options palette
     */
    optionsPalette(){
        throw "Tool:optionsPalette:Method Not Implemented";
    }

}

export {Tool};