package canfield;

import static org.junit.Assert.*;
import org.junit.Test;

/** Tests of the Game class.
 *  @author Qi Liu
 */

public class GameTest {

    /** Example. */
    @Test
    public void testInitialScore() {
        Game g = new Game();
        g.deal();
        assertEquals(5, g.getScore());
    }

    @Test
    public void testUndo() {
        Game g1 = new Game();
        g1.deal();
        Game g2 = new Game(g1);
        g1.stockToWaste();
        g1.undo();

        assertEquals(g1.topWaste(), g2.topWaste());
        assertEquals(g1.topReserve(), g2.topReserve());
        for (int i = 1; i <= g1.TABLEAU_SIZE; i += 1) {
            assertEquals(g1.topTableau(i), g2.topTableau(i));
        }
        for (int i = 1; i <= Card.NUM_SUITS; i += 1) {
            assertEquals(g1.topFoundation(i), g2.topFoundation(i));
        }
    }

}
