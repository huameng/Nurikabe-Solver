/* nurikabe algorithm:
	draw black squares around complete islands
	draw black squares between two white squares that are definitely from different islands
	draw black squares where a group of black square only has one breathing point
	draw white squares where an incomplete group of white squares only has one breathing point
	draw black squares where no island could possibly reach, both too far away from numbers and surrounded by black squares
	draw white squares where a black square would complete a 2x2 black square
	draw white squares where every possibly solution for a specific island requires that square to be white
	
*/

import java.util.*;

public class NurikabeSolver {
	
	public char[][] solveBoard(NurikabeBoard board) {
		int size = board.board.length;
		char[][] inProgressBoard = new char[size][size];
		for(int i=0;i<size;++i) {
			for(int j=0;j<size;++j) {
				if (board.board[i][j] == 0) {
					inProgressBoard[i][j] = '?';
				}
				else {
					inProgressBoard[i][j] = 'O';
				}
			}
		}
		// until I add a way to track progress and figure out when a) no progress is being made and b) the puzzle is complete
		// i will just do a certain number of iterations and then stop. hopefully it finishes by then
		for(int i=0;i<1;++i) {
			forceBlackSquares(inProgressBoard, board);
			forceWhiteSquares(inProgressBoard, board);
		}

		return inProgressBoard;
	}
	
	public char[][] bruteForceBoard(NurikabeBoard board) {
		// right now only works with boards of size <= 5, else OVERFLOW'D
		int size = board.board.length;
		char[][] maybeBoard = new char[size][size];
		
		for(int i=0;i<(1<<(size*size));++i) {
			if (i % 1000000 == 0) {
				System.out.println(i);
			}
			// brute force it, yo
			int temp = i;
			for(int j=0;j<size*size;++j) {
				maybeBoard[j/size][j%size] = (temp%2==1?'X':'O');
				temp /= 2;
			}
			// now we have to do some checks
			if (checkBoard(maybeBoard, board)) {
				return maybeBoard;
			}
		}
		return null;
	}
	
	public void forceBlackSquares(char[][] inProgressBoard, NurikabeBoard board) {
		fillInIslandSeparators(inProgressBoard, board);
		surroundCompletedIslands(inProgressBoard, board);
		fillInUnreachableSquares(inProgressBoard, board);
		// here, i should mark what possible numbers can reach every blank square on the map. squares that aren't reachable should be black
	}
	
	public void forceWhiteSquares(char[][] inProgressBoard, NurikabeBoard board) {
		fillInTwoByTwos(inProgressBoard, board);
	}
	
	public void fillInUnreachableSquares(char[][] inProgressBoard, NurikabeBoard board) {
		int size = board.board.length;
		int[] dx = {0,0,1,-1};
		int[] dy = {-1,1,0,0};
		boolean[][] reachable = new boolean[size][size];
		for(int i=0;i<size;++i) {
			for(int j=0;j<size;++j) {
				if (board.board[i][j] != 0) {
					boolean[][] visited = new boolean[size][size];
					// time to BFS!
					ArrayDeque<Integer> q = new ArrayDeque<Integer>();
					reachable[i][j] = true;
					visited[i][j] = true;
					q.offer(board.board[i][j]*size*size+i*size+j);
					while(!q.isEmpty()) {
						int cur = q.poll();
						int curY = (cur%(size*size))/size;
						int curX = cur%size;
						int stepsLeft = cur/(size*size) - 1;
						if (stepsLeft == 0) {
							continue;
						}
						for(int k=0;k<dx.length;++k) {
							int newY = curY + dy[k];
							int newX = curX + dx[k];
							if (newY < 0 || newX < 0 || newY >= size || newX >= size) {
								continue;
							}
							if (visited[newY][newX]) {
								continue;
							}
							if (inProgressBoard[newY][newX] == 'X') {
								continue;
							}
							reachable[newY][newX] = true;
							visited[newY][newX] = true;
							q.offer(stepsLeft*size*size+newY*size+newX);
						}
					}
				}
			}
		}
		for(int i=0;i<size;++i) {
			for(int j=0;j<size;++j) {
				if (!reachable[i][j]) {
					inProgressBoard[i][j] = 'X';
				}
			}
		}
	}
	
	public void fillInIslandSeparators(char[][] inProgressBoard, NurikabeBoard board) {
		int size = board.board.length;
		int[] dx = {0,0,1,-1};
		int[] dy = {-1,1,0,0};
		// squares that are next to 2 or more numbers must be black. if they were white, there would be an island with more than one number
		for(int i=0;i<size;++i) {
			for(int j=0;j<size;++j) {
				if (board.board[i][j] != 0) {
					continue;
				}
				int count = 0;
				for(int k=0;k<dx.length;++k) {
					int newY = i + dy[k];
					int newX = j + dx[k];
					if (newY < 0 || newX < 0 || newY >= size || newX >= size) {
						continue;
					}
					if (board.board[newY][newX] > 0 || inProgressBoard[newY][newX] == 'O') {
						++count;
					}
				}
				if (count > 1) {
					inProgressBoard[i][j] = 'X';
				}
			}
		}		
	}
	
	public void surroundCompletedIslands(char[][] inProgressBoard, NurikabeBoard board) {
		int size = board.board.length;
		int[] dx = {0,0,1,-1};
		int[] dy = {-1,1,0,0};
		boolean[][] visited = new boolean[size][size];
		for(int i=0;i<size;++i) {
			for(int j=0;j<size;++j) {
				if (board.board[i][j] != 0) {
					// now we do a BFS around this island to see how many squares are in it. we want to match target exactly
					// if we do match it exactly, our island is done and should be surrounded by black squares
					int targetIslandSize = board.board[i][j];
					ArrayList<Integer> thisIsland = new ArrayList<Integer>();
					ArrayDeque<Integer> q = new ArrayDeque<Integer>();
					thisIsland.add(i*size+j);
					q.add(i*size+j);
					visited[i][j] = true;
					int islandSize = 1;
					while(!q.isEmpty()) {
						int cur = q.poll();
						int curY = cur/size;
						int curX = cur%size;
						for(int k=0;k<dx.length;++k) {
							int newY = curY + dy[k];
							int newX = curX + dx[k];
							if (newY < 0 || newX < 0 || newY >= size || newX >= size) {
								continue;
							}
							if (visited[newY][newX]) {
								continue;
							}
							if (inProgressBoard[newY][newX] != 'O') {
								continue;
							}
							++islandSize;
							visited[newY][newX] = true;
							q.offer(newY*size+newX);
							thisIsland.add(newY*size+newX);
						}
					}
					if (targetIslandSize == islandSize) {
						for(int point : thisIsland) {
							int curX = point%size;
							int curY = point/size;
							for(int k=0;k<dx.length;++k) {
								int newY = curY + dy[k];
								int newX = curX + dx[k];
								if (newY < 0 || newX < 0 || newY >= size || newX >= size) {
									continue;
								}
								if (inProgressBoard[newY][newX] != '?') {
									continue;
								}
								inProgressBoard[newY][newX] = 'X';
							}
						}
					}
				}
			}
		}
	}
	
	public void fillInTwoByTwos(char[][] inProgressBoard, NurikabeBoard board) {
		int size = board.board.length;
		for(int i=0;i<size-1;++i) {
			for(int j=0;j<size-1;++j) {
				if (inProgressBoard[i+1][j] == 'X' && inProgressBoard[i][j+1] == 'X' && inProgressBoard[i+1][j+1] == 'X') {
					inProgressBoard[i][j] = 'O';
				}
			}
		}
	}
	
	public boolean checkBoard(char[][] maybeBoard, NurikabeBoard board) {
		// make sure the maybeBoard matches up with the given board with numbers
		int size = maybeBoard.length;
		Queue<Integer> q = new LinkedList<Integer>();
		
		int visitCount = 0;
		int blackNeeded = size*size;
		int blackFound = 0;
		
		boolean result = true;
		boolean[][] visited = new boolean[size][size];
		
		int[] dx = {0,0,1,-1};
		int[] dy = {1,-1,0,0};
		for(int i=0;i<size*size;++i) {
			if (maybeBoard[i/size][i%size] == 'X') {
				if (blackFound == 0) {
					q.offer(i);
				}
				++blackFound;
			}
		}
		
		// step 1: make sure all the black is connected, with a simple BFS
		while(!q.isEmpty()) {
			int cur = q.poll();
			++visitCount;
			int curX = cur%size;
			int curY = cur/size;
			visited[curY][curX] = true;
			for(int i=0;i<dx.length;++i) {
				int newX = curX + dx[i];
				int newY = curY + dy[i];
				if (newX < 0 || newY < 0 || newX >= size || newY >= size) {
					continue;
				}
				if (visited[newY][newX]) {
					continue;
				}
				if (maybeBoard[newY][newX] != 'X') {
					continue;
				}
				q.offer(newY*size + newX);
				visited[newY][newX] = true;
			}
		}
		if (blackFound != visitCount) {
			// result = false;
			return false;
		}
		
		
		// step 2: make sure there are the requested number of black squares
		for(int i=0;i<size*size;++i) {
			blackNeeded -= board.board[i/size][i%size];
		}
		
		if (blackNeeded != blackFound) {
			// result = false;
			return false;
		}
		
		// step 3: make sure there are no 2x2 black squares
		for(int i=0;i<size-1;++i) {
			for(int j=0;j<size-1;++j) {
				if (maybeBoard[i][j] == 'X') {
					if (maybeBoard[i+1][j] == 'X' && maybeBoard[i][j+1] == 'X' && maybeBoard[i+1][j+1] == 'X') {
						// result = false;
						return false;
					}
				}
			}
		}
		
		// step 4: make sure that each pool of white has a single number in it with the correct number
		visited = new boolean[size][size];
		for(int i=0;i<size*size;++i) {
			if (!visited[i/size][i%size] && maybeBoard[i/size][i%size] == 'O') {
				q.offer(i);
				int numbersFound = 0;
				int numbersSum = 0;
				int poolSize = 0;
				while(!q.isEmpty()) {
					int cur = q.poll();
					int curY = cur/size;
					int curX = cur%size;
					visited[curY][curX] = true;
					numbersSum += board.board[curY][curX];
					if (board.board[curY][curX] > 0) {
						++numbersFound;
					}
					++poolSize;
					for(int j=0;j<4;++j) {
						int newY = curY + dy[j];
						int newX = curX + dx[j];
						if (newX < 0 || newY < 0 || newX >= size || newY >= size) {
							continue;
						}
						if (visited[newY][newX]) {
							continue;
						}
						if (maybeBoard[newY][newX] != 'O') {
							continue;
						}
						q.offer(newY*size + newX);
						visited[newY][newX] = true;
					}
				}
				if (numbersFound != 1 || numbersSum != poolSize) {
					// result = false;
					return false;
				}
			}
		}
		
		return result;
	}
}