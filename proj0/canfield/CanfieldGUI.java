package canfield;

import ucb.gui.TopLevel;
import ucb.gui.LayoutSpec;

import java.awt.event.MouseEvent;

/** A top-level GUI for Canfield solitaire.
 *  @author Qi Liu
 */
class CanfieldGUI extends TopLevel {

    /** A new window with given TITLE and displaying GAME. */
    CanfieldGUI(String title, Game game) {
        super(title, true);
        _game = game;
        addMenuButton("Game->NewGame", "newGame");
        addMenuButton("Game->Undo", "undo");
        addMenuButton("Game->Quit", "quit");
        _display = new GameDisplay(game);
        add(_display, new LayoutSpec("y", 2, "width", 2));
        _display.setMouseHandler("click", this, "mouseClicked");
        _display.setMouseHandler("release", this, "mouseReleased");
        _display.setMouseHandler("drag", this, "mouseDragged");
        display(true);
    }

    /** Respond to "Quit" menu item. */
    public void quit(String dummy) {
        if (showOptions("Really quit?", "Quit?", "question",
                        "Yes", "Yes", "No") == 0) {
            System.exit(1);
        }
    }

    /** Respond to "Undo" menu item. */
    public void undo(String dummy) {
        _game.undo();
        _display.repaint();
    }

    /** Respond to "NewGame" menu item. */
    public void newGame(String dummy) {
        _game.deal();
        _display.repaint();
    }

    /** Action in response to mouse-clicking event EVENT. */
    public synchronized void mouseClicked(MouseEvent event) {
        int x = event.getX(), y = event.getY();

        if (_display.findPile(x, y).equals("stock")) {
            _game.stockToWaste();
        }

        _display.repaint();
    }

    /** Action in response to mouse-released event EVENT. */
    public synchronized void mouseReleased(MouseEvent event) {
        int x = event.getX(), y = event.getY();
        String releasedAtPile = _display.findPile(x, y);

        if (releasedAtPile.startsWith("foundation")) {
            if (_pressedPile.equals("waste")) {
                _game.wasteToFoundation();
            } else if (_pressedPile.equals("reserve")) {
                _game.reserveToFoundation();
            } else if (_pressedPile.startsWith("tableau")) {
                int k = Integer.parseInt(_pressedPile.substring(7));
                _game.tableauToFoundation(k);
            }
        } else if (releasedAtPile.startsWith("tableau")) {
            int k = Integer.parseInt(releasedAtPile.substring(7));
            if (_pressedPile.equals("waste")) {
                _game.wasteToTableau(k);
            } else if (_pressedPile.equals("reserve")) {
                _game.reserveToTableau(k);
            }  else if (_pressedPile.startsWith("foundation")) {
                int j = Integer.parseInt(_pressedPile.substring(10));
                _game.foundationToTableau(j, k);
            } else if (_pressedPile.startsWith("tableau")) {
                int j = Integer.parseInt(_pressedPile.substring(7));
                _game.tableauToTableau(j, k);
            }
        }
        if (_game.isWon()) {
            if (showOptions("You won! Another game?", "Congratulations!",
                            "question", "Yes", "Yes", "No") == 0) {
                _game.deal();
                _display.repaint();
            }
        }

        _pressedPile = null;
        _display.repaint();
    }

    /** Action in response to mouse-dragging event EVENT. */
    public synchronized void mouseDragged(MouseEvent event) {
        int x = event.getX(), y = event.getY();

        if (_pressedPile == null) {
            _pressedPile = _display.findPile(x, y);
        }
    }

    /** A string representation of the pile being pressed. */
    private String _pressedPile;

    /** The board widget. */
    private final GameDisplay _display;

    /** The game I am consulting. */
    private final Game _game;

}
