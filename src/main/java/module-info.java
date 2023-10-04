module com.nmu.evos.p32kheperasimulator {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires encog.core;
    requires com.fazecast.jSerialComm;

    opens com.nmu.evos to javafx.fxml;
    exports com.nmu.evos;
}