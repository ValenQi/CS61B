package loa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Formatter;
import java.util.NoSuchElementException;

import java.util.regex.Pattern;

import static loa.Piece.*;
import static loa.Direction.*;

/** Represents the state of a game of Lines of Action.
 *  @author Qi Liu
 */
class Board implements Iterable<Move> {

    /** Size of a board. */
    static final int M = 8;

    /** Pattern describing a valid square designator (cr). */
    static final Pattern ROW_COL = Pattern.compile("^[a-h][1-8]$");

    /** A Board whose initial contents are taken from INITIALCONTENTS
     *  and in which the player playing TURN is to move. The resulting
     *  Board has
     *        get(col, row) == INITIALCONTENTS[row-1][col-1]
     *  Assumes that PLAYER is not null and INITIALCONTENTS is MxM.
     *
     *  CAUTION: The natural written notation for arrays initializers puts
     *  the BOTTOM row of INITIALCONTENTS at the top.
     */
    Board(Piece[][] initialContents, Piece turn) {
        initialize(initialContents, turn);
    }

    /** A new board in the standard initial position. */
    Board() {
        clear();
    }

    /** A Board whose initial contents and state are copied from
     *  BOARD. */
    Board(Board board) {
        copyFrom(board);
    }

    /** Set my state to CONTENTS with SIDE to move. */
    void initialize(Piece[][] contents, Piece side) {
        _moves.clear();
        for (int r = 1; r <= M; r += 1) {
            for (int c = 1; c <= M; c += 1) {
                set(c, r, contents[r - 1][c - 1]);
            }
        }
        _turn = side;
    }

    /** Set me to the initial configuration. */
    void clear() {
        initialize(INITIAL_PIECES, BP);
    }

    /** Set my state to a copy of BOARD. */
    void copyFrom(Board board) {
        if (board == this) {
            return;
        }
        _moves.clear();
        _moves.addAll(board._moves);
        initialize(board._pieces, board._turn);
    }

    /** Return the contents of the board. */
    Piece[][] getContents() {
        return _pieces;
    }

    /** Return THIS board. */
    Board getBoard() {
        return this;
    }

    /** Return the contents of column C, row R, where 1 <= C,R <= 8,
     *  where column 1 corresponds to column 'a' in the standard
     *  notation. */
    Piece get(int c, int r) {
        return _pieces[c - 1][r - 1];
    }

    /** Return the contents of the square SQ.  SQ must be the
     *  standard printed designation of a square (having the form cr,
     *  where c is a letter from a-h and r is a digit from 1-8). */
    Piece get(String sq) {
        return get(col(sq), row(sq));
    }

    /** Return the column number (a value in the range 1-8) for SQ.
     *  SQ is as for {@link get(String)}. */
    static int col(String sq) {
        if (!ROW_COL.matcher(sq).matches()) {
            throw new IllegalArgumentException("bad square designator");
        }
        return sq.charAt(0) - 'a' + 1;
    }

    /** Return the row number (a value in the range 1-8) for SQ.
     *  SQ is as for {@link get(String)}. */
    static int row(String sq) {
        if (!ROW_COL.matcher(sq).matches()) {
            throw new IllegalArgumentException("bad square designator");
        }
        return sq.charAt(1) - '0';
    }

    /** Set the square at column C, row R to V, and make NEXT the next side
     *  to move, if it is not null. */
    void set(int c, int r, Piece v, Piece next) {
        _pieces[c - 1][r - 1] = v;
        if (next != null) {
            _turn = next;
        }
    }

    /** Set the square at column C, row R to V. */
    void set(int c, int r, Piece v) {
        set(c, r, v, null);
    }

    /** Assuming isLegal(MOVE), make MOVE. */
    void makeMove(Move move) {
        assert isLegal(move);
        _moves.add(move);
        Piece replaced = move.replacedPiece();
        Piece moved = move.movedPiece();
        int c0 = move.getCol0(), c1 = move.getCol1();
        int r0 = move.getRow0(), r1 = move.getRow1();
        if (replaced != EMP) {
            set(c1, r1, EMP);
        }
        set(c1, r1, moved);
        set(c0, r0, EMP);
        _turn = _turn.opposite();
    }

    /** Retract (unmake) one move, returning to the state immediately before
     *  that move.  Requires that movesMade () > 0. */
    void retract() {
        assert movesMade() > 0;
        Move move = _moves.remove(_moves.size() - 1);
        Piece replaced = move.replacedPiece();
        Piece moved = move.movedPiece();
        int c0 = move.getCol0(), c1 = move.getCol1();
        int r0 = move.getRow0(), r1 = move.getRow1();
        set(c1, r1, replaced);
        set(c0, r0, moved);
        _turn = _turn.opposite();
    }

    /** Return the Piece representing who is next to move. */
    Piece turn() {
        return _turn;
    }

    /** Return true IFF (C, R) denotes a square on the board, that is if
     *  1 <= C <= M, 1 <= R <= M. */
    static boolean inBounds(int c, int r) {
        return 1 <= c && c <= M && 1 <= r && r <= M;
    }

    /** Return true iff MOVE is legal for the player currently on move. */
    boolean isLegal(Move move) {
        return move == null ? false
               : (move.movedPiece() == _turn
                  && inBounds(move.getCol1(), move.getRow1())
                  && !blocked(move)
                  && move.length() == pieceCountAlong(move));
    }

    /** Return a sequence of all legal moves from this position. */
    Iterator<Move> legalMoves() {
        return new MoveIterator();
    }

    @Override
    public Iterator<Move> iterator() {
        return legalMoves();
    }

    /** Return true if there is at least one legal move for the player
     *  on move. */
    public boolean isLegalMove() {
        return iterator().hasNext();
    }

    /** Return true iff either player has all his pieces continguous. */
    boolean gameOver() {
        return piecesContiguous(BP) || piecesContiguous(WP);
    }

    /** Return true iff SIDE's pieces are continguous. */
    boolean piecesContiguous(Piece side) {
        int total = totalPieces(side);
        int[] p = findSidePiece(side);
        boolean[][] marked = new boolean[M][M];
        int totalContiguous = helperContiguous(side, p[0], p[1], marked);
        return total == totalContiguous;
    }

    /** Return the total number of SIDE's pieces on THIS.*/
    private int totalPieces(Piece side) {
        int total = 0;
        for (int c = 1; c <= M; c += 1) {
            for (int r = 1; r <= M; r += 1) {
                if (get(c, r) == side) {
                    total += 1;
                }
            }
        }
        return total;
    }

    /** Find a piece on SIDE and return its position in an array of ints,
     *  which stores the colomn of this piece at position 0,
     *  and the row of this piece at position 1. */
    private int[] findSidePiece(Piece side) {
        int[] position = new int[2];
        for (int c = 1; c <= M; c += 1) {
            for (int r = 1; r <= M; r += 1) {
                if (get(c, r) == side) {
                    position[0] = c;
                    position[1] = r;
                    return position;
                }
            }
        }
        throw new NoSuchElementException("There is no" + side.fullName()
                                         + "piece on the board");
    }

    /** Find the number of contiguous pieces on SIDE starting from (C0, R0).
     *  MARKED keeps track of whether a piece has been counted or not.
     *  Return the number. */
    private int helperContiguous(Piece side, int c0, int r0,
                                 boolean[][] marked) {
        marked[c0 - 1][r0 - 1] = true;
        int total = 1;
        for (int dc = -1; dc <= 1; dc += 1) {
            for (int dr = -1; dr <= 1; dr += 1) {
                int c1 = c0 + dc;
                int r1 = r0 + dr;
                if (inBounds(c1, r1) && _pieces[c1 - 1][r1 - 1] == side
                    && !marked[c1 - 1][r1 - 1]) {
                    total += helperContiguous(side, c1, r1, marked);
                }
            }
        }
        return total;
    }

    /** Return the total number of moves that have been made (and not
     *  retracted).  Each valid call to makeMove with a normal move increases
     *  this number by 1. */
    int movesMade() {
        return _moves.size();
    }

    /** Checks each square of OBJ and this to see if pieces are the same. */
    @Override
    public boolean equals(Object obj) {
        Board b = (Board) obj;
        if (!_turn.equals(b.turn())) {
            return false;
        }
        for (int r = 1; r <= M; r += 1) {
            for (int c = 1; c <= M; c += 1) {
                if (!get(c, r).equals(b.get(c, r))) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Empty squares are valued at 1.
     *  Black pieces are valued at 2.
     *  White pieces are valued at 3. */
    @Override
    public int hashCode() {
        int hashCode = 0;
        if (_turn.equals(BP)) {
            hashCode = 1;
        } else if (_turn.equals(WP)) {
            hashCode = 2;
        }
        double currSquare = 1;
        double base = 5;
        for (int r = 1; r <= M; r += 1) {
            for (int c = 1; c <= M; c += 1) {
                if (get(c, r).equals(EMP)) {
                    hashCode += 1 * Math.pow(base, currSquare);
                } else if (get(c, r).equals(BP)) {
                    hashCode += 2 * Math.pow(base, currSquare);
                } else if (get(c, r).equals(WP)) {
                    hashCode += 3 * Math.pow(base, currSquare);
                }
                currSquare += 1;
            }
        }
        return hashCode;
    }

    @Override
    public String toString() {
        Formatter out = new Formatter();
        out.format("===%n");
        for (int r = M; r >= 1; r -= 1) {
            out.format("    ");
            for (int c = 1; c <= M; c += 1) {
                out.format("%s ", get(c, r).abbrev());
            }
            out.format("%n");
        }
        out.format("Next move: %s%n===", turn().fullName());
        return out.toString();
    }

    /** Return the number of pieces in the line of action indicated by MOVE. */
    private int pieceCountAlong(Move move) {
        return pieceCountAlong(move.getCol0(), move.getRow0(),
                               move.direction());
    }

    /** Return the number of pieces in the line of action in direction DIR and
     *  containing the square at column C and row R. */
    private int pieceCountAlong(int c, int r, Direction dir) {
        int count = 0;
        int c0 = c + dir.dc;
        int r0 = r + dir.dr;
        while (inBounds(c0, r0)) {
            if (get(c0, r0) != EMP) {
                count += 1;
            }
            c0 += dir.dc;
            r0 += dir.dr;
        }
        c0 = c - dir.dc;
        r0 = r - dir.dr;
        while (inBounds(c0, r0)) {
            if (get(c0, r0) != EMP) {
                count += 1;
            }
            c0 -= dir.dc;
            r0 -= dir.dr;
        }
        if (get(c, r) == null) {
            return count;
        } else {
            return count + 1;
        }
    }

    /** Return true iff MOVE is blocked by an opposing piece or by a
     *  friendly piece on the target square. */
    private boolean blocked(Move move) {
        Piece target = move.replacedPiece();
        Piece start = move.movedPiece();
        int c0 = move.getCol0(), r0 = move.getRow0();
        int c1 = move.getCol1(), r1 = move.getRow1();
        Direction dir = move.direction();
        if (target == start) {
            return true;
        }
        while (c0 != c1 || r0 != r1) {
            Piece p = get(c0, r0);
            if (p != EMP && p != start) {
                return true;
            }
            c0 += dir.dc;
            r0 += dir.dr;
        }
        return false;
    }

    /** The standard initial configuration for Lines of Action. */
    static final Piece[][] INITIAL_PIECES = {
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { WP,  EMP, EMP, EMP, EMP, EMP, EMP, WP  },
        { EMP, BP,  BP,  BP,  BP,  BP,  BP,  EMP }
    };

    /** List of all unretracted moves on this board, in order. */
    private final ArrayList<Move> _moves = new ArrayList<>();
    /** Current side on move. */
    private Piece _turn;
    /** Current state of the board. */
    private Piece[][] _pieces = new Piece[M][M];

    /** An iterator returning the legal moves from the current board. */
    private class MoveIterator implements Iterator<Move> {

        /** Current piece under consideration. */
        private int _c, _r;
        /** Next direction of current piece to return. */
        private Direction _dir;
        /** Next move. */
        private Move _move;

        /** A new move iterator for turn(). */
        MoveIterator() {
            _c = 1; _r = 1; _dir = NOWHERE;
            incr();
        }

        @Override
        public boolean hasNext() {
            return _move != null;
        }

        @Override
        public Move next() {
            if (_move == null) {
                throw new NoSuchElementException("no legal move");
            }

            Move move = _move;
            incr();
            return move;
        }

        @Override
        public void remove() {
        }

        /** Advance to the next legal move. */
        private void incr() {
            for (; _r <= M; _r += 1) {
                for (; _c <= M; _c += 1) {
                    if (get(_c, _r) == turn()) {
                        for (_dir = _dir.succ(); _dir != null;
                             _dir = _dir.succ()) {
                            int length = pieceCountAlong(_c, _r, _dir);
                            Move m = Move.create(_c, _r, length, _dir,
                                                 getBoard());
                            if (isLegal(m)) {
                                _move = m;
                                return;
                            }
                        }
                    }
                    _dir = NOWHERE;
                }
                _c = 1;
            }
            _move = null;
        }
    }

}
