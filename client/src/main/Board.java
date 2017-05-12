package src.main;

import javafx.scene.control.Label;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Board {

    public static Stone[][] stones = new Stone[19][19];
    public static Map<Stone, Integer> chainMap = new HashMap<>();
    public static Map<Integer, HashSet<Stone>> stonesMap = new HashMap<>();
    public static Map<Integer, HashSet<Point>> libertyMap = new HashMap<>();
    public static HashSet<Integer> dead = new HashSet<>();
    public static Stone[] maybeKo = new Stone[2];
    public static int step = 1;

    /******************/
    private static int deadNum1 = 0;
    private static int deadNum2 = 0;
    private static Label player1Kill;
    private static Label player2Kill;
    /******************/
    private static boolean[] used = new boolean[361];

    public Board() {
        for (int i = 0; i < 19; ++i) {
            for (int j = 0; j < 19; ++j) {
                stones[i][j] = new Stone(i, j);
            }
        }
        maybeKo[0] = new Stone(-1, -1);
        maybeKo[1] = new Stone(-1, -1);
    }

    public static void clear() {
        for (int i = 0; i < 19; ++i) {
            for (int j = 0; j < 19; ++j) {
                stones[i][j].color = Stone.None;
            }
        }
        chainMap.clear();
        stonesMap.clear();
        libertyMap.clear();
        dead.clear();
        step = 1;
        /******/
        deadNum1 = 0;
        deadNum2 = 0;
        /********/
    }

    // Checks if the stones in color can be placed in the Point p.
    public int action(int x, int y, int color) {
        if (stones[x][y].color != Stone.None) {
            return Type.Action.INVALID;
        }
        return Core.action(x, y, color);
    }

    // Adds a stone at the Point (x, y)
    public void add(int x, int y, int color) {
        stones[x][y].color = color;
        stones[x][y].step = step;
        ++step;
        initStone(stones[x][y]);
        update(stones[x][y]);
    }

    // Removes the stones that were dead.
    public void remove() {
        /*******************************************/
        if (Client.getGameController().getTurn() == Stone.Black) {
            for (int chain : dead) {
                deadNum2 += stonesMap.get(chain).size();
            }
            player1Kill.setText(deadNum2 + "子");
        } else {
            for (int chain : dead) {
                deadNum1 += stonesMap.get(chain).size();
            }
            player2Kill.setText(deadNum1 + "子");
        }
        /*******************************************/
        for (int chain : dead) {
            HashSet<Stone> ss = stonesMap.get(chain);
            for (Stone s : ss) {
                if (s.up() != null && s.up().color == -s.color) {
                    extendLiberty(chainMap.get(s.up()), new Point(s.x, s.y));
                }
                if (s.down() != null && s.down().color == -s.color) {
                    extendLiberty(chainMap.get(s.down()), new Point(s.x, s.y));
                }
                if (s.left() != null && s.left().color == -s.color) {
                    extendLiberty(chainMap.get(s.left()), new Point(s.x, s.y));
                }
                if (s.right() != null && s.right().color == -s.color) {
                    extendLiberty(chainMap.get(s.right()), new Point(s.x, s.y));
                }
                s.color = Stone.None;
                chainMap.remove(s);
            }
            stonesMap.remove(chain);
            libertyMap.remove(chain);
            used[chain] = false;
        }
        dead.clear();
    }

    // Adds the stones that were dead in the dead.
    public static void addDead(ArrayList<Stone> list) {
        for (Stone stone : list) {
            dead.add(chainMap.get(stones[stone.x][stone.y]));
        }
    }

    // Initializes a new stone for chainMap, stonesMap and libertyMap.
    private void initStone(Stone stone) {
        // Initializes the chain where the stone is.
        int chain = 0;
        while (used[chain]) {
            ++chain;
        }
        used[chain] = true;
        chainMap.put(stone, chain);

        // Initializes the stonesList of the chain where the stone is.
        HashSet<Stone> stonesList = new HashSet<>();
        stonesList.add(stone);
        stonesMap.put(chain, stonesList);

        // Initializes the libertyMap of the stone.
        libertyMap.put(chain, new HashSet<>());
        if (stone.up() != null && stone.up().color == Stone.None) {
            extendLiberty(chain, new Point(stone.x, stone.y - 1));
        }
        if (stone.down() != null && stone.down().color == Stone.None) {
            extendLiberty(chain, new Point(stone.x, stone.y + 1));
        }
        if (stone.left() != null && stone.left().color == Stone.None) {
            extendLiberty(chain, new Point(stone.x - 1, stone.y));
        }
        if (stone.right() != null && stone.right().color == Stone.None) {
            extendLiberty(chain, new Point(stone.x + 1, stone.y));
        }
    }

    // Updates the informations of the nerghbor of the stone.
    private void update(Stone stone) {
        if (stone.up() != null && stone.up().color != Stone.None) {
            updateNeighbor(stone, stone.up());
        }
        if (stone.down() != null && stone.down().color != Stone.None) {
            updateNeighbor(stone, stone.down());
        }
        if (stone.left() != null && stone.left().color != Stone.None) {
            updateNeighbor(stone, stone.left());
        }
        if (stone.right() != null && stone.right().color != Stone.None) {
            updateNeighbor(stone, stone.right());
        }
    }

    private void updateNeighbor(Stone stone, Stone neighbor) {
        reduceLiberty(chainMap.get(neighbor), new Point(stone.x, stone.y));
        if (stone.color == neighbor.color) {
            connect(stone, neighbor);
        }
    }

    private void connect(Stone stone, Stone neighbor) {
        int oldChain = chainMap.get(neighbor);
        int newChain = chainMap.get(stone);
        if (newChain != oldChain) {
            HashSet<Stone> oldStoneSet = stonesMap.get(oldChain);
            HashSet<Stone> newStoneSet = stonesMap.get(newChain);
            HashSet<Point> oldPointSet = libertyMap.get(oldChain);
            HashSet<Point> newPointSet = libertyMap.get(newChain);
            for (Stone s : oldStoneSet) {
                chainMap.put(s, newChain);
                newStoneSet.add(s);
            }
            for (Point p : oldPointSet) {
                newPointSet.add(p);
            }
            stonesMap.remove(oldChain);
            libertyMap.remove(oldChain);
            used[oldChain] = false;
        }
    }

    private void reduceLiberty(int chain, Point point) {
        libertyMap.get(chain).remove(point);
    }

    private void extendLiberty(int chain, Point point) {
        libertyMap.get(chain).add(point);
    }

    public static void setPlayerKill(Label player1Kill, Label player2Kill) {
        Board.player1Kill = player1Kill;
        Board.player2Kill = player2Kill;
    }
}
