// License: GPL. For details, see LICENSE file.
package tictactoe.tools;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author Kishan
 */
public class ImageProvider {

  private String path;

  public ImageProvider(String path) {
    this.path = Objects.requireNonNull(path, "name");
  }

  public Image get() {
    BufferedImage image = null;
    try {
      image = ImageIO.read(getClass().getClassLoader().getResource(path));
    } catch (IOException ex) {
      Logger.getLogger(ImageProvider.class.getName()).log(Level.SEVERE, null, ex);
    }
    return image;
  }
}
