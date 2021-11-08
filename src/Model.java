
/**
 * The purpose of this class is to control how the board updates, whether the game is over,
 * and overall manipulates the view so that the connect 4 game can be played properly.
 * @author Bergen Kjeseth, Yosef Jacobson
 */
import java.io.Serializable;

public class Model extends java.util.Observable {
	private final int COLUMNS = 7;
	private final int ROWS = 6;
	private Integer[][] board;
	private int color;
	private boolean isWinner;

	/**
	 * This constructor creates a double array to represent the connect 4 board and
	 * fills each spot with 0's
	 */
	public Model() {
		this.board = new Integer[ROWS][COLUMNS];
		this.color = 1;
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				board[i][j] = 0;
			}
		}
	}

	/**
	 * This returns the connect 4 board
	 * 
	 * @return the connect 4 board
	 */
	public Integer[][] getBoard() {
		return this.board;
	}

	/**
	 * This method updates the view by notifying the observer with a new
	 * Connect4MoveMessage class
	 * 
	 * @param col the column where the player chose to play
	 * @return a boolean saying whether the column is full or not.
	 */
	public Connect4MoveMessage update(int col) {
		Connect4MoveMessage toReturn = null;
		int row = 0;
		for (int i = 0; i < ROWS; i++) {
			if (board[i][col] != 0) {
				row = i + 1;
			}
		}
		if (row >= ROWS) {
			toReturn = new Connect4MoveMessage(0, 0, 0);
			setChanged();
			notifyObservers(toReturn);
			return toReturn;
		}
		int color = this.color;
		if (color == 1) {
			this.color = 2;
		} else {
			this.color = 1;
		}
		;
		board[row][col] = color;
		toReturn = new Connect4MoveMessage(row, col, color);
		this.setChanged();
		notifyObservers(toReturn);
		return toReturn;
	}

	/**
	 * This method checks over all of the rows, the the columns then all possible
	 * diagonals to see if any player has won yet. It checks the diagonals by using
	 * 4 helper methods to check each of the 4 corners to see if any diagonals exist
	 * there. It iterates over 4 different list of points to checks each of the 4
	 * diagonal spots and their corresponding points
	 * 
	 * @return a boolean stating whether or not the game is over
	 */
	public boolean isGameOver() {

		// checks rows
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < 4; j++) {
				int color = board[i][j];
				if (color != 0 && board[i][j] == color && board[i][j + 1] == color && board[i][j + 2] == color
						&& board[i][j + 3] == color) {
					if (board[i][j] == color)
						return true;
				}
			}
		}

		// checks columns
		for (int col = 0; col < COLUMNS; col++) {
			for (int row = 0; row < 3; row++) {
				int rcolor = board[row][col];
				if (rcolor != 0 && board[row][col] == rcolor && board[row + 1][col] == rcolor
						&& board[row + 2][col] == rcolor && board[row + 3][col] == rcolor) {
					return true;
				}
			}
		}

		// checks diagonal
		int[][] downRight = new int[][] { { 0, 0 }, { 0, 1 }, { 0, 2 }, { 0, 3 }, { 1, 0 }, { 2, 0 }, { 3, 0 } };

		for (int[] dR : downRight) {
			if (checkDownRight(dR[0], dR[1])) {
				return true;
			}
		}

		int[][] downLeft = new int[][] { { 0, 6 }, { 0, 5 }, { 0, 4 }, { 0, 3 }, { 1, 6 }, { 2, 6 }, { 3, 6 } };

		for (int[] dL : downLeft) {
			if (checkDownLeft(dL[0], dL[1])) {
				return true;
			}
		}

		int[][] upRight = new int[][] { { 5, 0 }, { 5, 1 }, { 5, 2 }, { 5, 3 }, { 4, 0 }, { 3, 0 } };

		for (int[] uR : upRight) {
			if (checkUpRight(uR[0], uR[1])) {
				return true;
			}
		}

		int[][] upLeft = new int[][] { { 5, 6 }, { 4, 6 }, { 3, 6 }, { 5, 5 }, { 5, 4 }, { 5, 3 } };

		for (int[] uL : upLeft) {
			if (checkUpLeft(uL[0], uL[1])) {
				return true;
			}
		}

		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLUMNS; j++) {
				if (board[i][j] == 0) {
					return false;
				}
			}
		}
		return true;

	}

	/**
	 * This method takes a point on the board and checks to see if a diagonal of 4
	 * player numbers exists there
	 * 
	 * @param row A row on the board
	 * @param col A column on the board
	 * @return a boolean seeing if a diagonal exists
	 */
	private boolean checkDownRight(int row, int col) {
		int color = board[row][col];
		if (row + 3 > 6 || col + 3 > 7) {
			return false;
		}
		if (color != 0 && board[row][col] == color && board[row + 1][col + 1] == color
				&& board[row + 2][col + 2] == color && board[row + 3][col + 3] == color) {
			return true;
		}
		return false;
	}

	/**
	 * This method takes a point on the board and checks to see if a diagonal of 4
	 * player numbers exists there
	 * 
	 * @param row A row on the board
	 * @param col A column on the board
	 * @return a boolean seeing if a diagonal exists
	 */
	private boolean checkUpRight(int row, int col) {
		int color = board[row][col];
		if (row - 3 < 0 || col + 3 > 7) {
			return false;
		}
		if (color != 0 && board[row][col] == color && board[row - 1][col + 1] == color
				&& board[row - 2][col + 2] == color && board[row - 3][col + 3] == color) {
			return true;
		}
		return false;
	}

	/**
	 * This method takes a point on the board and checks to see if a diagonal of 4
	 * player numbers exists there
	 * 
	 * @param row A row on the board
	 * @param col A column on the board
	 * @return a boolean seeing if a diagonal exists
	 */
	private boolean checkDownLeft(int row, int col) {
		int color = board[row][col];
		if (color != 0 && board[row][col] == color && board[row + 1][col - 1] == color
				&& board[row + 2][col - 2] == color && board[row + 3][col - 3] == color) {
			return true;
		}
		return false;
	}

	/**
	 * This method takes a point on the board and checks to see if a diagonal of 4
	 * player numbers exists there
	 * 
	 * @param row A row on the board
	 * @param col A column on the board
	 * @return a boolean seeing if a diagonal exists
	 */
	private boolean checkUpLeft(int row, int col) {
		int color = board[row][col];
		if (color != 0 && board[row][col] == color && board[row - 1][col - 1] == color
				&& board[row - 2][col - 2] == color && board[row - 3][col - 3] == color) {
			return true;
		}
		return false;
	}

}
