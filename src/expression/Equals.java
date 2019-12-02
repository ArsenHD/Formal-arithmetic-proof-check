package expression;

public class Equals extends BinaryOperation {

    public Equals(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected String printInSubclass() {
        return "=";
    }

    @Override
    public Class<?> getOperation() {
        return Equals.class;
    }
}
