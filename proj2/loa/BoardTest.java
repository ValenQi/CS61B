package loa;

import org.junit.Test;
import static org.junit.Assert.*;
import static loa.Piece.*;

/** Junit test for class BOARD.
 *  @author Qi Liu
 */
public class BoardTest {

    /** Test get(), getContents(), col(), row(), set(), clear(),
     *  turn(), inBounds(). */
    @Test
    public void testBasics() {
        Board b = new Board(Board.INITIAL_PIECES, BP);

        assertEquals(BP, b.turn());
        assertEquals(EMP, b.get(5, 5));
        assertEquals(1, Board.col("a2"));
        assertEquals(2, Board.row("a2"));
        assertEquals(WP, b.get("a2"));
        Piece[][] contents = b.getContents();
        assertEquals(EMP, contents[5][5]);

        b.set(5, 5, BP);
        assertEquals(BP, b.get(5, 5));
        b.clear();
        assertEquals(EMP, b.get(5, 5));

        assertTrue(Board.inBounds(3, 7));
        assertFalse(Board.inBounds(2, 9));
    }

    /** Test makeMove(), isLegal(), retract(), movesMade(). */
    @Test
    public void testMove() {
        Board b = new Board(Board.INITIAL_PIECES, BP);
        Move m1 = Move.create(2, 1, 4, 3, b);
        Move m2 = Move.create(2, 1, 3, 2, b);
        Move m3 = Move.create(2, 1, 3, 1, b);
        Move m4 = Move.create(1, 3, 4, 3, b);
        Move m5 = Move.create(1, 7, 3, 7, b);
        Move m6 = Move.create(4, 1, 4, 4, b);

        assertTrue(b.isLegal(m1));
        assertFalse(b.isLegal(m2));
        assertFalse(b.isLegal(m3));

        b.makeMove(m1);
        assertEquals(BP, b.get(4, 3));
        assertEquals(1, b.movesMade());

        assertTrue(b.isLegal(m4));
        assertTrue(b.isLegal(m5));
        b.makeMove(m5);
        assertEquals(WP, b.get(3, 7));

        assertTrue(b.isLegal(m6));

        b.retract();
        assertEquals(EMP, b.get(2, 3));
        assertEquals(1, b.movesMade());
    }

    /** Test gameOver(), piecesContiguous(). */
    @Test
    public void testPiecesContiguous() {
        Piece[][] p = {{ EMP, EMP, EMP, BP,  BP,  EMP, EMP, EMP },
                       { WP,  EMP, EMP, EMP, BP,  EMP, EMP, WP  },
                       { WP,  EMP, EMP, EMP, BP,  EMP, EMP, WP  },
                       { WP,  EMP, EMP, BP,  BP,  EMP, EMP, WP  },
                       { WP,  EMP, EMP, BP,  BP,  EMP, EMP, WP  },
                       { WP,  EMP, EMP, BP,  EMP, EMP, EMP, WP  },
                       { WP,  EMP, BP,  EMP, EMP, EMP, EMP, WP  },
                       { EMP, EMP, BP,  BP,  EMP, EMP, EMP, EMP }};
        Board b = new Board(p, BP);
        assertTrue(b.piecesContiguous(BP));
        assertFalse(b.piecesContiguous(WP));
        assertTrue(b.gameOver());
    }

    /** Test MoveIterator inner class. */
    @Test
    public void testIterator() {
        Piece[][] p = {{ EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
                       { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
                       { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
                       { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
                       { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
                       { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
                       { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
                       { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }};
        Board b = new Board(p, BP);
        int count = 0;
        for (Move m : b) {
            count += 1;
        }
        assertEquals(36, count);
    }

}
