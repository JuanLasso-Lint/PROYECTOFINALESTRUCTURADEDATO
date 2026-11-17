module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.example.demo to javafx.fxml;
    opens org.example.demo.app to javafx.fxml;
    opens org.example.demo.viewController to javafx.fxml;
    opens org.example.demo.model to javafx.base;

    exports org.example.demo.model;
    exports org.example.demo.controller;
    exports org.example.demo.app;
    exports org.example.demo.model.Estructuras;
    exports org.example.demo.model.Exception;
    exports org.example.demo.viewController;
}