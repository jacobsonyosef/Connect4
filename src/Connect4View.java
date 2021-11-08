import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * GUI for the Connect4 game.
 * 
 * Displays a visual representation of the game board, and provides options to
 * start a networked game as a player or computer and as the server or client.
 * Supports mouse input for placing tokens. Displays either a win or loss
 * message when the game is over.
 * 
 * @author Yosef Jacobson
 *
 */
public class Connect4View extends Application implements Observer {
	private Controller controller;
	private GridPane gameView;
	private BorderPane mainWindow;
	private EventHandler<MouseEvent> clickHandler;

	/**
	 * Refreshes the view when a new move has been made in the model.
	 * 
	 * When notified by the corresponding Model of a change, iterates over all of
	 * the drawn Circles and sets the correct Circle to the correct color. Displays
	 * an error message if the chosen column is full, and displays a game over
	 * message when the game ends.
	 * 
	 * @param o   the Model notifying this of a change
	 * @param arg a Connect4MoveMessage containing the last move's information
	 */
	@Override
	public void update(Observable o, Object arg) {
		Connect4MoveMessage turnInfo = (Connect4MoveMessage) arg;
		int row = 5 - turnInfo.getRow();
		int col = turnInfo.getColumn();
		Paint color = (turnInfo.getColor() == 1) ? Color.YELLOW : Color.RED;

		if (turnInfo.getRow() == 0 && col == 0 && turnInfo.getColor() == 0) {
			Alert moveError = new Alert(Alert.AlertType.ERROR, "Column full, pick somewhere else!");
			moveError.showAndWait();
		}

		else {
			ObservableList<Node> circles = gameView.getChildren();
			for (Node c : circles) {
				if (GridPane.getRowIndex(c) == row && GridPane.getColumnIndex(c) == col) {
					Circle changed = (Circle) c;
					changed.setFill(color);
					break;
				}
			}

			if (controller.isGameOver()) {
				if (!controller.isLoser()) {
					gameOver("You won!");
				}

				else {
					gameOver("You lost. :(");
				}
			}
		}
	}

	/**
	 * Launches the GUI
	 * 
	 * @param stage the main window stage
	 */
	@Override
	public void start(Stage stage) throws Exception {
		initialize(stage);
	}

	/**
	 * Draws all GUI elements and sets up a model and controller for gameplay
	 * 
	 * @param stage the main window stage
	 */
	public void initialize(Stage stage) {
		Model model = new Model();
		model.addObserver(this);

		controller = new Controller(model);

		drawView();

		Scene scene = new Scene(mainWindow, 344, 321);
		stage.setTitle("Connect 4");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Draws all GUI elements, including the Circles representing Connect4 tokens,
	 * and a file menu that launches the network config dialog
	 */
	private void drawView() {
		// gameView is the main view with all the Circles
		gameView = new GridPane();
		gameView.setVgap(8);
		for (int k = 0; k < 7; k++) {
			ColumnConstraints columnFormat = new ColumnConstraints(48);
			columnFormat.setHalignment(HPos.CENTER);
			gameView.getColumnConstraints().add(columnFormat);
		}

		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 7; j++) {
				gameView.add(new Circle(20, Color.WHITE), j, i);
			}
		}

		// mainWindow is the GUI window, it contains the gameView and a menu
		mainWindow = new BorderPane();
		MenuBar menu = new MenuBar();
		Menu fileMenu = new Menu("File");
		MenuItem newGame = new MenuItem("New Game");

		// when "New Game" is selected from the menu, generates the network config
		// dialog, gets the user input when the dialog is closed, and performs the
		// appropriate action
		newGame.setOnAction((event) -> {
			NetworkSetupScreen setup = new NetworkSetupScreen();
			if (!setup.getCancelled()) {
				boolean isHuman = setup.getIsHuman();
				boolean isServer = setup.getIsServer();
				int port = setup.getPort();
				String ip = setup.getIP();
				if (isServer && isHuman) {
					controller.startServer(port);
					enableHuman();
				}

				else if (isHuman) {
					enableHuman();
					controller.startClient(ip, port, true);
				}

				else if (isServer) {
					controller.startServer(port);
					enableComputer(true);
				}

				else {
					controller.startClient(ip, port, false);
					enableComputer(false);
				}
			}
		});
		fileMenu.getItems().add(newGame);
		menu.getMenus().add(fileMenu);
		mainWindow.setTop(menu);

		mainWindow.setBackground(new Background(new BackgroundFill(Color.BLUE, null, null)));
		mainWindow.setCenter(gameView);
		BorderPane.setMargin(gameView, new Insets(8, 4, 8, 4));
	}

	/**
	 * Sets up a MouseEvent EventHandler to get user input for a human player
	 */
	private void enableHuman() {
		clickHandler = new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				int xPos = (int) event.getSceneX();
				int yPos = (int) event.getSceneY();

				if (yPos > 25) {
					if (xPos <= 52) {
						controller.humanTurn(0);
					}

					else if (xPos <= 100) {
						controller.humanTurn(1);
					}

					else if (xPos <= 148) {
						controller.humanTurn(2);
					}

					else if (xPos <= 196) {
						controller.humanTurn(3);
					}

					else if (xPos <= 244) {
						controller.humanTurn(4);
					}

					else if (xPos <= 292) {
						controller.humanTurn(5);
					}

					else {
						controller.humanTurn(6);
					}
				}
			}

		};
		mainWindow.addEventFilter(MouseEvent.MOUSE_CLICKED, clickHandler);
	}

	/**
	 * Creates an AI player to randomly make moves
	 * 
	 * @param isServer whether this player is the client or the server
	 */
	private void enableComputer(boolean isServer) {
		if (isServer) {
			controller.computerTurn();
		}

		else {
			controller.computerReceiveTurn();
		}
	}

	/**
	 * Displays a "game over" window that displays @param message
	 */
	private void gameOver(String message) {
		Alert popup = new Alert(Alert.AlertType.INFORMATION, message);
		popup.showAndWait();
		if (clickHandler != null) {
			mainWindow.removeEventFilter(MouseEvent.MOUSE_CLICKED, clickHandler);
		}
	}
}

/**
 * The Network Setup/Config window that allows the player to choose how to set
 * up their game. Displays all network options and saves the user's choices to
 * be received by the game when the window is closed.
 * 
 * @author Yosef Jacobson
 *
 */
class NetworkSetupScreen extends Stage {
	// user options
	private boolean isServer = true;
	private boolean isHuman = true;
	private boolean cancelled = true;
	private int port;
	private String ip;

	/**
	 * Constructor for NetworkSetupScreen
	 * 
	 * Draws all UI elements for the config screen, and saves their values to be
	 * accessed by the main game
	 */
	public NetworkSetupScreen() {
		// setting up the main window
		this.setWidth(450);
		this.setHeight(200);
		this.setTitle("Network Setup");
		initModality(Modality.APPLICATION_MODAL);

		// setting up radio buttons for Client/Server option
		ToggleGroup serverClient = new ToggleGroup();
		RadioButton server = new RadioButton("Server");
		server.setOnAction((event) -> {
			isServer = true;
		});
		server.setSelected(true);
		server.setToggleGroup(serverClient);
		RadioButton client = new RadioButton("Client");
		client.setOnAction((event) -> {
			isServer = false;
		});
		client.setToggleGroup(serverClient);

		// setting up radio buttons for Human/Computer option
		ToggleGroup humanComputer = new ToggleGroup();
		RadioButton human = new RadioButton("Human");
		human.setOnAction((event) -> {
			isHuman = true;
		});
		human.setSelected(true);
		human.setToggleGroup(humanComputer);
		RadioButton computer = new RadioButton("Computer");
		computer.setOnAction((event) -> {
			isHuman = false;
		});
		computer.setToggleGroup(humanComputer);

		// setting up a GridPane to hold all the radio buttons and their labels
		GridPane options = new GridPane();
		options.setHgap(10);
		options.setVgap(15);
		options.add(new Label("Create: "), 0, 0);
		options.add(server, 1, 0);
		options.add(client, 2, 0);
		options.add(new Label("Play as: "), 0, 1);
		options.add(human, 1, 1);
		options.add(computer, 2, 1);

		// setting up a GridPane to hold the IP and Port text fields, and creating said
		// text fields
		GridPane textInput = new GridPane();
		textInput.setHgap(10);
		TextField serverAddress = new TextField("localhost");
		TextField port = new TextField("4000");
		textInput.add(new Label("Server"), 0, 0);
		textInput.add(serverAddress, 1, 0);
		textInput.add(new Label("Port"), 2, 0);
		textInput.add(port, 3, 0);

		// setting up buttons for "OK" and "Cancel" and a GridPane to hold them
		Button ok = new Button("OK");
		ok.setOnAction((event) -> {
			cancelled = false;
			this.port = Integer.valueOf(port.getText());
			ip = serverAddress.getText();
			Stage window = (Stage) ok.getScene().getWindow();
			window.close();
		});
		Button cancel = new Button("Cancel");
		cancel.setOnAction((event) -> {
			Stage window = (Stage) cancel.getScene().getWindow();
			window.close();
		});
		GridPane buttons = new GridPane();
		buttons.setHgap(10);
		buttons.add(ok, 0, 0);
		buttons.add(cancel, 1, 0);

		// setting up the main FlowPane to hold all previously created UI elements
		FlowPane mainView = new FlowPane();
		mainView.setVgap(15);
		mainView.getChildren().addAll(options, textInput, buttons);
		FlowPane.setMargin(options, new Insets(10, 0, 0, 10));
		FlowPane.setMargin(textInput, new Insets(0, 0, 0, 10));
		FlowPane.setMargin(buttons, new Insets(0, 0, 0, 10));

		// set window to display on creation
		this.setScene(new Scene(mainView));
		this.showAndWait();
	}

	/**
	 * @return ip the input IP address
	 */
	public String getIP() {
		return ip;
	}

	/**
	 * @return port the input port number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return isHuman whether or not the player is a human
	 */
	public boolean getIsHuman() {
		return isHuman;
	}

	/**
	 * @return isServer whether the game will serve as the server or client
	 */
	public boolean getIsServer() {
		return isServer;
	}

	/**
	 * @return cancelled whether or not the dialog was cancelled
	 */
	public boolean getCancelled() {
		return cancelled;
	}
}