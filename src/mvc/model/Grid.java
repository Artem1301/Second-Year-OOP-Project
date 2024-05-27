package mvc.model;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

public class Grid {

    public static final int ROWS = 20;
    public static final int COLS = 12;
    public static final int DIM = 4;

    Block[][] mBlock;

    ArrayList mOccupiedBlocks;

    public Grid() {
        mBlock = new Block[ROWS][COLS];
        initializeBlocks();
        mOccupiedBlocks = new ArrayList();
    }

    public Block[][] getBlocks() {
        return mBlock;
    }

    synchronized public void initializeBlocks() {
//        paints board with blue blocks
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                mBlock[i][j] = new Block(false, Color.blue, i, j);
            }
        }
    }


    synchronized public boolean requestDown(Tetromino tetr) {
        boolean[][] bC;
        bC = tetr.getColoredSquares(tetr.mOrientation);
        for (int i = tetr.mCol; i < tetr.mCol + DIM; i++) {
            for (int j = tetr.mRow; j < tetr.mRow + DIM; j++) {
                if (bC[j - tetr.mRow][i - tetr.mCol] && j >= Grid.ROWS) {
                    return false;
                }
                if (bC[j - tetr.mRow][i - tetr.mCol] && mBlock[j][i].isOccupied()) {
                    return false;
                }


            }

        }
        return true;
    }

    synchronized public void addToOccupied(Tetromino tetr) {
        boolean[][] bC;
        bC = tetr.getColoredSquares(tetr.mOrientation);
        Color color = tetr.mColor;
        for (int i = tetr.mCol; i < tetr.mCol + DIM; i++) {
            for (int j = tetr.mRow; j < tetr.mRow + DIM; j++) {
                if (bC[j - tetr.mRow][i - tetr.mCol]) {
                    mOccupiedBlocks.add(new Block(true, color, j, i));
                }

            }

        }
    }

    synchronized public void checkTopRow() {
        for (Object mOccupiedBlock : mOccupiedBlocks) {
            Block block = (Block) mOccupiedBlock;
            if (block.getRow() <= 0) {
                CommandCenter.getInstance().setPlaying(false);
                CommandCenter.getInstance().setGameOver(true);
                clearGrid();
            }

        }
    }

    synchronized public void clearGrid() {
        initializeBlocks();
        mOccupiedBlocks.clear();
    }

    synchronized public void checkCompletedRow() {
        LinkedList fullRowItems = new LinkedList();
        LinkedList repositioningItems = new LinkedList();

        int nRows = Grid.ROWS - 1;
        while (nRows >= 0) {
            for (int i = mOccupiedBlocks.size() - 1; i >= 0; i--) {
                Block block = (Block) mOccupiedBlocks.get(i);
                if (block.getRow() == nRows) {
                    fullRowItems.add(i);
                }

            }
            if (fullRowItems.size() == Grid.COLS) {
                while (fullRowItems.size() > 0) {
                    Block blck = (Block) mOccupiedBlocks.remove(((Integer) fullRowItems.removeFirst()).intValue());
                    CommandCenter.getInstance().addScore(blck.getPoints());
                }
                if (CommandCenter.getInstance().getScore() > CommandCenter.getInstance().getHighScore()) {
                    CommandCenter.getInstance().setHighScore(CommandCenter.getInstance().getScore());
                }
                CommandCenter.getInstance().checkThreshold();
                for (int j = mOccupiedBlocks.size() - 1; j >= 0; j--) {
                    Block blk = (Block) mOccupiedBlocks.get(j);

                    if (blk.getRow() < nRows) {
                        mOccupiedBlocks.remove(j);
                        blk.setRow(blk.getRow() + 1);
                        repositioningItems.add(blk);
                    }

                }

                while (repositioningItems.size() > 0) {
                    mOccupiedBlocks.add(repositioningItems.removeLast());
                }
                fullRowItems.clear();
                repositioningItems.clear();
            } else if (fullRowItems.size() == 0) {
                return;
            } else {
                fullRowItems.clear();
                nRows--;
            }
        }

    }

    synchronized public void setBlocks(Tetromino tetr) {
        boolean[][] bC;
        bC = tetr.getColoredSquares(tetr.mOrientation);
        Color clr = tetr.mColor;

        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                mBlock[i][j] = new Block(false, Color.blue, i, j);
            }
        }
        for (int i = tetr.mCol; i < tetr.mCol + DIM; i++) {
            for (int j = tetr.mRow; j < tetr.mRow + DIM; j++) {
                if (bC[j - tetr.mRow][i - tetr.mCol]) {
                    mBlock[j][i] = new Block(false, clr, j - tetr.mRow, i - tetr.mCol);
                }

            }

        }
        for (Object mOccupiedBlock : mOccupiedBlocks) {
            Block b = (Block) mOccupiedBlock;
            try {
                mBlock[b.getRow()][b.getCol()] = new Block(true, b.getColor(), b.getRow(), b.getCol());
            } catch (NullPointerException e) {
                break;

            }
        }
    }

    synchronized public boolean requestLateral(Tetromino tetr) {
        boolean[][] bC;
        bC = tetr.getColoredSquares(tetr.mOrientation);
        for (int i = tetr.mCol; i < tetr.mCol + DIM; i++) {
            for (int j = tetr.mRow; j < tetr.mRow + DIM; j++) {
                if (bC[j - tetr.mRow][i - tetr.mCol] && (i < 0 || i >= Grid.COLS || j >= Grid.ROWS)) {
                    return false;
                }
                if (bC[j - tetr.mRow][i - tetr.mCol] && mBlock[j][i].isOccupied()) {
                    return false;
                }

            }

        }
        return true;
    }
}
