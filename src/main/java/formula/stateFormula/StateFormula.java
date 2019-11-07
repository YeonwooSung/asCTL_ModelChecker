package formula.stateFormula;

public abstract class StateFormula {

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
