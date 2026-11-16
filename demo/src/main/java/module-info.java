module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.demo to javafx.fxml;
    exports org.example.demo;
    exports org.example.demo.app;
    opens org.example.demo.app to javafx.fxml;
}