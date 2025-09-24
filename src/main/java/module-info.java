module com.xen.multiqueue.multiqueue {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.xen.multiqueue.multiqueue to javafx.fxml;
    exports com.xen.multiqueue.multiqueue;
}