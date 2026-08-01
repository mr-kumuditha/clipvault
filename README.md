<div align="center">

# 🎞️ ClipCycle

**A Filmstrip-Inspired Desktop Clipboard Manager & Data Structures Demonstration**

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-17.0.13-FF4081?style=for-the-badge&logo=openjfx&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit-5.10-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

</div>

---

## 📌 Project Overview

**ClipCycle** is a specialized desktop clipboard manager built for a university **Data Structures and Algorithms (PDSA)** coursework project.

It organizes related text items into an ordered sequence called a **Copy List**. Users can step forward and backward through the sequence, automatically syncing the active item to the system clipboard, inserting new items anywhere in the sequence, or deleting unwanted items seamlessly.

---

## 💡 Visual Metaphor & Design System

Rather than generic administrative dashboards, **ClipCycle** adopts a physical **filmstrip metaphor**:

- **Filmstrip Reel (`#14171C`)**: Dark background representing a physical film reel container.
- **Frames (`#262B33`)**: Each clipboard item sits in a distinct frame complete with sprocket hole perforations.
- **Active Frame Glow (`#E8A33D`)**: The active node lights up in warm amber, while inactive frames remain dimmed.
- **Projector Advances**: Navigating Next/Previous slides the sequence smoothly into view.

### 🎨 Color Palette

| Swatch | Variable Name | Hex Code | Role |
| :---: | :--- | :--- | :--- |
| ⬛ | `--bg-base` | `#14171C` | Deep reel background |
| 🔲 | `--frame-idle` | `#262B33` | Inactive item frame |
| 🟧 | `--frame-active-glow` | `#E8A33D` | Active item highlight |
| 🩶 | `--slate` | `#4A5568` | Borders & secondary controls |
| ⚪ | `--text-primary` | `#EDEDE3` | Primary item content |
| 🔘 | `--text-muted` | `--8A8F98` | Sequence index & captions |

---

## ⚙️ Hard Technical Constraint: Hand-Written Doubly Linked List

To meet strict academic requirements, **no built-in linked structures** (such as `java.util.LinkedList` or `ArrayDeque`) are used. The core Copy List is implemented entirely from scratch using pointers (`head`, `tail`, `current`, `prev`, `next`).

### 🧠 Data Structure Architecture

```
         [Head]                                                       [Tail]
           │                                                            │
           ▼                                                            ▼
      ┌─────────┐      ┌─────────┐      ┌─────────┐      ┌─────────┐
NULL ◄┤ Node 1  ├─────►│ Node 2  ├─────►│ Node 3  ├─────►│ Node 4  ├► NULL
      │(index 1)│◄─────┤(index 2)│◄─────┤(index 3)│◄─────┤(index 4)│
      └─────────┘      └────┬────┘      └─────────┘      └─────────┘
                            ▲
                            │
                        [Current]
```

### Core Operations & Oral Exam Guide

| Operation | Description | Time Complexity | Pointer Adjustments |
| :--- | :--- | :---: | :--- |
| `addItem(text)` | Appends node to the tail of the list | $\mathcal{O}(1)$ | Updates `tail.next` & new node `prev`, advances `tail` |
| `insertAfterCurrent(text)` | Inserts node immediately after active item | $\mathcal{O}(1)$ | Re-links 4 pointers (`current`, `newNode`, `successor`) |
| `deleteItem()` | Removes active node and reconnects neighbors | $\mathcal{O}(1)$ | Updates `prev.next` & `next.prev`, advances `current` |
| `next()` / `previous()` | Moves active frame forward or backward | $\mathcal{O}(1)$ | Shifts `current = current.next` or `current.prev` |
| `copyCurrent()` | Copies active node text to OS system clipboard | $\mathcal{O}(1)$ | Reads `current.content` and writes to OS clipboard |
| `start()` | Resets active pointer to the head of the list | $\mathcal{O}(1)$ | Sets `current = head` |

---

## 💾 Save File Format

ClipCycle persists Copy Lists to plain text (`.txt`) files without third-party dependencies or external serialization libraries. The first line of the file contains the Copy List's display name. Each subsequent line represents a text item in exact sequence from head to tail. When loaded, the application reconstructs the doubly linked list node by node in file order and resets all items to an unused state (`used = false`).

---

## ⌨️ Keyboard Shortcuts

| Shortcut | Action |
| :--- | :--- |
| `←` (Left Arrow) | Navigate to Previous item |
| `→` (Right Arrow) | Navigate to Next item |
| `Cmd + N` / `Ctrl + N` | Add New Item dialog |
| `Tab` / `Shift + Tab` | Focus cycle through interactive controls |

---

## 📁 Package Architecture

```
clipvault/
├── pom.xml                                  # Maven project descriptor & dependencies
├── LICENSE                                  # MIT open-source license
├── README.md                                # Project documentation
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── module-info.java             # JavaFX module descriptor
    │   │   └── com/clipcycle/
    │   │       ├── App.java                 # Main JavaFX application entry point
    │   │       ├── model/                   # Hand-written DoublyLinkedList & ClipboardNode
    │   │       └── controller/              # JavaFX UI Controller (MainController)
    │   └── resources/
    │       └── com/clipcycle/styles/
    │           └── clipcycle.css            # Filmstrip design system stylesheet
    └── test/
        └── java/com/clipcycle/model/        # JUnit 5 unit tests for data structure correctness (27 tests)
```

---

## 🚀 Quick Start & Running Locally

### Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher
- **Apache Maven**: Version 3.8+

### Environment Setup

Ensure `JAVA_HOME` points to JDK 17+:
```bash
export JAVA_HOME=$(/usr/libexec/java_home)
```

### Build & Execution Commands

```bash
# 1. Clone the repository
git clone https://github.com/mr-kumuditha/clipvault.git
cd clipvault

# 2. Compile the project
mvn clean compile

# 3. Launch the JavaFX desktop application
mvn javafx:run

# 4. Run data structure unit tests
mvn test
```

---

## 🧪 Unit Testing

The data structure logic is covered by 27 isolated **JUnit 5 unit tests** located in `src/test/java/com/clipcycle/model/CopyListTest.java`.

```bash
mvn test
```

All 27 test cases pass with 0 failures, validating edge cases such as empty-list handling, single-item deletion, head/tail deletion, and pointer integrity.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
