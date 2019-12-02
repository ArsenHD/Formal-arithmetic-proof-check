package expression;

public class Multiply extends BinaryOperation {
    public Multiply(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected String printInSubclass() {
        return "*";
    }

    @Override
    public Class<?> getOperation() {
        return Multiply.class;
    }
}
