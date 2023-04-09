import {Tool} from '/js/tool.mjs';


class ShapesTool extends Tool{

    static toolName = 'shapes';
    static renderTool() {
        let parentNode = document.createElement('div');
        parentNode.classList.add('fas');
        parentNode.classList.add('fa-draw-polygon')
        parentNode.setAttribute('data-tool', ShapesTool.toolName.toLowerCase());
 
        let tooltip = document.createElement('div');
        tooltip.classList.add('tooltip')
        tooltip.innerText = 'Shape';
        parentNode.appendChild(tooltip);

        return parentNode;
    }

    propertiesPalette(){

        if( this._optionsPalette === null ){
            let parentNode = document.createElement('div')
            let toolbar = document.createElement('div')
            let optionContent = document.createElement('div')

            // Bg Color
            let bgColor = document.createElement('div')
            bgColor.setAttribute('data-toolOption', 'fillColor');
            bgColor.classList.add('toolOption')
            bgColor.innerText = 'BG';
            toolbar.appendChild(bgColor);

            let bgColorSelection = document.createElement('input')
            bgColorSelection.setAttribute('type', 'color');

            let htmlColor = this.options.fillColor.replace("0x", "#");
            bgColorSelection.setAttribute('value', htmlColor);
            bgColorSelection.addEventListener('input', function(event){
                let PIXIColor = event.target.value.replace("#", "0x");
                this.options.fillColor = PIXIColor;
                this.render();
            }.bind(this))
            optionContent.appendChild(bgColorSelection);

            parentNode.appendChild(toolbar)
            parentNode.appendChild(optionContent);
            this._optionsPalette = parentNode;
        }
        return this._optionsPalette;
    }

    constructor(id){
        super(id);

        this.options = {
            fillColor: '0x5182ED',
            border: '',
            boundingPointRadius: 5
        }

        this.view = new PIXI.Graphics();
        
        this._optionsPalette = null;
        this._mouseDown = null;

        // model
        // (x,y) is upper left corner
        this.boundingRect = {
            x: NaN,
            y: NaN,           
            width: 0,
            height: 0
        }

        this.points = []

        this._oldtransform = -1;
        this._transform = -1;
        this._placePoint = -1;        
        this._addPoint = null;

        this.mode = 'create';
    }

    contains(point){
        // Treat the shape as a regular polygon and 
        // use a ray-cast to determine if point is inside the shape

        let inside = false;
        for( let i = 0, j = this.points.length-1; i < this.points.length; j = i, i++ ){
            let p1 = this.points[i];
            let p2 = this.points[j];
            
            let intersect = (
                (p1.y > point.y) != (p2.y > point.y) &&
                (point.x < (p2.x - p1.x) * (point.y - p1.y) / (p2.y - p1.y) + p1.x)
            );
            if( intersect === true ){
                inside = !inside;
            }
        }
        return  inside || this._inInteractionWidget({offsetX:point.x, offsetY:point.y}) >= 0 
    }

    _inInteractionWidget(event){
        this._transform = -1;
        let spot = 0;
        for( let point of this.points ){
            let X2 = (event.offsetX - point.x);
            X2 *= X2;

            let Y2 = (event.offsetY - point.y);
            Y2 *= Y2;

            let R2 = (this.options.boundingPointRadius*5)
            R2 *= R2;

            if( X2+Y2 < R2 )
            {
                this._transform = spot;
                break;
            }
            spot++;
        }
        return this._transform;
    }

    mouseDown(event){
        let handled = false;

        if( this.mode === 'create' ){
            if( isNaN(this.boundingRect.x) ){
                this.boundingRect.x = event.offsetX;
                this.boundingRect.y = event.offsetY;

                for( let i = 0; i < 4; i++ ) {
                    this.points.push( new PIXI.Point(this.boundingRect.x, this.boundingRect.y))
                }
            }
            else {
                this.mode = 'edit'; // ending point if the use doesn't drag to draw
            }
            handled = true;
           
        }
        else if( this.mode === 'edit' ) {
            
            if( this._placePoint >= 0 ){
                console.log( "SPLICE: ", this._placePoint, this._addPoint);
                this.points.splice((this._placePoint+1)%this.points.length, 0, new PIXI.Point(this._addPoint.x, this._addPoint.y));
                this._placePoint = null;
                this._addPoint = -1;
            }

            this._oldtransform = this._transform;
            this._transform = this._inInteractionWidget(event);

            if( this._transform >= 0 ){
                handled = true;
            }
        }

        this._mouseDown = {x:event.offsetX, y:event.offsetY};
        return handled;
    }

    mouseMove(event){
        // Bounding Rect is not started...
        if( isNaN(this.boundingRect.x) ) return false;
        
        if( this.mode === 'create' ){
            this.boundingRect.width = event.offsetX - this.boundingRect.x;
            this.boundingRect.height = event.offsetY - this.boundingRect.y;

            this.points[0].x = this.boundingRect.x;
            this.points[0].y = this.boundingRect.y

            this.points[1].x = this.boundingRect.x + this.boundingRect.width
            this.points[1].y = this.boundingRect.y

            this.points[2].x = this.boundingRect.x + this.boundingRect.width
            this.points[2].y = this.boundingRect.y + this.boundingRect.height

            this.points[3].x = this.boundingRect.x;
            this.points[3].y = this.boundingRect.y + this.boundingRect.height;

            
        }
        else if( this.mode === 'edit' ){
            this._oldtransform = -1;
            this._addPoint = null;
            this._placePoint = -1;

            if( this._mouseDown !== null && this._transform >= 0) {
                this.points[this._transform].x = event.offsetX;
                this.points[this._transform].y = event.offsetY;
            }
            else {

                this._transform = this._inInteractionWidget(event);
  
                
                if( this._transform < 0 ) {
                    
                    // Point-Line distance when each line is defined by two points
                    for( let i = 0; i < this.points.length; i++ ){

                        let secondPoint = (i+1)%this.points.length;

                        let num = (this.points[secondPoint].y-this.points[i].y)*event.offsetX - (this.points[secondPoint].x-this.points[i].x)*event.offsetY + this.points[secondPoint].x*this.points[i].y - this.points[secondPoint].y*this.points[i].x;
                        num = Math.abs(num);
        
                        let Y2 = (this.points[secondPoint].y-this.points[i].y);
                        Y2 *= Y2;
        
                        let X2 = (this.points[secondPoint].x - this.points[i].x);
                        X2 *= X2
        
                        let dist = num/Math.sqrt(X2+Y2);
                        if( dist < 5 ){
                            // We're near a line, so project a perpendicular from the mouse the line and find the intersection

                            // Slope from A to B
                            let atob = {x: this.points[secondPoint].x - this.points[i].x, y: this.points[secondPoint].y-this.points[i].y}

                            // Slope from A to Mouse (M)
                            let atom = {x: event.offsetX - this.points[i].x, y: event.offsetY -this.points[i].y}
                            
                            // Length of vector AB
                            let len = atob.x*atob.x + atob.y*atob.y
                            
                            // M dot B
                            let dot = atom.x*atob.x + atom.y*atob.y

                            // Find the parametric value to arrive on the line
                            let t = Math.min( 1, Math.max(0, dot/len));

                            // AB dot AM
                            dot = (this.points[secondPoint].x-this.points[i].x) * (event.offsetY-this.points[i].y) - (this.points[secondPoint].y-this.points[i].y)*(event.offsetY-this.points[i].x)

							// if t is zero or one, then we're parallel to the line, skip it
							if( t == 0 || t == 1 ){
								continue;
							}

                            this._placePoint = i;
                            this._addPoint = {x: this.points[i].x+atob.x*t, y:this.points[i].y+atob.y*t};
                            break;
                        }
                    }
                }
            }
        }
        this.render();
    }

    mouseUp(event){

        if( this.mode === 'create' && this.boundingRect.width !== 0 && this.boundingRect.height !== 0 ){
            this.mode = 'edit';
        }

        // Fix Upper Left point if shape is inverted
        if( this.boundingRect.width < 0 ){
            this.boundingRect.width *= -1;
            this.boundingRect.x = this.boundingRect.x - this.boundingRect.width
        }

        if( this.boundingRect.height < 0 ){
            this.boundingRect.height *= -1;
            this.boundingRect.y = this.boundingRect.y - this.boundingRect.height;
        }

        // Double clicked a point, delete it
        if( this._transform !== -1 && this._transform === this._oldtransform ){
            this.points.splice(this._transform, 1);
        }

        this._transform = this._oldtransform = -1;
        this._mouseDown = null;
        this._placePoint = -1;
        this._addPoint = null;

        

        this.render();
    }

    render(){
        if( this.boundingRect.x === NaN ) return;

        this.view.clear();

        if(this.points.length > 1 ){
            this.view.lineStyle(0, 0);
            this.view.beginFill(this.options.fillColor);

            this.view.moveTo( this.points[0].x, this.points[0].y )
            for( let i = 1; i < this.points.length; i++ ){
                this.view.lineTo(this.points[i].x, this.points[i].y);
            }
            this.view.endFill();
        }

        if( this.selected === true ){
            this.renderWidgets();
        }
    }

    renderWidgets(){
        this.view.lineStyle(2, 0x000000);

        // Draw interaction widgets of the bound rectangle
        this.view.beginFill(0x000000);
        for( let point of this.points ){
            this.view.drawCircle( point.x, point.y, this.options.boundingPointRadius);
        }
        this.view.endFill();

        if( this._transform >= 0 ){
            this.view.beginFill(0x0000FF);
            this.view.drawCircle(this.points[this._transform].x, this.points[this._transform].y, this.options.boundingPointRadius);
            this.view.endFill();
        }

        if( this._addPoint !== null ) {
            this.view.beginFill(0x00FF00);
            this.view.drawCircle(this._addPoint.x, this._addPoint.y, this.options.boundingPointRadius);
            this.view.endFill();
        }
    }
}

export {ShapesTool};
