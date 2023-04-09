package org.headroyce.declanm2022;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @author Brian Sea
 *
 * The tool for creating multi-point lines
 */
public class LineTool extends Tool{

    // Model
    private LList<Point> points;
    private Point movePoint;

    // The mode states
    // "draw"   -- currently drawing the shape
    // "modify" -- changing the already drawn shape

    // Mouse status to detect dragging
    private boolean mouseDown, mouseMove;

    // Selected elements of the line
    private int selectedPoint, oldSelectedPoint;

    // The styling of the lines
    private static final int LINE_WIDTH = 2;
    // the interaction circles when selected
    private static final int POINT_RADIUS = 5;

    // View
    private Canvas view;

    /**
     * Get the name of this tool
     * @return the all lowercase name for this tool
     */
    static public String toolName() { return "line"; }

    /**
     * Create the graphical element used to activate the tool
     * @return the top-level JavaFX Graphical Node
     */
    static public Node renderTool() {
        Button toolGUI = new Button();
        toolGUI.setTooltip(new Tooltip("Line"));

        try {
            System.out.println("Running");
            FileInputStream input = new FileInputStream("assets/images/vector-line.png");
            Image img = new Image(input);
            ImageView deleteView = new ImageView(img);
            deleteView.setFitHeight(30);
            deleteView.setFitWidth(30);
            toolGUI.setGraphic(deleteView);
        } catch (FileNotFoundException e) {
            System.err.println("NOPE");
            toolGUI.setText("Line");
        }

        return toolGUI;
    }

    @Override
    public Pane propertiesPalette() {
        return new Pane();
    }


    public LineTool(Canvas view){
        setMode("create");

        this.view = view;
        points = new LList<>();
        select(false);
    }

    /**
     * Sets the selection mode of the shape
     * @param selectMe true to select the shape
     */
    public void select(boolean selectMe){
        super.select(selectMe);

        if( selectMe == false ) {
            this.selectedPoint = -2;
            movePoint = null;
        }
    }

    /**
     * Checks to the see if a point is within the shape
     * @param p the PIXI point to test
     * @return true if p is inside the shape, false otherwise
     */
    @Override
    public boolean contains(Point p){

        // Register contain if within 5px
        final int DIST_WITHIN = 3;

        // Test each line segment for distance
        boolean inShape = false;
        for( int spot = 0; spot < this.points.size()-1; spot++ ){

            // line segment-Point Distance formula
            // See http://csharphelper.com/blog/2016/09/find-the-shortest-distance-between-a-point-and-a-line-segment-in-c/
            Point fPoint = this.points.get(spot);
            Point lPoint = this.points.get(spot+1);

            double dx = lPoint.x - fPoint.x;
            double dy = lPoint.y - fPoint.y;

            // This segment is only one point...
            if( dx == 0 && dy == 0 ){
                return (fPoint.x == p.x) && (fPoint.y == p.y);
            }

            // Calculate t (the parametric) to minimize the distance to the segment
            double t = ((p.x-fPoint.x)*dx + (p.y-fPoint.y)*dy) / (dx*dx+dy*dy);
            if( t < 0 ){
                // at or before the first end point
                dx = p.x - fPoint.x;
                dy = p.y - fPoint.y;
            }
            else if( t > 1 ){
                // at or after the second end point
                dx = p.x - lPoint.x;
                dy = p.y - lPoint.y;
            }
            else {
                // In the middle somewhere, so find the point using the parametric
                Point closest = new Point(fPoint.x+t*dx, fPoint.y+t*dy);
                dx = p.x - closest.x;
                dy = p.y - closest.y;
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
     * Allow the shape to handle pressing of the mouse
     * @param md the point where the mouse was pressed
     * @return true if the event was handled, false otherwise
     */
    @Override
    public boolean mouseDown(Point md){
        this.mouseDown = true;

        // If the same point is selected twice in a row, then we delete the point
        // so save the old selected point
        oldSelectedPoint = selectedPoint;

        // Index of the selected point, Must start be below -1
        selectedPoint = -2;

        // Check to see if the point is within an interaction circle
        // Using Point-Circle distance formula
        int spot = 0;
        for( Point point : this.points ){
            double X2 = (point.x - md.x) * (point.x-md.x);
            double Y2 = (point.y - md.y) * (point.y-md.y);
            double R2 = POINT_RADIUS*POINT_RADIUS;

            // Point is within the circle!
            if( X2+Y2 < R2 ){
                selectedPoint = spot;
                break;
            }
            spot = spot + 1;
        }

        // Should we remain selected?
        boolean stayActive = true;
        if( this.getMode().equals("create")){

            // We 'double clicked' the last circle, so finish the shape
            if( selectedPoint == this.points.size() -1 ){
                this.setMode("edit");
                stayActive = false;
            }
            else {
                // Add midpoint first
                if( this.points.size() > 0 ) {
                    Point lastPoint = this.points.get(this.points.size() - 1);
                    Point mid = new Point( (lastPoint.x+md.x)/2, (lastPoint.y+md.y)/2);
                    this.points.add(mid);
                }
                this.points.add(md);
            }
        }
        else {
            if( selectedPoint == -2 ){
                stayActive = false;
            }
            else {
                // Selected a midPoint, so we need to add two points
                // One before and one after which are midpoints
                if( this.selectedPoint % 2 == 1 ){
                    Point currPoint = this.points.get(this.selectedPoint);
                    Point prevPoint = this.points.get(this.selectedPoint-1);
                    Point afterPoint = this.points.get(this.selectedPoint+1);

                    Point prevMidPoint = new Point((currPoint.x+prevPoint.x)/2, (currPoint.y+prevPoint.y)/2);
                    Point afterMidPoint = new Point((currPoint.x+afterPoint.x)/2, (currPoint.y+afterPoint.y)/2);

                    this.points.insert(afterMidPoint, this.selectedPoint);
                    this.points.insert(prevMidPoint, this.selectedPoint-1);
                    // Adjust selectedPoint to accommodate new points
                    this.selectedPoint++;
                }
            }
        }
        return stayActive;
    }

    /**
     * Handle a mouse movement. Use the select attribute to see if shape is currently selected.
     * @param p the location where the mouse currently is
     * @return true if the event is handled, false otherwise
     */
    @Override
    public boolean mouseMove( Point p ){
        if( this.points.size() == 0 ){ return false; }

        if( getMode().equals("create")) {
            Point lastPoint = this.points.get(this.points.size()-1);

            // Create a move point so we can draw it during a render
            if (lastPoint != null && getMode().equals("create")) {
                movePoint = p;
            }
        }
        return true;
    }

    /**
     * Handle a mouse drag.  Use the selected attribute to see if the shape is currently selected.
     * @param p the location of the dragging
     * @return true if the event is handled, false otherwise
     */
    @Override
    public boolean mouseDrag( Point p ){
        this.mouseMove = true;
        if( this.getMode().equals("create")){
            return mouseMove(p);
        }
        if( this.getMode().equals("edit") && this.selectedPoint >= 0) {
            // User is dragging a point, so move it
            Point selectedPoint = this.points.get(this.selectedPoint);
            selectedPoint.x = p.x;
            selectedPoint.y = p.y;

            // Recalculate midpoints based on the new point location
            if (this.selectedPoint % 2 == 0) {
                for (int spot = this.selectedPoint - 2; spot < this.selectedPoint + 2 && spot < this.points.size() - 2; spot += 2) {
                    // Handle dragging the first point
                    if( spot < 0 ){
                        continue;
                    }

                    Point fpoint = this.points.get(spot);
                    Point midpoint = this.points.get(spot + 1);
                    Point lpoint = this.points.get(spot + 2);

                    midpoint.x = (fpoint.x + lpoint.x) / 2;
                    midpoint.y = (fpoint.y + lpoint.y) / 2;
                }
            }
        }
        return true;
    }

    /**
     * Handle a mouse release.  Use the selected attribute to see if the shape is currently selected.
     * @param p the location where the mouse was released
     * @return true if the event was handled, false otherwise
     */
    @Override
    public boolean mouseUp(Point p){

        // Since the mouse didn't move, then treat this is a click
        if(!this.mouseMove ){
            // Selected the same point twice, so delete it
            // We also need to delete the midpoint after the point
            // and recalculate the previous midpoint
            if(this.selectedPoint >= 0 && this.selectedPoint == this.oldSelectedPoint){
                this.points.remove(this.selectedPoint); // point
                this.points.remove(this.selectedPoint); // midpoint after point

                // Recalculate the the midpoint that's left
                // The doesn't apply to the first and last points
                if( this.selectedPoint >= 2 && this.selectedPoint < this.points.size()) {
                    Point reCalcMidPoint = this.points.get(this.selectedPoint - 1);
                    Point leftOfMid = this.points.get(this.selectedPoint - 2);
                    Point rightOfMid = this.points.get(this.selectedPoint);
                    reCalcMidPoint.x = (leftOfMid.x+rightOfMid.x)/2;
                    reCalcMidPoint.y = (leftOfMid.y+rightOfMid.y)/2;
                }

                // Remove selection points
                this.selectedPoint = this.oldSelectedPoint = -2;
            }
        }
        // Clear mouse statuses
        this.mouseDown = this.mouseMove = false;
        return true;
    }

    /**
     * Render the line
     */
    @Override
    public void render(){
        if( this.points.size() == 0 ){ return; }

        GraphicsContext gc = view.getGraphicsContext2D();

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(LineTool.LINE_WIDTH);

        Point firstPoint = this.points.get(0);
        gc.moveTo(firstPoint.x, firstPoint.y);
        for(int spot = 1; spot < this.points.size(); spot++ ){
            Point point = this.points.get(spot);
            gc.strokeLine(firstPoint.x, firstPoint.y, point.x, point.y);
            firstPoint = point;
        }
        if( this.isSelected()){
            renderWidgets();
        }
    }

    /**
     * Render the selection and interaction GUI
     */
    @Override
    public void renderWidgets(){

        // If we aren't currently selected, then get out
        if( !isSelected() ){
            return;
        }

        GraphicsContext gc = view.getGraphicsContext2D();
        gc.setFill(Color.BLACK);

        // Draw the interaction circles
        int spot = 0;
        for( Point p : this.points ){
            if( spot % 2 != 0 ){
                gc.setFill(Color.RED);  // mid points
            }
            else{
                gc.setFill(Color.BLACK); // actual points
            }

            gc.fillOval(p.x-POINT_RADIUS, p.y-POINT_RADIUS, POINT_RADIUS*2, POINT_RADIUS*2);
            spot ++;
        }

        // Draw the move point and a line to it from the last committed point of the line
        if( movePoint != null ){
            Point lastPoint = this.points.get(this.points.size()-1);
            gc.strokeLine(lastPoint.x, lastPoint.y, movePoint.x, movePoint.y);
            gc.fillOval(movePoint.x-POINT_RADIUS, movePoint.y-POINT_RADIUS, POINT_RADIUS*2, POINT_RADIUS*2);
        }

        // If there's a selected point, then we color it
        if( selectedPoint >= 0 ){
            gc.setFill(Color.GREEN);
            Point point = this.points.get(selectedPoint);
            gc.fillOval(point.x-POINT_RADIUS, point.y-POINT_RADIUS, POINT_RADIUS*2, POINT_RADIUS*2);
        }
    }
}