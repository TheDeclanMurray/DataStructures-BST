package org.headroyce.declanm2022;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The View Element which handles the display and interation with all the Plans
 */
public class PlansIndex extends VBox {

    private VBox plansArea;
    private Button sortingButton, addButton;

    /**
     * Allow sorting via title using a Binary Search Tree
     */
    // 0, 1, 2 -- Manual, Ascending, Descending
    private int sortMode;
    private final String[] sortImages = {"align-center.png", "sort-amount-down.png", "sort-amount-up.png"};
    private BST<PlansIndexItem> sortByTitle;


    /**
     * Collection of plans in our index
     */
    private ArrayList<Plan> plans;

    /**
     * Allow external entities to setup an event listener to know when a plan is selected
     */
    private EventHandler<ActionEvent> selectedPlanEventHandler;


    public PlansIndex(){

        sortByTitle = new BST<>();

        // Titlebar
        Label title = new Label("Plans");
        title.prefWidthProperty().bind(this.widthProperty());

        // Setup toolbar for our index
        HBox tools = new HBox();
        this.getStyleClass().add("plansIndex");
        title.getStyleClass().add("plansIndexHeader");
        tools.getStyleClass().add("plansIndexTools");

        // Add a new plan
        addButton = new Button();
        addButton.setTooltip(new Tooltip("Add"));
        Image img = new Image(getClass().getResourceAsStream("/images/plus-square.png"));
        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        addButton.setGraphic(imageView);

        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Plan newPlan = new Plan(null);
                addPlan(newPlan);
            }
        });

        // Sorting button to allow the user to reorder the plans based on title
        sortingButton = new Button();
        sortingButton.setTooltip(new Tooltip("Sort"));
        img = new Image(getClass().getResourceAsStream("/images/"+sortImages[sortMode]));
        imageView = new ImageView(img);
        imageView.setFitWidth(30);
        imageView.setFitHeight(30);
        sortingButton.setGraphic(imageView);
        sortingButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                    // When the button is selected then we change the icon
                    // and reorder the plans based on the new ordering
                    sortMode = (sortMode+1) % 3;

                    Image img = new Image(getClass().getResourceAsStream("/images/"+sortImages[sortMode]));
                    ((ImageView)sortingButton.getGraphic()).setImage(img);
                    if( sortMode != 0 ) {
                        List<PlansIndexItem> order = sortByTitle.inOrder();

                        // Suggestions by Otto Reed
                        // Descending Order
                        if( sortMode == 2 ){
                            Collections.reverse(order);
                        }
                        plansArea.getChildren().clear();
                        plansArea.getChildren().addAll(order); //error
                    }
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        tools.getChildren().addAll(sortingButton, spacer, addButton);

        plansArea = new VBox();
        plansArea.getStyleClass().add("planIndexList");


        this.getChildren().addAll(title, tools, plansArea);
    }

    /**
     * Add a plan to the index
     * @param p the plan to add
     */
    public void addPlan( Plan p ){
        if( p == null ) return;

        // Wrap the plan in a view and bind the width to the index's width
        PlansIndexItem guiItem = new PlansIndexItem(p);
        guiItem.prefWidthProperty().bind(this.widthProperty());
        guiItem.setPrefHeight(50);

        // If the item is clicked on then fire the PlanSelection Event
        guiItem.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if( selectedPlanEventHandler != null ) {
                    PlansIndexItem pi = (PlansIndexItem) event.getSource();
                    ActionEvent e = new ActionEvent(pi.plan, Event.NULL_SOURCE_TARGET);
                    selectedPlanEventHandler.handle(e);
                }
            }
        });

        plansArea.getChildren().add(0, guiItem);

        // Add the new plan to our BST
        sortByTitle.add(guiItem);

        // fire the selection event to display the new plan
        if( selectedPlanEventHandler != null ){
            ActionEvent evt = new ActionEvent(p, Event.NULL_SOURCE_TARGET);
            selectedPlanEventHandler.handle(evt);
        }
    }

    public void setOnPlanSelected( EventHandler<ActionEvent> handler ){
        this.selectedPlanEventHandler = handler;
    }

    /**
     * The view of each plan's list item
     *
     * Each list item can be compared to other list items
     */
    private class PlansIndexItem extends HBox implements Comparable<PlansIndexItem>{

        private CheckBox selected;
        private TextField title;
        private Button info;

        private Plan plan;


        @Override
        /**
         * Compare list items to each other
         */
        public int compareTo(PlansIndexItem other) {

            // If the string version are the same, then we consider the plan
            // equal to each other (this allows two plans of the same title)
            int hashCompare = this.toString().compareTo(other.toString());
            if( hashCompare == 0 ){
                return 0;
            }

            // The String versions are the same, so compare titles
            return this.plan.getTitle().compareTo(other.plan.getTitle());
        }

        public PlansIndexItem(Plan plan){
            if( plan == null ) throw new IllegalArgumentException("Plan cannot be null");
            this.plan = plan;

            selected = new CheckBox();
            title = new TextField(this.plan.getTitle());
            info = new Button();
            Image img = new Image(getClass().getResourceAsStream("/images/info.png"));
            ImageView imageView = new ImageView(img);
            imageView.setFitWidth(30);
            imageView.setFitHeight(30);
            info.setGraphic(imageView);

            this.setAlignment(Pos.CENTER);

            title.setMaxWidth(Double.MAX_VALUE);
            HBox.setMargin(title, new Insets(5,5,5,5));
            HBox.setHgrow(title, Priority.ALWAYS);
            title.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observableValue, String oldVal, String newVal) {
                    // The textfield has changed (title)
                    // So remove the element from the BST, change the title, and then add it back
                    sortByTitle.remove(PlansIndexItem.this);
                    PlansIndexItem.this.plan.setTitle(newVal);
                    sortByTitle.add(PlansIndexItem.this);
                }
            });
            selected.prefHeightProperty().bind(this.heightProperty());
            HBox.setMargin(selected, new Insets(5,5,5,5));

            info.setMinWidth(45);
            info.setMaxWidth(Double.MAX_VALUE);
            info.setMaxHeight(Double.MAX_VALUE);
            info.prefHeightProperty().bind(this.heightProperty());
            HBox.setMargin(info, new Insets(5,5,5,5));

            this.setBorder(new Border(new BorderStroke(Color.BLACK,
                    BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            this.getChildren().addAll(title, info);
        }
    }
}
