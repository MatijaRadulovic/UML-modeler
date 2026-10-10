# ClassyCrafT

Desktop app for building UML **class diagrams**. Java 17 + Swing.

Made as a team project at the Faculty of Computer Science (RAF) for the Software Design course.

**Authors:** Matija Radulovic (RN 134/23), Jovana Lazic (RN 24/22)

## Features

- Project tree (left panel): **Project → Package → Diagram**, with add / rename / delete
- Diagram elements: **class, interface, enum**, each with attributes, methods and visibility
- Connections: **dependency, aggregation, composition, generalization**
- Canvas tools: select, move, zoom, edit content, delete, duplicate, fit to view
- **Undo / redo** (command pattern)
- **Save / load projects** as JSON
- **Diagram templates**: save a diagram to a file and load it into another one
- **Export** the active diagram as a PNG
- Project author editing, "About us" dialog, error popups + console/file logging

## Requirements

- JDK 17+ (tested compiling on 21). A full JDK is needed, not just a JRE.
- Maven

## Build & run

```bash
cd ClassyCrafT
mvn compile
mvn exec:java -Dexec.mainClass=raf.dsw.classycraft.app.AppCore
```

Or open `ClassyCrafT/` in IntelliJ and run `raf.dsw.classycraft.app.AppCore`.

## Usage

1. Use the toolbar / **Add** menu to create a project, then a package, then a diagram in the tree.
2. Open the diagram in a tab.
3. Pick a tool from the side toolbar (class / interface / enum / connection) and click on the canvas.
   For connections, click the source element, then the target.
4. Save with the toolbar's save button (select the project in the tree first).

## Architecture

Package root: `ClassyCrafT/src/main/java/raf/dsw/classycraft/app/`

| Package | What's in it |
|---|---|
| `controller/` | Swing `Action`s (`ActionManager` holds them all) and `events/` |
| `core/treemodel/` | Model: composite tree (`ClassyNode`), nodes (`Project`, `Package`, `Diagram`, `ClassyClass`, connections...), factories |
| `core/state/` | **State** pattern: one state per canvas tool (`ClassState`, `MoveState`, `ZoomState`...), driven by `StateManager` |
| `core/msggen/` | Publisher/subscriber message generator + console/file loggers |
| `core/serializer/` | `JacksonUtil` – JSON save/load |
| `gui/swing/main/` | `MainFrame`, menu/toolbars, **Command** classes + `CommandManager` for undo/redo |
| `gui/swing/main/pkgview/` | Diagram tabs, `DiagramView` (canvas), `DiagramViewModel` |
| `gui/swing/painters/` | One painter per element type, created by `PainterSimpleFactory` |
| `gui/swing/treeview/` | Project tree view/controller |
| `gui/swing/popups/` | Dialogs (edit class content, about us) |

Patterns used: Composite, State, Command, Observer, Factory.

Flow of adding a class (click on canvas → `DiagramView` → `StateManager` → `ClassState` → `ClassAddCommand` → `CommandManager` → painter added and repainted):
see [`uml/dijagram_toka.png`](uml/dijagram_toka.png).

## Other files

- `specifikacija.tex` / `specifikacija1_tim_...pdf` – project specification (Serbian): requirements, competitor comparison, use cases
- `use_case_dijgram.png` – use case diagram
- `uml/` – class diagram of the app (`kt1_uml.png`, `stara verzija.png` = older version) and the flow diagram
- `ClassyCrafT/templates/` – sample diagram template files (JSON)

## Known issues

- `ExportAction` renders to a fixed 800×800 image and throws if the save dialog is cancelled (`File` is null).
- `ExportAction`/`SaveAction` import `com.sun.tools.javac.Main` (unused) – this is why a full JDK is needed.
- `lombok` version is `RELEASE` in `pom.xml`; pin it for reproducible builds.
- `FileLogger` writes to `target/classes/logs/log.txt`, so logs vanish on clean.
- `target/` is committed to git; should be in `.gitignore`.
