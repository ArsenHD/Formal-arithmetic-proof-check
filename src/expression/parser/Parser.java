package expression.parser;

import expression.Expression;

public interface Parser {
    Expression parse(String expression);
}
