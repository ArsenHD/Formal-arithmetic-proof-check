package expression;

import java.util.Objects;

public abstract class UnaryOperation extends Expression {
    public Expression argument;

    public UnaryOperation(Expression argument) {
        this.argument = argument;
    }

    @Override
    public String print() {
        StringBuilder sb = new StringBuilder();
        sb.append(printInSubclass())
                .append(argument.print());
        return sb.toString();
    }

    @Override
    public boolean equals(Object expression) {
        if (expression instanceof UnaryOperation && getOperation().equals(((UnaryOperation) expression).getOperation())) {
            return argument.equals(((UnaryOperation) expression).argument);
        }
        return false;
    }

    public int hashCode() {
        return Objects.hash(argument);
    }

    protected abstract String printInSubclass();
}
