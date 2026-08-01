package com.clipcycle.model;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * A hand-written Doubly Linked List that manages an ordered sequence
 * of {@link ClipboardNode}s — the "Copy List."
 *
 * <p>Three structural pointers are maintained at all times:
 * <ul>
 *   <li><b>head</b> — the first node in the list (head.prev == null)</li>
 *   <li><b>tail</b> — the last  node in the list (tail.next == null)</li>
 *   <li><b>current</b> — the "active" node the user is looking at</li>
 * </ul>
 *
 * <p><b>Academic constraint:</b> no java.util.LinkedList or any built-in
 * linked structure is used.  Every operation directly manipulates
 * node references (prev/next).
 */
public class CopyList {

    private String name;
    private ClipboardNode head;
    private ClipboardNode tail;
    private ClipboardNode current;
    private int size;
    private boolean autoAdvance;

    /**
     * Creates an empty Copy List with the given name.
     * All structural pointers start as null, size starts at 0.
     */
    public CopyList(String name) {
        this.name = name;
        this.head = null;
        this.tail = null;
        this.current = null;
        this.size = 0;
        this.autoAdvance = false;
    }

    // ════════════════════════════════════════════════════════════════
    //  addItem — append a new node at the TAIL of the list
    // ════════════════════════════════════════════════════════════════

    /**
     * Creates a new node for {@code text} and appends it after the
     * current tail.
     *
     * <p><b>Pointer changes:</b>
     * <ul>
     *   <li>If the list is empty → head, tail, and current all point
     *       to the new node.  No prev/next links needed.</li>
     *   <li>Otherwise →
     *       <ol>
     *         <li>newNode.prev = old tail  (link backward)</li>
     *         <li>old tail.next = newNode  (link forward)</li>
     *         <li>tail = newNode           (advance tail pointer)</li>
     *       </ol>
     *       current is NOT moved — the user stays where they are.</li>
     * </ul>
     *
     * @param text the content for the new clipboard item
     */
    public void addItem(String text) {
        ClipboardNode newNode = new ClipboardNode(text);

        if (head == null) {
            // Empty list: this node is everything
            head = newNode;
            tail = newNode;
            current = newNode;
        } else {
            // Append after current tail:
            //   [old tail] ⇄ [newNode]
            newNode.setPrev(tail);   // newNode looks back at old tail
            tail.setNext(newNode);   // old tail looks forward at newNode
            tail = newNode;          // tail pointer advances
        }
        size++;
    }

    // ════════════════════════════════════════════════════════════════
    //  start — reset current to the head of the list
    // ════════════════════════════════════════════════════════════════

    /**
     * Moves the {@code current} pointer back to the head node,
     * beginning a fresh pass through the list.
     *
     * <p><b>Pointer change:</b> current = head.  No node links change.
     *
     * @return the head node's content, or null if the list is empty
     */
    public String start() {
        current = head;
        return (current != null) ? current.getContent() : null;
    }

    // ════════════════════════════════════════════════════════════════
    //  next — advance current one step toward the tail
    // ════════════════════════════════════════════════════════════════

    /**
     * Moves {@code current} to {@code current.next} if a next node
     * exists.  If current is already at the tail (next == null), the
     * pointer does not move.
     *
     * <p><b>Pointer change:</b> current = current.next (or stays put).
     * No node links change.
     *
     * @return the content of the (possibly unchanged) current node,
     *         or null if the list is empty
     */
    public String next() {
        if (current == null) {
            return null;             // list is empty
        }
        if (current.getNext() != null) {
            current = current.getNext();  // advance one frame
        }
        // If already at tail, current stays — returns same content
        return current.getContent();
    }

    // ════════════════════════════════════════════════════════════════
    //  previous — move current one step toward the head
    // ════════════════════════════════════════════════════════════════

    /**
     * Moves {@code current} to {@code current.prev} if a previous
     * node exists.  If current is already at the head (prev == null),
     * the pointer does not move.
     *
     * <p><b>Pointer change:</b> current = current.prev (or stays put).
     * No node links change.
     *
     * @return the content of the (possibly unchanged) current node,
     *         or null if the list is empty
     */
    public String previous() {
        if (current == null) {
            return null;             // list is empty
        }
        if (current.getPrev() != null) {
            current = current.getPrev();  // rewind one frame
        }
        return current.getContent();
    }

    // ════════════════════════════════════════════════════════════════
    //  insertAfterCurrent — splice a new node right after current
    // ════════════════════════════════════════════════════════════════

    /**
     * Creates a new node and inserts it immediately after the current
     * node.  If the list is empty (current == null) this behaves like
     * {@link #addItem(String)}.
     *
     * <p><b>Pointer changes (when current is NOT the tail):</b>
     * <pre>
     *   BEFORE:  ... ⇄ [current] ⇄ [successor] ⇄ ...
     *   AFTER:   ... ⇄ [current] ⇄ [newNode] ⇄ [successor] ⇄ ...
     * </pre>
     * <ol>
     *   <li>newNode.prev  = current          (look back)</li>
     *   <li>newNode.next  = successor        (look forward)</li>
     *   <li>current.next  = newNode          (current re-links forward)</li>
     *   <li>successor.prev = newNode         (successor re-links backward)</li>
     * </ol>
     *
     * <p><b>When current IS the tail:</b> steps 1-3 are the same,
     * but instead of step 4 we set {@code tail = newNode} because
     * there is no successor.
     *
     * <p>{@code current} is NOT moved — it still points at the same
     * node it did before the insert.
     *
     * @param text the content for the new clipboard item
     */
    public void insertAfterCurrent(String text) {
        if (current == null) {
            // List is empty — fall back to addItem
            addItem(text);
            return;
        }

        ClipboardNode newNode = new ClipboardNode(text);
        ClipboardNode successor = current.getNext();

        // Wire newNode into the chain between current and successor
        newNode.setPrev(current);       // newNode looks back at current
        newNode.setNext(successor);     // newNode looks forward at successor
        current.setNext(newNode);       // current now points forward to newNode

        if (successor != null) {
            // Middle insert: successor must look back at newNode
            successor.setPrev(newNode);
        } else {
            // current was the tail, so newNode is the new tail
            tail = newNode;
        }
        size++;
    }

    // ════════════════════════════════════════════════════════════════
    //  deleteItem — remove the current node, reconnect neighbors
    // ════════════════════════════════════════════════════════════════

    /**
     * Removes the current node from the list and reconnects its
     * neighbors so the chain stays intact.  After deletion, current
     * advances to the successor if one exists; otherwise it falls
     * back to the predecessor.
     *
     * <p>Four cases arise depending on where current sits:
     *
     * <h4>Case 1 — Only node (head == current == tail)</h4>
     * <pre>
     *   BEFORE:  head → [X] ← tail,  current → [X]
     *   AFTER:   head = null, tail = null, current = null
     * </pre>
     * No neighbors to reconnect.  List becomes empty.
     *
     * <h4>Case 2 — Deleting the head (current == head, successor exists)</h4>
     * <pre>
     *   BEFORE:  head → [X] ⇄ [B] ⇄ ...
     *   AFTER:   head → [B] ⇄ ...    (B.prev set to null)
     * </pre>
     * <ol>
     *   <li>head = successor             (new first node)</li>
     *   <li>successor.prev = null        (nothing before head)</li>
     *   <li>current = successor          (advance into the list)</li>
     * </ol>
     *
     * <h4>Case 3 — Deleting the tail (current == tail, predecessor exists)</h4>
     * <pre>
     *   BEFORE:  ... ⇄ [A] ⇄ [X] ← tail
     *   AFTER:   ... ⇄ [A] ← tail     (A.next set to null)
     * </pre>
     * <ol>
     *   <li>tail = predecessor           (new last node)</li>
     *   <li>predecessor.next = null      (nothing after tail)</li>
     *   <li>current = predecessor        (fall back — no successor)</li>
     * </ol>
     *
     * <h4>Case 4 — Deleting a middle node</h4>
     * <pre>
     *   BEFORE:  ... ⇄ [A] ⇄ [X] ⇄ [B] ⇄ ...
     *   AFTER:   ... ⇄ [A] ⇄ [B] ⇄ ...
     * </pre>
     * <ol>
     *   <li>predecessor.next = successor (A skips over X forward)</li>
     *   <li>successor.prev = predecessor (B skips over X backward)</li>
     *   <li>current = successor          (advance into the list)</li>
     * </ol>
     *
     * @return the text content of the deleted node, or null if the
     *         list was already empty
     */
    public String deleteItem() {
        if (current == null) {
            return null;  // nothing to delete
        }

        String deletedContent = current.getContent();
        ClipboardNode predecessor = current.getPrev();
        ClipboardNode successor  = current.getNext();

        // ── Case 1: only node ──────────────────────────────────────
        if (predecessor == null && successor == null) {
            head    = null;
            tail    = null;
            current = null;

        // ── Case 2: deleting the head ──────────────────────────────
        } else if (predecessor == null) {
            head = successor;           // successor becomes new head
            successor.setPrev(null);    // nothing before head
            current = successor;        // advance current forward

        // ── Case 3: deleting the tail ──────────────────────────────
        } else if (successor == null) {
            tail = predecessor;         // predecessor becomes new tail
            predecessor.setNext(null);  // nothing after tail
            current = predecessor;      // fall back to predecessor

        // ── Case 4: deleting a middle node ─────────────────────────
        } else {
            predecessor.setNext(successor);  // A skips over X
            successor.setPrev(predecessor);  // B skips over X
            current = successor;             // advance current forward
        }

        size--;
        return deletedContent;
    }

    // ════════════════════════════════════════════════════════════════
    //  copyCurrent — write the active node's text to the OS clipboard
    // ════════════════════════════════════════════════════════════════

    /**
     * Copies the current node's text content to the real system
     * clipboard using java.awt, which writes to the same OS clipboard
     * that JavaFX (and every other application) reads from.
     *
     * <p><b>Pointer change:</b> marks current node as used.  If
     * {@code autoAdvance} is true, also moves current to the next
     * unused node (via {@link #findNextUnused()}).
     *
     * @return {@code true} if text was successfully written to the
     *         clipboard; {@code false} if the list is empty
     *         (current == null) and there was nothing to copy
     */
    public boolean copyCurrent() {
        if (current == null) {
            return false;  // nothing to copy
        }
        // Wrap the text in a StringSelection (a Transferable) and
        // hand it to the system clipboard.  The second argument
        // (ClipboardOwner) is null because we don't need notification
        // when another app overwrites the clipboard.
        StringSelection selection = new StringSelection(current.getContent());
        Toolkit.getDefaultToolkit()
               .getSystemClipboard()
               .setContents(selection, null);
        current.setUsed(true);  // mark this frame as "completed"

        // Auto-advance: skip to the next unused node if enabled.
        // If no unused node remains ahead, current stays put.
        if (autoAdvance) {
            ClipboardNode nextUnused = findNextUnused();
            if (nextUnused != null) {
                current = nextUnused;
            }
        }

        return true;
    }

    // ════════════════════════════════════════════════════════════════
    //  Accessors — used by controllers and tests
    // ════════════════════════════════════════════════════════════════

    /** The user-given name for this Copy List. */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** First node, or null if empty. */
    public ClipboardNode getHead() {
        return head;
    }

    /** Last node, or null if empty. */
    public ClipboardNode getTail() {
        return tail;
    }

    /** The currently active node, or null if empty. */
    public ClipboardNode getCurrent() {
        return current;
    }

    /** Content of the current node, or null if empty. */
    public String getCurrentContent() {
        return (current != null) ? current.getContent() : null;
    }

    /** Number of nodes in the list. */
    public int getSize() {
        return size;
    }

    /** True if the list contains zero nodes. */
    public boolean isEmpty() {
        return size == 0;
    }

    // ── Auto-advance setting ────────────────────────────────────────

    /** True if copyCurrent() should automatically advance to the next unused node. */
    public boolean isAutoAdvance() {
        return autoAdvance;
    }

    public void setAutoAdvance(boolean autoAdvance) {
        this.autoAdvance = autoAdvance;
    }

    // ════════════════════════════════════════════════════════════════
    //  findNextUnused — locate the next un-copied node ahead
    // ════════════════════════════════════════════════════════════════

    /**
     * Walks forward from {@code current.next} and returns the first
     * node whose {@code used} flag is false, or null if every
     * remaining node ahead has already been copied.
     *
     * <p><b>Pointer change:</b> none — this is a read-only scan.
     * The caller decides whether to move {@code current}.
     *
     * @return the next unused node ahead of current, or null
     */
    public ClipboardNode findNextUnused() {
        if (current == null) {
            return null;
        }
        ClipboardNode walker = current.getNext();
        while (walker != null) {
            if (!walker.isUsed()) {
                return walker;  // found an unused frame
            }
            walker = walker.getNext();
        }
        return null;  // everything ahead is already used
    }

    // ════════════════════════════════════════════════════════════════
    //  Progress tracking — how many nodes have been copied
    // ════════════════════════════════════════════════════════════════

    /**
     * Counts how many nodes in the list have their {@code used} flag
     * set to true.  Walks head→tail, O(n).
     *
     * @return the number of nodes that have been copied at least once
     */
    public int getUsedCount() {
        int count = 0;
        ClipboardNode walker = head;
        while (walker != null) {
            if (walker.isUsed()) {
                count++;
            }
            walker = walker.getNext();
        }
        return count;
    }

    /**
     * Returns a human-readable progress string, e.g. "3 of 7 copied".
     *
     * @return progress string showing used count vs total size
     */
    public String getProgress() {
        return getUsedCount() + " of " + size + " copied";
    }

    /**
     * Returns the 1-based position of current by walking from head.
     * Returns 0 if the list is empty.
     *
     * <p>This is O(n) because we traverse from head to current,
     * counting steps.  Acceptable here because the list is short
     * and this is only called for UI display, not inner-loop work.
     */
    public int getCurrentIndex() {
        if (current == null) {
            return 0;
        }
        int index = 1;
        ClipboardNode walker = head;
        while (walker != null && walker != current) {
            walker = walker.getNext();
            index++;
        }
        return index;
    }

    // ════════════════════════════════════════════════════════════════
    //  Persistence — Save & Load plain text files
    // ════════════════════════════════════════════════════════════════

    /**
     * Saves this Copy List to a plain text file.
     * Line 1: Copy List Name
     * Lines 2..N: Node text contents in sequence (head -> tail)
     *
     * @param file target File to write
     * @throws IOException if a file write error occurs
     */
    public void saveToFile(File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
            writer.println(name);
            ClipboardNode walker = head;
            while (walker != null) {
                writer.println(walker.getContent());
                walker = walker.getNext();
            }
        }
    }

    /**
     * Replaces the contents of this Copy List by reading a plain text or RTF file.
     * Handles macOS RTF (.rtf), line endings (\r, \n, \r\n), UTF-8 BOM, and character encoding.
     * Line 1 becomes the list name.
     * Subsequent lines become items added sequentially.
     * All items start fresh with used = false.
     * Sets current pointer to head via start().
     *
     * @param file source File to read
     * @throws IOException if a file read error occurs
     */
    public void loadFromFile(File file) throws IOException {
        String content = null;

        // Try RTF parsing if file ends with .rtf
        if (file.getName().toLowerCase().endsWith(".rtf")) {
            try {
                javax.swing.text.rtf.RTFEditorKit rtf = new javax.swing.text.rtf.RTFEditorKit();
                javax.swing.text.Document doc = rtf.createDefaultDocument();
                try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                    rtf.read(fis, doc, 0);
                    content = doc.getText(0, doc.getLength());
                }
            } catch (Exception ignored) {
                // Fall back to plain text read below if RTF parse fails
            }
        }

        if (content == null) {
            try {
                content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                content = Files.readString(file.toPath(), StandardCharsets.ISO_8859_1);
            }
        }

        // If raw text starts with RTF signature, parse via RTFEditorKit
        if (content.startsWith("{\\rtf")) {
            try {
                javax.swing.text.rtf.RTFEditorKit rtf = new javax.swing.text.rtf.RTFEditorKit();
                javax.swing.text.Document doc = rtf.createDefaultDocument();
                try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                    rtf.read(fis, doc, 0);
                    content = doc.getText(0, doc.getLength());
                }
            } catch (Exception ignored) {
            }
        }

        // Strip UTF-8 Byte Order Mark (BOM) common on macOS / Windows text editors
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }

        // Split by all OS line endings (\r\n, \n, \r)
        String[] lines = content.split("\\r?\\n|\\r");

        this.head = null;
        this.tail = null;
        this.current = null;
        this.size = 0;

        if (lines.length == 0 || (lines.length == 1 && lines[0].trim().isEmpty())) {
            this.name = "Untitled List";
            return;
        }

        String firstLine = lines[0].trim();
        this.name = firstLine.isEmpty() ? "Loaded List" : firstLine;

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            // Ignore single trailing empty line produced by text editors ending with newline
            if (i == lines.length - 1 && line.isEmpty()) {
                continue;
            }
            addItem(line);
        }
        start();
    }
}
