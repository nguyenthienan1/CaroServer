package caro;

import java.awt.Point;

public class Board {
	int row = 20;
	int col = 20;
	int[][] matrix = new int[20][20];
	public Point flagPiece = new Point(-1, -1);

	public Board() {
	}

	public int setPiece(int x, int y, boolean isX) {
		if (x >= 20 || y >= 20 || matrix[x][y] != 0) {
			return 0;
		}
		matrix[x][y] = isX ? 1 : 2;
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
		for (int i = y + 1; i < row; i++) {
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
		for (int i = x + 1; i < row; i++) {
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
		for (int i = x + 1, j = y - 1; i < row && j >= 0; i++, j--) {
			if (matrix[i][j] == matrix[x][y]) {
				count++;
			} else {
				break;
			}
		}
		for (int i = x - 1, j = y + 1; i >= 0 && j < row; i--, j++) {
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
		for (int i = x + 1, j = y + 1; i < row && j < row; i++, j++) {
			if (matrix[i][j] == matrix[x][y]) {
				count++;
			} else {
				break;
			}
		}
		return count + 1;
	}
}
