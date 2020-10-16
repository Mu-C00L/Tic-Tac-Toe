// License: GPL. For details, see LICENSE file.
package tictactoe;

import java.util.Arrays;

/**
 *
 * @author Kishan
 */
public class TicTacToeClient implements Runnable {

  public static enum Mark {
    X,
    O,
    EMPTY
  }

  public class Square {

    /* Grid to play game */
    Mark[][] grid;

    public Square() {
      this.grid = new Mark[3][3];
      Arrays.fill(grid, Mark.EMPTY);
    }

    /**
     * Fills the given tile if it is valid and empty.
     *
     * @param x The x coordinate of the grid.
     * @param y The y coordinate of the grid.
     * @param mark The mark to fill on the tile
     * @return True if the tile was empty and filled otherwise false.
     */
    public boolean fill(int x, int y, Mark mark) {
      if (isValidTile(x, y) && isEmpty(x, y)) {
        grid[x][y] = mark;
        return true;
      } else {
        return false;
      }
    }

    private boolean isEmpty(int x, int y) {
      return isValidTile(x, y) && grid[x][y] == Mark.EMPTY;
    }

    private boolean isValidTile(int x, int y) {
      return x > -1 && x < 3 && y > -1 && y < 3;
    }
  }

  @Override
  public void run() {
  }
}
