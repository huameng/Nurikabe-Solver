public class NurikabeBoard {
	int[][] board;
	int size, totalBlacks, totalWhites;
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
	}
}