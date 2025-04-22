module com.frontend.jfx {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.frontend.jfx to javafx.fxml;
    exports com.frontend.jfx;
}
