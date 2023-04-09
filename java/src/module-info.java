module plans {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens org.headroyce.declanm2022 to javafx.fxml;
    exports org.headroyce.declanm2022;
}