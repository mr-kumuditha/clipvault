
package com.clipcycle.controller;

import com.clipcycle.model.ClipboardNode;
import com.clipcycle.model.CopyList;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.util.Optional;

/**
 * Controller class managing the interactions between the filmstrip JavaFX UI
 * and the underlying {@link CopyList} doubly-linked list model.
 *
 * <p>All node reference manipulation remains strictly inside {@link CopyList};
 * this controller only invokes model methods and updates UI elements.
 */
public class MainController {

    private final CopyList list;
    private final Label positionLabel;
    private final Label progressLabel;
    private final Label statusLabel;
    private final Label listNameLabel;
    private final HBox strip;
    private final ScrollPane scrollPane;

    private final Button prevBtn;
    private final Button copyBtn;
    private final Button nextBtn;
    private final Button startBtn;
    private final Button addBtn;
    private final Button insertBtn;
    private final Button deleteBtn;
    private final Button saveBtn;
    private final Button loadBtn;
    private final ToggleButton autoAdvanceToggle;

    private PauseTransition statusTimer;

    public MainController(CopyList list,
                          Label listNameLabel,
                          Label positionLabel,
                          Label progressLabel,
                          Label statusLabel,
                          HBox strip,
                          ScrollPane scrollPane,
                          Button prevBtn,
                          Button copyBtn,
                          Button nextBtn,
                          Button startBtn,
                          Button addBtn,
                          Button insertBtn,
                          Button deleteBtn,
                          Button saveBtn,
                          Button loadBtn,
                          ToggleButton autoAdvanceToggle) {
        this.list = list;
        this.listNameLabel = listNameLabel;
        this.positionLabel = positionLabel;
        this.progressLabel = progressLabel;
        this.statusLabel = statusLabel;
        this.strip = strip;
        this.scrollPane = scrollPane;
        this.prevBtn = prevBtn;
        this.copyBtn = copyBtn;
        this.nextBtn = nextBtn;
        this.startBtn = startBtn;
        this.addBtn = addBtn;
        this.insertBtn = insertBtn;
        this.deleteBtn = deleteBtn;
        this.saveBtn = saveBtn;
        this.loadBtn = loadBtn;
        this.autoAdvanceToggle = autoAdvanceToggle;

        setupEventHandlers();
        refreshUI();
    }

    /**
     * Binds user action handlers to button clicks.
     */
    private void setupEventHandlers() {
        // ── Primary Controls ──────────────────────────────────────────
        startBtn.setOnAction(e -> handleStart());
        prevBtn.setOnAction(e -> handlePrevious());
        copyBtn.setOnAction(e -> handleCopyCurrent());
        nextBtn.setOnAction(e -> handleNext());

        // ── Secondary Controls ────────────────────────────────────────
        addBtn.setOnAction(e -> handleAddItem());
        insertBtn.setOnAction(e -> handleInsertAfter());
        deleteBtn.setOnAction(e -> handleDeleteItem());
        saveBtn.setOnAction(e -> handleSaveList());
        loadBtn.setOnAction(e -> handleLoadList());

        // ── Toggle Controls ──────────────────────────────────────────
        autoAdvanceToggle.setOnAction(e ->
                list.setAutoAdvance(autoAdvanceToggle.isSelected()));
    }

    // ════════════════════════════════════════════════════════════════
    //  Command Handlers
    // ════════════════════════════════════════════════════════════════

    public void handleAddItem() {
        Optional<String> result = showInputDialog("Add New Item", "Enter clipboard text content:", "");
        if (result.isPresent()) {
            String text = result.get().trim();
            if (!text.isEmpty()) {
                list.addItem(text);
                refreshUI();
                showTemporaryStatus("✓ Added new item");
            } else {
                showTemporaryStatus("⚠ Item text cannot be empty");
            }
        }
    }

    public void handleStart() {
        if (list.isEmpty()) return;
        list.start();
        refreshUI();
        showTemporaryStatus("⏮ Jumped to Start");
    }

    public void handlePrevious() {
        if (list.isEmpty()) return;
        list.previous();
        refreshUI();
    }

    public void handleNext() {
        if (list.isEmpty()) return;
        list.next();
        refreshUI();
    }

    public void handleCopyCurrent() {
        if (list.isEmpty()) {
            showTemporaryStatus("✗ Nothing to copy");
            return;
        }
        boolean success = list.copyCurrent();
        if (success) {
            refreshUI();  // update checkmarks, progress, and auto-advance position
            showTemporaryStatus("✓ Copied to clipboard!");
        } else {
            showTemporaryStatus("✗ Copy failed");
        }
    }

    public void handleInsertAfter() {
        if (list.isEmpty()) {
            handleAddItem();
            return;
        }
        Optional<String> result = showInputDialog("Insert Item", "Insert text immediately after current active frame:", "");
        if (result.isPresent()) {
            String text = result.get().trim();
            if (!text.isEmpty()) {
                list.insertAfterCurrent(text);
                refreshUI();
                showTemporaryStatus("✓ Inserted after current frame");
            } else {
                showTemporaryStatus("⚠ Item text cannot be empty");
            }
        }
    }

    public void handleDeleteItem() {
        if (list.isEmpty()) return;
        String deletedText = list.deleteItem();
        refreshUI();
        if (deletedText != null) {
            showTemporaryStatus("✓ Deleted active frame");
        }
    }

    public void handleSaveList() {
        if (list.isEmpty()) {
            showTemporaryStatus("✗ List is empty");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Copy List");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt")
        );
        String safeName = list.getName().replaceAll("[^a-zA-Z0-9._-]", "_");
        fileChooser.setInitialFileName(safeName + ".txt");

        File file = fileChooser.showSaveDialog(scrollPane.getScene().getWindow());
        if (file != null) {
            try {
                list.saveToFile(file);
                showTemporaryStatus("✓ Saved: " + file.getName());
            } catch (Exception ex) {
                showTemporaryStatus("✗ Save failed");
            }
        }
    }

    public void handleLoadList() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Copy List");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Supported Files (*.txt, *.rtf, *.text, *.csv, *.log)", "*.txt", "*.rtf", "*.text", "*.csv", "*.log"),
                new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"),
                new FileChooser.ExtensionFilter("Rich Text Format (*.rtf)", "*.rtf"),
                new FileChooser.ExtensionFilter("All Files", "*")
        );

        File file = fileChooser.showOpenDialog(scrollPane.getScene().getWindow());
        if (file != null) {
            try {
                list.loadFromFile(file);
                refreshUI();
                showTemporaryStatus("✓ Loaded: " + file.getName());
            } catch (Exception ex) {
                showTemporaryStatus("✗ Load failed");
            }
        }
    }

    /**
     * Jump directly to a clicked node in the filmstrip.
     */
    private void jumpToNode(ClipboardNode targetNode) {
        if (targetNode == null || list.isEmpty()) return;
        // Reset to start and walk next until we match targetNode
        ClipboardNode cur = list.getHead();
        list.start();
        while (cur != null && cur != targetNode) {
            cur = cur.getNext();
            list.next();
        }
        refreshUI();
    }

    // ════════════════════════════════════════════════════════════════
    //  UI Rendering & Sync
    // ════════════════════════════════════════════════════════════════

    /**
     * Re-renders the filmstrip items from head to tail using model pointers,
     * updates active indicators, updates counters, and auto-scrolls.
     */
    public void refreshUI() {
        listNameLabel.setText(list.getName());
        strip.getChildren().clear();

        if (list.isEmpty()) {
            Label emptyLabel = new Label("(Copy List is empty. Click + Add Item to get started)");
            emptyLabel.getStyleClass().add("empty-label");
            strip.getChildren().add(emptyLabel);

            positionLabel.setText("0 of 0");
            setControlsEnabled(false);
            return;
        }

        setControlsEnabled(true);
        ClipboardNode activeNode = list.getCurrent();
        ClipboardNode walker = list.getHead();
        int index = 1;
        Node activeCellNode = null;

        while (walker != null) {
            boolean isActive = (walker == activeNode);
            VBox cell = buildFrameCell(index, walker.getContent(), isActive, walker);

            if (isActive) {
                activeCellNode = cell;
            }

            strip.getChildren().add(cell);
            walker = walker.getNext();
            index++;
        }

        positionLabel.setText(String.format("item %d of %d", list.getCurrentIndex(), list.getSize()));
        progressLabel.setText("\u2713 " + list.getProgress());

        // Auto-center scroll active frame after layout pass
        if (activeCellNode != null) {
            final Node targetNode = activeCellNode;
            scrollPane.applyCss();
            scrollPane.layout();
            centerNodeInScrollPane(targetNode);
        }
    }

    private void setControlsEnabled(boolean enabled) {
        if (!enabled || list.isEmpty()) {
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            copyBtn.setDisable(true);
            startBtn.setDisable(true);
            deleteBtn.setDisable(true);
            insertBtn.setDisable(false); // Insert when empty acts as Add
        } else {
            copyBtn.setDisable(false);
            startBtn.setDisable(false);
            deleteBtn.setDisable(false);
            insertBtn.setDisable(false);

            // Enable Previous only if a predecessor node exists
            ClipboardNode cur = list.getCurrent();
            prevBtn.setDisable(cur == null || cur.getPrev() == null);

            // Enable Next only if a successor node exists
            nextBtn.setDisable(cur == null || cur.getNext() == null);
        }
    }

    private VBox buildFrameCell(int index, String content, boolean isActive, ClipboardNode targetNode) {
        VBox cell = new VBox();
        cell.getStyleClass().add("frame-cell");
        if (isActive) {
            cell.getStyleClass().add("frame-cell-active");
        }
        if (targetNode.isUsed()) {
            cell.getStyleClass().add("frame-cell-used");
        }

        // Click on frame cell advances current pointer to this frame
        cell.setOnMouseClicked(e -> jumpToNode(targetNode));

        // ── Top sprocket row ──
        Region holeTopL = new Region();
        holeTopL.getStyleClass().add("sprocket-hole");

        Label indexLabel = new Label(String.format("%02d", index));
        indexLabel.getStyleClass().add("frame-index");

        Region holeTopR = new Region();
        holeTopR.getStyleClass().add("sprocket-hole");

        HBox sprocketsTop = new HBox(holeTopL, indexLabel, holeTopR);
        sprocketsTop.getStyleClass().add("sprocket-row");

        // ── Frame body (StackPane for checkmark overlay) ──
        Label contentLabel = new Label(content);
        contentLabel.getStyleClass().add("frame-content");

        StackPane body = new StackPane(contentLabel);
        body.getStyleClass().add("frame-body");

        // Show checkmark on used (already-copied) frames
        if (targetNode.isUsed()) {
            Label checkmark = new Label("\u2713");
            checkmark.getStyleClass().add("frame-checkmark");
            StackPane.setAlignment(checkmark, Pos.TOP_RIGHT);
            body.getChildren().add(checkmark);
        }

        // ── Bottom sprocket row ──
        HBox sprocketsBottom = new HBox();
        sprocketsBottom.getStyleClass().add("sprocket-row");
        for (int i = 0; i < 3; i++) {
            Region hole = new Region();
            hole.getStyleClass().add("sprocket-hole");
            sprocketsBottom.getChildren().add(hole);
        }

        cell.getChildren().addAll(sprocketsTop, body, sprocketsBottom);
        return cell;
    }

    private void centerNodeInScrollPane(Node node) {
        double width = scrollPane.getContent().getBoundsInLocal().getWidth();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();

        if (width <= viewportWidth) {
            scrollPane.setHvalue(0.5);
            return;
        }

        double nodeMinX = node.getBoundsInParent().getMinX();
        double nodeWidth = node.getBoundsInParent().getWidth();
        double nodeCenter = nodeMinX + (nodeWidth / 2.0);

        double hvalue = (nodeCenter - (viewportWidth / 2.0)) / (width - viewportWidth);
        scrollPane.setHvalue(Math.max(0, Math.min(1, hvalue)));
    }

    private void showTemporaryStatus(String message) {
        if (statusTimer != null) {
            statusTimer.stop();
        }
        statusLabel.setText(message);
        statusTimer = new PauseTransition(Duration.seconds(2.5));
        statusTimer.setOnFinished(e -> statusLabel.setText(""));
        statusTimer.play();
    }

    private Optional<String> showInputDialog(String title, String header, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText("Text:");

        // Apply app stylesheet to dialog pane
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/clipcycle/styles/clipcycle.css").toExternalForm()
        );

        // Input validation: disable OK button when text is empty or whitespace-only
        Node okButton = dialog.getDialogPane().lookupButton(javafx.scene.control.ButtonType.OK);
        if (okButton != null) {
            okButton.setDisable(defaultValue.trim().isEmpty());
            dialog.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
                okButton.setDisable(newVal == null || newVal.trim().isEmpty());
            });
        }

        return dialog.showAndWait();
    }
}

