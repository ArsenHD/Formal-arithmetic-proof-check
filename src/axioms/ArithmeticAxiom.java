package axioms;

import expression.*;

public class ArithmeticAxiom extends Axiom {
    public ArithmeticAxiom(Expression expression) {
        super(expression);
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
        } else if (left instanceof Constant && right instanceof Constant) {
            return left.equals(right);
        } else if (left instanceof Increment && right instanceof Increment) {
            return matches(((Increment) left).expression, ((Increment) right).expression);
        } else if (left instanceof Variable && right instanceof Variable) {
            if (variablesMap.containsKey(((Variable) left).name)) {
                return variablesMap.get(((Variable) left).name).equals(right);
            }
            variablesMap.put(((Variable) left).name, right);
            return true;
        }
        return false;
    }
}
