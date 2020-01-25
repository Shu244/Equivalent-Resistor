package optimizer;

public class DNADecipherUnit {
    public static final int OPERATOR = 1;
    public static final int OPERAND = 2;
    public static final int DNA = 3;

    private int mOperator;
    private Resistor mResistor;
    private DNA mDNA;
    private int mType;
    private String mVisual;

    public DNADecipherUnit(Resistor operand) {
        mResistor = operand;
        mType = OPERAND;
        mVisual = mResistor.toString();
    }

    public DNADecipherUnit(int operator) {
        mOperator = operator;
        mType = OPERATOR;
    }

    public DNADecipherUnit(DNA dna) {
        mDNA = dna;
        mType = DNA;
    }

    public DNA getDNA() {
        return mDNA;
    }

    public Resistor getResistor() {
        return mResistor;
    }

    public int type() {
        return mType;
    }

    public int getOperator() {
        return mOperator;
    }

    public String getVisual() {
        if(mType == OPERATOR)
            throw new ArithmeticException("Cannot get visual of operator.");
        return mVisual;
    }

    public void combine(DNADecipherUnit unit, int operator) {
        String operatorStr = "P";
        if(operator == Resistor.SERIES)
            operatorStr = "S";
        mVisual = String.format("( %s --%s--> %s)", mVisual, operatorStr, unit.mVisual);
    }
}
