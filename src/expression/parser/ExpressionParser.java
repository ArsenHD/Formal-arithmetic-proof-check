package expression.parser;

import expression.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionParser implements Parser {
    private String expression;
    private int pos;
    private Token curToken;
    private String variableName;
    private String predicateName;

    private static Map<String, Token> tokenMap = new HashMap<>();

    private static void fillTokenMap() {
        tokenMap.put("!", Token.NEGATE);
        tokenMap.put("&", Token.AND);
        tokenMap.put("|", Token.OR);
        tokenMap.put("->", Token.IMP);
        tokenMap.put("(", Token.OPEN_BRACKET);
        tokenMap.put("@", Token.FORALL);
        tokenMap.put("?", Token.EXISTS);
        tokenMap.put("+", Token.ADD);
        tokenMap.put("*", Token.MULTIPLY);
        tokenMap.put("=", Token.EQUALS);
        tokenMap.put("'", Token.INCREMENT);
        tokenMap.put("0", Token.ZERO);
    }

    protected enum Token {
        AND("and"),
        OR("or"),
        IMP("imp"),
        VAR("value"),
        NEGATE("negate"),
        OPEN_BRACKET("bracket"),
        END("end"),
        ERROR("error"),
        PREDICATE("predicate"),
        EXISTS("exists"),
        FORALL("forall"),
        ADD("add"),
        MULTIPLY("multiply"),
        EQUALS("equals"),
        INCREMENT("increment"),
        ZERO("zero");

        String name;

        Token(String name) {
            this.name = name;
        }
    }

    public ExpressionParser() {
        fillTokenMap();
    }

    @Override
    public Expression parse(String expr) {
        expression = expr;
        pos = 0;
        curToken = getToken();

        return implication(false);
    }

    private Token getToken() {
        skipWhitespaces();

        if (pos >= expression.length()) {
            return Token.END;
        }

        char c = expression.charAt(pos++);

        if (tokenMap.containsKey(String.valueOf(c))) {
            curToken = tokenMap.get(String.valueOf(c));
            return curToken;
        }

        if (c == '-' && expression.charAt(pos) == '>') {
            pos++;
            return Token.IMP;
        }

        if (Character.isLetter(c) && Character.isUpperCase(c)) {
            int start = pos - 1;

            while (pos < expression.length() && Character.isDigit(expression.charAt(pos))) {
                pos++;
            }

            predicateName = expression.substring(start, pos);

            return Token.PREDICATE;
        }

        if (Character.isLetter(c) && Character.isLowerCase(c)) {
            int start = pos - 1;

            while (pos < expression.length() && Character.isDigit(expression.charAt(pos))) {
                pos++;
            }

            variableName = expression.substring(start, pos);

            return Token.VAR;
        }

        return Token.ERROR;
    }

    private Expression implication(boolean get) {
        return or(get);
    }

    private Expression or(boolean get) {
        Expression left = and(get);

        while (true) {
            switch (curToken) {
                case IMP:
                    left = new Implication(left, or(true));
                    break;
                case OR:
                    left = new Or(left, and(true));
                    break;
                default:
                    return left;
            }
        }
    }

    private Expression and(boolean get) {
        Expression left = equals(get);

        while (true) {
            switch(curToken) {
                case AND:
                    left = new And(left, equals(true));
                    break;
                default:
                    return left;
            }
        }
    }

    private Expression equals(boolean get) {
        Expression left = add(get);

        while (true) {
            switch(curToken) {
                case EQUALS:
                    left = new Equals(left, add(true));
                    break;
                case INCREMENT:
                    left = new Increment(left);
                    curToken = getToken();
                    break;
                default:
                    return left;
            }
        }
    }

    private Expression add(boolean get) {
        Expression left = multiply(get);

        while (true) {
            switch (curToken) {
                case ADD:
                    left = new Add(left, multiply(true));
                    break;
                case INCREMENT:
                    left = new Increment(left);
                    curToken = getToken();
                    break;
                default:
                    return left;
            }
        }
    }

    private Expression multiply(boolean get) {
        Expression left = primaryExpression(get);

        while (true) {
            switch (curToken) {
                case MULTIPLY:
                    left = new Multiply(left, primaryExpression(true));
                    break;
                case INCREMENT:
                    left = new Increment(left);
                    curToken = getToken();
                    break;
                default:
                    return left;
            }
        }
    }

    private Expression primaryExpression(boolean get) {
        if (get) {
            curToken = getToken();
        }

        switch (curToken) {
            case EXISTS:
                curToken = getToken();
                pos++;
                String varExists = variableName;
                return new QuantifierExpression("?", varExists, implication(true));
            case FORALL:
                curToken = getToken();
                pos++;
                String varForall = variableName;
                return new QuantifierExpression("@", varForall, implication(true));
            case PREDICATE:
                if (pos == expression.length()) {
                    return new Predicate(predicateName, new ArrayList<>());
                }
                List<Expression> terms = getTermsList();
                curToken = getToken();
                return new Predicate(predicateName, terms);
            case NEGATE:
                return new Negate(primaryExpression(true));
            case OPEN_BRACKET:
                Expression e = implication(true);
                curToken = getToken();
                return e;
            case VAR:
                String var = variableName;
                if (pos == expression.length()) {
                    return new Variable(var);
                }
                List<Expression> varTerms = getTermsList();
                curToken = getToken();
                if (varTerms.isEmpty()) {
                    return new Variable(var);
                }
                return new Function(var, varTerms);
            case ZERO:
                curToken = getToken();
                return new Constant("0");
            default:
                return null;
        }
    }

    private List<Expression> getTermsList() {
        List<Expression> terms = new ArrayList<>();
        if (expression.charAt(pos) == '(') {
            pos++;
            int bracketsCounter = 0;
            int closingBracketPos = -1;
            List<Integer> commaIndices = new ArrayList<>();
            commaIndices.add(pos - 1);
            for (int i = pos; i < expression.length(); i++) {
                if (expression.charAt(i) == ',' && bracketsCounter == 0) {
                    commaIndices.add(i);
                }
                else if (expression.charAt(i) == '(') {
                    bracketsCounter++;
                }
                else if (expression.charAt(i) == ')') {
                    if (bracketsCounter > 0) {
                        bracketsCounter--;
                    }
                    else if (bracketsCounter == 0) {
                        closingBracketPos = i;
                        break;
                    }
                }
            }
            commaIndices.add(closingBracketPos);
            pos = closingBracketPos + 1;

            Parser expressionParser = new ExpressionParser();
            for (int i = 0; i < commaIndices.size() - 1; i++) {
                terms.add(expressionParser
                        .parse(expression.substring(commaIndices.get(i) + 1, commaIndices.get(i + 1))));
            }
        }
        return terms;
    }

    private void skipWhitespaces() {
        while (pos < expression.length() && Character.isWhitespace(expression.charAt(pos))) {
            pos++;
        }
    }
}
