package expression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Predicate extends Expression {

    public String name;
    public List<Expression> terms;

    public Predicate(String name, List<Expression> terms) {
        this.name = name;
        this.terms = terms;
    }

    @Override
    public String print() {
        return name + (terms.isEmpty() ? "" : "(" + String.join(",",
                terms.stream()
                .map(Expression::toString)
                .collect(Collectors.toCollection(ArrayList::new))) + ")");
    }

    @Override
    public boolean equals(Object expression) {
        if (!(expression instanceof Predicate)) {
            return false;
        }

        if (!name.equals(((Predicate) expression).name)) {
            return false;
        }

        if (terms.size() != ((Predicate) expression).terms.size()) {
            return false;
        }

        for (int i = 0; i < terms.size(); i++) {
            if (!terms.get(i).equals(((Predicate) expression).terms.get(i))) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Class<?> getOperation() {
        return Predicate.class;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
