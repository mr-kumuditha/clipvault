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

    // Model and injected UI component references (wired up by the FXML controller)
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

    // Timer used to auto-clear the temporary status message
    private PauseTransition statusTimer;

    /**
     * Wires the given model and UI controls together, binds event handlers,
     * and performs an initial render of the filmstrip.
     */
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
        // Frame navigation and copy actions
        startBtn.setOnAction(e -> handleStart());
        prevBtn.setOnAction(e -> handlePrevious());
        copyBtn.setOnAction(e -> handleCopyCurrent());
        nextBtn.setOnAction(e -> handleNext());

        // List editing actions
        addBtn.setOnAction(e -> handleAddItem());
        insertBtn.setOnAction(e -> handleInsertAfter());
        deleteBtn.setOnAction(e -> handleDeleteItem());
        saveBtn.setOnAction(e -> handleSaveList());
        loadBtn.setOnAction(e -> handleLoadList());

        // Toggle auto-advance behavior on the model
        autoAdvanceToggle.setOnAction(e ->
                list.setAutoAdvance(autoAdvanceToggle.isSelected()));
    }

    // ---- Command Handlers ----

    /**
     * Prompts the user for new clipboard text and appends it as a new
     * tail node via {@link CopyList#addItem(String)}.
     */
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

    /**
     * Moves the current pointer back to the head node of the list.
     */
    public void handleStart() {
        if (list.isEmpty()) return;
        list.start();
        refreshUI();
        showTemporaryStatus("⏮ Jumped to Start");
    }

    /**
     * Moves the current pointer one node backward using the active
     * node's prev reference.
     */
    public void handlePrevious() {
        if (list.isEmpty()) return;
        list.previous();
        refreshUI();
    }

    /**
     * Moves the current pointer one node forward using the active
     * node's next reference.
     */
    public void handleNext() {
        if (list.isEmpty()) return;
        list.next();
        refreshUI();
    }

    /**
     * Copies the content of the current node to the system clipboard
     * and marks that node as used.
     */
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

    /**
     * Inserts a new node immediately after the current node. If the
     * list is empty there is no "current" node to insert after, so
     * this falls back to {@link #handleAddItem()}.
     */
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

    /**
     * Removes the current node from the list. {@link CopyList#deleteItem()}
     * is responsible for re-linking the neighboring prev/next nodes.
     */
    public void handleDeleteItem() {
        if (list.isEmpty()) return;
        String deletedText = list.deleteItem();
        refreshUI();
        if (deletedText != null) {
            showTemporaryStatus("✓ Deleted active frame");
        }
    }

    /**
     * Opens a file chooser and writes the current list contents to a
     * text file on disk.
     */
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

    /**
     * Opens a file chooser and loads list contents from a supported
     * text-based file, replacing the current list.
     */
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
     * Jumps directly to a clicked node in the filmstrip. Since nodes are
     * only reachable by walking next/prev links, this resets to the head
     * and walks forward until the target node is reached (O(n) traversal).
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

    // ---- UI Rendering & Sync ----

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

        // Walk the doubly linked list head-to-tail via next references,
        // building one filmstrip cell per node
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

    /**
     * Enables or disables navigation/editing buttons based on whether the
     * list is empty and whether the current node has a prev/next neighbor.
     */
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

    /**
     * Builds a single filmstrip "frame" cell representing one node in the
     * copy list: index label, content text, and a checkmark overlay if the
     * node has already been copied (used).
     */
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

        // Top sprocket row: decorative holes flanking the frame index label
        Region holeTopL = new Region();
        holeTopL.getStyleClass().add("sprocket-hole");

        Label indexLabel = new Label(String.format("%02d", index));
        indexLabel.getStyleClass().add("frame-index");

        Region holeTopR = new Region();
        holeTopR.getStyleClass().add("sprocket-hole");

        HBox sprocketsTop = new HBox(holeTopL, indexLabel, holeTopR);
        sprocketsTop.getStyleClass().add("sprocket-row");

        // Frame body: content label, with checkmark overlay stacked on top
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

        // Bottom sprocket row: purely decorative holes
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

    /**
     * Computes and applies the horizontal scroll value needed to center
     * the given node within the scroll pane's viewport.
     */
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

    /**
     * Displays a status message for a short duration, then clears it.
     * Restarts the timer if a message is already showing.
     */
    private void showTemporaryStatus(String message) {
        if (statusTimer != null) {
            statusTimer.stop();
        }
        statusLabel.setText(message);
        statusTimer = new PauseTransition(Duration.seconds(2.5));
        statusTimer.setOnFinished(e -> statusLabel.setText(""));
        statusTimer.play();
    }

    /**
     * Shows a styled text input dialog, keeping the OK button disabled
     * while the entered text is empty or whitespace-only.
     */
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
