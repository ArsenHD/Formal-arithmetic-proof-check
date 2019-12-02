package expression;

import java.util.Objects;

public class Variable extends Expression {
    public String name;

    public Variable(String name) {
        this.name = name;
    }

    @Override
    public String print() {
        return name;
    }

    @Override
    public boolean equals(Object expression) {
        return expression instanceof Variable && name.equals(((Variable) expression).name);
    }

    @Override
    public Class<?> getOperation() {
        return Variable.class;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
