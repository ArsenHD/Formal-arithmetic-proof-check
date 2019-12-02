package expression;

public class Implication extends BinaryOperation {

    public Implication(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected String printInSubclass() {
        return "->";
    }

    @Override
    public Class<?> getOperation() {
        return Implication.class;
    }
}
