package expression;

public class Increment extends Expression {

    public Expression expression;

    public Increment(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String print() {
        return expression.toString() + "'";
    }

    @Override
    public boolean equals(Object expr) {
        return (expr instanceof Increment && expression.equals(((Increment) expr).expression));
    }

    @Override
    public Class<?> getOperation() {
        return Increment.class;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
