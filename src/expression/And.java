package expression;

public class And extends BinaryOperation {

    public And(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected String printInSubclass() {
        return "&";
    }

    @Override
    public Class<?> getOperation() {
        return And.class;
    }
}
