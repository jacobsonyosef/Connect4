import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import javafx.application.Platform;

/**
 * This class is used to manipulate the controller based on the actions from the
 * view
 * 
 * @author Yosef Jacobson
 *
 */
public class Controller {

	private Model model;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private boolean isLoser, isConnected, isTurn;

	/**
	 * Constructor. Ties Controller to @param model
	 */
	public Controller(Model model) {
		this.model = model;
		isLoser = true;
		isConnected = false;
		isTurn = false;
	}

	/**
	 * This method calls the update method in the model with what column was chosen
	 * by the player, then sends the move and waits to receive the next one if a
	 * connection has been established.
	 * 
	 * @param col the column chosen by the player
	 * @return whether the column is full or not
	 */
	public void humanTurn(int col) {
		if (isTurn) {
			Connect4MoveMessage move = model.update(col);

			if (isConnected) {
				isLoser = true;
				sendAndReceive(move);
			}
		}
	}

	/**
	 * Used for the first turn of the client, receives a move without making one
	 * first.
	 * 
	 * @throws ClassNotFoundException or IOException if there is an error reading
	 *                                the next move
	 */
	public void humanReceiveTurn() {
		new Thread(() -> {
			try {
				Connect4MoveMessage nextMove = (Connect4MoveMessage) input.readObject();
				Platform.runLater(() -> {
					model.update(nextMove.getColumn());
					isTurn = true;
				});
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

	}

	/**
	 * While the game hasn't ended, chooses a random column for the computer to play
	 * and calls update with that column, then sends the move and waits for the next
	 * one if a connection has been established
	 * 
	 */
	public void computerTurn() {
		while (!isGameOver()) {
			if (isTurn) {
				Random rand = new Random();
				int col = rand.nextInt(7);
				Connect4MoveMessage move = model.update(col);

				if (isConnected) {
					isLoser = true;
					sendAndReceiveAI(move);
				}
			}
		}
	}

	/**
	 * First has the computer wait to receive the first turn, then calls
	 * computerTurn() to begin the AI's game loop
	 * 
	 * @throws ClassNotFoundException or IOException if there is an error reading
	 *                                the next move
	 */
	public void computerReceiveTurn() {
		try {
			Connect4MoveMessage nextMove = (Connect4MoveMessage) input.readObject();
			model.update(nextMove.getColumn());
			isTurn = true;
			computerTurn();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Sends @param move to the other player, then waits for the next move if the
	 * game hasn't ended
	 * 
	 * @throws ClassNotFoundException or IOException if there is an error writing
	 *                                this move or reading the next move
	 */
	private void sendAndReceive(Connect4MoveMessage move) {
		try {
			isTurn = false;
			output.writeObject(move);
			new Thread(() -> {
				if (!isGameOver()) {
					try {
						Connect4MoveMessage nextMove = (Connect4MoveMessage) input.readObject();
						Platform.runLater(() -> {
							model.update(nextMove.getColumn());
							isLoser = false;
							isTurn = true;
						});
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Non-threaded version of sendAndReceive() for AI players
	 * 
	 * @param move the move to be sent
	 * @throws ClassNotFoundException or IOException if there is an error writing
	 *                                this move or reading the next move
	 */
	private void sendAndReceiveAI(Connect4MoveMessage move) {
			try {
				isTurn = false;
				output.writeObject(move);
				if (!isGameOver()) {
					try {
						Connect4MoveMessage nextMove = (Connect4MoveMessage) input.readObject();
						model.update(nextMove.getColumn());
						isLoser = false;
						isTurn = true;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * This method calls the model method isGameOver to determine if someone has
	 * won.
	 * 
	 * @return whether the game is over or not
	 */
	public boolean isGameOver() {
		return model.isGameOver();
	}

	/**
	 * @return whether or not this instance of the game was the loser
	 */
	public boolean isLoser() {
		return isLoser;
	}

	/**
	 * Starts a server instance
	 * 
	 * Waits for a connection from a client, then sets the input and output streams
	 * to the client's
	 * 
	 * @param port the port to be opened for the server
	 * 
	 * @throws IOException if there's a problem opening the server or getting the
	 *                     connection
	 */
	public void startServer(int port) {
		try {
			ServerSocket server = new ServerSocket(port);
			Socket connection = server.accept();
			output = new ObjectOutputStream(connection.getOutputStream());
			input = new ObjectInputStream(connection.getInputStream());
			isConnected = true;
			isTurn = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts a client instance
	 * 
	 * Connects to the server at @param address and @param port, then set the input
	 * and output streams to the server's, and sets the client to receive the first
	 * move
	 * 
	 * @throws UnknownHostException if there's a problem connecting to the specified
	 *                              address and port
	 * 
	 * @throws IOException          if there's a problem getting the socket or IO
	 *                              streams from the server
	 */
	public void startClient(String address, int port, boolean isHuman) {
		try {
			Socket server = new Socket(address, port);
			output = new ObjectOutputStream(server.getOutputStream());
			input = new ObjectInputStream(server.getInputStream());
			isConnected = true;
			isTurn = false;

			if (isHuman) {
				humanReceiveTurn();
			}

			else {
				computerReceiveTurn();
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
