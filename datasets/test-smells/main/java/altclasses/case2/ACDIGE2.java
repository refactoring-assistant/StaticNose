package altclasses.case2;

;

enum Color {
  BLACK(1), WHITE(0);
  private final int value;
  Color(int value) { this.value = value; }
  public int getValue() { return value; }
}

interface ChessPiece {
  int getRow();
  int getCol();
  Color getColor();
  boolean canMove(int row, int col);
  boolean canKill(ChessPiece piece);
}

abstract class AbstractChessPiece implements ChessPiece {
  private int row, col;
  private Color color;

  public AbstractChessPiece(int row, int col, Color color) {
    if (row > 7 || row < 0 || col > 7 || col < 0) {
      throw new IllegalArgumentException("altclasses.case2.ChessPiece out of bounds");
    }
    this.row = row;
    this.col = col;
    this.color = color;
  }

  public AbstractChessPiece(AbstractChessPiece original) {
    this(original.row, original.col, original.color);
  }

  @Override
  public int getRow() {
    return this.row;
  }

  @Override
  public int getCol() {
    return this.col;
  }

  @Override
  public Color getColor() {
    return this.color;
  }

  @Override
  public abstract boolean canMove(int row, int col);

  @Override
  public abstract boolean canKill(ChessPiece piece);
}

class Bishop extends AbstractChessPiece {
  public Bishop(int row, int col, Color color) {
    super(row, col, color);
  }

  public Bishop(Bishop original) {
    this(original.getRow(), original.getCol(), original.getColor());
  }

  @Override
  public boolean canMove(int row, int col) {
    if (row < 0 || row > 7 || col < 0 || col > 7
      || row == this.getRow() || col == this.getCol()){
      return false;
    }
    int rowDiff = Math.abs(row - this.getRow());
    int colDiff =Math.abs(col - this.getCol());
    return (rowDiff == colDiff);
  }

  @Override
  public boolean canKill(ChessPiece piece) {
    return (piece.getColor() != this.getColor()
      && this.canMove(piece.getRow(), piece.getCol()));
  }
}

class King extends AbstractChessPiece {
  private static final int VALID_KING_1 = 1;
  private static final int VALID_KING_0 = 0;

  public King(int row, int col, Color color) {
    super(row, col, color);
  }

  public King(King original) {
    this(original.getRow(), original.getCol(), original.getColor());
  }

  @Override
  public boolean canMove(int row, int col) {
    if (row < 0 || row > 7 || col < 0 || col > 7
      || (row == this.getRow() && col == this.getCol())){
      return false;
    }

    int rowDiff = Math.abs(row-this.getRow());
    int colDiff = Math.abs(col-this.getCol());

    return (rowDiff == VALID_KING_1 && colDiff == VALID_KING_1)
      || (rowDiff == VALID_KING_1 && colDiff == VALID_KING_0)
      || (rowDiff == VALID_KING_0 && colDiff == VALID_KING_1);
  }

  @Override
  public boolean canKill(ChessPiece piece) {
    return (piece.getColor() != this.getColor()
      && this.canMove(piece.getRow(), piece.getCol()));
  }
}

class Knight extends AbstractChessPiece {
  private final static int VALID_COL_DIFF_2 = 2;
  private final static int VALID_COL_DIFF_1 = 1;
  private final static int VALID_ROW_DIFF_2 = 2;
  private final static int VALID_ROW_DIFF_1 = 1;

  public Knight(int row, int col, Color color) {
    super(row, col, color);
  }

  public Knight(Knight original) {
    this(original.getRow(), original.getCol(), original.getColor());
  }

  @Override
  public boolean canMove(int row, int col) {
    if (row < 0 || row > 7 || col < 0 || col > 7
      || row == this.getRow() || col == this.getCol()){
      return false;
    }

    int rowDiff = Math.abs(row-this.getRow());
    int colDiff = Math.abs(col-this.getCol());

    return ((rowDiff == VALID_ROW_DIFF_2 && colDiff == VALID_COL_DIFF_1)
      || (rowDiff == VALID_ROW_DIFF_1 && colDiff == VALID_COL_DIFF_2));
  }

  @Override
  public boolean canKill(ChessPiece piece) {
    return (piece.getColor() != this.getColor()
      && this.canMove(piece.getRow(), piece.getCol()));
  }
}

class Pawn extends AbstractChessPiece {
  private static final int BLACK_START_ROW = 6;
  private static final int WHITE_START_ROW = 1;
  private static final int VALID_BLACK_ROW_DIFF_1 = -1;
  private static final int VALID_BLACK_ROW_DIFF_2 = -2;
  private static final int VALID_WHITE_ROW_DIFF_1 = 1;
  private static final int VALID_WHITE_ROW_DIFF_2 = 2;
  private static final int VALID_COL_DIFF = 0;
  private static final int VALID_COL_KILL_DIFF = 1;

  public Pawn(int row, int col, Color color) {
    super(row, col, color);
    if (color == Color.WHITE && row != 1) {
      throw new IllegalArgumentException("Set white pawns at the wrong row!");
    } else if (color == Color.BLACK && row != 6) {
      throw new IllegalArgumentException("Set black pawns at the wrong row!");
    }
  }

  public Pawn(Pawn original) {
    this(original.getRow(), original.getCol(), original.getColor());
  }

  @Override
  public boolean canMove(int row, int col) {
    if (row < 0 || row > 7 || col < 0 || col > 7
      || (row == this.getRow() && col == this.getCol())) {
      return false;
    }

    int rowDiff = row-this.getRow();
    int colDiff = col-this.getCol();

    if (this.getColor() == Color.BLACK) {
      if (this.getRow() == BLACK_START_ROW) {
        return (rowDiff == VALID_BLACK_ROW_DIFF_1 && colDiff == VALID_COL_DIFF)
          || (rowDiff == VALID_BLACK_ROW_DIFF_2 && colDiff == VALID_COL_DIFF);
      } else {
        return rowDiff == VALID_BLACK_ROW_DIFF_1 && colDiff == VALID_COL_DIFF;
      }
    }

    if (this.getColor() == Color.WHITE) {
      if (this.getRow() == WHITE_START_ROW) {
        return (rowDiff == VALID_WHITE_ROW_DIFF_1 && colDiff == VALID_COL_DIFF)
          || (rowDiff == VALID_WHITE_ROW_DIFF_2 && colDiff == VALID_COL_DIFF);
      } else {
        return rowDiff == VALID_WHITE_ROW_DIFF_1 && colDiff == VALID_COL_DIFF;
      }
    }
    return false;
  }

  @Override
  public boolean canKill(ChessPiece piece) {
    if (this.getColor() == piece.getColor()
      || piece.getRow() > 7 || piece.getRow() < 0 || piece.getCol() > 7 || piece.getCol() < 0) {
      return false;
    }

    int rowDiff = piece.getRow()-this.getRow();
    int colDiff = Math.abs(piece.getCol()-this.getCol());

    if (this.getColor() == Color.BLACK) {
      return rowDiff == VALID_BLACK_ROW_DIFF_1 && colDiff == VALID_COL_KILL_DIFF;
    } else {
      return rowDiff == VALID_WHITE_ROW_DIFF_1 && colDiff == VALID_COL_KILL_DIFF;
    }
  }

  protected boolean hasMoved() {
    if (this.getColor() == Color.BLACK && this.getRow() == BLACK_START_ROW) {
      return false;
    }
    if (this.getColor() == Color.WHITE && this.getRow() == WHITE_START_ROW) {
      return false;
    }
    return true;
  }
}

class Queen extends AbstractChessPiece {
  public Queen(int row, int col, Color color) {
    super(row, col, color);
  }

  public Queen(Queen original) {
    this(original.getRow(), original.getCol(), original.getColor());
  }

  @Override
  public boolean canMove(int row, int col) {
    if (row < 0 || row > 7 || col < 0 || col > 7
      || (row == this.getRow() && col == this.getCol())){
      return false;
    }

    int rowDiff = Math.abs(row-this.getRow());
    int colDiff = Math.abs(col-this.getCol());
    return (rowDiff == colDiff || rowDiff == 0 || colDiff == 0);
  }

  @Override
  public boolean canKill(ChessPiece piece) {
    return (piece.getColor() != this.getColor()
      && this.canMove(piece.getRow(), piece.getCol()));
  }
}

class Rook extends AbstractChessPiece {
  private static final int VALID_ROOK_0 = 0;

  public Rook(int row, int col, Color color) {
    super(row, col, color);
  }

  public Rook(Rook original) {
    this(original.getRow(), original.getCol(), original.getColor());
  }

  @Override
  public boolean canMove(int row, int col) {
    if (row < 0 || row > 7 || col < 0 || col > 7
      || row == this.getRow() || col == this.getCol()){
      return false;
    }

    int rowDiff = Math.abs(row-this.getRow());
    int colDiff = Math.abs(col-this.getCol());
    return (rowDiff == VALID_ROOK_0 || colDiff == VALID_ROOK_0);
  }

  @Override
  public boolean canKill(ChessPiece piece) {
    return (piece.getColor() != this.getColor()
      && this.canMove(piece.getRow(), piece.getCol()));
  }
}