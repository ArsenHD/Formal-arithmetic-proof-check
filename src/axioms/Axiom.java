package axioms;

import expression.BinaryOperation;
import expression.Expression;
import expression.UnaryOperation;
import expression.Variable;

import java.util.HashMap;
import java.util.Map;

public class Axiom {
    public Expression expression;

    protected final Map<String, Expression> variablesMap = new HashMap<>();

    public Axiom(Expression expression) {
        this.expression = expression;
    }

    public boolean matches(Expression expr) {
        variablesMap.clear();
        return matches(expression, expr);
    }

    private boolean matches(Expression left, Expression right) {
        if (left instanceof BinaryOperation && right instanceof BinaryOperation) {
            if (left.getOperation().equals(right.getOperation())) {
                return matches(((BinaryOperation) left).leftArg, ((BinaryOperation) right).leftArg) &&
                        matches(((BinaryOperation) left).rightArg, ((BinaryOperation) right).rightArg);
            }
        } else if (left instanceof UnaryOperation && right instanceof UnaryOperation) {
            if (left.getOperation().equals(right.getOperation())) {
                return matches(((UnaryOperation) left).argument, ((UnaryOperation) right).argument);
            }
        } else if (left instanceof Variable) {
            if (variablesMap.containsKey(((Variable) left).name)) {
                return variablesMap.get(((Variable) left).name).equals(right);
            }
            variablesMap.put(((Variable) left).name, right);
            return true;
        }
        return false;
    }
}
