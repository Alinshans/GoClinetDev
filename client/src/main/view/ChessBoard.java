package src.main.view;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import src.main.Board;
import src.main.Stone;
import src.main.Type;
import src.main.communication.Encoder;

import java.awt.Point;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;

public class ChessBoard implements Initializable {

    private Timer player1timer;
    private Timer player2timer;
    private Board board = new Board();
    private Circle[][] stonesCircle = new Circle[19][19];
    private int color = -1;

    @FXML
    private Pane chessPane;

    private static final int borderGap = 20;
    private static final int stoneGap = 32;
    private static final int xLen = 18 * stoneGap + 2 * borderGap;
    private static final int yLen = 18 * stoneGap + 2 * borderGap;
    private static final int stoneRadius = (stoneGap - 4) / 2;

    private Point pixel = new Point();
    private Point index = new Point();

    @FXML
    private void onClick(MouseEvent event) {
        if (GameController.isBegin()) {
            /*********** test ***********/
            if (color == -1) {
                player1timer.pause();
                player2timer.start();
            } else {
                player2timer.pause();
                player1timer.start();
            }
            /*********** test ***********/
            getPixelPos(event);
            int action = action();
            if (action != Type.Action.INVALID) {
                String jsonmsg = Encoder.actionRequest(action, color, index.x, index.y);
                System.out.println(jsonmsg);
                if (action == Type.Action.KILL) {
                    place(index.x, index.y, color);
                    for (int chain : Board.dead) {
                        remove(chain);
                    }
                    board.remove();
                } else {
                    place(index.x, index.y, color);
                }
                color = -color;
            }
        }

    }

    private void getPixelPos(MouseEvent event) {
        pixel.setLocation(event.getX(), event.getY());
    }

    private void getIndexPos() {
        index.setLocation((pixel.x - borderGap) / stoneGap, (pixel.y - borderGap) / stoneGap);
    }

    private int action() {
        if (pixel.x < borderGap - stoneRadius || pixel.x > xLen - borderGap + stoneRadius
                || pixel.y < borderGap - stoneRadius || pixel.y > yLen - borderGap + stoneRadius) {
            return Type.Action.INVALID;
        }
        int gridX = (pixel.x - borderGap) % stoneGap;
        int gridY = (pixel.y - borderGap) % stoneGap;
        int indexX = (pixel.x - borderGap) / stoneGap;
        int indexY = (pixel.y - borderGap) / stoneGap;
        if (gridX < stoneRadius && gridY < stoneRadius) {
            pixel.x = indexX * stoneGap + borderGap;
            pixel.y = indexY * stoneGap + borderGap;
        } else if (gridX < stoneRadius && gridY > stoneGap - stoneRadius) {
            pixel.x = indexX * stoneGap + borderGap;
            pixel.y = (indexY + 1) * stoneGap + borderGap;
        } else if (gridX > stoneGap - stoneRadius && gridY < stoneRadius) {
            pixel.x = (indexX + 1) * stoneGap + borderGap;
            pixel.y = indexY * stoneGap + borderGap;
        } else if (gridX > stoneGap - stoneRadius && gridY > stoneGap - stoneRadius) {
            pixel.x = (indexX + 1) * stoneGap + borderGap;
            pixel.y = (indexY + 1) * stoneGap + borderGap;
        } else {
            return Type.Action.INVALID;
        }
        getIndexPos();
        return board.action(index, color);
    }

    private void place(int x, int y, int color) {
        Circle stone = stonesCircle[x][y];
        System.out.println("ChessBoard Place: (" + x + "," + y + ")");
        if (color == Stone.Black) {
            stone.setFill(Color.BLACK);
        } else {
            stone.setFill(Color.WHITE);
        }
        stone.setLayoutX(pixel.x);
        stone.setLayoutY(pixel.y);
        stone.setRadius(stoneRadius);
        chessPane.getChildren().add(stone);
        board.add(x, y, this.color);
    }

    private void remove(int chain) {
        HashSet<Stone> stones = Board.stonesMap.get(chain);
        System.out.print("ChessBoard remove chain " + chain + " : ");
        for (Stone s : stones) {
            System.out.print("Stone(" + s.x + "," + s.y + ") ");
            chessPane.getChildren().remove(stonesCircle[s.x][s.y]);
        }
        System.out.println();
    }

    public void setTimer(Timer timer01, Timer timer02) {
        this.player1timer = timer01;
        this.player2timer = timer02;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        drawBoard();
        drawLine();
        drawStar();
        initStonesCircle();
    }

    private void drawBoard() {
        Rectangle rec = new Rectangle(0, 0, xLen, yLen);
        rec.setFill(Color.rgb(249, 214, 91));
        chessPane.getChildren().add(rec);
    }

    private void drawLine() {
        Line line;
        for (int i = 0; i < 19; ++i) {
            line = new Line(borderGap, i * stoneGap + borderGap, xLen - borderGap, i * stoneGap + borderGap);
            chessPane.getChildren().add(line);
        }
        for (int i = 0; i < 19; ++i) {
            line = new Line(i * stoneGap + borderGap, borderGap, i * stoneGap + borderGap, yLen - borderGap);
            line.setStroke(Color.BLACK);
            chessPane.getChildren().add(line);
        }
    }

    private void drawStar() {
        int x = 3;
        int y;
        Circle circle;
        for (int i = 0; i < 3; ++i) {
            y = 3;
            for (int j = 0; j < 3; ++j) {
                circle = new Circle();
                circle.setFill(Color.BLACK);
                circle.setRadius(3);
                circle.setLayoutX(x * stoneGap + borderGap);
                circle.setLayoutY(y * stoneGap + borderGap);
                chessPane.getChildren().add(circle);
                y = y + 6;
            }
            x = x + 6;
        }
    }

    private void initStonesCircle() {
        for (int i = 0; i < 19; ++i) {
            for (int j = 0; j < 19; ++j) {
                stonesCircle[i][j] = new Circle();
            }
        }
    }

}
