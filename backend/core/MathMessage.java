package core;

import util.Stack;

public class MathMessage extends Message {

    private String expression;

    public MathMessage(String content, User sender) {
        super(content, sender);
        this.expression = calculateExpression();
    }

    private int getPrecedence(String operator) {
        switch (operator) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "^":
                return 3;
        }

        return -1;
    }

    private String evaluate(String operand1, String operand2, String operator) {
        switch (operator) {
            case "+":
                return String.valueOf(Double.parseDouble(operand1) + Double.parseDouble(operand2));
            case "-":
                return String.valueOf(Double.parseDouble(operand1) - Double.parseDouble(operand2));
            case "*":
                return String.valueOf(Double.parseDouble(operand1) * Double.parseDouble(operand2));
            case "/":
                return String.valueOf(Double.parseDouble(operand1) / Double.parseDouble(operand2));
            case "^":
                return String.valueOf(Math.pow(Double.parseDouble(operand1), Double.parseDouble(operand2)));
        }

        return null;
    }

    public String calculateExpression() {
        try {
            Stack<String> operatorStack = new Stack<>();
            Stack<String> operandStack = new Stack<>();

            String expr = getContent();
            expr = expr.replaceAll("\\s+", "");

            for (int i = 0; i < expr.length(); i++) {
                char c = expr.charAt(i);

                switch (c) {
                    case '(': {
                        operatorStack.push("(");
                        break;
                    }

                    case ')': {
                        while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                            String operator = operatorStack.pop();
                            String operand2 = operandStack.pop();
                            String operand1 = operandStack.pop();

                            operandStack.push(evaluate(operand1, operand2, operator));
                        }
                        operatorStack.pop();
                        break;
                    }

                    case '+':
                    case '-':
                    case '*':
                    case '/':
                    case '^': {
                        while (!operatorStack.isEmpty() && getPrecedence(operatorStack.peek()) >= getPrecedence(String.valueOf(c))) {
                            String operator = operatorStack.pop();
                            String operand2 = operandStack.pop();
                            String operand1 = operandStack.pop();

                            operandStack.push(evaluate(operand1, operand2, operator));
                        }
                        operatorStack.push(String.valueOf(c));
                        break;
                    }

                    default: {
                        StringBuilder operand = new StringBuilder();
                        while (i < expr.length() && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                            operand.append(expr.charAt(i));
                            i++;
                        }
                        i--;
                        operandStack.push(operand.toString());
                        break;
                    }
                }
            }

            while (!operatorStack.isEmpty()) {
                String operator = operatorStack.pop();
                String operand2 = operandStack.pop();
                String operand1 = operandStack.pop();

                operandStack.push(evaluate(operand1, operand2, operator));
            }

            return operandStack.pop();
        } catch (Exception e) {
            return "Undefined";
        }
    }


    @Override
    public String toString() {
        return "{\"from\": \"" + getSender().getUsername() + "\", \"content\": \"" + getContent() + "\", \"result\": \"" + expression + "\"}";
    }
}
