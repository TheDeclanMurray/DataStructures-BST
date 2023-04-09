package org.headroyce.declanm2022;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;

import javafx.scene.control.Tooltip;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The entire workspace of the application.
 *
 * @author Brian Sea
 */
public class DrawingWorkspace extends Pane {

    private DrawingArea drawingArea;
    private VBox toolPalette;
    private Node activeToolNode;
    private Pane activeToolPalette;

    // All the registered tools
    // (Tool_Name -> Constructor)
    private HashMap<String, Constructor<? extends Tool>> registeredTools;

    private Button openPlans;
    private EventHandler<ActionEvent> openPlansHandler;

    public DrawingWorkspace(){
        ArrayList<Class<? extends Tool>> tools = new ArrayList<>();
        tools.add(LineTool.class);
        tools.add(ShapesTool.class);

        registeredTools = new HashMap<>();
        drawingArea = new DrawingArea(this);

        toolPalette = new VBox();
        toolPalette.getStyleClass().add("toolsPalette");
        toolPalette.layoutYProperty().bind(this.heightProperty().divide(2).subtract(toolPalette.heightProperty().divide(2)));

        // Register all the currently supported tools
        for( Class<? extends Tool> tool : tools ){
            try {

                // Grab the tool's name
                Method method = tool.getMethod("toolName");
                String toolname = (String)method.invoke(null);
                Constructor<? extends Tool> con = tool.getConstructor(Canvas.class);
                registeredTools.put(toolname, con);

                // Grab the GUI for the tool's selection area and attach it to the main GUI
                method = tool.getMethod("renderTool");
                Node toolGUI = (Node)method.invoke(null);

                // Handle switching to a tool and changing the status to indicate which tools we're on
                toolGUI.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        if( activeToolNode != null ) {
                            activeToolNode.getStyleClass().remove("active");
                            activeToolNode.setEffect(null);
                        }

                        drawingArea.setActiveTool(null);
                        if( activeToolNode != toolGUI ){
                            drawingArea.setActiveTool(con);
                            activeToolNode = toolGUI;
                            activeToolNode.getStyleClass().add("active");

                            ColorAdjust ca = new ColorAdjust();
                            ca.setBrightness(-0.5);
                            activeToolNode.setEffect(ca);
                        }
                        else {
                            activeToolNode = null;
                        }

                    }
                });

                toolPalette.getChildren().add(toolGUI);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        Button delete = new Button();
        delete.setTooltip(new Tooltip("Delete"));
        Image img = new Image(getClass().getResourceAsStream("/images/trash-alt.png"));
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(30);
        imageView.setFitWidth(30);
        delete.setGraphic(imageView);

        delete.setAlignment(Pos.BOTTOM_LEFT);
        delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                drawingArea.delete();

                if( activeToolNode  != null  ) {
                    activeToolNode.getStyleClass().remove("active");
                    drawingArea.setActiveTool(null);
                    activeToolNode.setEffect(null);
                    activeToolNode = null;
                }


            }
        });

        openPlans = new Button();
        openPlans.setTooltip(new Tooltip("Plans"));
        img = new Image(getClass().getResourceAsStream("/images/plans.png"));
        imageView = new ImageView(img);
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        openPlans.setGraphic(imageView);
        openPlans.setLayoutY(0);
        openPlans.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(openPlans.getStyleClass().contains("active")){
                    openPlans.getStyleClass().remove("active");
                    openPlans.setEffect(null);
                }
                else {
                    openPlans.getStyleClass().add("active");
                    ColorAdjust ca = new ColorAdjust();
                    ca.setBrightness(-0.5);
                    openPlans.setEffect(ca);
                }
                if( openPlansHandler != null ){
                    openPlansHandler.handle(actionEvent);
                }
            }
        });

        drawingArea.prefHeightProperty().bind(this.heightProperty());
        drawingArea.prefWidthProperty().bind(this.widthProperty());
        this.getChildren().addAll(drawingArea, toolPalette);
        delete.layoutYProperty().bind(this.heightProperty().subtract(delete.heightProperty()));
        this.getChildren().add(delete);
        this.getChildren().add(openPlans);




        this.setOnKeyReleased(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {

                System.out.println("Key Code: "+keyEvent.getCode());

                switch(keyEvent.getCode() ){
                    case DELETE:
                        drawingArea.delete();
                        break;
                    case ESCAPE:
                        if(drawingArea.numSelected() > 0 ){
                            drawingArea.escape();
                        }
                        else if( activeToolNode != null ){
                            activeToolNode.getStyleClass().remove("active");
                            activeToolNode.setEffect(null);
                            drawingArea.setActiveTool(null);
                            activeToolNode = null;
                        }
                        break;
                }
            }
        });




    }

    public void setActivePlan( Plan p ){
        drawingArea.setActivePlan(p);
        getChildren().remove(activeToolPalette);
        activeToolPalette = null;
    }

    public void setOnOpenPlansIndex(EventHandler<ActionEvent> handler ){
        openPlansHandler = handler;
    }

    public void setToolPalette(Tool tool ){
        if( tool == null ){
            getChildren().remove(activeToolPalette);
            activeToolPalette = null;
        }
        else {
            activeToolPalette = tool.propertiesPalette();
            activeToolPalette.layoutXProperty().bind(this.widthProperty().subtract(activeToolPalette.widthProperty()));
            getChildren().add(activeToolPalette);
        }
    }

    public void initiate(){

    }
}