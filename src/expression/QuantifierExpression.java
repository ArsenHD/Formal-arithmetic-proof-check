package expression;

public class QuantifierExpression extends Expression {
    public String symbol;
    public String variable;
    public Expression expression;

    public QuantifierExpression(String symbol, String variable, Expression expression) {
        this.symbol = symbol;
        this.variable = variable;
        this.expression = expression;
    }

    @Override
    public String print() {
        return "(" + symbol + variable + "." + expression.toString() + ")";
    }

    @Override
    public boolean equals(Object expr) {
        return expr instanceof QuantifierExpression && symbol.equals(((QuantifierExpression) expr).symbol)
                && variable.equals(((QuantifierExpression) expr).variable)
                && expression.equals(((QuantifierExpression) expr).expression);
    }

    @Override
    public Class<?> getOperation() {
        return QuantifierExpression.class;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
