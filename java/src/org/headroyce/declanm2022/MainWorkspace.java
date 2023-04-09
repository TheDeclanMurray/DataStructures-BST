package org.headroyce.declanm2022;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.layout.BorderPane;

public class MainWorkspace extends BorderPane {
    private DrawingWorkspace dw;
    private PlansIndex plansIndex;

    public MainWorkspace(){
        dw = new DrawingWorkspace();
        plansIndex = new PlansIndex();
        plansIndex.prefWidthProperty().bind(this.widthProperty().divide(3));

        dw.setOnOpenPlansIndex(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if( plansIndex.getParent() != null ){
                    setLeft(null);
                }
                else {
                    setLeft(plansIndex);
                }
            }
        });

        plansIndex.setOnPlanSelected(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               dw.setActivePlan((Plan)event.getSource());
            }
        });

        this.setCenter(dw);

    }

    public void initiate(){

    }

}
