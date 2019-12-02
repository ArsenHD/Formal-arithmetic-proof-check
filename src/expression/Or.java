package expression;

public class Or extends BinaryOperation {
    public Or(Expression left, Expression right) {
        super(left, right);
    }

    @Override
    protected String printInSubclass() {
        return "|";
    }

    @Override
    public Class<?> getOperation() {
        return Or.class;
    }
}
