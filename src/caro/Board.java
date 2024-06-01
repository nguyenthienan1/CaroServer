package caro;

import java.awt.Point;

public class Board {
	public static final int EMPTY = 0;
	public static final int X = 1;
	public static final int O = 2;
	public int size = 20;
	public int[][] matrix = new int[size][size];
	public Point flagPiece = new Point(-1, -1);
	public int countMoves;

	public Board() {
	}

	public int getSize() {
		return size;
	}

	public int getCell(int i, int j) {
		return matrix[i][j];
	}

	public void placeMove(int x, int y, int symbol) {
		if (matrix[x][y] == EMPTY) {
			matrix[x][y] = symbol;
		}
	}

	public void undoMove(int x, int y) {
		matrix[x][y] = EMPTY;
	}

	/***
	 * đặt cờ vào bàn
	 * 
	 * @param x   vị trí x
	 * @param y   vị trí y
	 * @param isX cờ x hay o
	 * @return 0 nếu vị trí x >= size hoặc y >= size hoặc vị trí đó đã được đặt, 1
	 *         nếu cờ được đặt bình thường, 2 nếu player 1 win, 3 nếu player 2 win,
	 *         4 nếu bàn cờ đầy
	 */
	public int setPiece(int x, int y, int symbol) {
		if (x >= size || y >= size || matrix[x][y] != EMPTY) {
			return 0;
		}
		matrix[x][y] = symbol;
		flagPiece.setLocation(x, y);
		countMoves++;

		int player = checkWin(x, y);
		if (player == 1) {
			return 2;
		} else if (player == 2) {
			return 3;
		}

		if (isFull()) {
			return 4;
		}
		return 1;
	}

	public int evaluateBoard(int player) {
		int score = 0;

		// Đánh giá hàng ngang
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (matrix[i][j] == player) {
					score += evaluatePosition(i, j, player, 1, 0);
				}
			}
		}

		// Đánh giá hàng dọc
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (matrix[i][j] == player) {
					score += evaluatePosition(i, j, player, 0, 1);
				}
			}
		}

		// Đánh giá đường chéo chính
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (matrix[i][j] == player) {
					score += evaluatePosition(i, j, player, 1, 1);
				}
			}
		}

		// Đánh giá đường chéo phụ
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (matrix[i][j] == player) {
					score += evaluatePosition(i, j, player, 1, -1);
				}
			}
		}

		return score;
	}

	private int evaluatePosition(int x, int y, int player, int dx, int dy) {
		int count = 0;
		int score = 0;
		int empty = 0;

		// Đếm số lượng quân cờ liên tiếp
		for (int i = 0; i < 5; i++) {
			int nx = x + i * dx;
			int ny = y + i * dy;
			if (nx >= 0 && nx < matrix.length && ny >= 0 && ny < matrix[0].length) {
				if (matrix[nx][ny] == player) {
					count++;
				} else if (matrix[nx][ny] == EMPTY) {
					empty++;
				}
			} else {
				return 0; // Ra ngoài biên thì giá trị là 0
			}
		}

		if (count == 4 && empty == 1) {
			score += 10000; // Bốn quân liên tiếp và một ô trống
		} else if (count == 3 && empty == 2) {
			score += 1000; // Ba quân liên tiếp và hai ô trống
		} else if (count == 2 && empty == 3) {
			score += 100; // Hai quân liên tiếp và ba ô trống
		} else if (count == 1 && empty == 4) {
			score += 10; // Một quân liên tiếp và bốn ô trống
		}
		return score;
	}

	public boolean isEmpty() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] != EMPTY) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isFull() {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] == EMPTY) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean checkWinCondition(int player) {
		// Check rows
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size - 4; j++) {
				if (matrix[i][j] == player && matrix[i][j + 1] == player && matrix[i][j + 2] == player
						&& matrix[i][j + 3] == player && matrix[i][j + 4] == player) {
					return true;
				}
			}
		}

		// Check columns
		for (int i = 0; i < size - 4; i++) {
			for (int j = 0; j < size; j++) {
				if (matrix[i][j] == player && matrix[i + 1][j] == player && matrix[i + 2][j] == player
						&& matrix[i + 3][j] == player && matrix[i + 4][j] == player) {
					return true;
				}
			}
		}

		// Check main diagonals
		for (int i = 0; i < size - 4; i++) {
			for (int j = 0; j < size - 4; j++) {
				if (matrix[i][j] == player && matrix[i + 1][j + 1] == player && matrix[i + 2][j + 2] == player
						&& matrix[i + 3][j + 3] == player && matrix[i + 4][j + 4] == player) {
					return true;
				}
			}
		}

		// Check anti-diagonals
		for (int i = 0; i < size - 4; i++) {
			for (int j = 4; j < size; j++) {
				if (matrix[i][j] == player && matrix[i + 1][j - 1] == player && matrix[i + 2][j - 2] == player
						&& matrix[i + 3][j - 3] == player && matrix[i + 4][j - 4] == player) {
					return true;
				}
			}
		}

		return false;
	}

	private int checkWin(int x, int y) {
		if (countVertical(x, y) >= 5 || countHorizontal(x, y) >= 5 || countDiagonal1(x, y) >= 5
				|| countDiagonal2(x, y) >= 5) {
			return matrix[x][y];
		}
		return 0;
	}

	public int countVertical(int x, int y) {
		int count = 0;
		for (int i = y + 1; i < size; i++) {
			if (matrix[x][i] == matrix[x][y]) {
				count++;
			} else {
				break;
			}
		}
		for (int i = y - 1; i >= 0; i--) {
			if (matrix[x][i] == matrix[x][y]) {
				count++;
			} else {
				break;
			}
		}
		return count + 1;
	}

	public int countHorizontal(int x, int y) {
		int count = 0;
		for (int i = x + 1; i < size; i++) {
			if (matrix[i][y] == matrix[x][y]) {
				count++;
			} else {
				break;
			}
		}
		for (int i = x - 1; i >= 0; i--) {
			if (matrix[i][y] == matrix[x][y]) {
				count++;
			} else {
				break;
			}
		}
		return count + 1;
	}

	public int countDiagonal1(int x, int y) {
		int count = 0;
		for (int i = x + 1, j = y - 1; i < size && j >= 0; i++, j--) {
			if (matrix[i][j] == matrix[x][y]) {
				count++;
			} else {
				break;
			}
		}
		for (int i = x - 1, j = y + 1; i >= 0 && j < size; i--, j++) {
			if (matrix[i][j] == matrix[x][y]) {
				count++;
			} else {
				break;
			}
		}
		return count + 1;
	}

	public int countDiagonal2(int x, int y) {
		int count = 0;
		for (int i = x - 1, j = y - 1; i >= 0 && j >= 0; i--, j--) {
			if (matrix[i][j] == matrix[x][y]) {
				count++;
			} else {
				break;
			}
		}
		for (int i = x + 1, j = y + 1; i < size && j < size; i++, j++) {
			if (matrix[i][j] == matrix[x][y]) {
				count++;
			} else {
				break;
			}
		}
		return count + 1;
	}
}
