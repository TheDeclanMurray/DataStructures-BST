import {LinkedList} from '/js/linkedlist.mjs';
import {Tool} from '/js/tool.mjs';

/**
 * @author Brian Sea
 * @classdesc The tool for creating multi-point lines
 * @class
 */
class LineTool extends Tool{
    static toolName = 'line';
    static renderTool(){
        let div = document.createElement('div');
        div.setAttribute('data-tool', LineTool.toolName.toLowerCase());
        div.classList.add('tool');
        div.classList.add('fas');
        div.classList.add('fa-project-diagram');

        let tooltip = document.createElement('div');
        tooltip.classList.add('tooltip');
        tooltip.innerText = 'Line'

        div.appendChild(tooltip);

        return div;
    }

    propertiesPalette() {
        let parentNode = document.createElement('div');

        return parentNode;
    }

    constructor(id){
        super(id);

        // Mouse status to detect dragging
        this._mouseDown = false;
        this._mouseMove = false;
        
        // Selected elements of the line
        this._oldSelectedPoint = -2;
        this._selectedPoint = -2;

        // The styling of the lines
        this.style = {
            lineWidth: 2,      
            pointRadius: 5              // the interaction circles when selected
        }

        // View
        this.view = new PIXI.Graphics();

        // Model
        // Each odd index is the midpoint on the line (P0,P1)
        // [P0, M0, P1, M1, P2]
        this.points = new LinkedList();

        // Must be set after the points exists
        this.mode = 'draw';
    }

    set selected(newSelect){
        super.selected = newSelect;
        if( this.selected === false ){
            this._selectedPoint = -2;
        }
    }
    get selected() {
        return super.selected;
    }

    /**
     * Checks to see if a point is within the shape
     * @param point the PIXI point to test
     * @return true if point is inside the shape, false otherwise
     * @override
     */
    contains( point ) {

        // Register contain if within 10px
        const DIST_WITHIN = 6;
        let inShape = false;

        // Test each line segment for distance
        for( let spot = 0; spot < this.points.size-1; spot++ ){
            let fPoint = this.points.get(spot);
            let lPoint = this.points.get(spot+1);

            let dx = lPoint.x - fPoint.x;
            let dy = lPoint.y - fPoint.y;

            // This segment is only one point...
            if( dx == 0 && dy == 0 ){
                return (fPoint.x == point.x) && (fPoint.y == point.y);
            }

            // Calculate t (the parametric) to minimize the distance to the segment
            let t = ((point.x-fPoint.x)*dx + (point.y-fPoint.y)*dy) / (dx*dx+dy*dy);
            if( t < 0 ){
                // at or before the first end point
                dx = point.x - fPoint.x;
                dy = point.y - fPoint.y;
            }
            else if( t > 1 ){
                // at or after the second end point
                dx = point.x - lPoint.x;
                dy = point.y - lPoint.y;
            }
            else {
                // In the middle somewhere, so find the point using the parametric
                let closest = new PIXI.Point(fPoint.x+t*dx, fPoint.y+t*dy);
                dx = point.x - closest.x;
                dy = point.y - closest.y;
            }

            // See if the distance is within our bounds
            if( Math.sqrt(dx*dx+dy*dy) < DIST_WITHIN ){
                inShape = true;
                break;
            }

        }
        return inShape;
    }

    /**
     * 
     * @param event the event caused by pressing the mouse
     * @override
     */
    mouseDown(event){
        // Not a left click, so deactivate
        if( event.button !== 0 ){ return false; }
        if( this.selected == false ){ return false;}
        this._mouseDown = true;


        // If the same point is selected twice in a row, then we delete the point
        // so save the old selected point
        this._oldSelectedPoint = this._selectedPoint;
        
        // Index of the selected point, Must start be below -1
        this._selectedPoint = -2;

        // Check to see if the point is within an interaction circle
        // Using Point-Circle distance formula
        let spot = 0;
        for( let point of this.points) {
            let X2 = 0;
            let Y2 = 0;
            let R2 = 0;
        
            X2 = (point.x - event.offsetX)
            X2 *= X2;

            Y2 = (point.y - event.offsetY);
            Y2 *= Y2;

            R2 = (this.style.pointRadius * 2);
            R2 *= R2;

            // Point is within the circle!
            if( (X2+Y2) < R2 ) {
                this._selectedPoint = spot;
                break;
            }
            spot = spot + 1;
        }
        
        // Should we remain selected?
        let stayActive = true;
        if( this.mode === 'draw'){
            // We 'double clicked' the last circle, so finish the shape
            if( this._selectedPoint === this.points.size-1) {
                this.mode = 'modify'
            }
            else {
                let point = new PIXI.Point(event.offsetX, event.offsetY);
                
                // Add MidPoint First
                if( this.points.size > 0 ){
                    let lastPoint = this.points.get(this.points.size-1);
                    let x = (lastPoint.x + point.x)/2;
                    let y = (lastPoint.y + point.y)/2;
                    this.points.add(new PIXI.Point(x,y));
                    
                }
                // add new Point
                this.points.add(point);
            }
        }
        else{

            // Not near a interaction circle so get out
            if( this._selectedPoint < 0 ){
                return;
            }

            // Selected a midPoint, so we need to add two points
            // One before and one after which are midpoints
            if( this._selectedPoint % 2 === 1 ){
                let currPoint = this.points.get(this._selectedPoint);
                let prevPoint = this.points.get(this._selectedPoint-1)
                let afterPoint = this.points.get(this._selectedPoint+1);

                let prevMidPoint = new PIXI.Point((currPoint.x+prevPoint.x)/2, (currPoint.y+prevPoint.y)/2);
                let afterMidPoint = new PIXI.Point((currPoint.x+afterPoint.x)/2, (currPoint.y+afterPoint.y)/2);

                this.points.insert(afterMidPoint, this._selectedPoint);
                this.points.insert(prevMidPoint, this._selectedPoint-1);
                // Adjust selectedPoint to accommodate new points
                this._selectedPoint++;
            } 
        }
        
        this.render();
        return stayActive;
    }

    /**
     * Handle a mouse movement. Use the select attribute to see if shape is currently selected.
     * @param event event caused by moving the mouse
     * @override
     */
    mouseMove(event){
        this._mouseMove = true;
        if( this.mode === 'draw'){
            let lastPoint = this.points.get(this.points.size-1);
            if( lastPoint !== null ){
                this.render();
            
                // Draw a line from the last point to where the mouse currently is
                this.view.moveTo( lastPoint.x, lastPoint.y);
                this.view.lineTo( event.offsetX, event.offsetY);

                this.view.beginFill(0);
                this.view.drawCircle(event.offsetX, event.offsetY, this.style.pointRadius);
                this.view.endFill();
            }
        }
        else if( this.mode === 'modify' && this._mouseDown && this._selectedPoint >= 0){
            
            // User is dragging a point
            let selectedPoint = this.points.get(this._selectedPoint);
            selectedPoint.x = event.offsetX;
            selectedPoint.y = event.offsetY;

            // Recalculate midpoints based off the new point location
            if( this._selectedPoint % 2 === 0 ){
                for( let spot = this._selectedPoint - 2; spot < this._selectedPoint+2 && spot < this.points.size-2; spot+=2 ){
                    
                    // Avoid the first point
                    if( spot < 0 ){
                        continue;
                    }

                    let fpoint = this.points.get(spot);
                    let midpoint = this.points.get(spot+1);
                    let lpoint = this.points.get(spot+2);

                    midpoint.x = (fpoint.x+lpoint.x)/2
                    midpoint.y = (fpoint.y+lpoint.y)/2;
                }
            }
            this.render();
        }
    }

    /**
     * Handle a mouse release.  Use the selected attribute to see if the shape is currently selected.
     * @param event event caused by releasing the mouse
     * @override
     */
    mouseUp(event){
        // Selected the same point twice, so delete it
        // We also need to delete the midpoint after the point
        // and recalculate the previous midpoint
        if(!this._mouseMove && this.mode === 'modify'){
            if(this._selectedPoint >= 0 && this._selectedPoint === this._oldSelectedPoint){
                this.points.remove(this._selectedPoint); // point
                this.points.remove(this._selectedPoint); // midpoint after point
                
                // Recalculate the the midpoint that's left 
                // Avoid the first and last points
                if( this._selectedPoint >= 2 && this._selectedPoint < this.points.size ){
                    let reCalcMidPoint = this.points.get(this._selectedPoint-1)
                    let leftOfMid = this.points.get(this._selectedPoint-2)
                    let rightOfMid = this.points.get(this._selectedPoint);

                    reCalcMidPoint.x = (leftOfMid.x+rightOfMid.x)/2;
                    reCalcMidPoint.y = (leftOfMid.y+rightOfMid.y)/2
                }

                // Remove selection points
                this._selectedPoint = this._oldSelectedPoint = -2;
                this.render();
            }
        }

        // Clear mouse statuses
        this._mouseDown = false;
        this._mouseMove = false;
    }

    /**
     * Render the selection and interaction GUI
     * @override
     */
    renderWidgets(){

        // If we aren't currently selected, then get out
        if( this.selected === false ){
            return;
        }

        // TODO: Can we move this to an iterator?
        // Main point circles
        this.view.beginFill(0);
        for( let spot = 0; spot < this.points.size; spot += 2 ) {
            let point = this.points.get(spot);
            this.view.drawCircle(point.x, point.y, this.style.pointRadius);
        }
        this.view.endFill();

        // Circles used for midpoints
        this.view.beginFill(0xFF0000);
        for( let spot = 1; spot < this.points.size; spot+= 2 ){
            let point = this.points.get(spot);
            this.view.drawCircle(point.x, point.y, this.style.pointRadius);
        }
        this.view.endFill();

        // Color the selected points
        if( this._selectedPoint >= 0 ){
            this.view.beginFill(0xFFFF00);
            let selectedPoint = this.points.get(this._selectedPoint);
            this.view.drawCircle(selectedPoint.x, selectedPoint.y, this.style.pointRadius);
        }
    }

    /**
     * Render the line
     * @override
     */
    render(){
        if( this.points.size === 0 ){ return; }

        this.view.clear();
        let firstPoint = this.points.get(0);

        // lines
        this.view.lineStyle(this.style.lineWidth, 0, 1);
        this.view.moveTo(firstPoint.x, firstPoint.y);
        // Skip Midpoints
        for( let spot = 2; spot < this.points.size; spot+=2 ) {
            let point = this.points.get(spot);
            this.view.lineTo(point.x, point.y);
        }
        
        if(this.selected === true ){
            this.renderWidgets();
        }
        
    }

    print() {
        console.log(`Tool: ${Line.toolName}`);
        this.points.print();
    }
}


export {LineTool}
