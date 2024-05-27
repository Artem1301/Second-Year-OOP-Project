package mvc.controller;

import mvc.model.*;
import mvc.view.GamePanel;
import sounds.Sound;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;


public class Game implements Runnable, KeyListener {


    public static final Dimension DIM = new Dimension(500, 700);
    public static final int THRESHOLD = 2400;
    public static int nAutoDelay = 300;
    public static final int TETROMINO_NUMBER = 100;
    private GamePanel gmpPanel;
    public static Random R = new Random();
    public final static int ANIM_DELAY = 45;

    private Thread thrAnim;
    private Thread thrAutoDown;
    private Thread thrLoaded;
    private long lTime;
    private long lTimeStep;
    final static int PRESS_DELAY = 40;
    private boolean bMuted = true;


    private final int PAUSE = 80, // p
            QUIT = 81, // q
            LEFT = 37, // left arrow
            RIGHT = 39, // right arrow
            START = 83, // s
            MUTE = 77, // m
            DOWN = 40, // move faster
            SPACE = 32; // rotate


    private Clip clpMusicBackground;
    private Clip clpBomb;

    public Game() {

        gmpPanel = new GamePanel(DIM);
        gmpPanel.addKeyListener(this);
        clpBomb = Sound.clipForLoopFactory("explosion-02.wav");
        clpMusicBackground = Sound.clipForLoopFactory("tetris_tone_loop_1_.wav");


    }

    public static void main(String args[]) {
        EventQueue.invokeLater(() -> {
            try {
                Game game = new Game();
                game.fireUpThreads();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private void fireUpThreads() {
        if (thrAnim == null) {
            thrAnim = new Thread(this);
            thrAnim.start();
        }
        if (thrAutoDown == null) {
            thrAutoDown = new Thread(this);
            thrAutoDown.start();
        }

        if (!CommandCenter.getInstance().isLoaded() && thrLoaded == null) {
            thrLoaded = new Thread(this);
            thrLoaded.start();
        }
    }


    public void run() {

        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        long lStartTime = System.currentTimeMillis();
        if (!CommandCenter.getInstance().isLoaded() && Thread.currentThread() == thrLoaded) {
            CommandCenter.getInstance().setLoaded(true);
        }

        // thread animates the scene
        while (Thread.currentThread() == thrAutoDown) {
            if (!CommandCenter.getInstance().isPaused() && CommandCenter.getInstance().isPlaying()) {
                tryMovingDown();
            }
            gmpPanel.repaint();
            try {
                lStartTime += nAutoDelay;
                Thread.sleep(Math.max(0, lStartTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                break;
            }
        }
        while (Thread.currentThread() == thrAnim) {
            if (!CommandCenter.getInstance().isPaused() && CommandCenter.getInstance().isPlaying()) {
                updateGrid();
            }
            gmpPanel.repaint();


            try {
                lStartTime += ANIM_DELAY;
                Thread.sleep(Math.max(0, lStartTime - System.currentTimeMillis()));
            } catch (InterruptedException e) {
                break;
            }
        }
    }
    private void updateGrid() {
        gmpPanel.grid.setBlocks(gmpPanel.tetrCurrent);

    }


    private void tryMovingDown() {
        Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
        tetrTest.moveDown();
        if (gmpPanel.grid.requestDown(tetrTest)) {
            gmpPanel.tetrCurrent.moveDown();
            tetrTest = null;
        }
        else if (CommandCenter.getInstance().isPlaying() && gmpPanel.tetrCurrent instanceof Bomb) {
            clpBomb.stop();
            clpBomb.flush();
            clpBomb.setFramePosition(0);
            clpBomb.start();
            gmpPanel.grid.clearGrid();
            CommandCenter.getInstance().addScore(1000);
            if (CommandCenter.getInstance().getHighScore() < CommandCenter.getInstance().getScore()) {
                CommandCenter.getInstance().setHighScore(CommandCenter.getInstance().getScore());
            }
            gmpPanel.tetrCurrent = gmpPanel.tetrOnDeck;
            gmpPanel.tetrOnDeck = createNewTetromino();
            tetrTest = null;
        }
        else if (CommandCenter.getInstance().isPlaying()) {
            gmpPanel.grid.addToOccupied(gmpPanel.tetrCurrent);
            gmpPanel.grid.checkTopRow();
            gmpPanel.grid.checkCompletedRow();
            gmpPanel.tetrCurrent = gmpPanel.tetrOnDeck;
            gmpPanel.tetrOnDeck = createNewTetromino();
            tetrTest = null;
        } else {
            tetrTest = null;
        }

    }


    private void startGame() {
        gmpPanel.tetrCurrent = createNewTetromino();
        gmpPanel.tetrOnDeck = createNewTetromino();

        CommandCenter.getInstance().clearAll();
        CommandCenter.getInstance().initGame();
        CommandCenter.getInstance().setPlaying(true);
        CommandCenter.getInstance().setPaused(false);
        CommandCenter.getInstance().setGameOver(false);
        if (!bMuted)
            clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
    }

    private Tetromino createNewTetromino() {
        int nKey = R.nextInt(TETROMINO_NUMBER);
        if (nKey >= 0 && nKey <= 12) {
            return new LongPiece();
        } else if (nKey > 12 && nKey <= 23) {
            return new SquarePiece();
        } else if (nKey > 23 && nKey <= 35) {
            return new SPiece();
        } else if (nKey > 35 && nKey <= 46) {
            return new TPiece();
        } else if (nKey > 46 && nKey <= 58) {
            return new ZPiece();
        } else if (nKey > 58 && nKey <= 71) {
            return new LPiece();
        } else if (nKey > 71 && nKey <= 84) {
            return new JPiece();
        } else if (nKey > 84 && nKey <= 98) {
            return new PlusPiece();
        } else {
            return new Bomb();
        }
    }


    private static void stopLoopingSounds(Clip... clpClips) {
        for (Clip clp : clpClips) {
            clp.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        lTime = System.currentTimeMillis();
        int nKeyPressed = e.getKeyCode();
        if (nKeyPressed == START && CommandCenter.getInstance().isLoaded() && !CommandCenter.getInstance().isPlaying())
            startGame();

        if (nKeyPressed == PAUSE & lTime > lTimeStep + PRESS_DELAY) {
            CommandCenter.getInstance().setPaused(!CommandCenter.getInstance().isPaused());
            lTimeStep = System.currentTimeMillis();
        }
        if (nKeyPressed == QUIT && lTime > lTimeStep + PRESS_DELAY) {
            System.exit(0);
        }
        if (nKeyPressed == DOWN && (lTime > lTimeStep + PRESS_DELAY - 35) && CommandCenter.getInstance().isPlaying()) {
            tryMovingDown();
            lTimeStep = System.currentTimeMillis();
        }
        if (nKeyPressed == RIGHT && lTime > lTimeStep + PRESS_DELAY) {
            Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
            tetrTest.moveRight();
            if (gmpPanel.grid.requestLateral(tetrTest)) {
                gmpPanel.tetrCurrent.moveRight();
                tetrTest = null;
                lTimeStep = System.currentTimeMillis();
            } else {
                tetrTest = null;
            }
        }
        if (nKeyPressed == LEFT && lTime > lTimeStep + PRESS_DELAY) {
            Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
            tetrTest.moveLeft();
            if (gmpPanel.grid.requestLateral(tetrTest)) {
                gmpPanel.tetrCurrent.moveLeft();
                tetrTest = null;
                lTimeStep = System.currentTimeMillis();
            } else {
                tetrTest = null;
            }
        }
        if (nKeyPressed == SPACE) {
            Tetromino tetrTest = gmpPanel.tetrCurrent.cloneTetromino();
            tetrTest.rotate();
            if (gmpPanel.grid.requestLateral(tetrTest)) {
                gmpPanel.tetrCurrent.rotate();
                tetrTest = null;
                lTimeStep = System.currentTimeMillis();
            } else {
                tetrTest = null;
            }
        }
        if (nKeyPressed == MUTE) {
            if (!bMuted) {
                stopLoopingSounds(clpMusicBackground);
                stopLoopingSounds(clpBomb);
                bMuted = !bMuted;
            } else {
                clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
                bMuted = !bMuted;
            }
        }

    }


    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}


