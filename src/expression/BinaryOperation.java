package expression;

import java.util.Objects;

public abstract class BinaryOperation extends Expression {
    public Expression leftArg;
    public Expression rightArg;

    public BinaryOperation(Expression left, Expression right) {
        leftArg = left;
        rightArg = right;
    }

    @Override
    public String print() {
        String s = "(" +
                leftArg.print() +
                " " +
                printInSubclass() +
                " " +
                rightArg.print() +
                ")";
        return s;
    }

    @Override
    public boolean equals(Object expression) {
        if (expression instanceof BinaryOperation && getOperation().equals(((BinaryOperation) expression).getOperation())) {
            return leftArg.equals(((BinaryOperation) expression).leftArg) && rightArg.equals(((BinaryOperation) expression).rightArg);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftArg, rightArg, printInSubclass());
    }

    protected abstract String printInSubclass();
}
