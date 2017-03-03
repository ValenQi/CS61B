package loa;

import java.util.ArrayList;
import static loa.Board.*;

/** An automated Player.
 *  @author Qi Liu
 */
class MachinePlayer extends Player {

    /** A MachinePlayer that plays the SIDE pieces in GAME. */
    MachinePlayer(Piece side, Game game) {
        super(side, game);
    }

    @Override
    Move makeMove() {
        Move m = guessBestMove(side(), getBoard(), Integer.MAX_VALUE,
                                                   Integer.MIN_VALUE);
        System.out.println(side().abbrev().toUpperCase() + "::" + m);
        return m;
    }

    /** Return the best move for SIDE on BOARD using static evaluation
     *  with cutoffs MAX and MIN, which just look at the next possible moves. */
    Move guessBestMove(Piece side, Board board, int max, int min) {
        Move bestMove = null;
        if (side == side()) {
            int bestScore = Integer.MIN_VALUE;
            for (Move m : board) {
                int newScore = eval(m, board);
                if (newScore > bestScore) {
                    bestScore = newScore;
                    bestMove = m;
                    if (newScore >= max) {
                        break;
                    }
                }
            }
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (Move m : board) {
                int newScore = eval(m, board);
                if (newScore < bestScore) {
                    bestScore = newScore;
                    bestMove = m;
                    if (newScore <= min) {
                        break;
                    }
                }
            }
        }
        return bestMove;
    }

    /** Return an evaluation of MOVE on the current BOARD state,
     *  which is the score of BOARD after MOVE. The state of BOARD
     *  should not be changed after evaluation. */
    private int eval(Move move, Board board) {
        board.makeMove(move);
        int score = eval(board);
        board.retract();
        return score;
    }

    /** Return an evaluation of the current BOARD state: the difference between
     *. the total distances between opponents' pieces and the total distances
     *  between MY pieces. The higher the return value, the better for ME. */
    private int eval(Board board) {
        if (board.piecesContiguous(side())) {
            return Integer.MAX_VALUE;
        } else if (board.piecesContiguous(side().opposite())) {
            return Integer.MIN_VALUE;
        }
        ArrayList<int[]> myPieces = new ArrayList<int[]>();
        ArrayList<int[]> oppPieces = new ArrayList<int[]>();
        Piece[][] contents = board.getContents();
        for (int i = 1; i <= M; i += 1) {
            for (int j = 1; j <= M; j += 1) {
                if (contents[i - 1][j - 1] == side()) {
                    myPieces.add(new int[]{i, j});
                } else if (contents[i - 1][j - 1] == side().opposite()) {
                    oppPieces.add(new int[]{i, j});
                }
            }
        }
        int myDistances = distances(myPieces);
        int oppDistances = distances(oppPieces);
        return oppDistances - myDistances;
    }

    /** Return the total distances between PIECES. */
    private int distances(ArrayList<int[]> pieces) {
        double distances, dc, dr;
        int total = 0;
        for (int i = 0; i < pieces.size(); i += 1) {
            for (int j = i + 1; j < pieces.size(); j += 1) {
                dc = Math.pow(pieces.get(i)[0] - pieces.get(j)[0], 2);
                dr = Math.pow(pieces.get(i)[1] - pieces.get(j)[1], 2);
                distances = Math.sqrt(dc + dr);
                total += Math.round(distances);
            }
        }
        return total;
    }

    /** The depth that AI looks forward.
    private final int d = 3; */

    /** Return the best move using alpha-beta pruning.
    Move findBestMove(Piece side, Board board, int depth, int alpha, int beta) {
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        for (Move m : board) {
            board.makeMove(m);
            int newScore = alphabeta(side.opposite(), board, depth - 1,
                                     alpha, beta);
            board.retract();
            if (newScore > bestScore) {
                bestScore = newScore;
                bestMove = m;
            }
        }
        return bestMove;
    }

    /** Return the best value for SIDE on BOARD, looking into DEPTH, using
     *  alpha-beta pruning.
    int alphabeta(Piece side, Board board, int depth, int alpha, int beta) {
        if (board.gameOver() || depth == 0) {
            return eval(board);
        }
        if (side == side()) {
            int bestScore = Integer.MIN_VALUE;
            for (Move m : board) {
                board.makeMove(m);
                int newScore = alphabeta(side().opposite(), board,
                                             depth - 1, alpha, beta);
                board.retract();
                bestScore = Math.max(bestScore, newScore);
                alpha = Math.max(alpha, bestScore);
                if (alpha >= beta) {
                    break;
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (Move m : board) {
                board.makeMove(m);
                int newScore = alphabeta(side().opposite(), board,
                                             depth - 1, alpha, beta);
                board.retract();
                bestScore = Math.min(bestScore, newScore);
                alpha = Math.min(beta, bestScore);
                if (alpha >= beta) {
                    break;
                }
            }
            return bestScore;
        }
    }
    */

}
