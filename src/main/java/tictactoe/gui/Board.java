// License: GPL. For details, see LICENSE file.
package tictactoe.gui;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import tictactoe.GameFiles;
import tictactoe.TicTacToeClient;
import tictactoe.TicTacToeServer;

/**
 *
 * @author Kishan
 */
public class Board extends JPanel {

  private final Image startscreen;
  private static Board instance;
  private final JButton startServer;
  private final JButton startClient;
  private final JTextField serverAddress;

  private Board() {
    startServer = new JButton("Create Server");
    startClient = new JButton("Create Client");
    serverAddress = new JTextField(15);

    startscreen = GameFiles.STARTSCREEN;
    initBoard();
    setServerButton();
    setClientButton();
    serverAddress.setToolTipText("Default : 127.0.0.1");
    
    setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(0, 5, 0, 5);

    // Left column
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.gridx = 0;
    gbc.gridy = 1;
    add(new JLabel("Enter Server Address"), gbc);

    gbc.gridx++;
    gbc.gridy = 0;
    add(startServer, gbc);
    gbc.gridy++;
    add(serverAddress, gbc);

    gbc.gridx++;
    gbc.gridy = 1;
    add(startClient, gbc);
  }

  /**
   * Returns the unique instance of the class.
   *
   * @return The unique instance of the class.
   */
  public static synchronized Board getInstance() {
    if (instance == null) {
      instance = new Board();
    }
    return instance;
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

  private void setServerButton() {
    startServer.addActionListener(e -> {
      new Thread() {
        public void run() {
          TicTacToeServer server = new TicTacToeServer();
          server.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
          server.execute();
        }
      }.start();
    });
  }

  private void setClientButton() {
    startClient.addActionListener(e -> {
      new Thread() {
        public void run() {
          TicTacToeClient client;
          String address = serverAddress.getText();
          client = address == null ? new TicTacToeClient("127.0.0.1") : new TicTacToeClient(address);
          client.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        }
      }.start();
    });
  }

}
