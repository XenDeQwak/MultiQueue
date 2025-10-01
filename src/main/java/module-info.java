module com.xen.multiqueue.multiqueue {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.xen.multiqueue to javafx.fxml;
    exports com.xen.multiqueue;
    exports com.xen.multiqueue.models;
    opens com.xen.multiqueue.models to javafx.fxml;
}