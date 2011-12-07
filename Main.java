/*
	Joseph Thuemler
	Last Updated: December 7th, 2012
*/

import java.util.*;

public class Main {
	public static void main(String[] args) {
		int SIZE = 5;
		int[][] intBoard = {{2,0,1,0,1},{0,0,0,0,0},{0,0,0,2,0},{2,0,0,0,4},{0,0,0,0,0}};
		NurikabeBoard board = new NurikabeBoard(intBoard);
		NurikabeSolver solver = new NurikabeSolver();
		char[][] solvedBoard = solver.solveBoard(board);
		for(int i=0;i<SIZE;++i) {
			// O is open, X is filled, ? is unknown yet (still working on the algorithm!)
			System.out.println(solvedBoard[i]);
		}
	}
}