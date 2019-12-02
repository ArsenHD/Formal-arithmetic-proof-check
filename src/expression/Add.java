package expression;

public class Add extends BinaryOperation {

    public Add(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected String printInSubclass() {
        return "+";
    }

    @Override
    public Class<?> getOperation() {
        return Add.class;
    }
}
