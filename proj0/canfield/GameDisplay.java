package canfield;

import ucb.gui.Pad;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

import javax.imageio.ImageIO;

import java.io.InputStream;
import java.io.IOException;

/** A widget that displays a Pinball playfield.
 *  @author P. N. Hilfinger
 */
class GameDisplay extends Pad {

    /** Color of display field. */
    private static final Color BACKGROUND_COLOR = Color.white;

    /* Coordinates and lengths in pixels unless otherwise stated. */

    /** Preferred dimensions of the playing surface. */
    private static final int BOARD_WIDTH = 800, BOARD_HEIGHT = 600;

    /** Displayed dimensions of a card image. */
    private static final int CARD_HEIGHT = 125, CARD_WIDTH = 90;

    /** Displayed dimensions of the margins between cards,
     *  and between cards and the boarders.*/
    private static final int M_BOARDER = 40, M_VERTICAL = 60, M_HORIZONTAL = 30;

    /** A graphical representation of GAME. */
    public GameDisplay(Game game) {
        _game = game;
        setPreferredSize(BOARD_WIDTH, BOARD_HEIGHT);
    }

    /** Return an Image read from the resource named NAME. */
    private Image getImage(String name) {
        InputStream in =
            getClass().getResourceAsStream("/canfield/resources/" + name);
        try {
            return ImageIO.read(in);
        } catch (IOException excp) {
            return null;
        }
    }

    /** Return an Image of CARD. */
    private Image getCardImage(Card card) {
        return getImage("playing-cards/" + card + ".png");
    }

    /** Return an Image of the back of a card. */
    private Image getBackImage() {
        return getImage("playing-cards/blue-back.png");
    }

    /** Draw CARD at X, Y on G. */
    private void paintCard(Graphics2D g, Card card, int x, int y) {
        if (card != null) {
            g.drawImage(getCardImage(card), x, y,
                        CARD_WIDTH, CARD_HEIGHT, null);
        }
    }

    /** Draw card back at X, Y on G. */
    private void paintBack(Graphics2D g, int x, int y) {
        g.drawImage(getBackImage(), x, y, CARD_WIDTH, CARD_HEIGHT, null);
    }

    /** Draw the reserve pile in G. */
    private void drawReserve(Graphics2D g) {
        paintCard(g, _game.topReserve(), M_BOARDER,
            M_BOARDER + M_VERTICAL + CARD_HEIGHT);
    }

    /** Draw in G the top card of the foundation pile #K if there is one,
     *  or a white card if pile #K is empty. */
    private void drawFoundation(Graphics2D g, int k) {
        if (_game.topFoundation(k) != null) {
            paintCard(g, _game.topFoundation(k),
                M_BOARDER + M_HORIZONTAL * (k + 2) + CARD_WIDTH * (k + 1),
                M_BOARDER);
        } else {
            g.drawImage(getImage("playing-cards/WHITE.png"),
                M_BOARDER + M_HORIZONTAL * (k + 2) + CARD_WIDTH * (k + 1),
                M_BOARDER, CARD_WIDTH, CARD_HEIGHT, null);
        }
    }

    /** Draw in G the stock pile. */
    private void drawStock(Graphics2D g) {
        paintBack(g, M_BOARDER,
            M_BOARDER + M_VERTICAL * 2 + CARD_HEIGHT * 2);
    }

    /** Draw in G the top card of the waste pile if there is one,
     *  or a white card if the waste is empty. */
    private void drawWaste(Graphics2D g) {
        if (_game.topWaste() != null) {
            paintCard(g, _game.topWaste(),
                M_BOARDER + M_HORIZONTAL + CARD_WIDTH,
                M_BOARDER + M_VERTICAL * 2 + CARD_HEIGHT * 2);
        } else {
            g.drawImage(getImage("playing-cards/WHITE.png"),
                M_BOARDER + M_HORIZONTAL + CARD_WIDTH,
                M_BOARDER + M_VERTICAL * 2 + CARD_HEIGHT * 2,
                CARD_WIDTH, CARD_HEIGHT, null);
        }
    }

    /** Draw in G #K tableau pile, with the cards overlapping one another
     *  in order. */
    private void drawTableau(Graphics2D g, int k) {
        int size = _game.tableauSize(k);
        for (int j = 0; j < size; j += 1) {
            paintCard(g, _game.getTableau(k, size - 1 - j),
                M_BOARDER + M_HORIZONTAL * (k + 2) + CARD_WIDTH * (k + 1),
                M_BOARDER + M_VERTICAL + CARD_HEIGHT + CARD_HEIGHT / 4 * j);
        }
    }

    @Override
    public synchronized void paintComponent(Graphics2D g) {
        g.setColor(BACKGROUND_COLOR);
        Rectangle b = g.getClipBounds();
        g.fillRect(0, 0, b.width, b.height);
        drawReserve(g);
        for (int k = 1; k <= Card.NUM_SUITS; k += 1) {
            drawFoundation(g, k);
        }
        drawStock(g);
        drawWaste(g);
        for (int k = 1; k <= _game.TABLEAU_SIZE; k += 1) {
            drawTableau(g, k);
        }
    }

    /** Find the pile that (X, Y) corresponds to,
     *  and return a string that represents the pile. */
    public String findPile(int x, int y) {
        if (between(x, M_BOARDER, M_BOARDER + CARD_WIDTH,
                    y, M_BOARDER + M_VERTICAL + CARD_HEIGHT,
                       M_BOARDER + M_VERTICAL + CARD_HEIGHT * 2)) {
            return "reserve";
        } else if (between(x, M_BOARDER, M_BOARDER + CARD_WIDTH,
                           y, M_BOARDER + M_VERTICAL * 2 + CARD_HEIGHT * 2,
                              M_BOARDER + M_VERTICAL * 2 + CARD_HEIGHT * 3)) {
            return "stock";
        } else if (between(x, M_BOARDER + M_HORIZONTAL + CARD_WIDTH,
                              M_BOARDER + M_HORIZONTAL + CARD_WIDTH * 2,
                           y, M_BOARDER + M_VERTICAL * 2 + CARD_HEIGHT * 2,
                              M_BOARDER + M_VERTICAL * 2 + CARD_HEIGHT * 3)) {
            return "waste";
        } else if (inFoundationPile(x, y, 1)) {
            return "foundation1";
        } else if (inFoundationPile(x, y, 2)) {
            return "foundation2";
        } else if (inFoundationPile(x, y, 3)) {
            return "foundation3";
        } else if (inFoundationPile(x, y, 4)) {
            return "foundation4";
        } else if (inTableauPile(x, y, 1)) {
            return "tableau1";
        } else if (inTableauPile(x, y, 2)) {
            return "tableau2";
        } else if (inTableauPile(x, y, 3)) {
            return "tableau3";
        } else if (inTableauPile(x, y, 4)) {
            return "tableau4";
        } else {
            return "";
        }
    }

    /** Check if (X, Y) lies in the range: ([MINX, MAXX], [MINY, MAXY]),
     *  and return true or false. */
    private boolean between(int x, int minX, int maxX,
                            int y, int minY, int maxY) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    /** Find if (X, Y) lies in #K foundation pile, and return true or false. */
    private boolean inFoundationPile(int x, int y, int k) {
        return between(x, M_BOARDER + M_HORIZONTAL * (k + 2)
                          + CARD_WIDTH * (k + 1),
                          M_BOARDER + M_HORIZONTAL * (k + 2)
                          + CARD_WIDTH * (k + 2),
                       y, M_BOARDER, M_BOARDER + CARD_HEIGHT);
    }

    /** Find if (X, Y) lies in #K tableau pile, and return true or false. */
    private boolean inTableauPile(int x, int y, int k) {
        int size = _game.tableauSize(k);
        int addedHeight = (int) (CARD_HEIGHT / 4 * (size - 1) + 1);
        return between(x, M_BOARDER + M_HORIZONTAL * (k + 2)
                          + CARD_WIDTH * (k + 1),
                          M_BOARDER + M_HORIZONTAL * (k + 2)
                          + CARD_WIDTH * (k + 2),
                       y, M_BOARDER + M_VERTICAL + CARD_HEIGHT,
                          M_BOARDER + M_VERTICAL + CARD_HEIGHT * 2
                          + addedHeight);
    }

    /** Game I am displaying. */
    private final Game _game;

}
