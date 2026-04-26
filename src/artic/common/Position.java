package artic.common;

public class Position
{
    public int row;
    public int column;
    
    public Position(final int r, final int c) {
        this.row = r;
        this.column = c;
    }
    
    @Override
    public String toString() {
        return String.format("(%d, %d)", this.row, this.column);
    }
    
    @Override
    public int hashCode() {
        return 31 * (31 + this.row) + this.column;
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other == null || !(other instanceof Position)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        final Position o = (Position)other;
        return o.row == this.row && o.column == this.column;
    }
}
