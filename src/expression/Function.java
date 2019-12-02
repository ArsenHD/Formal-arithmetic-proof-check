package expression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Function extends Expression {

    public String name;
    public List<Expression> terms;

    public Function(String name, List<Expression> terms) {
        this.name = name;
        this.terms = terms;
    }

    @Override
    public String print() {
        return name + "(" + String.join(",", terms.stream()
                .map(Expression::toString)
                .collect(Collectors.toCollection(ArrayList::new))) + ")";
    }

    @Override
    public boolean equals(Object expression) {
        if (!(expression instanceof Function)) {
            return false;
        }

        if (!name.equals(((Function) expression).name)) {
            return false;
        }

        if (terms.size() != ((Function) expression).terms.size()) {
            return false;
        }

        for (int i = 0; i < terms.size(); i++) {
            if (!terms.get(i).equals(((Function) expression).terms.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Class<?> getOperation() {
        return Function.class;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
