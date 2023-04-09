package org.headroyce.declanm2022;

import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Generic Tool class; All valid tools must extend this.
 *
 * @author Brian Sea
 */
public abstract class Tool {

    private String id;
    private boolean selected;
    private String mode;

    public String getID() { return this.id; }
    public boolean setID( String id ){
        boolean rtn = false;
        if( id != null ){
            this.id = id;
            rtn = true;
        }
        return rtn;
    }

    public boolean isSelected(){ return selected;}
    public void select( boolean selection ) { selected = selection; }

    public String getMode(){ return mode; }
    public boolean setMode( String newMode ){
        boolean rtn = false;
        if( newMode != null ){
            mode = newMode;
            rtn = true;
        }
        return rtn;
    }

    /**
     * The name of this tool
     * @return the name of the tool in all lowercase
     */
    static public String toolName(){
        return "generic tool";
    }

    /**
     * Create a new GUI element to display for tool selection
     * @return a top-level JavaFX GUI Node for this tool
     */
    static public Node renderTool(){
        throw new UnsupportedOperationException("renderTool not implemented");
    }

    public abstract Pane propertiesPalette();

    /**
     * Check to see if a point is inside the shape
     * @param p a point to test
     * @return true if p is inside the shape, false otherwise
     */
    abstract boolean contains(Point p);

    /**
     * Allow the shape to handle pressing of the mouse
     * @param p the point where the mouse was pressed
     * @return true if the event was handled, false otherwise
     */
    abstract public boolean mouseDown(Point p);

    /**
     * Handle a mouse movement. Use the select attribute to see if shape is currently selected.
     * @param p the location where the mouse currently is
     * @return true if the event is handled, false otherwise
     */
    abstract public boolean mouseMove(Point p );

    /**
     * Handle a mouse release.  Use the selected attribute to see if the shape is currently selected.
     * @param p the location where the mouse was released
     * @return true if the event was handled, false otherwise
     */
    abstract public boolean mouseUp(Point p);

    /**
     * Handle a mouse drag.  Use the selected attribute to see if the shape is currently selected.
     * @param p the location of the dragging
     * @return true if the event is handled, false otherwise
     */
    abstract public boolean mouseDrag(Point p);

    /**
     * Render the shape to its view
     */
    abstract public void render();

    /**
     * Render the selection and interaction GUI
     */
    abstract public void renderWidgets();
}
