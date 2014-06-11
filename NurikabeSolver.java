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

  public int[] dx = new int[]{-1,1,0,0};
  public int[] dy = new int[]{0,0,-1,1};
  public int size;
  public char[][] inProgressBoard;
	
	public char[][] solveBoard(NurikabeBoard board) {
		size = board.board.length;
		inProgressBoard = new char[size][size];
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
		for(int i=0;i<10;++i) {
			forceBlackSquares(board);
			forceWhiteSquares(board);
      board.updatePossibilities(inProgressBoard);
      testPossibilities(board);
      board.reducePossibilities();
      board.updateSquarePossibilities(inProgressBoard);
      // board.printSquarePossibilities();
      board.printPossibilities();
		}

		return inProgressBoard;
	}
	
	public char[][] bruteForceBoard(NurikabeBoard board) {
		// right now only works with boards of size <= 5, else OVERFLOW'D
		int size = board.board.length;
		char[][] maybeBoard = new char[size][size];
		
		for(int i=0;i<(1<<(size*size));++i) {
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
	
	public void forceBlackSquares(NurikabeBoard board) {
		// int size = board.board.length;
		fillInIslandSeparators(board);
		surroundCompletedIslands(board);
		fillInUnreachableSquares(inProgressBoard, board);
		fillInNecessaryBlacks(board);
    fillInBlackByPossibilities(board);
    dontAllowSeparations(board);
	}
	
	public void forceWhiteSquares(NurikabeBoard board) {
		fillInTwoByTwos(board);
		fillInNecessaryWhites(board);
    preventTwoByTwos(board);
    fillInWhiteByPossibilities(board);
	}
  
  public void dontGetTooClose(NurikabeBoard board) {
    int size = board.board.length;
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (inProgressBoard[i][j] == '?') {
          
        }
      }
    }
  }
  
  public void testPossibilities(NurikabeBoard board) {
    for (List<List<Set<Integer>>> llsi : board.possibilities) {
      for (List<Set<Integer>> lsi : llsi) {
        if (lsi.size() <= 1) {
          continue;
        }
        List<Integer> toRemove = new ArrayList<Integer>();
        for (int s=0;s<lsi.size();++s) {
          Set<Integer> possibility = lsi.get(s);
          // this is a single possibility. let's pretend all the squares are white
          Map<Integer, Character> oldBoard = new HashMap<Integer, Character>();
          Set<Integer> blacksToExamine = new HashSet<Integer>();
          for (int i : possibility) {
            oldBoard.put(i, inProgressBoard[i/size][i%size]);
            inProgressBoard[i/size][i%size] = 'O';
            int curX = i%size;
            int curY = i/size;
            for(int d=0;d<dx.length;++d) {
              int newX = curX+dx[d];
              int newY = curY+dy[d];
              if (!inBounds(newX, newY)) {
								continue;
							}
              blacksToExamine.add(newY*size+newX);
            }
          }
          // we need to find an adjacent black square to test. test all of them I guess?
          for(int b : blacksToExamine) {
            if (inProgressBoard[b/size][b%size] == 'O') {
              continue;
            }
            if (!checkIfBlacksWork(board, b/size, b%size)) {
              toRemove.add(s);
              break;
            }
          }
          for (int i : possibility) {
            inProgressBoard[i/size][i%size] = oldBoard.get(i);
          }
        }
        for(int s=toRemove.size()-1;s>=0;--s) {
          lsi.remove(toRemove.get(s).intValue());
        }
      }
    }
  }
  
  public boolean checkIfBlacksWork(NurikabeBoard board, int i, int j) {
    // assume all question marks are black. do we have at least the required number potentially, and are they all connected to (i,j) which will be black?
    // if not, our possibility is bad.
    int potential = 1;
    boolean[][] visited = new boolean[size][size];
    visited[i][j] = true;
    Queue<Integer> q = new ArrayDeque<Integer>();
    q.offer(i*size+j);
    while(!q.isEmpty()) {
      int cur = q.poll();
      int curI = cur/size;
      int curJ = cur%size;
      for(int dd=0;dd<dx.length;++dd) {
        int bestI = curI + dy[dd];
        int bestJ = curJ + dx[dd];
        if (!inBounds(bestI, bestJ)) continue;
        if (visited[bestI][bestJ]) continue;
        if (inProgressBoard[bestI][bestJ] == 'O') continue;
        if (bestI == i && bestJ == j) continue;
        ++potential;
        q.offer(bestI*size+bestJ);
        visited[bestI][bestJ] = true;
      }
    }
    return potential >= board.totalBlacks;
  }
  
  public void dontAllowSeparations(NurikabeBoard board) {
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (inProgressBoard[i][j] == '?') {
          // if we make this square white, will it permanently disconnect a black region from another black region?
          for(int d=0;d<dx.length;++d) {
            int newI = i+dy[d];
            int newJ = j+dx[d];
            if (!inBounds(newI, newJ)) continue;
            if (inProgressBoard[newI][newJ] != 'X') continue;
            // assume all question marks are black. do we have at least the required number potentially?
            // if not, (i,j) has to be black!
            int potential = 1;
            boolean[][] visited = new boolean[size][size];
            visited[newI][newJ] = true;
            Queue<Integer> q = new ArrayDeque<Integer>();
            q.offer(newI*size+newJ);
            while(!q.isEmpty()) {
              int cur = q.poll();
              int curI = cur/size;
              int curJ = cur%size;
              for(int dd=0;dd<dx.length;++dd) {
                int bestI = curI + dy[dd];
                int bestJ = curJ + dx[dd];
                if (!inBounds(bestI, bestJ)) continue;
                if (visited[bestI][bestJ]) continue;
                if (inProgressBoard[bestI][bestJ] == 'O') continue;
                if (bestI == i && bestJ == j) continue;
                ++potential;
                q.offer(bestI*size+bestJ);
                visited[bestI][bestJ] = true;
              }
            }
            if (potential < board.totalBlacks) {
              inProgressBoard[i][j] = 'X';
              break;
            }
          }
        }
      }
    }
  }
  
  public void fillInBlackByPossibilities(NurikabeBoard board) {
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (board.squarePossibilities.get(i).get(j).size() == 1) {
          int theOne = board.squarePossibilities.get(i).get(j).iterator().next();
          if (theOne == -1) {
            inProgressBoard[i][j] = 'X';
          }
        }
      }
    }
  }
  
  public void fillInWhiteByPossibilities(NurikabeBoard board) {
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (board.possibilities.get(i).get(j).size() > 1) {
          // find intersection, mark them all 'O'
          Set<Integer> whites = new HashSet<Integer>();
          for(Set<Integer> s : board.possibilities.get(i).get(j)) {
            if (whites.size() == 0) {
              whites.addAll(s);
            }
            else {
              whites.retainAll(s);
            }
          }
          for(int ii : whites) {
            inProgressBoard[ii/size][ii%size] = 'O';
          }
        }
      }
    }
  }
	
	public void fillInNecessaryWhites(NurikabeBoard board) {
		int[] dx = {0,0,1,-1};
		int[] dy = {-1,1,0,0};
		for(int i=0;i<size;++i) {
			for(int j=0;j<size;++j) {
				if (inProgressBoard[i][j] == 'O') {
					boolean[][] visited = new boolean[size][size];
					int islandSize = 1;
					int requestedSize = board.board[i][j];
					ArrayList<Integer> breathingPoints = new ArrayList<Integer>();
					ArrayDeque<Integer> q = new ArrayDeque<Integer>();
					visited[i][j] = true;
					q.offer(i*size+j);
					while(!q.isEmpty()) {
						int cur = q.poll();
						int curY = cur/size;
						int curX = cur%size;
						for(int k=0;k<dx.length;++k) {
							int newY = curY + dy[k];
							int newX = curX + dx[k];
							if (!inBounds(newX, newY)) {
								continue;
							}
							if (visited[newY][newX]) {
								continue;
							}
							switch (inProgressBoard[newY][newX]) {
								case 'X':
									break;
								case 'O':
									++islandSize;
									q.offer(newY*size + newX);
									visited[newY][newX] = true;
									requestedSize = Math.max(requestedSize, board.board[newY][newX]);
									break;
								case '?':
									breathingPoints.add(newY*size + newX);
									visited[newY][newX] = true;
							}
						}
					}
					if ((islandSize < requestedSize || requestedSize == 0) && breathingPoints.size() == 1) {
						int newWhite = breathingPoints.get(0);
						inProgressBoard[newWhite/size][newWhite%size] = 'O';
					}
				}
			}
		}
	}
  
  public boolean inBounds(int x, int y) {
    return (x >= 0 && y >= 0 && x < size && y < size);
  }
	
	public void fillInNecessaryBlacks(NurikabeBoard board) {
		for(int i=0;i<size;++i) {
			for(int j=0;j<size;++j) {
				if (inProgressBoard[i][j] == 'X') {
					boolean[][] visited = new boolean[size][size];
					int islandSize = 1;
					int requestedSize = board.board[i][j];
					ArrayList<Integer> breathingPoints = new ArrayList<Integer>();
					ArrayDeque<Integer> q = new ArrayDeque<Integer>();
					q.offer(i*size+j);
					while(!q.isEmpty()) {
						int cur = q.poll();
						int curY = cur/size;
						int curX = cur%size;
						for(int k=0;k<dx.length;++k) {
							int newY = curY + dy[k];
							int newX = curX + dx[k];
							if (!inBounds(newX, newY)) {
								continue;
							}
							if (visited[newY][newX]) {
								continue;
							}
							switch (inProgressBoard[newY][newX]) {
								case 'X':
									++islandSize;
									q.offer(newY*size + newX);
									visited[newY][newX] = true;
									requestedSize = Math.max(requestedSize, board.board[newY][newX]);
									break;
								case '?':
									breathingPoints.add(newY*size + newX);
									visited[newY][newX] = true;
							}
						}
					}
					if (islandSize < board.totalBlacks && breathingPoints.size() == 1) {
						int newWhite = breathingPoints.get(0);
						inProgressBoard[newWhite/size][newWhite%size] = 'X';
					}
				}
			}
		}
	}
	
	public void fillInUnreachableSquares(char[][] inProgressBoard, NurikabeBoard board) {
		boolean[][] reachable = new boolean[size][size];
		for(int i=0;i<size;++i) {
			for(int j=0;j<size;++j) {
				if (board.board[i][j] != 0) {
					boolean[][] visited = new boolean[size][size];
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
	
	public void fillInIslandSeparators(NurikabeBoard board) {
		// squares that are next to 2 or more numbers must be black. if they were white, there would be an island with more than one number
		for(int i=0;i<size;++i) {
			for(int j=0;j<size;++j) {
				if (inProgressBoard[i][j] != '?') {
					continue;
				}
				int count = 0;
				// check if putting a white square at (i,j) would cause two islands to be connected
				int islandCount = 0;
				int cellCount = 1;
				int expectedCells = 0;
				boolean[][] visited = new boolean[size][size];
				ArrayDeque<Integer> q = new ArrayDeque<Integer>();
				q.offer(i*size + j);
				visited[i][j] = true;
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
						visited[newY][newX] = true;
						q.offer(newY*size + newX);
						++cellCount;
						if (board.board[newY][newX] > 0) {
							++islandCount;
							expectedCells += board.board[newY][newX];
						}
					}
				}
				if (islandCount > 1 || (islandCount == 1 && expectedCells < cellCount)) {
					inProgressBoard[i][j] = 'X';
				}
			}
		}		
	}
	
	public void surroundCompletedIslands(NurikabeBoard board) {
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
	
	public void fillInTwoByTwos(NurikabeBoard board) {
		for(int i=0;i<size-1;++i) {
			for(int j=0;j<size-1;++j) {
				if (inProgressBoard[i+1][j] == 'X' && inProgressBoard[i][j+1] == 'X' && inProgressBoard[i+1][j+1] == 'X') {
					inProgressBoard[i][j] = 'O';
				}
			}
		}
		for(int i=0;i<size-1;++i) {
			for(int j=1;j<size;++j) {
				if (inProgressBoard[i+1][j] == 'X' && inProgressBoard[i][j-1] == 'X' && inProgressBoard[i+1][j-1] == 'X') {
					inProgressBoard[i][j] = 'O';
				}
			}
		}
		for(int i=1;i<size;++i) {
			for(int j=0;j<size-1;++j) {
				if (inProgressBoard[i-1][j] == 'X' && inProgressBoard[i][j+1] == 'X' && inProgressBoard[i-1][j+1] == 'X') {
					inProgressBoard[i][j] = 'O';
				}
			}
		}
		for(int i=1;i<size;++i) {
			for(int j=1;j<size;++j) {
				if (inProgressBoard[i-1][j] == 'X' && inProgressBoard[i][j-1] == 'X' && inProgressBoard[i-1][j-1] == 'X') {
					inProgressBoard[i][j] = 'O';
				}
			}
		}
	}
  
  public void preventTwoByTwos(NurikabeBoard board) {
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (inProgressBoard[i][j] == '?') {
          // try making it black, then fill in anything around it if necessary. did you make a 2x2?
          char[][] boardCopy = new char[size][size];
          for(int ii=0;ii<size;++ii) {
            for(int jj=0;jj<size;++jj) {
              boardCopy[ii][jj] = inProgressBoard[ii][jj];
            }
          }
          boardCopy[i][j] = 'X';
          fillInUnreachableSquares(boardCopy, board);
          if (hasTwoByTwo(boardCopy, board, i, j)) {
            inProgressBoard[i][j] = 'O';
          }
        }
      }
    }
  }
  
  public boolean hasTwoByTwo(char[][] inProgressBoard, NurikabeBoard board, int i, int j) {
    if (i != size-1 && j != size-1) {
      if (inProgressBoard[i+1][j] == 'X' && inProgressBoard[i][j+1] == 'X' && inProgressBoard[i+1][j+1] == 'X' && inProgressBoard[i][j] == 'X') {
        return true;
      }
    }
    if (i != size-1 && j != 0) {
      if (inProgressBoard[i+1][j] == 'X' && inProgressBoard[i][j-1] == 'X' && inProgressBoard[i+1][j-1] == 'X' && inProgressBoard[i][j] == 'X') {
        return true;
      }
    }
    if (i != 0 && j != size-1) {
      if (inProgressBoard[i-1][j] == 'X' && inProgressBoard[i][j+1] == 'X' && inProgressBoard[i-1][j+1] == 'X' && inProgressBoard[i][j] == 'X') {
        return true;
      }
    }
    if (i != 0 && j != 0) {
      if (inProgressBoard[i-1][j] == 'X' && inProgressBoard[i][j-1] == 'X' && inProgressBoard[i-1][j-1] == 'X' && inProgressBoard[i][j] == 'X') {
        return true;
      }
    }
    return false;
  }
	
	public boolean checkBoard(char[][] maybeBoard, NurikabeBoard board) {
		// make sure the maybeBoard matches up with the given board with numbers
		Queue<Integer> q = new LinkedList<Integer>();
		
		int visitCount = 0;
		int blackNeeded = size*size;
		int blackFound = 0;
		
		boolean result = true;
		boolean[][] visited = new boolean[size][size];
		
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
			return false;
		}
		
		
		// step 2: make sure there are the requested number of black squares
		for(int i=0;i<size*size;++i) {
			blackNeeded -= board.board[i/size][i%size];
		}
		
		if (blackNeeded != blackFound) {
			return false;
		}
		
		// step 3: make sure there are no 2x2 black squares
		for(int i=0;i<size-1;++i) {
			for(int j=0;j<size-1;++j) {
				if (maybeBoard[i][j] == 'X') {
					if (maybeBoard[i+1][j] == 'X' && maybeBoard[i][j+1] == 'X' && maybeBoard[i+1][j+1] == 'X') {
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
					return false;
				}
			}
		}
		
		return result;
	}
}