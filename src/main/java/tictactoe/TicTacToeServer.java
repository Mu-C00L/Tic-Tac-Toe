// License: GPL. For details, see LICENSE file.
package tictactoe;

import java.awt.BorderLayout;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author Kishan
 */
public class TicTacToeServer extends JFrame {

  private final String[] board = new String[9]; // tic-tac-toe board
  private final JTextArea outputArea; // for outputting moves
  private final Player[] players; // array of Players
  private ServerSocket server; // server socket to connect with clients
  private int currentPlayer; // keeps track of player with current move
  private final static int PLAYER_X = 0; // constant for first player
  private final static int PLAYER_O = 1; // constant for second player
  private final static String[] MARKS = {"X", "O"}; // array of marks
  private final ExecutorService runGame; // will run players
  private final Lock gameLock; // to lock game for synchronization
  private final Condition otherPlayerConnected; // to wait for other player
  private final Condition otherPlayerTurn; // to wait for other player's turn

  // set up tic-tac-toe server and GUI that displays messages
  public TicTacToeServer() {
    super("Tic-Tac-Toe Server"); // set title of window

    // create ExecutorService with a thread for each player
    runGame = Executors.newFixedThreadPool(2);
    gameLock = new ReentrantLock(); // create lock for game

    // condition variable for both players being connected
    otherPlayerConnected = gameLock.newCondition();

    // condition variable for the other player's turn
    otherPlayerTurn = gameLock.newCondition();

    for (int i = 0; i < 9; i++) {
      board[i] = ""; // create tic-tac-toe board
    }
    players = new Player[2]; // create array of players
    currentPlayer = PLAYER_X; // set current player to first player

    try {
      server = new ServerSocket(12345, 2); // set up ServerSocket
    } catch (IOException ioException) {
      System.out.println(ioException.toString());
    }

    outputArea = new JTextArea(); // create JTextArea for output
    add(outputArea, BorderLayout.CENTER);
    outputArea.setText("Server awaiting connections\n");
    setSize(300, 300); // set size of window
    setVisible(true); // show window
  }

  // wait for two connections so game can be played
  public void execute() {
    // wait for each client to connect
    for (int i = 0; i < players.length; i++) {
      // wait for connection, create Player, start runnable
      try {
        /**
         * When a client connects, a new Player object is created to manage the connection as a separate thread, and the thread is executed in the runGame thread pool.
         *
         * The Player constructor receives the Socket object representing the connection to the client and gets the associated input and output streams.
         */
        players[i] = new Player(server.accept(), i);
        runGame.execute(players[i]); // execute player runnable
      } catch (IOException ioException) {
        System.out.println(ioException.toString());
        System.exit(1);
      }
    }

    gameLock.lock(); // lock game to signal player X's thread

    try {
      players[PLAYER_X].setSuspended(false); // resume player X
      otherPlayerConnected.signal(); // wake up player X's thread
    } finally {
      gameLock.unlock(); // unlock game after signalling player X
    }
  }

  // display message in outputArea
  private void displayMessage(final String messageToDisplay) {
    // display message from event-dispatch thread of execution
    SwingUtilities.invokeLater(() -> {
      // updates outputArea
      outputArea.append(messageToDisplay); // add message
    });
  }

  // determine if move is valid
  public boolean validateAndMove(int location, int player) {
    // while not current player, must wait for turn
    while (player != currentPlayer) {
      gameLock.lock(); // lock game to wait for other player to go

      try {
        otherPlayerTurn.await(); // wait for player's turn
      } catch (InterruptedException exception) {
        System.out.println(exception.toString());
      } finally {
        gameLock.unlock(); // unlock game after waiting
      }
    }

    // if location not occupied, make move
    if (!isOccupied(location)) {
      board[location] = MARKS[currentPlayer]; // set move on board
      currentPlayer = (currentPlayer + 1) % 2; // change player

      // let new current player know that move occurred
      players[currentPlayer].otherPlayerMoved(location);

      gameLock.lock(); // lock game to signal other player to go

      try {
        otherPlayerTurn.signal(); // signal other player to continue
      } finally {
        gameLock.unlock(); // unlock game after signaling
      }

      return true; // notify player that move was valid
    } else {
      // move was not valid
      return false; // notify player that move was invalid
    }
  }

  // determine whether location is occupied
  public boolean isOccupied(int location) {
    return board[location].equals(MARKS[PLAYER_X]) || board[location].equals(MARKS[PLAYER_O]);
  }

  /**
   * Check if there is 3 of the same marks in a row.
   *
   * @return True if there is a winner, false if there is not a winner.
   */
  public boolean hasWinner() {
    return (!board[0].isEmpty() && board[0].equals(board[1]) && board[0].equals(board[2]))
      || (!board[3].isEmpty() && board[3].equals(board[4]) && board[3].equals(board[5]))
      || (!board[6].isEmpty() && board[6].equals(board[7]) && board[6].equals(board[8]))
      || (!board[0].isEmpty() && board[0].equals(board[3]) && board[0].equals(board[6]))
      || (!board[1].isEmpty() && board[1].equals(board[4]) && board[1].equals(board[7]))
      || (!board[2].isEmpty() && board[2].equals(board[5]) && board[2].equals(board[8]))
      || (!board[0].isEmpty() && board[0].equals(board[4]) && board[0].equals(board[8]))
      || (!board[2].isEmpty() && board[2].equals(board[4]) && board[2].equals(board[6]));
  }

  /**
   * Check if the game board is full.
   *
   * @return True if the board is full, false if there is an empty slot.
   */
  public boolean boardFilledUp() {
    for (int i = 0; i < board.length; ++i) {
      if (board[i].isEmpty()) {
        return false;
      }
    }

    return true;
  }

  // place code in this method to determine whether game over 
  public boolean isGameOver() {
    return hasWinner() || boardFilledUp();
  }

  // private inner class Player manages each Player as a runnable
  private class Player implements Runnable {

    private final Socket connection; // connection to client
    private Scanner input; // input from client
    private Formatter output; // output to client
    private final int playerNumber; // tracks which player this is
    private final String mark; // mark for this player
    private boolean suspended = true; // whether thread is suspended

    // set up Player thread
    public Player(Socket socket, int number) {
      playerNumber = number; // store this player's number
      mark = MARKS[playerNumber]; // specify player's mark
      connection = socket; // store socket for client

      // obtain streams from Socket
      try {
        input = new Scanner(connection.getInputStream());
        output = new Formatter(connection.getOutputStream());
      } catch (IOException ioException) {
        System.out.println(ioException.toString());
        System.exit(1);
      }
    }

    // send message that other player moved
    public void otherPlayerMoved(int location) {
      output.format("Opponent moved\n");
      output.format("%d\n", location); // send location of move
      output.flush(); // flush output
      output.format(hasWinner() ? "DEFEAT\n" : boardFilledUp() ? "TIE\n" : "");
      output.flush();
    }

    // control thread's execution
    @Override
    public void run() {
      // send client its mark (X or O), process messages from client
      try {
        displayMessage("Player " + mark + " connected\n");
        output.format("%s\n", mark); // send player's mark
        output.flush(); // flush output

        /**
         * Wait until both players are connected
         */
        // if player X, wait for another player to arrive
        if (playerNumber == PLAYER_X) {
          output.format("%s\n%s", "Player X connected", "Waiting for another player\n");
          output.flush(); // flush output
          gameLock.lock(); // lock game to  wait for second player

          try {
            while (suspended) {
              otherPlayerConnected.await(); // wait for player O
            }
          } catch (InterruptedException exception) {
            System.out.println(exception.toString());
          } finally {
            gameLock.unlock(); // unlock game after second player
          }

          // send message that other player connected
          output.format("Other player connected. Your move.\n");
          output.flush(); // flush output
        } else {
          output.format("Player O connected, please wait\n");
          output.flush(); // flush output
        }

        // while game not over
        while (!isGameOver()) {
          int location = 0; // initialize move location

          if (input.hasNext()) {
            location = input.nextInt(); // get move location
          }
          // check for valid move
          if (validateAndMove(location, playerNumber)) {
            displayMessage("\nlocation: " + location);
            output.format("Valid move.\n"); // notify client
            output.flush(); // flush output
            output.format(hasWinner() ? "VICTORY\n" : boardFilledUp() ? "TIE\n" : "");
            output.flush();
          } else {
            // move was invalid
            output.format("Invalid move, try again\n");
            output.flush(); // flush output
          }
        }
      } finally {
        try {
          connection.close(); // close connection to client
        } catch (IOException ioException) {
          System.out.println(ioException.toString());
          System.exit(1);
        }
      }
    }

    // set whether or not thread is suspended
    public void setSuspended(boolean status) {
      suspended = status; // set value of suspended
    }
  }
}
