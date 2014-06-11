import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;

public class NurikabeBoard {
	int[][] board;
	int size, totalBlacks, totalWhites;
  List<List<List<Set<Integer>>>> possibilities;
  List<List<Set<Integer>>> squarePossibilities;
  int[] dx = new int[]{-1,1,0,0};
  int[] dy = new int[]{0,0,-1,1};
  
	public NurikabeBoard(int[][] board) {
		this.board = board;
		this.size = board.length;
		for(int i=0;i<this.size;++i) {
			for(int j=0;j<this.size;++j) {
				if (board[i][j] != 0) {
					totalWhites += board[i][j];
				}
			}
		}
		totalBlacks = this.size * this.size - totalWhites;
    possibilities = new ArrayList<List<List<Set<Integer>>>>();
    for(int i=0;i<this.size;++i)  {
      List<List<Set<Integer>>> list = new ArrayList<List<Set<Integer>>>();
      for(int j=0;j<this.size;++j) {
        List<Set<Integer>> maybies = new ArrayList<Set<Integer>>();
        list.add(maybies);
      }
      possibilities.add(list);
    }
    squarePossibilities = new ArrayList<List<Set<Integer>>>();
    for(int i=0;i<this.size;++i) {
      List<Set<Integer>> ls = new ArrayList<Set<Integer>>();
      for(int j=0;j<this.size;++j) {
        Set<Integer> s = new HashSet<Integer>();
        ls.add(s);
      }
      squarePossibilities.add(ls);
    }
    createPossibilities();
    // printPossibilities();
    createSquarePossibilities();
    // printSquarePossibilities();
	}
  
  public void reducePossibilities() {
    for(List<List<Set<Integer>>> lls : possibilities) {
      for(List<Set<Integer>> ls : lls) {
        Set<Integer> toRemove = new HashSet<Integer>();
        for(int i=0;i<ls.size();++i) {
          J: for(int j=i+1;j<ls.size();++j) {
            Set<Integer> a = ls.get(i);
            Set<Integer> b = ls.get(j);
            for(int aa : a) {
              if (!b.contains(aa)) {
                continue J;
              }
            }
            toRemove.add(j);
          }
        }
        List<Integer> sortedSet = new ArrayList<Integer>();
        for(int i : toRemove) {
          sortedSet.add(i);
        }
        Collections.sort(sortedSet);
        for(int i=sortedSet.size()-1;i>=0;--i) {
          ls.remove(sortedSet.get(i).intValue());
        }
      }
    }
  }
  
  public void printPossibilities() {
    for(List<List<Set<Integer>>> lls : possibilities) {
      for(List<Set<Integer>> ls : lls) {
        for(Set<Integer> s : ls) {
          System.out.println(s);
        }
        System.out.println("---------------------------------------");
      }
    }
  }
  
  public void createSquarePossibilities() {
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (board[i][j] != 0) {
          squarePossibilities.get(i).get(j).add(i*size+j);
        } else {
          squarePossibilities.get(i).get(j).add(-1); // black square
          for(int ii=0;ii<size;++ii) {
            for(int jj=0;jj<size;++jj) {
              for(Set<Integer> s : possibilities.get(ii).get(jj)) {
                if (s.contains(i*size+j)) {
                  squarePossibilities.get(i).get(j).add(ii*size+jj);
                }
              }
            }
          }
        }
      }
    }
  }
  
  public void printSquarePossibilities() {
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        System.out.print(squarePossibilities.get(i).get(j));
        System.out.print(" , ");
      }
      System.out.println();
    }
    System.out.println("-----------------------------------------");
  }
  
  
  public void updateSquarePossibilities(char[][] currentBoard) {
    dontGetBlocked();
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (currentBoard[i][j] == 'O') {
          squarePossibilities.get(i).get(j).remove(-1);
        } else if (currentBoard[i][j] == 'X') {
          squarePossibilities.get(i).get(j).clear();
          squarePossibilities.get(i).get(j).add(-1);
        } else {
          Set<Integer> toRemove = new HashSet<Integer>();
          A: for(int maybe : squarePossibilities.get(i).get(j)) {
            if (maybe == -1) continue;
            for(Set<Integer> s : possibilities.get(maybe/size).get(maybe%size)) {
              if (s.contains(i*size+j)) {
                continue A;
              }
            }
            toRemove.add(maybe);
          }
          for(int remove : toRemove) {
            squarePossibilities.get(i).get(j).remove(remove);
          }
        }
      }
    }
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (currentBoard[i][j] == 'O' && squarePossibilities.get(i).get(j).size() == 1) {
          int ok = squarePossibilities.get(i).get(j).iterator().next();
          for(int d=0;d<dx.length;++d) {
            int newI = i+dy[d];
            int newJ = j+dx[d];
            if (newI < 0 || newJ < 0 || newI >= size || newJ >= size) {
              continue;
            }
            Set<Integer> toRemove = new HashSet<Integer>();
            for(int maybe : squarePossibilities.get(newI).get(newJ)) {
              if (maybe != -1 && maybe != ok) toRemove.add(maybe);
            }
            squarePossibilities.get(newI).get(newJ).removeAll(toRemove);
          }
        }
      }
    }
  }
  
  public void dontGetBlocked() {
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (possibilities.get(i).get(j).size() > 1) {
          Set<Integer> boundaries = new HashSet<Integer>();
          boolean started = false;
          for(Set<Integer> s : possibilities.get(i).get(j)) {
            Set<Integer> sBoundaries = new HashSet<Integer>();
            for(int sq : s) {
              int sI = sq/size;
              int sJ = sq%size;
              for(int d=0;d<dx.length;++d) {
                int newI = sI+dy[d];
                int newJ = sJ+dx[d];
                if (!inBounds(newI, newJ)) continue;
                int num = newI*size+newJ;
                if (s.contains(num)) continue;
                sBoundaries.add(num);
              }
            }
            if (!started) {
              started = true;
              boundaries.addAll(sBoundaries);
            } else {
              boundaries.retainAll(sBoundaries);
            }
          }
          for(int needed : boundaries) {
            Set<Integer> toRemove = new HashSet<Integer>();
            int golden = i*size+j;
            for(int poss : squarePossibilities.get(needed/size).get(needed%size)) {
              if (poss != -1 && poss != golden) toRemove.add(poss);
            }
            for(int rem : toRemove) squarePossibilities.get(needed/size).get(needed%size).remove(rem);
          }
        }
      }
    }
  }
  
  public boolean inBounds(int y, int x) {
    return (x >= 0 && y >= 0 && x < size && y < size);
  }
  
  /*
    assume board is totally blank. just don't go next to another island?
  */
  public void createPossibilities() {
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (board[i][j] != 0) {
          // List<Set<Integer>> foo = dfs(j,i,board[i][j]-1,j,i);
          List<Set<Integer>> foo = getRegions(j,i,board[i][j]);
          for(Set<Integer> s : foo) {
            // s.add(i*size+j);
            // if (s.size() != board[i][j]) continue;
            possibilities.get(i).get(j).add(s);
          }
        }
      }
    }
  }
  
  public List<Set<Integer>> getRegions(int x, int y, int regionSize) {
    int finalSize = regionSize;
    // int[] dx = new int[]{-1,1,0,0};
    // int[] dy = new int[]{0,0,-1,1};
    // int size = board.length;
    // they contain (x,y) and have size total squares
    List<Set<Integer>> toRet = new ArrayList<Set<Integer>>();
    Set<Integer> toAdd = new HashSet<Integer>();
    toAdd.add(y*size+x);
    toRet.add(toAdd);
    --regionSize;
    if (regionSize == 0) return toRet;
    while(regionSize != 0) {
      List<Set<Integer>> newToRet = new ArrayList<Set<Integer>>();
      for(Set<Integer> s : toRet) {
        for(int i : s) {
          A: for(int d=0;d<dx.length;++d) {
            int anX = i%size;
            int anY = i/size;
            int newX = anX + dx[d];
            int newY = anY + dy[d];
            if (newX < 0 || newY < 0 || newX >= size || newY >= size) continue;
            // check if anything next to the new square is a number (except for this, if it's a number!)
            for(int dd=0;dd<dx.length;++dd) {
              int bestX = newX + dx[d];
              int bestY = newY + dy[d];
              if (bestX < 0 || bestY < 0 || bestX >= size || bestY >= size) continue;
              if (s.contains(bestY*size+bestX)) continue;
              if (board[bestY][bestX] != 0) {
                continue A;
              }
            }
            Set<Integer> newSet = new HashSet<Integer>();
            newSet.addAll(s);
            newSet.add(newY*size+newX);
            newToRet.add(newSet);
          }
        }
      }
      toRet = newToRet;
      --regionSize;
    }
    List<Set<Integer>> reallyReturn = new ArrayList<Set<Integer>>();
    for(Set<Integer> s : toRet) {
      if (s.size() == finalSize) reallyReturn.add(s);
    }
    return reallyReturn;
  }
  
  // this method allows squares to be revisited! need to check size after
  private List<Set<Integer>> dfs(int x, int y, int depth, int origX, int origY) {
    List<Set<Integer>> toRet = new ArrayList<Set<Integer>>();
    int[] dx = new int[]{-1,1,0,0};
    int[] dy = new int[]{0,0,-1,1};
    if (depth == 0) {
      HashSet<Integer> set = new HashSet<Integer>();
      set.add(y*size+x);
      toRet.add(set);
      return toRet;
    }
    A: for(int d=0;d<dx.length;++d) {
      int newx = x + dx[d];
      int newy = y + dy[d];
      if (newx < 0 || newy < 0 || newx >= size || newy >= size) continue;
      if (board[newy][newx] > 0) continue;
      for(int dd=0;dd<dx.length;++dd) {
        int newxx = newx+dx[dd];
        int newyy = newy+dy[dd];
        if (newxx < 0 || newyy < 0 || newxx >= size || newyy >= size) continue;
        if (newxx == origX && newyy == origY) continue;
        if (board[newyy][newxx] > 0) continue A;
      }
      // alright, we're valid enough for me
      List<Set<Integer>> results = dfs(newx, newy, depth-1, origX, origY);
      // List<Set<Integer>> newToRet = new ArrayList<Set<Integer>>();
      for(Set<Integer> s : results) {
        s.add(newy*size+newx);
        toRet.add(s);
      }
      // toRet = newToRet;
      System.err.println(toRet.size());
    }
    return toRet;
  }

  public void updatePossibilities(char[][] curBoard) {
    // check for black squares, remove possibilities!
    for(int i=0;i<size;++i) {
      for(int j=0;j<size;++j) {
        if (curBoard[i][j] == 'X') {
          for(List<List<Set<Integer>>> lls : possibilities) {
            for(List<Set<Integer>> ls : lls) {
              int z = 0;
              while(z < ls.size()) {
                if (ls.get(z).contains(i*size+j)) {
                  ls.remove(z);
                } else {
                  ++z;
                }
              }
            }
          }
        } else if (squarePossibilities.get(i).get(j).size() == 1) {
          int goldenIsland = squarePossibilities.get(i).get(j).iterator().next();
          if (goldenIsland == -1) continue;
          // this square is owned. make sure there aren't any possibilities with it in other islands
          for(int ii=0;ii<size;++ii) {
            for(int jj=0;jj<size;++jj) {  
              // if (goldenIsland/size == ii && goldenIsland%size == jj) continue;
              int z = 0;
              A: while(z < possibilities.get(ii).get(jj).size()) {
                if ((ii != goldenIsland/size || jj != goldenIsland%size) && possibilities.get(ii).get(jj).get(z).contains(i*size+j) && !possibilities.get(ii).get(jj).get(z).contains(goldenIsland)) {
                  possibilities.get(ii).get(jj).remove(z);
                } else if ((ii != goldenIsland/size || jj != goldenIsland%size)) {
                  for(int d=0;d<dx.length;++d) {
                    int newI = i+dy[d];
                    int newJ = j+dx[d];
                    if (newI < 0 || newJ < 0 || newI >= size || newJ >= size) continue;
                    if (possibilities.get(ii).get(jj).get(z).contains(newI*size+newJ)) {
                      possibilities.get(ii).get(jj).remove(z);
                      break;
                    }
                  }
                  ++z;
                  // also make sure no possibilities from other islands use directly adjacent squares!
                } else if (ii == goldenIsland/size && jj == goldenIsland%size && !possibilities.get(ii).get(jj).get(z).contains(i*size+j)) {
                  possibilities.get(ii).get(jj).remove(z);
                  // ++z;
                } else {
                  ++z;
                }
              }
            }
          }
        }
      }
    }
  }
}