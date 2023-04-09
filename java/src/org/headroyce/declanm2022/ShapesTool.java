package org.headroyce.declanm2022;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class ShapesTool extends Tool{
    /**
     * Get the name of this tool
     * @return the all lowercase name for this tool
     */
    static public String toolName() { return "shapes"; }

    /**
     * Create the graphical element used to activate the tool
     * @return the top-level JavaFX Graphical Node
     */
    static public Node renderTool() {
        Button toolGUI = new Button();
        toolGUI.setTooltip(new Tooltip("Shapes"));
        try {
            FileInputStream input = new FileInputStream("assets/images/draw-polygon.png");
            Image img = new Image(input);
            ImageView deleteView = new ImageView(img);
            deleteView.setFitHeight(30);
            deleteView.setFitWidth(30);
            toolGUI.setGraphic(deleteView);
        } catch (FileNotFoundException e) {
            toolGUI.setText("Shapes");
        }
        return toolGUI;
    }

    public Pane propertiesPalette() {
       HBox parent = new HBox();
       ColorPicker bgColor = new ColorPicker(fillColor);

       bgColor.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Platform.runLater(new Runnable() {
                    public void run() {
                        fillColor = bgColor.getValue();
                        render();
                    }
                });

            }
        });

        parent.getChildren().add(bgColor);

        return parent;

    }

    private ArrayList<Point> points;

    private Color fillColor;
    private static final int pointRadius = 5;

    private Canvas view;

    private Point mouseDown;
    private Point mouseMove;

    private Point addPoint;
    private int placePoint;
    private int transformPoint;
    private int oldtransformPoint;

    private int inInteractionWidgets(Point p ){
        this.transformPoint = -1;

        int spot = 0;
        for( Point point : this.points ){
            double X2 = (p.x - point.x);
            X2 *= X2;

            double Y2 = (p.y - point.y);
            Y2 *= Y2;

            double R2 = (ShapesTool.pointRadius*4);
            R2 *= R2;

            if( X2+Y2 < R2 )
            {
                this.transformPoint = spot;
                break;
            }
            spot++;
        }
        return this.transformPoint;
    }

    public ShapesTool(Canvas view){
        points = new ArrayList<Point>();
        fillColor = Color.ROYALBLUE;
        transformPoint = oldtransformPoint = -1;
        this.placePoint = -1;

        this.view = view;
        setMode("create");
    }

    @Override
    boolean contains(Point p) {
        // Treat the shape as a regular polygon and
        // use a ray-cast to determine if point is inside the shape

        boolean inside = false;
        for( int i = 0, j = this.points.size()-1; i < this.points.size(); j = i, i++ ){
            Point p1 = this.points.get(i);
            Point p2 = this.points.get(j);

            boolean intersect = (
                (p1.y > p.y) != (p2.y > p.y) &&
                (p.x < (p2.x - p1.x) * (p.y - p1.y) / (p2.y - p1.y) + p1.x)
            );
            if( intersect == true ){
                inside = !inside;
            }
        }

        return inside || (inInteractionWidgets(p) >= 0);
    }

    @Override
    public boolean mouseDown(Point p) {
        boolean handled = false;
        this.mouseDown = new Point(p.x, p.y);

        if( this.getMode().equals("create")){
            if( this.points.size() == 0 ){
                for( int i = 0; i < 4; i++ ) {
                    this.points.add( new Point(p.x, p.y));
                }

            }
            handled = true;
        }
        else if( this.getMode().equals("edit")) {
            if( this.placePoint >= 0 ){
                this.points.add((this.placePoint+1)%this.points.size(), new Point(this.addPoint.x, this.addPoint.y));
                this.addPoint = null;
            }

            this.oldtransformPoint = this.transformPoint;
            this.transformPoint = this.inInteractionWidgets(p);

            if( this.transformPoint >= 0 ) {
                handled = true;
            }
        }

        return handled;
    }

    @Override
    public boolean mouseMove(Point p) {
        // Bounding Rect is not started...
        if( this.points.size() == 0 ) return false;

        this.mouseMove = p;
        if( this.getMode().equals("create") && this.points.size() > 0 && this.mouseDown != null ){
            Point UL = this.points.get(0);

            double width = p.x - this.mouseDown.x;
            double height = p.y - this.mouseDown.y;

            if( width < 0 ){
                UL.x = p.x;
                width *= -1;
            }
            if( height < 0 ){
                UL.y = p.y;
                height *= -1;
            }

            this.points.get(1).x = UL.x + width;
            this.points.get(1).y = UL.y;

            this.points.get(2).x = UL.x + width;
            this.points.get(2).y = UL.y + height;

            this.points.get(3).x = UL.x;
            this.points.get(3).y = UL.y + height;
        }
        else if( this.getMode().equals("edit") ){
            this.oldtransformPoint = -1;
            this.placePoint = -1;
            this.addPoint = null;

            if( this.mouseDown != null && this.transformPoint >= 0) {
                this.points.get(this.transformPoint).x = p.x;
                this.points.get(this.transformPoint).y = p.y;
            }
            else {
                this.transformPoint = this.inInteractionWidgets(p);

                if( this.transformPoint < 0 ) {
                    // Point-Line distance when each line is defined by two points
                    for( int i = 0; i < this.points.size(); i++ ){

                        Point firstPoint = this.points.get(i);
                        Point secondPoint = this.points.get((i+1)%this.points.size());

                        double num = (secondPoint.y-firstPoint.y)*p.x - (secondPoint.x-firstPoint.x)*p.y + secondPoint.x*firstPoint.y - secondPoint.y*firstPoint.x;
                        num = Math.abs(num);

                        double Y2 = (secondPoint.y-firstPoint.y);
                        Y2 *= Y2;

                        double X2 = (secondPoint.x - firstPoint.x);
                        X2 *= X2;

                        double dist = num/Math.sqrt(X2+Y2);
                        if( dist < 6 ){
                            // We're near a line, so project a perpendicular from the mouse the line and find the intersection

                            // Slope from A to B
                            Point atob =  new Point(secondPoint.x - firstPoint.x, secondPoint.y-firstPoint.y);

                            // Slope from A to Mouse (M)
                            Point atom = new Point(p.x - firstPoint.x, p.y -firstPoint.y);

                            // Length of vector AB
                            double len = atob.x*atob.x + atob.y*atob.y;

                            // M dot B
                            double dot = atom.x*atob.x + atom.y*atob.y;

                            // Find the parametric value to arrive on the line
                            double t = Math.min( 1, Math.max(0, dot/len));

                            // AB dot AM
                            dot = (secondPoint.x-firstPoint.x) * (p.y-firstPoint.y) - (secondPoint.y-firstPoint.y)*(p.y-firstPoint.x);

                            // If t is zero or one, then we're parallel to the line, skip it
                            if( t == 0 || t == 1){
                                continue;
                            }

                            this.placePoint = i;
                            this.addPoint = new Point(firstPoint.x+atob.x*t,firstPoint.y+atob.y*t);
                            break;
                        }
                    }
                }
            }
        }
        this.render();
        return true;
    }

    @Override
    public boolean mouseUp(Point p) {

        boolean hasDimensions = (Math.abs(this.points.get(0).x - this.points.get(1).x) >= 5 || Math.abs(this.points.get(0).y - this.points.get(1).y) >= 5 );
        if( this.getMode().equals("create") && hasDimensions){
            this.setMode("edit");
            this.mouseDown = this.mouseMove = null;
        }
        else if( this.getMode().equals("edit")){
            // Double clicked a point, delete it
            if( this.mouseMove == null && this.transformPoint >= 0 &&
                    this.transformPoint == this.oldtransformPoint ){
                this.points.remove(this.transformPoint);
                this.transformPoint = this.oldtransformPoint = -1;
            }

            this.placePoint = -1;
            this.addPoint = null;
            this.mouseDown = this.mouseMove = null;
        }


        this.render();
        return true;
    }

    @Override
    public boolean mouseDrag(Point p) {
        if( getMode().equals("create") && this.mouseMove == null) {
            return mouseMove(p);
        }
        else if( getMode().equals("edit") && this.transformPoint >= 0){
            Point moveMe = this.points.get(this.transformPoint);
            moveMe.x = p.x;
            moveMe.y = p.y;
        }


        this.render();
        return true;
    }

    @Override
    public void render() {
        if( this.points.size() == 0 ) return;

        if(this.points.size() > 1 ){
            GraphicsContext gc = view.getGraphicsContext2D();
            gc.setFill(this.fillColor);

            gc.beginPath();
            gc.moveTo( this.points.get(0).x, this.points.get(0).y );
            for( int i = 1; i < this.points.size(); i++ ){
                gc.lineTo(this.points.get(i).x, this.points.get(i).y);
            }
            gc.fill();
        }

        if( this.isSelected()){
            this.renderWidgets();
        }

    }

    @Override
    public void renderWidgets() {
        GraphicsContext gc = view.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.setLineWidth(2);


        // Draw interaction widgets of the bound rectangle
        for( Point point : this.points ){
            gc.fillOval( point.x-ShapesTool.pointRadius, point.y-ShapesTool.pointRadius,2*this.pointRadius,2*this.pointRadius);
        }

        if( this.transformPoint >= 0 ){
            gc.setFill(Color.BLUE);
            gc.fillOval(this.points.get(this.transformPoint).x-ShapesTool.pointRadius, this.points.get(this.transformPoint).y - ShapesTool.pointRadius,
                    2*this.pointRadius, 2*this.pointRadius);

        }

        if( this.addPoint != null) {
            gc.setFill(Color.GREEN);
            gc.fillOval(this.addPoint.x - ShapesTool.pointRadius, this.addPoint.y - ShapesTool.pointRadius, ShapesTool.pointRadius*2, ShapesTool.pointRadius*2);
        }
    }
}
