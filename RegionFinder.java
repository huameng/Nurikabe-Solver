import java.util.*;

public class RegionFinder {
  
  public static void main(String[] args) {
    int[][] intBoard = {{0,2,0,0,0,0,0,0,0,0},{0,0,0,1,0,0,0,1,0,0},{0,0,0,0,0,0,0,0,0,4},{2,0,0,0,0,0,0,0,0,0},{0,0,0,4,0,0,0,0,0,0},{0,0,0,0,0,0,6,0,0,4},{0,0,0,0,4,0,0,0,0,0},{0,3,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0},{0,3,0,1,0,3,0,3,0,3}};
    List<Set<Integer>> regions = getRegions(4,6,4,intBoard);
    System.out.println(regions);
  }
  
  public static List<Set<Integer>> getRegions(int x, int y, int regionSize, int[][] board) {
    int finalSize = regionSize;
    int[] dx = new int[]{-1,1,0,0};
    int[] dy = new int[]{0,0,-1,1};
    int size = board.length;
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
}