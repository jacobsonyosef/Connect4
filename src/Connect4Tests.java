import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test suite for Connect4. Tests non-networked controller/model operations
 * 
 * @author Yosef Jacobson
 *
 */

public class Connect4Tests {

	@Test
	void controllerTest1() {
		Model model = new Model();
		Controller controller = new Controller(model);

		for (int i = 0; i < 7; i++) {
			controller.humanTurn(i);
		}

		assertEquals(controller.isGameOver(), false);

		for (int i = 0; i < 3; i++) {
			controller.humanTurn(1);
			controller.humanTurn(2);
		}

		assertEquals(controller.isGameOver(), true);
	}

	@Test
	void controllerTest2() {
		Model model = new Model();
		Controller controller = new Controller(model);

		controller.computerTurn();
		assertEquals(controller.isLoser(), true);
	}
}
