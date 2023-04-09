/**
 * All information regarding a sketch
 */
class Plan{
    constructor(view = null){

        // The view element to watch for modifications
        // We also update the view if the plan changes
        this._view = view;

        this._title = 'Untitled';
        this.timeCreated = new Date();
        this.timeModified = this.timeCreated;

        // All the shapes in this sketch
        this.shapes = [];
    }

    get title() { return this._title; }
    set title(newTitle){
        this._title = newTitle;
        this._view.updateIcon();
    }

    get view() { return this._view; }
    set view(newView){
        this._view = newView;
        this._view.updateIcon();
    }

    /**
     * Add a shape to our plan
     * @param tool the shape to add
     * @param index the index to add at (0 = first, > length = last)
     */
    addShape( tool, index = 0 ){
        if( index > this.shapes.length ){
            this.shapes.push(tool);
        }
        else {
            this.shapes.splice(index, 0, tool);
        }
        
        this._view.updateIcon();
    }

    /**
     * Remove a shape from our plan
     * @param index the index of the shape to remove
     */
    removeShape(index){
        if(index < 0 || index >= this.shapes.length ){
            throw Error(`Plan:Remove:Access Out Of Bounds: ${index}`);
        }

        this.shapes.splice(index, 1);
        this._view.updateIcon();
    }

    /**
     * Clear the plan of shapes
     */
    clear(){
        this.shapes = [];
        this._view.updateIcon();
    }
}

/**
 * The view element for our Plan
 */
class PlanIcon{
    constructor(plan){
        this.plan = plan;

        this.icon = document.createElement('div');
        this.icon.classList.add('planIcon');
        this.icon.innerText = this.plan.title;


        this.info = document.createElement('div');
        this.info.classList.add('planInfo')
        
        let titleDiv = document.createElement('div');
        titleDiv.innerText = 'Title'
        let titleInput = document.createElement('input');
        titleInput.value = this.plan.title;
        titleInput.addEventListener('input', function(event){
            this.plan.title = event.target.value.trim();
            this.updateIcon();
        }.bind(this))

        this.info.appendChild(titleDiv);
        this.info.appendChild(titleInput);
    }

    /**
     * Get the info tooltip for this plan
     */
    updateInfo() {
        return this.info;
    }

    /**
     * Update the view based on changes in the plan
     */
    updateIcon() {
        this.icon.innerText = this.plan.title;

        this.updateInfo();
        return this.icon;
    }
}
export {Plan, PlanIcon}