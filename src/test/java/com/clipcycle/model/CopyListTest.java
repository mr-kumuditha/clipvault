package com.clipcycle.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for the hand-written Doubly Linked List ({@link CopyList})
 * and its nodes ({@link ClipboardNode}).
 *
 * <p>Grouped by operation so failures pinpoint exactly which pointer
 * manipulation broke.
 */
class CopyListTest {

    private CopyList list;

    @BeforeEach
    void setUp() {
        list = new CopyList("Test List");
    }

    // ────────────────────────────────────────────────────────────────
    //  addItem
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        @DisplayName("first item becomes head, tail, and current")
        void firstItem_setsHeadTailCurrent() {
            list.addItem("alpha");

            assertEquals(1, list.getSize());
            assertEquals("alpha", list.getHead().getContent());
            assertSame(list.getHead(), list.getTail(),
                    "head and tail must be the same node when size == 1");
            assertSame(list.getHead(), list.getCurrent(),
                    "current must point to the only node");
        }

        @Test
        @DisplayName("adding several items maintains forward/backward order")
        void severalItems_maintainOrder() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");

            assertEquals(3, list.getSize());
            assertEquals("A", list.getHead().getContent());
            assertEquals("C", list.getTail().getContent());

            // Walk forward: A → B → C → null
            ClipboardNode n = list.getHead();
            assertEquals("A", n.getContent());
            n = n.getNext();
            assertEquals("B", n.getContent());
            n = n.getNext();
            assertEquals("C", n.getContent());
            assertNull(n.getNext(), "tail.next must be null");

            // Walk backward: C → B → A → null
            n = list.getTail();
            assertEquals("C", n.getContent());
            n = n.getPrev();
            assertEquals("B", n.getContent());
            n = n.getPrev();
            assertEquals("A", n.getContent());
            assertNull(n.getPrev(), "head.prev must be null");
        }

        @Test
        @DisplayName("head.prev is null, tail.next is null")
        void boundaryPointersAreNull() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");

            assertNull(list.getHead().getPrev());
            assertNull(list.getTail().getNext());
        }

        @Test
        @DisplayName("adding items does not move current away from first node")
        void addDoesNotMoveCurrent() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");

            // current should still be A (set on the first addItem)
            assertEquals("A", list.getCurrentContent());
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  start
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("start")
    class Start {

        @Test
        @DisplayName("resets current to head")
        void resetsCurrentToHead() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start();
            list.next();
            list.next(); // now at C

            String result = list.start();
            assertEquals("A", result);
            assertSame(list.getHead(), list.getCurrent());
        }

        @Test
        @DisplayName("returns null on empty list")
        void emptyList_returnsNull() {
            assertNull(list.start());
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  next
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("next")
    class Next {

        @Test
        @DisplayName("advances current through the list")
        void advancesThroughList() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start();

            assertEquals("B", list.next());
            assertEquals("C", list.next());
        }

        @Test
        @DisplayName("calling next past the tail stays at tail")
        void pastTail_staysAtTail() {
            list.addItem("A");
            list.addItem("B");
            list.start();
            list.next(); // at B (tail)

            String result = list.next(); // should NOT crash, stays at B
            assertEquals("B", result);
            assertSame(list.getTail(), list.getCurrent());

            // Call multiple times to confirm stability
            assertEquals("B", list.next());
            assertEquals("B", list.next());
        }

        @Test
        @DisplayName("returns null on empty list")
        void emptyList_returnsNull() {
            assertNull(list.next());
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  previous
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("previous")
    class Previous {

        @Test
        @DisplayName("moves current backward through the list")
        void movesBackward() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start();
            list.next();
            list.next(); // at C

            assertEquals("B", list.previous());
            assertEquals("A", list.previous());
        }

        @Test
        @DisplayName("calling previous past the head stays at head")
        void pastHead_staysAtHead() {
            list.addItem("A");
            list.addItem("B");
            list.start(); // at A (head)

            String result = list.previous(); // should stay at A
            assertEquals("A", result);
            assertSame(list.getHead(), list.getCurrent());

            // Confirm stability
            assertEquals("A", list.previous());
            assertEquals("A", list.previous());
        }

        @Test
        @DisplayName("returns null on empty list")
        void emptyList_returnsNull() {
            assertNull(list.previous());
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  insertAfterCurrent
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("insertAfterCurrent")
    class InsertAfterCurrent {

        @Test
        @DisplayName("inserts in the middle and re-links four pointers")
        void middleInsert_relinks() {
            list.addItem("A");
            list.addItem("C");
            list.start(); // current = A

            list.insertAfterCurrent("B"); // chain: A ⇄ B ⇄ C

            assertEquals(3, list.getSize());

            // Forward walk
            assertEquals("A", list.getHead().getContent());
            assertEquals("B", list.getHead().getNext().getContent());
            assertEquals("C", list.getHead().getNext().getNext().getContent());
            assertNull(list.getTail().getNext());

            // Backward walk
            assertEquals("C", list.getTail().getContent());
            assertEquals("B", list.getTail().getPrev().getContent());
            assertEquals("A", list.getTail().getPrev().getPrev().getContent());
            assertNull(list.getHead().getPrev());

            // current should still be A (not moved by insert)
            assertEquals("A", list.getCurrentContent());
        }

        @Test
        @DisplayName("inserting when current is the tail makes new tail")
        void atTail_becomesNewTail() {
            list.addItem("A");
            list.addItem("B");
            list.start();
            list.next(); // current = B (tail)

            list.insertAfterCurrent("C"); // chain: A ⇄ B ⇄ C

            assertEquals(3, list.getSize());
            assertEquals("C", list.getTail().getContent());
            assertNull(list.getTail().getNext());
            assertEquals("B", list.getTail().getPrev().getContent());
            // current is still B
            assertEquals("B", list.getCurrentContent());
        }

        @Test
        @DisplayName("inserting into empty list (current null) adds as first node")
        void emptyList_fallsBackToAdd() {
            list.insertAfterCurrent("solo");

            assertEquals(1, list.getSize());
            assertEquals("solo", list.getHead().getContent());
            assertSame(list.getHead(), list.getTail());
            assertSame(list.getHead(), list.getCurrent());
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  deleteItem
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteItem")
    class DeleteItem {

        @Test
        @DisplayName("deleting the HEAD: second node becomes new head")
        void deleteHead() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start(); // current = A (head)

            String deleted = list.deleteItem();

            assertEquals("A", deleted);
            assertEquals(2, list.getSize());

            // B is now head
            assertEquals("B", list.getHead().getContent());
            assertNull(list.getHead().getPrev(),
                    "new head.prev must be null");
            assertSame(list.getHead(), list.getCurrent(),
                    "current should advance to the new head");

            // Chain integrity: B ⇄ C
            assertEquals("C", list.getHead().getNext().getContent());
            assertSame(list.getHead(), list.getTail().getPrev());
        }

        @Test
        @DisplayName("deleting the TAIL: predecessor becomes new tail")
        void deleteTail() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start();
            list.next();
            list.next(); // current = C (tail)

            String deleted = list.deleteItem();

            assertEquals("C", deleted);
            assertEquals(2, list.getSize());

            // B is now tail
            assertEquals("B", list.getTail().getContent());
            assertNull(list.getTail().getNext(),
                    "new tail.next must be null");
            assertSame(list.getTail(), list.getCurrent(),
                    "current should fall back to predecessor");

            // Chain integrity: A ⇄ B
            assertEquals("A", list.getTail().getPrev().getContent());
            assertSame(list.getTail(), list.getHead().getNext());
        }

        @Test
        @DisplayName("deleting a MIDDLE node: neighbors reconnect")
        void deleteMiddle() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start();
            list.next(); // current = B (middle)

            String deleted = list.deleteItem();

            assertEquals("B", deleted);
            assertEquals(2, list.getSize());

            // A and C are now directly linked
            assertSame(list.getHead().getNext(), list.getTail(),
                    "head.next should be tail after middle delete");
            assertSame(list.getTail().getPrev(), list.getHead(),
                    "tail.prev should be head after middle delete");

            // current advanced to successor (C)
            assertEquals("C", list.getCurrentContent());
        }

        @Test
        @DisplayName("deleting the ONLY node: list becomes empty")
        void deleteOnlyNode() {
            list.addItem("solo");
            list.start();

            String deleted = list.deleteItem();

            assertEquals("solo", deleted);
            assertEquals(0, list.getSize());
            assertTrue(list.isEmpty());
            assertNull(list.getHead(), "head must be null");
            assertNull(list.getTail(), "tail must be null");
            assertNull(list.getCurrent(), "current must be null");
        }

        @Test
        @DisplayName("delete on empty list returns null safely")
        void emptyList_returnsNull() {
            assertNull(list.deleteItem());
            assertEquals(0, list.getSize());
        }

        @Test
        @DisplayName("multiple deletes until empty, then list is stable")
        void multipleDeletesUntilEmpty() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start();

            list.deleteItem(); // delete A, current → B
            assertEquals("B", list.getCurrentContent());
            assertEquals(2, list.getSize());

            list.deleteItem(); // delete B, current → C
            assertEquals("C", list.getCurrentContent());
            assertEquals(1, list.getSize());

            list.deleteItem(); // delete C, list empty
            assertNull(list.getCurrentContent());
            assertEquals(0, list.getSize());

            // Should not crash on further deletes
            assertNull(list.deleteItem());
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  getCurrentIndex
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getCurrentIndex")
    class GetCurrentIndex {

        @Test
        @DisplayName("tracks 1-based position as current moves")
        void tracksPosition() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start();

            assertEquals(1, list.getCurrentIndex());
            list.next();
            assertEquals(2, list.getCurrentIndex());
            list.next();
            assertEquals(3, list.getCurrentIndex());
            list.previous();
            assertEquals(2, list.getCurrentIndex());
        }

        @Test
        @DisplayName("returns 0 for empty list")
        void emptyList_returnsZero() {
            assertEquals(0, list.getCurrentIndex());
        }

        @Test
        @DisplayName("updates after delete")
        void updatesAfterDelete() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start();
            list.next(); // at B, index 2

            list.deleteItem(); // B removed, current → C, now index 2 of 2
            assertEquals(2, list.getCurrentIndex());
            assertEquals(2, list.getSize());
        }
    }

    // ────────────────────────────────────────────────────────────────────
    //  Progress Tracking
    // ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Progress Tracking")
    class ProgressTracking {

        @Test
        @DisplayName("node used flag defaults to false")
        void usedDefaultsFalse() {
            list.addItem("A");
            assertFalse(list.getHead().isUsed());
        }

        @Test
        @DisplayName("copyCurrent sets the used flag on the active node")
        void copyCurrent_setsUsedFlag() {
            list.addItem("A");
            list.addItem("B");
            list.start();

            assertFalse(list.getCurrent().isUsed());
            list.copyCurrent();
            assertTrue(list.getCurrent().isUsed(),
                    "current node should be marked as used after copyCurrent()");

            // B should still be unused
            list.next();
            assertFalse(list.getCurrent().isUsed(),
                    "non-copied node should remain unused");
        }

        @Test
        @DisplayName("getUsedCount returns correct count after partial copies")
        void getUsedCount_partialCopies() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start();

            assertEquals(0, list.getUsedCount());

            list.copyCurrent(); // copy A
            assertEquals(1, list.getUsedCount());

            list.next();
            list.copyCurrent(); // copy B
            assertEquals(2, list.getUsedCount());

            // C is unused, total is 3
            assertEquals(3, list.getSize());
        }

        @Test
        @DisplayName("getProgress returns human-readable string")
        void getProgress_returnsString() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start();

            assertEquals("0 of 3 copied", list.getProgress());

            list.copyCurrent();
            assertEquals("1 of 3 copied", list.getProgress());

            list.next();
            list.next();
            list.copyCurrent();
            assertEquals("2 of 3 copied", list.getProgress());
        }

        @Test
        @DisplayName("copying same node twice does not double-count")
        void copySameNodeTwice_noDoubleCount() {
            list.addItem("A");
            list.start();

            list.copyCurrent();
            list.copyCurrent(); // copy same node again

            assertEquals(1, list.getUsedCount());
            assertEquals("1 of 1 copied", list.getProgress());
        }
    }

    // ────────────────────────────────────────────────────────────────────
    //  Auto-Advance
    // ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Auto-Advance")
    class AutoAdvance {

        @Test
        @DisplayName("autoAdvance is off by default")
        void offByDefault() {
            assertFalse(list.isAutoAdvance());
        }

        @Test
        @DisplayName("copyCurrent when autoAdvance is off does not change current pointer")
        void autoAdvanceOff_currentStaysPut() {
            list.addItem("A");
            list.addItem("B");
            list.start(); // current = A

            list.copyCurrent();
            assertEquals("A", list.getCurrentContent());
        }

        @Test
        @DisplayName("copyCurrent when autoAdvance is on moves current to next unused node")
        void autoAdvanceOn_advancesToNextUnused() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.setAutoAdvance(true);
            list.start(); // current = A

            list.copyCurrent(); // copies A, auto-advances to B
            assertEquals("B", list.getCurrentContent());

            list.copyCurrent(); // copies B, auto-advances to C
            assertEquals("C", list.getCurrentContent());
        }

        @Test
        @DisplayName("findNextUnused skips already used nodes ahead")
        void findNextUnused_skipsUsedNodes() {
            list.addItem("A");
            list.addItem("B");
            list.addItem("C");
            list.start(); // current = A

            // Mark B as used manually
            list.getHead().getNext().setUsed(true);

            ClipboardNode nextUnused = list.findNextUnused();
            assertNotNull(nextUnused);
            assertEquals("C", nextUnused.getContent());
        }

        @Test
        @DisplayName("copyCurrent when all remaining items are used leaves current unchanged without error")
        void allRemainingUsed_leavesCurrentInPlace() {
            list.addItem("A");
            list.addItem("B");
            list.setAutoAdvance(true);
            list.start();

            // Mark B as used
            list.getTail().setUsed(true);

            list.copyCurrent(); // copies A, finds no unused nodes ahead
            assertEquals("A", list.getCurrentContent());
        }
    }

    // ────────────────────────────────────────────────────────────────────
    //  Save and Load
    // ────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Save and Load")
    class SaveAndLoad {

        @Test
        @DisplayName("saveToFile and loadFromFile persist items in order and reset used flags")
        void saveAndLoad_persistsOrderAndResetsUsed() throws IOException {
            list.setName("Project Tasks");
            list.addItem("Task 1");
            list.addItem("Task 2");
            list.addItem("Task 3");
            list.start();

            // Copy Task 1 so it becomes used
            list.copyCurrent();
            assertTrue(list.getHead().isUsed());

            File tempFile = File.createTempFile("clipcycle_test", ".txt");
            tempFile.deleteOnExit();

            // Save list to temp file
            list.saveToFile(tempFile);
            assertTrue(tempFile.length() > 0, "Saved file should not be empty");

            // Load into a new CopyList instance
            CopyList newList = new CopyList("Empty");
            newList.loadFromFile(tempFile);

            assertEquals("Project Tasks", newList.getName());
            assertEquals(3, newList.getSize());

            // Check item content and order
            ClipboardNode n = newList.getHead();
            assertEquals("Task 1", n.getContent());
            assertFalse(n.isUsed(), "Loaded items must reset used flag to false");

            n = n.getNext();
            assertEquals("Task 2", n.getContent());
            assertFalse(n.isUsed());

            n = n.getNext();
            assertEquals("Task 3", n.getContent());
            assertFalse(n.isUsed());

            assertNull(n.getNext());

            // Check current pointer is reset to head
            assertEquals("Task 1", newList.getCurrentContent());
        }

        @Test
        @DisplayName("loadFromFile parses .rtf files created by macOS TextEdit")
        void rtfFile_loadsPlainText() throws IOException {
            File tempRtf = File.createTempFile("clipcycle_mac", ".rtf");
            tempRtf.deleteOnExit();

            String rtfContent = "{\\rtf1\\ansi\\deff0{\\fonttbl{\\f0 Arial;}}\n" +
                                "\\f0\\fs24 Mac List Title\\par\n" +
                                "First Item\\par\n" +
                                "Second Item\\par\n" +
                                "}";
            java.nio.file.Files.writeString(tempRtf.toPath(), rtfContent);

            CopyList rtfList = new CopyList("Untitled");
            rtfList.loadFromFile(tempRtf);

            assertEquals("Mac List Title", rtfList.getName());
            assertEquals(2, rtfList.getSize());
            assertEquals("First Item", rtfList.getHead().getContent());
            assertEquals("Second Item", rtfList.getTail().getContent());
        }
    }

    // ────────────────────────────────────────────────────────────────
    //  ClipboardNode direct tests
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("ClipboardNode")
    class NodeTests {

        @Test
        @DisplayName("new node has null prev and next")
        void newNode_unlinked() {
            ClipboardNode node = new ClipboardNode("test");
            assertEquals("test", node.getContent());
            assertNull(node.getPrev());
            assertNull(node.getNext());
        }

        @Test
        @DisplayName("new node has used == false")
        void newNode_unusedByDefault() {
            ClipboardNode node = new ClipboardNode("test");
            assertFalse(node.isUsed());
        }

        @Test
        @DisplayName("setContent updates the text")
        void setContent_updates() {
            ClipboardNode node = new ClipboardNode("old");
            node.setContent("new");
            assertEquals("new", node.getContent());
        }

        @Test
        @DisplayName("toString includes content")
        void toString_includesContent() {
            ClipboardNode node = new ClipboardNode("hello");
            assertTrue(node.toString().contains("hello"));
        }
    }
}
