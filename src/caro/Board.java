package caro;

import java.awt.Point;

public class Board {
	public static final int EMPTY = 0;
	public static final int X = 1;
	public static final int O = 2;
	public int size = 20;
	public int[][] matrix = new int[size][size];
	public Point flagPiece = new Point(-1, -1);

	public Board() {
	}

	/***
	 * đặt cờ vào bàn
	 * @param x vị trí x
	 * @param y vị trí y
	 * @param isX cờ x hay o
	 * @return 0 nếu vị trí x >= size hoặc y >= size hoặc vị trí đó đã được đặt, 1 nếu cờ được đặt bình thường, 2 nếu player 1 win, 3 nếu player 2 win
	 */
	public int setPiece(int x, int y, boolean isX) {
		if (x >= size || y >= size || matrix[x][y] != EMPTY) {
			return 0;
		}
		matrix[x][y] = isX ? X : O;
		flagPiece.setLocation(x, y);

		int player = checkWin(x, y);
		if (player == 1) {
			return 2;
		} else if (player == 2) {
			return 3;
		}
		return 1;
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
