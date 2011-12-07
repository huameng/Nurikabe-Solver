/*
	Joseph Thuemler
	Last Updated: December 7th, 2012
*/

import java.util.*;

public class Main {
	public static void main(String[] args) {
		int SIZE = 5;
		int[][] board = {{4,0,3,0,0},{0,0,0,0,0},{0,0,0,2,0},{0,0,0,0,0},{3,0,0,0,0}};
		Nurikabe n = new Nurikabe(board);
		char[][] solvedBoard = n.solveBoard();
		for(int i=0;i<SIZE;++i) {
			// O is open, X is filled, ? is unknown yet (still working on the algorithm!)
			System.out.println(solvedBoard[i]);
		}
	}
}