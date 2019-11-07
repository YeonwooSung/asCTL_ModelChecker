package formula.pathFormula;

public abstract class PathFormula {

    protected int type;
    public abstract void writeToBuffer(StringBuilder buffer);

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        writeToBuffer(buffer);
        return buffer.toString();
    }

    public int getType() {
        return type;
    }
}
