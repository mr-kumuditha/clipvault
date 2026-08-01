package com.clipcycle;

import com.clipcycle.controller.MainController;
import com.clipcycle.model.CopyList;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

/**
 * Application entry point — bootstraps the filmstrip UI shell and wires it to
 * the {@link CopyList} doubly-linked list model via {@link MainController}.
 */
public class App extends Application {

    private static final double WINDOW_WIDTH = 960;
    private static final double WINDOW_HEIGHT = 580;

    private static final Preferences prefs = Preferences.userNodeForPackage(App.class);
    private static final String PREF_THEME = "theme_mode";

    /** Default sample items added on application launch. */
    private static final String[] SAMPLE_ITEMS = {
            "Hello World",
            "Java",
            "NIBM",
            "Kumuditha",
            "Data Structures and Algorithms"
    };

    private BorderPane rootPane;
    private Label listNameLabel;
    private Label positionLabel;
    private Label progressLabel;
    private Label statusLabel;
    private HBox strip;
    private ScrollPane scrollPane;

    private Button prevBtn;
    private Button copyBtn;
    private Button nextBtn;
    private Button startBtn;
    private Button addBtn;
    private Button insertBtn;
    private Button deleteBtn;
    private Button saveBtn;
    private Button loadBtn;
    private ToggleButton autoAdvanceToggle;

    @Override
    public void start(Stage stage) {
        rootPane = new BorderPane();
        rootPane.getStyleClass().add("root-pane");

        // Apply saved theme preference (default to dark)
        String currentTheme = prefs.get(PREF_THEME, "dark");
        if ("light".equalsIgnoreCase(currentTheme)) {
            rootPane.getStyleClass().add("theme-light");
        } else {
            rootPane.getStyleClass().add("theme-dark");
        }

        rootPane.setTop(buildHeader());
        rootPane.setCenter(buildFilmstrip());
        rootPane.setBottom(buildControlBar());

        // Initialize CopyList model and add initial items
        CopyList list = new CopyList("Dev Deployment Workflow");
        for (String item : SAMPLE_ITEMS) {
            list.addItem(item);
        }
        // Set current to head node initially
        list.start();

        MainController controller = new MainController(list,
                listNameLabel,
                positionLabel,
                progressLabel,
                statusLabel,
                strip,
                scrollPane,
                prevBtn,
                copyBtn,
                nextBtn,
                startBtn,
                addBtn,
                insertBtn,
                deleteBtn,
                saveBtn,
                loadBtn,
                autoAdvanceToggle);

        Scene scene = new Scene(rootPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(
                getClass().getResource("/com/clipcycle/styles/clipcycle.css")
                        .toExternalForm());

        // Keyboard navigation shortcuts
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT -> controller.handlePrevious();
                case RIGHT -> controller.handleNext();
                case N -> {
                    if (event.isShortcutDown()) {
                        controller.handleAddItem();
                    }
                }
                default -> {
                }
            }
        });

        stage.setTitle("ClipCycle");
        stage.setScene(scene);
        stage.setMinWidth(720);
        stage.setMinHeight(400);
        stage.show();
    }

    // ════════════════════════════════════════════════════════════════
    // Header — title + list name on the left, theme toggle & stats on the right
    // ════════════════════════════════════════════════════════════════

    private HBox buildHeader() {
        Label title = new Label("CLIPCYCLE");
        title.getStyleClass().add("app-title");

        listNameLabel = new Label("My Copy List");
        listNameLabel.getStyleClass().add("list-name-label");
        listNameLabel.setId("list-name-label");

        VBox left = new VBox(4, title, listNameLabel);
        left.setAlignment(Pos.CENTER_LEFT);

        positionLabel = new Label("frame 1 of 1");
        positionLabel.getStyleClass().add("position-label");
        positionLabel.setId("position-label");

        progressLabel = new Label("0 of 0 copied");
        progressLabel.getStyleClass().add("progress-label");
        progressLabel.setId("progress-label");

        statusLabel = new Label("");
        statusLabel.getStyleClass().add("status-label");
        statusLabel.setId("status-label");

        VBox statsBox = new VBox(4, positionLabel, progressLabel, statusLabel);
        statsBox.setAlignment(Pos.CENTER_RIGHT);

        boolean isLight = "light".equalsIgnoreCase(prefs.get(PREF_THEME, "dark"));
        ToggleButton themeToggle = new ToggleButton(isLight ? "☀️ Light" : "🌙 Dark");
        themeToggle.getStyleClass().add("theme-toggle-btn");
        themeToggle.setId("theme-toggle-btn");
        themeToggle.setSelected(isLight);

        themeToggle.setOnAction(e -> {
            boolean lightMode = themeToggle.isSelected();
            if (lightMode) {
                rootPane.getStyleClass().remove("theme-dark");
                if (!rootPane.getStyleClass().contains("theme-light")) {
                    rootPane.getStyleClass().add("theme-light");
                }
                themeToggle.setText("☀️ Light");
                prefs.put(PREF_THEME, "light");
            } else {
                rootPane.getStyleClass().remove("theme-light");
                if (!rootPane.getStyleClass().contains("theme-dark")) {
                    rootPane.getStyleClass().add("theme-dark");
                }
                themeToggle.setText("🌙 Dark");
                prefs.put(PREF_THEME, "dark");
            }
        });

        HBox right = new HBox(16, themeToggle, statsBox);
        right.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(left, spacer, right);
        header.getStyleClass().add("header-bar");
        return header;
    }

    // ════════════════════════════════════════════════════════════════
    // Filmstrip — horizontal scrollable strip container
    // ════════════════════════════════════════════════════════════════

    private ScrollPane buildFilmstrip() {
        strip = new HBox();
        strip.getStyleClass().add("filmstrip-strip");

        scrollPane = new ScrollPane(strip);
        scrollPane.getStyleClass().add("filmstrip-scroll");
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true);
        return scrollPane;
    }

    // ════════════════════════════════════════════════════════════════
    // Control bar — primary (nav/copy) + secondary (list editing)
    // ════════════════════════════════════════════════════════════════

    private VBox buildControlBar() {
        // ── Primary row ──
        prevBtn = new Button("\u25C0  Previous");
        prevBtn.getStyleClass().add("primary-btn");
        prevBtn.setId("prev-btn");

        copyBtn = new Button("Copy Current");
        copyBtn.getStyleClass().addAll("primary-btn", "copy-btn");
        copyBtn.setId("copy-btn");

        nextBtn = new Button("Next  \u25B6");
        nextBtn.getStyleClass().add("primary-btn");
        nextBtn.setId("next-btn");

        HBox primaryRow = new HBox(prevBtn, copyBtn, nextBtn);
        primaryRow.getStyleClass().add("primary-controls");

        // ── Secondary row ──
        startBtn = new Button("\u23EE Start");
        startBtn.getStyleClass().add("secondary-btn");
        startBtn.setId("start-btn");

        addBtn = new Button("+ Add Item");
        addBtn.getStyleClass().add("secondary-btn");
        addBtn.setId("add-btn");

        insertBtn = new Button("\u2935 Insert After");
        insertBtn.getStyleClass().add("secondary-btn");
        insertBtn.setId("insert-btn");

        deleteBtn = new Button("\u2715 Delete");
        deleteBtn.getStyleClass().addAll("secondary-btn", "delete-btn");
        deleteBtn.setId("delete-btn");

        saveBtn = new Button("\uD83D\uDCBE Save List");
        saveBtn.getStyleClass().add("secondary-btn");
        saveBtn.setId("save-btn");

        loadBtn = new Button("\uD83D\uDCC2 Load List");
        loadBtn.getStyleClass().add("secondary-btn");
        loadBtn.setId("load-btn");

        HBox secondaryRow = new HBox(startBtn, addBtn, insertBtn, deleteBtn, saveBtn, loadBtn);
        secondaryRow.getStyleClass().add("secondary-controls");

        // ── Auto-advance toggle ──
        autoAdvanceToggle = new ToggleButton("↻ Auto-advance");
        autoAdvanceToggle.getStyleClass().add("auto-advance-toggle");
        autoAdvanceToggle.setId("auto-advance-toggle");

        Region toggleSpacer = new Region();
        HBox.setHgrow(toggleSpacer, Priority.ALWAYS);

        HBox toggleRow = new HBox(toggleSpacer, autoAdvanceToggle);
        toggleRow.getStyleClass().add("toggle-row");

        VBox bar = new VBox(16, primaryRow, secondaryRow, toggleRow);
        bar.getStyleClass().add("control-bar");
        return bar;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
