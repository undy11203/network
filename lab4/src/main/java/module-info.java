module org.project {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.protobuf;
    requires org.slf4j;
    requires lombok;
    requires com.google.common;
    requires java.desktop;

    opens org.project to javafx.fxml, com.google.protobuf;
    opens org.project.protobuf to com.google.protobuf;
    opens org.project.controllers to javafx.fxml;
    opens org.project.model to com.google.common;
    exports org.project;
    exports org.project.controllers;
    exports org.project.events.message;
    exports org.project.exceptions;
    exports org.project.model;
    exports org.project.model.snake;
    exports org.project.utils;
    exports org.project.model.communication.converters;
    exports org.project.model.communication;
    opens org.project.utils to javafx.fxml;
    exports org.project.model.communication.udp;
    exports org.project.model.communication.gameplayers;
}