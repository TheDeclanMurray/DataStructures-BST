package org.headroyce.declanm2022;

import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * The main drawing canvas for our application
 *
 * @author Brian Sea
 */
public class DrawingArea extends StackPane {

    // The main drawing canvas
    private Canvas mainCanvas;

    // The active tool's constructor so we can make new objects
    private Constructor<? extends Tool> activeTool;

    // The plan to draw
    private Plan activePlan;

    // All the selected shapes in the world
    private ArrayList<Tool> selectedShapes;

    private DrawingWorkspace mainWorkspace;

    public DrawingArea(DrawingWorkspace mw){
        selectedShapes = new ArrayList<>();
        mainWorkspace = mw;

        mainCanvas = new Canvas();

        // Force the canvas to resize to the screen's size

        mainCanvas.widthProperty().bind(this.widthProperty());
        mainCanvas.heightProperty().bind(this.heightProperty());

        // Attach mouse handlers to the canvas
        EventHandler<MouseEvent> handler = new MouseHandler();
        mainCanvas.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
        mainCanvas.addEventHandler(MouseEvent.MOUSE_RELEASED, handler);
        mainCanvas.addEventHandler(MouseEvent.MOUSE_MOVED, handler);
        mainCanvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, handler);

        this.getChildren().add(mainCanvas);
    }

    /**
     * Switch to and active a new tool
     * @param t the constructor of the tool
     * @return true if the is switched, false otherwise
     */
    public boolean setActiveTool(Constructor<? extends Tool> t){
        boolean rtn = false;
        if( t != activeTool){
            activeTool = t;
            rtn = true;

            // Clear the selections and render the world
            for( Tool tool : selectedShapes){
                tool.select(false);
            }
            selectedShapes.clear();
            mainWorkspace.setToolPalette(null);
            renderWorld();
        }
        return rtn;
    }

    Constructor<? extends Tool> getActiveTool() {
        return activeTool;
    }

    /**
     * Render the viewable canvas
     */
    public void renderWorld(){
        if( activePlan == null ) return;

        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        gc.clearRect(0,0, mainCanvas.getWidth(), mainCanvas.getHeight());

        for(Tool tool : activePlan){
            tool.render();
        }
    }

    /**
     * Helps to handle all of the mouse events on the canvas
     */
    private class MouseHandler implements EventHandler<MouseEvent> {
        @Override
        public void handle(MouseEvent event){
            if( activePlan == null ) return;

            Point p = new Point(event.getX(), event.getY());

            if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)){
                if( activeTool != null ) {
                    // Nothing is selected, so create and select a new shape
                    if (selectedShapes.isEmpty()) {
                        try {
                            // Creates a new instance of the tool
                            Tool tool = activeTool.newInstance(mainCanvas);
                            activePlan.add(tool);
                            selectedShapes.add(tool);
                            tool.select(true);
                            mainWorkspace.setToolPalette(tool);

                        } catch (InstantiationException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // Send the mouse event to all selected shapes
                boolean eventHandled = false;
                for( Tool tool : selectedShapes){
                    eventHandled = tool.mouseDown(p) || eventHandled;
                }
                // If no selected shape handles the event, then
                // that means it's outside all shapes, so deselect everything
                if( !eventHandled ){
                    for( Tool shape : selectedShapes ){
                        shape.select(false);
                    }
                    selectedShapes.clear();
                    mainWorkspace.setToolPalette(null);

                    for (Tool tool : activePlan) {
                        if (tool.contains(p)) {
                            selectedShapes.add(tool);
                            tool.select(true);
                            mainWorkspace.setToolPalette(tool);
                            break;
                        }
                    }
                }

            }
            else if( event.getEventType().equals(MouseEvent.MOUSE_RELEASED)){
                for( Tool tool : selectedShapes ){
                    tool.mouseUp(p);
                }
            }
            else if( event.getEventType().equals(MouseEvent.MOUSE_MOVED)){
                for( Tool tool : selectedShapes ){
                    tool.mouseMove(p);
                }
            }
            else if( event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)){
                for( Tool tool : selectedShapes ){
                    tool.mouseDrag(p);
                }
            }
            renderWorld();
        }
    }

    public void delete(){
        if( selectedShapes.size() > 0 ){
            for( Tool tool : selectedShapes ){
                activePlan.remove(tool);
            }
            selectedShapes.clear();
            mainWorkspace.setToolPalette(null);
        }
        else {
            activePlan.clear();
        }
        renderWorld();
    }

    public void setActivePlan( Plan p ){
        // Deselect everything
        for( Tool tool : selectedShapes ){
            tool.select(false);
        }

        activePlan = p;
        selectedShapes.clear();
        renderWorld();
    }

    public int numSelected() {
        return selectedShapes.size();
    }
    public void escape(){
        for( Tool tool : selectedShapes ){
            tool.select(false);
        }
        selectedShapes.clear();
        mainWorkspace.setToolPalette(null);

        renderWorld();
    }

    public void initiate(){

    }
}