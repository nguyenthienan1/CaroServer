package caro;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import network.logic.Service;

import static caro.Board.*;

public class AIPlayer extends Player {
	private static final int MAX_DEPTH = 1;
	private Service service = new ServiceTemp();

	public AIPlayer() {
		username = "bot";
		id = -10;
	}

	public Service getService() {
		return service;
	}

	public void move() {
		int[] bestMove = makeMove();
//		System.out.println("best move " + bestMove[0] + " " + bestMove[1]);
		room.setPiece(bestMove[0], bestMove[1], this);
	}

	public int[] makeMove() {
		Board board = room.board;

		//random nước đi đầu tiên của bot
		if (board.isEmpty() || board.countMoves < 2) {
			int xRandom = ThreadLocalRandom.current().nextInt(7, 13);
			int yRandom = ThreadLocalRandom.current().nextInt(7, 13);
			return new int[] { xRandom, yRandom };
		}

		int[] bestMove = null;
		int bestValue = Integer.MIN_VALUE;

		for (int i = 0; i < board.getSize(); i++) {
			for (int j = 0; j < board.matrix[i].length; j++) {
				if (board.getCell(i, j) == EMPTY) {
					board.placeMove(i, j, symbol);
					int moveValue = minimax(board, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
					board.undoMove(i, j); // Undo move
					if (moveValue > bestValue) {
						bestMove = new int[] { i, j };
						bestValue = moveValue;
					}
				}
			}
		}
		return bestMove;
	}

	private int minimax(Board board, int depth, int alpha, int beta, boolean isMaximizing) {
		int opponent = (symbol == X) ? O : X;

		if (board.checkWinCondition(symbol)) {
			return 1000 - depth;
		} else if (board.checkWinCondition(opponent)) {
			return -1000 + depth;
		} else if (board.isFull() || depth == MAX_DEPTH) {
			return board.evaluateBoard(symbol) - board.evaluateBoard(opponent);
		}

		if (isMaximizing) {
			int maxEval = Integer.MIN_VALUE;
			for (int i = 0; i < board.getSize(); i++) {
				for (int j = 0; j < board.matrix[i].length; j++) {
					if (board.getCell(i, j) == EMPTY) {
						board.placeMove(i, j, symbol);
						int eval = minimax(board, depth + 1, alpha, beta, false);
						board.undoMove(i, j); // Undo move
						maxEval = Math.max(maxEval, eval);
						alpha = Math.max(alpha, eval);
						if (beta <= alpha) {
							break;
						}
					}
				}
			}
			return maxEval;
		} else {
			int minEval = Integer.MAX_VALUE;
			for (int i = 0; i < board.getSize(); i++) {
				for (int j = 0; j < board.matrix[i].length; j++) {
					if (board.getCell(i, j) == EMPTY) {
						board.placeMove(i, j, opponent);
						int eval = minimax(board, depth + 1, alpha, beta, true);
						board.undoMove(i, j); // Undo move
						minEval = Math.min(minEval, eval);
						beta = Math.min(beta, eval);
						if (beta <= alpha) {
							break;
						}
					}
				}
			}
			return minEval;
		}
	}

	private class ServiceTemp implements Service {

		@Override
		public void sendMessageDialog(String mes) {
			// TODO Auto-generated method stub

		}

		@Override
		public void loginSuccess() {
			// TODO Auto-generated method stub

		}

		@Override
		public void logOutSuccess() {
			// TODO Auto-generated method stub

		}

		@Override
		public void joinRoomSuccess(int num) {
			// TODO Auto-generated method stub

		}

		@Override
		public void sendListRoom() {
			// TODO Auto-generated method stub

		}

		@Override
		public void leaveRoomSuccess() {
			// TODO Auto-generated method stub

		}

		@Override
		public void sendBoard(Board board) {
			// TODO Auto-generated method stub

		}

		@Override
		public void sendChatRoom(String content) {
			// TODO Auto-generated method stub

		}

		@Override
		public void sendResetBoard() {
			// TODO Auto-generated method stub

		}

		@Override
		public void senListPlayerInRoom(Set<Player> players, int type) {
			// TODO Auto-generated method stub

		}

		@Override
		public void sendInfoRoom(String info) {
			// TODO Auto-generated method stub

		}

	}
}
