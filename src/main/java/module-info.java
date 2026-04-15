module javafx.forum1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires com.google.zxing;
    requires com.google.zxing.javase;




    opens javafx.forum1 to javafx.fxml;
    opens javafx.forum1.controller to javafx.fxml;
    opens javafx.forum1.model to javafx.base;
    exports javafx.forum1;
}