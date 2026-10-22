module com.joel.lectorcodigo {
    requires javafx.controls;
    requires javafx.fxml;
    requires jasperreports;
    requires java.desktop;
    requires org.xerial.sqlitejdbc;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires org.apache.logging.log4j;
    //requires atlantafx.base;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires javafx.swing;

    opens com.joel.lectorcodigo to javafx.fxml;
    opens com.joel.lectorcodigo.datos to javafx.base;
    exports com.joel.lectorcodigo;
}