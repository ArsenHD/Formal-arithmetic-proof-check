package expression;

public class Constant extends Expression {

    private String name;

    public Constant(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String print() {
        return name;
    }

    @Override
    public boolean equals(Object expression) {
        return (expression instanceof Constant) && (name.equals(((Constant) expression).name));
    }

    @Override
    public Class<?> getOperation() {
        return Constant.class;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
