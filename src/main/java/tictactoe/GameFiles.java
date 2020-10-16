// License: GPL. For details, see LICENSE file.
package tictactoe;

import java.awt.Image;
import tictactoe.tools.ImageProvider;

/**
 * Class for storing in game resources.
 * @author Kishan
 */
public class GameFiles {
  public static final Image STARTSCREEN = new ImageProvider("images/startscreen.jpeg").get().
    getScaledInstance(500, 500, Image.SCALE_SMOOTH); 
}
