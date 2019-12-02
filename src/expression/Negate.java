package expression;

public class Negate extends UnaryOperation {

    public Negate(Expression argument) {
        super(argument);
    }

    @Override
    protected String printInSubclass() {
        return "!";
    }

    @Override
    public Class<?> getOperation() {
        return Negate.class;
    }
}
