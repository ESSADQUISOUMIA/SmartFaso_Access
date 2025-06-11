module com.example.facialrecognition {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    requires opencv; // Si vous utilisez OpenCV comme un module

    opens com.example.facialrecognition to javafx.fxml;
    opens com.example.facialrecognition.controller to javafx.fxml;
    opens com.example.facialrecognition.model to javafx.base, javafx.fxml;
    opens com.example.facialrecognition.service to javafx.fxml;
    opens com.example.facialrecognition.database to javafx.fxml;
    opens com.example.facialrecognition.util to javafx.fxml;
    opens com.example.facialrecognition.security to javafx.fxml;

    // AJOUTEZ CES DEUX LIGNES IMPORTANTES :
//    exports com.example.facialrecognition.view;
    opens com.example.facialrecognition.view to javafx.fxml;

    exports com.example.facialrecognition;
    exports com.example.facialrecognition.controller;
    exports com.example.facialrecognition.model;
    exports com.example.facialrecognition.service;
    exports com.example.facialrecognition.database;
    exports com.example.facialrecognition.util;
    exports com.example.facialrecognition.security;
}