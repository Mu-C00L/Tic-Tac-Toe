// License: GPL. For details, see LICENSE file.
package tictactoe.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JButton;
import javax.swing.JPanel;
import tictactoe.GameFiles;

/**
 *
 * @author Kishan
 */
public class Board extends JPanel {

  private Image startscreen;

  public Board() {
    startscreen = GameFiles.STARTSCREEN;
    initBoard();
    add(new JButton("This is a button"));
  }

  private void initBoard() {
    int w = startscreen.getWidth(this);
    int h = startscreen.getHeight(this);
    setPreferredSize(new Dimension(w, h));
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.drawImage(startscreen, 0, 0, null);
  }

}
