// License: GPL. For details, see LICENSE file.
package tictactoe;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.Socket;
import java.net.InetAddress;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public final class TicTacToeClient extends JFrame implements Runnable {

  private final JTextField idField; // textfield to display player's mark
  private final JTextArea displayArea; // JTextArea to display output
  private final JPanel boardPanel; // panel for tic-tac-toe board
  private final JPanel panel2; // panel to hold board
  private final Square[][] board; // tic-tac-toe board
  private Square currentSquare; // current square
  private Socket connection; // connection to server
  private Scanner input; // input from server
  private Formatter output; // output to server
  private final String ticTacToeHost; // host name for server
  private String myMark; // this client's mark
  private boolean myTurn; // determines which client's turn it is
  private final String X_MARK = "X"; // mark for first client
  private final String O_MARK = "O"; // mark for second client

  // set up user-interface and board
  public TicTacToeClient(String host) {
    ticTacToeHost = host; // set name of server
    displayArea = new JTextArea(4, 30); // set up JTextArea
    displayArea.setEditable(false);
    add(new JScrollPane(displayArea), BorderLayout.SOUTH);

    boardPanel = new JPanel(); // set up panel for squares in board
    boardPanel.setLayout(new GridLayout(3, 3, 0, 0));
    board = new Square[3][3]; // create board

    // loop over the rows in the board
    for (int row = 0; row < board.length; row++) {
      // loop over the columns in the board
      for (int column = 0; column < board[row].length; column++) {
        // create square
        board[row][column] = new Square(" ", row * 3 + column);
        boardPanel.add(board[row][column]); // add square       
      }
    }

    idField = new JTextField(); // set up textfield
    idField.setEditable(false);
    add(idField, BorderLayout.NORTH);

    panel2 = new JPanel(); // set up panel to contain boardPanel
    panel2.add(boardPanel, BorderLayout.CENTER); // add board panel
    add(panel2, BorderLayout.CENTER); // add container panel

    setSize(300, 225); // set size of window
    setVisible(true); // show window

    startClient();
  }

  // start the client thread
  public void startClient() {
    // connect to server and get streams
    try {
      // make connection to server
      connection = new Socket(InetAddress.getByName(ticTacToeHost), 12345);

      // get streams for input and output
      input = new Scanner(connection.getInputStream());
      output = new Formatter(connection.getOutputStream());
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }

    // create and start worker thread for this client
    ExecutorService worker = Executors.newFixedThreadPool(1);
    worker.execute(this); // execute client
  }

  /**
   * The Player’s run method controls the information that is sent to and received from the client.
   */
  @Override
  public void run() {
    myMark = input.nextLine(); // get player's mark (X or O)

    SwingUtilities.invokeLater(() -> {
      // display player's mark
      idField.setText("You are player \"" + myMark + "\"");
    });

    myTurn = (myMark.equals(X_MARK)); // determine if client's turn

    // receive messages sent to client and output them
    while (true) {
      if (input.hasNextLine()) {
        processMessage(input.nextLine());
      }
    }
  }

  // process messages sent to the client
  private void processMessage(String message) {
    // valid move occurred
    switch (message) {
      case "Valid move.":
        displayMessage("Valid move, please wait.\n");
        setMark(currentSquare, myMark); // set mark in square
        break;
      case "Invalid move, try again":
        displayMessage(message + "\n"); // display invalid move
        myTurn = true; // still this client's turn
        break;
      case "Opponent moved":
        int location = input.nextInt(); // get move location
        input.nextLine(); // skip newline after int location
        int row = location / 3; // calculate row
        int column = location % 3; // calculate column
        setMark(board[row][column],
          (myMark.equals(X_MARK) ? O_MARK : X_MARK)); // mark move  
        displayMessage("Opponent moved. Your turn.\n");
        myTurn = true; // now this client's turn
        break;
      case "DEFEAT":
      case "TIE":
      case "VICTORY":
        //  Game is over, display the results and stop game
        displayMessage(message + "\n"); // display the message
        myTurn = false;
        break;
      default:
        displayMessage(message + "\n"); // display the message
        break;
    }
  }

  // manipulate displayArea in event-dispatch thread
  private void displayMessage(final String messageToDisplay) {
    SwingUtilities.invokeLater(() -> {
      displayArea.append(messageToDisplay); // updates output
    });
  }

  // utility method to set mark on board in event-dispatch thread
  private void setMark(final Square squareToMark, final String mark) {
    SwingUtilities.invokeLater(() -> {
      squareToMark.setMark(mark); // set mark in square
    });
  }

  // send message to server indicating clicked square
  public void sendClickedSquare(int location) {
    // if it is my turn
    if (myTurn) {
      output.format("%d\n", location); // send location to server
      output.flush();
      myTurn = false; // not my turn any more
    }
  }

  // set current Square
  public void setCurrentSquare(Square square) {
    currentSquare = square; // set current square to argument
  }

  // private inner class for the squares on the board
  private class Square extends JPanel {

    private String mark; // mark to be drawn in this square
    private final int location; // location of square

    public Square(String squareMark, int squareLocation) {
      mark = squareMark; // set mark for this square
      location = squareLocation; // set location of this square

      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
          setCurrentSquare(Square.this); // set current square

          // send location of this square
          sendClickedSquare(getSquareLocation());
        }
      });
    }

    // return preferred size of Square
    @Override
    public Dimension getPreferredSize() {
      return new Dimension(30, 30); // return preferred size
    }

    // return minimum size of Square
    @Override
    public Dimension getMinimumSize() {
      return getPreferredSize(); // return preferred size
    }

    // set mark for Square
    public void setMark(String newMark) {
      mark = newMark; // set mark of square
      repaint(); // repaint square
    }

    // return Square location
    public int getSquareLocation() {
      return location; // return location of square
    }

    // draw Square
    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      g.drawRect(0, 0, 29, 29); // draw square
      g.drawString(mark, 11, 20); // draw mark   
    }
  }
}
