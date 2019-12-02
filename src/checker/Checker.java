package checker;

import axioms.ArithmeticAxiom;
import axioms.Axiom;
import expression.*;
import expression.parser.ExpressionParser;
import expression.parser.Parser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Checker {

    private Expression expression;
    private List<Expression> proof;
    private List<Expression> checkedExpressions;
    private List<Expression> hypotheses;
    private List<Axiom> axioms;
    private List<ArithmeticAxiom> arithmeticAxioms;
    private List<String> variableNames;
    private Map<String, List<String>> rightImplicationOperands;
    private Parser parser;
    private boolean isVariableFree;
    private boolean variableLock;
    private Expression substituteExpression;
    private Scanner scanner;

    public Checker() throws IOException {
        scanner = new Scanner(System.in);
        parser = new ExpressionParser();
        proof = new ArrayList<>();
        checkedExpressions = new ArrayList<>();
        rightImplicationOperands = new HashMap<>();
        variableNames = new ArrayList<>();
        initializeAxioms();
        initializeArithmeticAxioms();
    }

    public void check() {
        readInput();

        int lineCounter = 0;

        for (Expression e : proof) {
            lineCounter++;

            if (isHypothesis(e) || isModusPonens(e) || isAxiom(e)
                    || isForallIntroduction(e) || isExistsIntroduction(e)) {
                addExpressionToLists(e);
            } else {
                System.out.println("Line #" + lineCounter + " can't be obtained");
                return;
            }
        }

        if (!checkLastProofExpression()) {
            System.out.println("Required hasn't been proven");
            return;
        }

        System.out.println("Proof is correct");
    }

    private void initializeAxioms() throws IOException {
        axioms = Files.readAllLines(Paths.get("res/axioms")).stream()
                .map(parser::parse)
                .map(Axiom::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void initializeArithmeticAxioms() throws IOException {
        arithmeticAxioms = Files.readAllLines(Paths.get("res/arithmetic_axioms")).stream()
                .map(parser::parse)
                .map(ArithmeticAxiom::new)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void addExpressionToLists(Expression expr) {
        checkedExpressions.add(expr);

        if (expr instanceof Implication) {
            if (rightImplicationOperands.containsKey(((Implication) expr).rightArg.toString())) {
                rightImplicationOperands.get(((Implication) expr).rightArg.toString()).add(expr.toString());
            }
            else {
                List<String> list = new ArrayList<>();
                list.add(expr.toString());
                rightImplicationOperands.put(((Implication) expr).rightArg.toString(), list);
            }
        }
    }

    private boolean isHypothesis(Expression expr) {
        for (Expression hypothesis: hypotheses) {
            if (expr.equals(hypothesis)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAxiom(Expression expr) {
        for (Axiom axiom: axioms) {
            if (axiom.matches(expr)) {
                return true;
            }
        }
        return isAxiom11(expr) || isAxiom12(expr) || isArithmeticAxiom(expr);
    }

    private boolean isArithmeticAxiom(Expression expr) {
        for (ArithmeticAxiom axiom: arithmeticAxioms) {
            if (axiom.matches(expr)) {
                return true;
            }
        }

        return isNinthArithmeticAxiom(expr);
    }

    private Expression getExpression(String expr) {
        return parser.parse(expr);
    }

    private boolean isNinthArithmeticAxiom(Expression expr) {
        if (expr instanceof Implication && ((Implication) expr).leftArg instanceof And
        && ((And) ((Implication) expr).leftArg).rightArg instanceof QuantifierExpression
        && ((QuantifierExpression) ((And) ((Implication) expr).leftArg).rightArg).symbol.equals("@")) {
            String e = ((Implication) expr).rightArg.toString();
            String variable = ((QuantifierExpression) ((And) ((Implication) expr).leftArg).rightArg).variable;
            return doesVariableHaveFreeEntries(getExpression(e), variable)
                    && substitute(getExpression(e), variable, new Constant("0")).equals(((And) ((Implication) expr).leftArg).leftArg)
                    && ((QuantifierExpression) ((And) ((Implication) expr).leftArg).rightArg).expression instanceof Implication
                    && substitute(getExpression(e), variable, new Increment(new Variable(variable)))
                    .equals(((Implication) ((QuantifierExpression) ((And) ((Implication) expr).leftArg).rightArg).expression).rightArg)
                    && getExpression(e).equals(((Implication) ((QuantifierExpression) ((And) ((Implication) expr).leftArg).rightArg).expression).leftArg);
        }
        return false;
    }

    private Expression substitute(Expression expr, String variable, Expression substituteExpr) {
        if (expr instanceof BinaryOperation) {
            ((BinaryOperation) expr).leftArg = substitute(((BinaryOperation) expr).leftArg, variable, substituteExpr);
            ((BinaryOperation) expr).rightArg = substitute(((BinaryOperation) expr).rightArg, variable, substituteExpr);
            return expr;
        } else if (expr instanceof UnaryOperation) {
            ((UnaryOperation) expr).argument = substitute(((UnaryOperation) expr).argument, variable, substituteExpr);
            return expr;
        } else if (expr instanceof Function) {
            List<Expression> newTerms = new ArrayList<>();
            for (Expression term: ((Function) expr).terms) {
                newTerms.add(substitute(term, variable, substituteExpr));
            }
            ((Function) expr).terms = newTerms;
            return expr;
        } else if (expr instanceof Increment) {
            ((Increment) expr).expression = substitute(((Increment) expr).expression, variable, substituteExpr);
            return expr;
        } else if (expr instanceof Predicate) {
            List <Expression> newTerms = new ArrayList<>();
            for (Expression term: ((Predicate) expr).terms) {
                newTerms.add(substitute(term, variable, substituteExpr));
            }
            ((Predicate) expr).terms = newTerms;
            return expr;
        } else if (expr instanceof QuantifierExpression) {
            if (variable.equals(((QuantifierExpression) expr).variable)) {
                return expr;
            }
            ((QuantifierExpression) expr).expression = substitute(((QuantifierExpression) expr).expression, variable, substituteExpr);
            return expr;
        } else if (expr instanceof Constant) {
            return expr;
        } else if (expr instanceof Variable) {
            if (variable.equals(((Variable) expr).name)) {
                expr = substituteExpr;
            }
            return expr;
        }
        return null;
    }

    private boolean isModusPonens(Expression expr) {
        if (rightImplicationOperands.containsKey(expr.toString())) {
            for (String s: rightImplicationOperands.get(expr.toString())) {
                Expression e = parser.parse(s);
                Expression left = ((Implication) e).leftArg;
                if (checkedExpressions.contains(left)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isForallIntroduction(Expression expr) {
        if (expr instanceof Implication && ((Implication) expr).rightArg instanceof QuantifierExpression
        && ((QuantifierExpression) ((Implication) expr).rightArg).symbol.equals("@")) {
            Expression e = new Implication(((Implication) expr).leftArg,
                    ((QuantifierExpression) ((Implication) expr).rightArg).expression);
            String variable = ((QuantifierExpression) ((Implication) expr).rightArg).variable;

            return checkedExpressions.contains(e) && !doesVariableHaveFreeEntries(((Implication) expr).leftArg, variable) &&
                    (hypotheses.size() == 0 || !doesVariableHaveFreeEntries(hypotheses.get(hypotheses.size() - 1), variable));
        }
        return false;
    }

    private boolean isExistsIntroduction(Expression expr) {
        if (expr instanceof Implication && ((Implication) expr).leftArg instanceof QuantifierExpression
        && ((QuantifierExpression) ((Implication) expr).leftArg).symbol.equals("?")) {
            Expression e = new Implication(((QuantifierExpression) ((Implication) expr).leftArg).expression,
                    ((Implication) expr).rightArg);
            String variable = ((QuantifierExpression) ((Implication) expr).leftArg).variable;

            return checkedExpressions.contains(e) && !doesVariableHaveFreeEntries(((Implication) expr).rightArg, variable) &&
                    (hypotheses.size() == 0 || !doesVariableHaveFreeEntries(hypotheses.get(hypotheses.size() - 1), variable));
        }
        return false;
    }

    private boolean doesVariableHaveFreeEntries(Expression expr, String variable) {
        if (expr instanceof BinaryOperation) {
            return doesVariableHaveFreeEntries(((BinaryOperation) expr).leftArg, variable)
                    || doesVariableHaveFreeEntries(((BinaryOperation) expr).rightArg, variable);
        } else if (expr instanceof UnaryOperation) {
            return doesVariableHaveFreeEntries(((UnaryOperation) expr).argument, variable);
        } else if (expr instanceof Function) {
            boolean flag = false;
            for (Expression term: ((Function) expr).terms) {
                flag = flag || doesVariableHaveFreeEntries(term, variable);
            }
            return flag;
        } else if (expr instanceof Increment) {
            return doesVariableHaveFreeEntries(((Increment) expr).expression, variable);
        } else if (expr instanceof Predicate) {
            boolean flag = false;
            for (Expression term: ((Predicate) expr).terms) {
                flag = flag || doesVariableHaveFreeEntries(term, variable);
            }
            return flag;
        } else if (expr instanceof QuantifierExpression) {
            if (!variable.equals(((QuantifierExpression) expr).variable)) {
                return doesVariableHaveFreeEntries(((QuantifierExpression) expr).expression, variable);
            }
            return false;
        } else if (expr instanceof Variable) {
            return variable.equals(((Variable) expr).name);
        }
        return false;
    }

    private boolean isAxiom11(Expression expr) {
        if (expr instanceof Implication && ((Implication) expr).leftArg instanceof QuantifierExpression
                && ((QuantifierExpression) ((Implication) expr).leftArg).symbol.equals("@")) {
            QuantifierExpression left = (QuantifierExpression) ((Implication) expr).leftArg;
            String var = left.variable;
            Expression formula = left.expression;
            Expression formulaWithSubstitution = ((Implication) expr).rightArg;
            isVariableFree = true;
            substituteExpression = null;

            if (!matches(formula, formulaWithSubstitution, var)) {
                return false;
            }

            return substituteExpression == null || (isTerm(substituteExpression)
                    && isTermFreeForSubstitution(formula, substituteExpression, var));
        }
        return false;
    }

    private boolean isAxiom12(Expression expr) {
        if (expr instanceof Implication && ((Implication) expr).rightArg instanceof QuantifierExpression
            && ((QuantifierExpression) ((Implication) expr).rightArg).symbol.equals("?")) {
            QuantifierExpression right = (QuantifierExpression) ((Implication) expr).rightArg;
            String var = right.variable;
            Expression formula = right.expression;
            Expression formulaWithSubstitution = ((Implication) expr).leftArg;
            isVariableFree = true;
            substituteExpression = null;

            if (!matches(formula, formulaWithSubstitution, var)) {
                return false;
            }

            return substituteExpression == null || (isTerm(substituteExpression)
                    && isTermFreeForSubstitution(formula, substituteExpression, var));
        }
        return false;
    }

    private boolean isTerm(Expression expr) {
        return expr instanceof Add || expr instanceof Multiply
                || expr instanceof Function || expr instanceof Variable
                || expr instanceof Constant || expr instanceof Increment;
    }

    private boolean isTermFreeForSubstitution(Expression expr, Expression term, String variable) {
        variableNames.clear();
        int termVariablesNumber = countTermVariables(term);
        variableLock = false;
        return canSubstituteWithTerm(expr, variable);
    }

    private boolean canSubstituteWithTerm(Expression expr, String variable) {
        if (expr instanceof BinaryOperation) {
            return canSubstituteWithTerm(((BinaryOperation) expr).leftArg, variable)
                    && canSubstituteWithTerm(((BinaryOperation) expr).rightArg, variable);
        } else if (expr instanceof UnaryOperation) {
            return canSubstituteWithTerm(((UnaryOperation) expr).argument, variable);
        } else if (expr instanceof Function) {
            boolean flag = true;
            for (Expression e: ((Function) expr).terms) {
                flag = flag && canSubstituteWithTerm(e, variable);
            }
            return flag;
        } else if (expr instanceof Increment) {
            return canSubstituteWithTerm(((Increment) expr).expression, variable);
        } else if (expr instanceof Predicate) {
            boolean flag = true;
            for (Expression e: ((Predicate) expr).terms) {
                flag = flag && canSubstituteWithTerm(e, variable);
            }
            return flag;
        } else if (expr instanceof QuantifierExpression) {
            if (variable.equals(((QuantifierExpression) expr).variable)) {
                return true;
            }
            boolean prev = variableLock;
            for (String name: variableNames) {
                if (name.equals(((QuantifierExpression) expr).variable)) {
                    variableLock = true;
                    break;
                }
            }
            boolean result = canSubstituteWithTerm(((QuantifierExpression) expr).expression, variable);
            variableLock = prev;
            return result;
        } else if (expr instanceof Constant) {
            return true;
        } else if (expr instanceof Variable) {
            if (!variable.equals(((Variable) expr).name)) {
                return true;
            }
            return !variableLock;
        }
        return false;
    }

    private int countTermVariables(Expression term) {
        if (term instanceof BinaryOperation) {
            return countTermVariables(((BinaryOperation) term).leftArg) + countTermVariables(((BinaryOperation) term).rightArg);
        } else if (term instanceof Function) {
            int counter = 0;
            for (Expression e: ((Function) term).terms) {
                counter += countTermVariables(e);
            }
            return counter;
        } else if (term instanceof Constant) {
            return 0;
        } else if (term instanceof Increment) {
            return countTermVariables(((Increment) term).expression);
        } else if (term instanceof Variable) {
            variableNames.add(((Variable) term).name);
            return 1;
        }
        return -1;
    }

    private boolean matches(Expression left, Expression right, String variable) {
        if (left instanceof BinaryOperation && right instanceof BinaryOperation) {
            if (left.getOperation().equals(right.getOperation())) {
                return matches(((BinaryOperation) left).leftArg, ((BinaryOperation) right).leftArg, variable) &&
                        matches(((BinaryOperation) left).rightArg, ((BinaryOperation) right).rightArg, variable);
            }
        } else if (left instanceof UnaryOperation && right instanceof UnaryOperation) {
            if (left.getOperation().equals(right.getOperation())) {
                return matches(((UnaryOperation) left).argument, ((UnaryOperation) right).argument, variable);
            }
        } else if (left instanceof Constant && right instanceof Constant) {
            return ((Constant) left).getName().equals(((Constant) right).getName());
        } else if (left instanceof Function && right instanceof Function) {
            if (!((Function) left).name.equals(((Function) right).name)) {
                return false;
            }

            if (((Function) left).terms.size() != ((Function) right).terms.size()) {
                return false;
            }

            boolean flag = true;
            for (int i = 0; i < ((Function) left).terms.size(); i++) {
                flag = flag && matches(((Function) left).terms.get(i), ((Function) right).terms.get(i), variable);
            }

            return flag;
        } else if (left instanceof Increment && right instanceof Increment) {
            return matches(((Increment) left).expression, ((Increment) right).expression, variable);
        } else if (left instanceof Predicate && right instanceof Predicate) {
            if (!((Predicate) left).name.equals(((Predicate) right).name)) {
                return false;
            }

            if (((Predicate) left).terms.size() != ((Predicate) right).terms.size()) {
                return false;
            }

            boolean flag = true;
            for (int i = 0; i < ((Predicate) left).terms.size(); i++) {
                flag = flag && matches(((Predicate) left).terms.get(i), ((Predicate) right).terms.get(i), variable);
            }

            return flag;
        } else if (left instanceof QuantifierExpression && right instanceof QuantifierExpression) {
            if (!((QuantifierExpression) left).variable.equals(((QuantifierExpression) right).variable)) {
                return false;
            }
            if (!((QuantifierExpression) left).symbol.equals(((QuantifierExpression) right).symbol)) {
                return false;
            }
            boolean prevIsVariableFree = isVariableFree;
            if (((QuantifierExpression) left).variable.equals(variable)) {
                isVariableFree = false;
            }
            boolean result = matches(((QuantifierExpression) left).expression, ((QuantifierExpression) right).expression, variable);
            isVariableFree = prevIsVariableFree;
            return result;
        } else if (left instanceof Variable) {
            if (!((Variable) left).name.equals(variable)) {
                return left.equals(right);
            }

            if (!isVariableFree) {
                return left.equals(right);
            }

            if (substituteExpression == null) {
                substituteExpression = right;
                return true;
            } else {
                return right.equals(substituteExpression);
            }
        }
        return false;
    }

    private void readInput() {
        readHeader();
        readProof();
    }

    private void readHeader() {
        String header = scanner.nextLine();
        int pos = header.indexOf("|-");

        String premisesList = header.substring(0, pos);
        boolean isEmpty = true;
        for (int i = 0; i < premisesList.length(); i++) {
            if (!Character.isWhitespace(premisesList.charAt(i))) {
                isEmpty = false;
                break;
            }
        }
        if (isEmpty) {
            hypotheses = new ArrayList<>();
        } else {
            int bracketsCounter = 0;
            int firstPos = 0;
            int secondPos = -1;
            hypotheses = new ArrayList<>();
            for (int i = 0; i < premisesList.length(); i++) {
                if (premisesList.charAt(i) == '(') {
                    bracketsCounter++;
                } else if (premisesList.charAt(i) == ')') {
                    bracketsCounter--;
                } else if (premisesList.charAt(i) == ',' && bracketsCounter == 0) {
                    secondPos = i;
                    hypotheses.add(new ExpressionParser().parse(premisesList.substring(firstPos, secondPos)));
                    firstPos = secondPos + 1;
                }
            }
            hypotheses.add(new ExpressionParser().parse(premisesList.substring(firstPos)));
        }

        String expressionToProve = header.substring(pos + 2);
        expression = parser.parse(expressionToProve);
    }

    private void readProof() {
        while (scanner.hasNext()) {
            proof.add(parser.parse(scanner.nextLine()));
        }
    }

    private boolean checkLastProofExpression() {
        if (proof.size() == 0) {
            return false;
        }
        Expression lastProofExpression = proof.get(proof.size() - 1);
        return expression.equals(lastProofExpression);
    }
}
