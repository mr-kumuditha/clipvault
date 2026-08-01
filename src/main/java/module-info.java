module com.clipcycle {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;   // java.awt.Toolkit used by CopyList.copyCurrent()
    requires java.prefs;     // Preferences API used for theme persistence

    // Let JavaFX reflectively access these packages (needed for FXML loading)
    opens com.clipcycle to javafx.fxml;

    exports com.clipcycle;
    exports com.clipcycle.model;
    exports com.clipcycle.controller;
}

