package expression;

public abstract class Expression {
    public abstract String print();
    public abstract boolean equals(Object expression);
    public abstract Class<?> getOperation();
    public abstract int hashCode();

    @Override
    public String toString() {
        return print();
    }
}
