package com.example.pattern.token;

import com.example.market.common.candlestick.model.Candlestick;
import com.example.pattern.common.MethodDescriptor;
import com.example.pattern.common.registry.MethodDescriptionRegistry;
import com.example.pattern.condition.parser.reference.CandlestickPatternReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.pattern.token.Tokenizer.Token;
import static com.example.pattern.token.Tokenizer.Type;
import static com.example.pattern.token.Tokenizer.Type.*;

public class PatternTokenExecutor {

    private int leftParenthesisOpens = 0;
    private int pos = 0;
    private final List<Token> tokens;
    private final ObjectMapper mapper;
    private final List<Candlestick> candlesticks;
    private final MethodDescriptionRegistry registry;
    private final Set<Integer> methodRightParenthesisPos = new HashSet<>();
    private final CandlestickPatternReference candlestickPatternReference = new CandlestickPatternReference();

    public PatternTokenExecutor(List<Candlestick> candlesticks, List<Token> tokens,
                                MethodDescriptionRegistry registry, ObjectMapper mapper) {
        this.tokens = tokens;
        this.mapper = mapper;
        this.candlesticks = candlesticks;
        this.registry = registry;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token next() {
        return tokens.get(pos + 1);
    }

    private Token consume() {
        return tokens.get(pos++);
    }

    private boolean match(Type t) {
        if (peek().type == t) {
            pos++;
            return true;
        }
        return false;
    }

    // Entry
    // TODO: 1/31/2026 add rounding mode argument (scale) to operations
    public boolean parse() throws Exception {
        return toBoolean(parseComparisonOrArithmeticOrBoolean());
    }

    // OR
    private boolean parseOr() throws Exception {
        boolean left = parseAnd();
        while (peek().type == Type.OR) {
            consume();
            boolean right = parseAnd();
            left = left || right;
        }
        return left;
    }

    // AND
    private boolean parseAnd() throws Exception {
        boolean left = parseNot();
        while (peek().type == Type.AND) {
            consume();
            boolean right = parseNot();
            left = left && right;
        }
        return left;
    }

    // NOT
    private boolean parseNot() throws Exception {
        if (match(Type.NOT)) return !parsePrimary();
        return parsePrimary();
    }

    private boolean parsePrimary() throws Exception {
        Token token = peek();

        if (token.type == Type.LEFT_PARENTHESIS) {
            leftParenthesisOpens++;
            consume();

            // parse full comparison + arithmetic + boolean inside parentheses
            Object val = parseComparisonOrArithmeticOrBoolean();

            if (!match(RIGHT_PARENTHESIS) && --leftParenthesisOpens < 0)
                throw new IllegalArgumentException(
                        "Expected ) but have " + getStringBeforeException());

            return toBoolean(val);
        }

        if (token.type == Type.IDENTIFIER) {
            if ("true".equalsIgnoreCase(token.text) || "false".equalsIgnoreCase(token.text)) {
                consume();
                return Boolean.parseBoolean(token.text);
            }

            List<String> chain = readQualifiedName();
            if (peek().type == Type.LEFT_PARENTHESIS) {
                leftParenthesisOpens++;
                Object res = parseFunctionCall(joinChain(chain));

                if (res instanceof Boolean) {
                    return (Boolean) res;
                } else {
                    return toBoolean(parseComparisonOrArithmetic(res));
//                    throw new RuntimeException();
                }
            }

            if (chain.size() == 2 && chain.get(0).matches("E\\d+")) {
                return toBoolean(parseComparisonOrArithmetic(
                        resolveCandlestickRef(Integer.parseInt(chain.get(0).substring(1)), chain.get(1))));
            }

            throw new IllegalArgumentException(
                    "Unexpected identifier in boolean context: " + joinChain(chain));
        }

        // fallback: parse comparison / arithmetic, then convert to boolean
        return toBoolean(parseComparisonOrArithmetic());
    }

    private Object parseComparisonOrArithmeticOrBoolean() throws Exception {
        // We temporarily parse a full boolean expression starting from here.
        // BUT we must allow arithmetic-only or comparison-only expressions too.

        int savedPos = pos; // backup

        try {
            // Try full boolean parse
            return parseOr();
        } catch (Exception ignored) {

            // Boolean parsing failed → fallback to arithmetic/comparison only
            pos = savedPos;
            return parseComparisonOrArithmetic();
        }
    }

    private Object parseComparisonOrArithmetic() throws Exception {
        return parseComparisonOrArithmetic(null);
    }

    // Updated method to parse comparison or arithmetic expression
    private Object parseComparisonOrArithmetic(Object left) throws Exception {
        Token token;

        if (left == null) {
            token = peek();

            if ("true".equalsIgnoreCase(token.text) || "false".equalsIgnoreCase(token.text)) {
                consume();
                return Boolean.parseBoolean(token.text);
            }

            left = parseArithmeticExpression(); // parse arithmetic first
        }

        token = peek();

        if (token.is(RIGHT_PARENTHESIS) && notMethodRightParenthesis()) {
            if (--leftParenthesisOpens < 0) {
                throw new IllegalArgumentException("Unexpected )" + getStringBeforeException());
            }
            consume();
            token = peek();
        }

        if (token.is(GREATER_THAN) || token.is(LESS_THAN) || token.is(GREATER_THAN_OR_EQUAL) ||
                token.is(LESS_THAN_OR_EQUAL) || token.is(EQUALS) || token.is(NOT_EQUALS)) {
            consume();
            Object right = parseArithmeticExpression();
            return compare(left, right, token.type);
        }

        return left;
    }

    // helper: read qualified name segments but do not consume trailing LPAREN or other tokens
    private List<String> readQualifiedName() {
        List<String> segs = new ArrayList<>();
        Token first = consume(); // IDENT
        segs.add(first.text);
        while (peek().type == Type.DOT) {
            consume(); // DOT
            Token next = consume();
            if (next.type != Type.IDENTIFIER)
                throw new IllegalArgumentException("Expected identifier after '.' at pos " + pos);
            segs.add(next.text);
        }
        return segs;
    }

    private String joinChain(List<String> chain) {
        return String.join(".", chain);
    }

    private Object parseArithmeticExpression() throws Exception {
        Object left = parseArithmeticFactor();

        while (isArithmeticExpression(peek())) {
            Token token = consume();
            Object right = parseArithmeticFactor();

            if (!(left instanceof BigDecimal leftBD) || !(right instanceof BigDecimal rightBD))
                throw new IllegalArgumentException("Arithmetic expects numbers");

            left = switch (token.type) {
                case PLUS -> leftBD.add(rightBD);
                case MINUS -> leftBD.subtract(rightBD);
                case MULTIPLY -> leftBD.multiply(rightBD);
                default -> leftBD.divide(rightBD);
            };

            if (peek().is(RIGHT_PARENTHESIS) && isArithmeticExpression(next())) {
                consume();
            }
        }
        return left;
    }

    private boolean isArithmeticExpression(Token token) {
        return token.is(PLUS) || token.is(MINUS) || token.is(MULTIPLY) || token.is(DIVIDE);
    }

    // Arithmetic expression (returns BigDecimal)
    private Object parseArithmeticExpression2() throws Exception {
        Object left = parseArithmeticTerm();

        while (peek().type == PLUS || peek().type == MINUS) {
            Token op = consume();
            Object right = parseArithmeticTerm();
            if (!(left instanceof BigDecimal) || !(right instanceof BigDecimal))
                throw new IllegalArgumentException("Arithmetic expects numbers");
            left = op.type == PLUS ? ((BigDecimal) left).add(
                    (BigDecimal) right) : ((BigDecimal) left).subtract((BigDecimal) right);

            if (peek().type == RIGHT_PARENTHESIS &&
                    (next().type == PLUS || next().type == MINUS)) {
                consume();
            }
        }
        return left;
    }

    private Object parseArithmeticTerm() throws Exception {
        Object left = parseArithmeticFactor();

        while (peek().type == MULTIPLY || peek().type == DIVIDE) {
            Token op = consume();
            Object right = parseArithmeticFactor();
            if (!(left instanceof BigDecimal) || !(right instanceof BigDecimal))
                throw new IllegalArgumentException("Arithmetic expects numbers");
            left = op.type == MULTIPLY ? ((BigDecimal) left).multiply(
                    (BigDecimal) right) : ((BigDecimal) left).divide((BigDecimal) right);

            if (peek().type == RIGHT_PARENTHESIS &&
                    (next().type == MULTIPLY || next().type == DIVIDE)) {
                consume();
            }
        }
        return left;
    }

    private Object parseArithmeticFactor() throws Exception {
        Token t = peek();

        if (t.type == Type.NUMBER) {
            consume();
            return new BigDecimal(t.text);
        }

        if (t.type == Type.STRING) {
            consume();
            return stripQuotes(t.text);
        }

        if (t.type == Type.IDENTIFIER) {
            List<String> chain = readQualifiedName();
            if (peek().type == Type.LEFT_PARENTHESIS) {
                leftParenthesisOpens++;
                return parseFunctionCall(joinChain(chain));
            }
            if (chain.size() == 2 && chain.get(0).matches("E\\d+")) {
                return resolveCandlestickRef(Integer.parseInt(chain.get(0).substring(1)), chain.get(1));
            }
            throw new IllegalArgumentException(
                    "Unexpected identifier in arithmetic: " + joinChain(chain));
        }

        if (t.type == Type.LEFT_PARENTHESIS) {
            leftParenthesisOpens++;
            consume();
            Object v = parseArithmeticExpression();

            //or remember how many times left parenthesis was opened and right parenthesis was closed
//			consume();
            if (notMethodRightParenthesis() && !match(RIGHT_PARENTHESIS) && --leftParenthesisOpens < 0) {
                throw new IllegalArgumentException(
                        "Missing ) but have " + getStringBeforeException());
            }
            return v;
        }

        if (t.type == MINUS) {
            consume();
            Object v = parseArithmeticFactor();
            if (!(v instanceof BigDecimal))
                throw new IllegalArgumentException("Unary - on non-number");
            return ((BigDecimal) v).negate();
        }

        throw new IllegalArgumentException("Unexpected token in factor: " + t);
    }

    private BigDecimal resolveCandlestickRef(int idx, String method) {
        return candlestickPatternReference.value(method, candlesticks.get(candlesticks.size() - 1 - idx));
    }

    private boolean compare(Object leftObjVal, Object rightObjVal, Type op) {
        if (!(leftObjVal instanceof BigDecimal leftVal)) {
            throw new IllegalArgumentException("Comparison expects numeric expressions");
        }

        if (!(rightObjVal instanceof BigDecimal rightVal)) {
            throw new IllegalArgumentException("Comparison expects numeric expressions");
        }

        int cmp = leftVal.compareTo(rightVal);

        return switch (op) {
            case GREATER_THAN -> cmp > 0;
            case LESS_THAN -> cmp < 0;
            case GREATER_THAN_OR_EQUAL -> cmp >= 0;
            case LESS_THAN_OR_EQUAL -> cmp <= 0;
            case EQUALS -> cmp == 0;
            case NOT_EQUALS -> cmp != 0;
            default -> throw new IllegalArgumentException("Unknown comparison " + op);
        };
    }

    // convert object to boolean: boolean -> itself, BigDecimal -> !=0, String -> boolean parse
    private boolean toBoolean(Object val) {
        if (val instanceof Boolean) return (Boolean) val;
//		if (val instanceof BigDecimal) return ((BigDecimal) val).compareTo(BigDecimal.ZERO) != 0;
        if (val instanceof String) return Boolean.parseBoolean((String) val);
        throw new IllegalArgumentException("Cannot convert to boolean: " + val);
    }

    // ---------------- Function handling ----------------
    // fullIdent is qualified name like "weather.isCold" or "custom.math.call"
    private Object parseFunctionCall(String fullIdent) throws Exception {
        if (!fullIdent.contains("."))
            throw new IllegalArgumentException("Function must be bean.method, got: " + fullIdent);
        // only call when next token is LPAREN (caller ensures this)
        consume();
//		if (!match(Type.LEFT_PARENTHESIS)) throw new IllegalArgumentException(
//			"Expected ( after function " + fullIdent + " at position " + pos);

        // resolve bean & method: use everything except last segment as bean, last as method
        String[] segs = fullIdent.split("\\.");
        if (segs.length < 2)
            throw new IllegalArgumentException("Invalid function name: " + fullIdent);
        String method = segs[segs.length - 1];
        String bean = String.join(".", java.util.Arrays.copyOf(segs, segs.length - 1));

        MethodDescriptor desc = registry.resolve(bean, method);
        if (desc == null) throw new IllegalArgumentException("Unknown function: " + fullIdent);

        Object[] parsedArgs = buildArgsForMethod(desc.method());

        if (!match(RIGHT_PARENTHESIS) && --leftParenthesisOpens < 0)
            throw new IllegalArgumentException("Expected ) but have " + getStringBeforeException());

        Parameter[] params = desc.method().getParameters();
        Object[] invokeArgs = new Object[params.length];
        for (int i = 0; i < params.length; i++)
            invokeArgs[i] =
                    convertArgument(i < parsedArgs.length ? parsedArgs[i] : null, params[i].getType());

        Object result = desc.method().invoke(desc.bean(), invokeArgs);
        return result == null ? true : result;
    }

    private Object[] buildArgsForMethod(java.lang.reflect.Method method) throws Exception {
        List<Object> args = new ArrayList<>();
        if (peek().type == RIGHT_PARENTHESIS) return new Object[0];

        addMethodRightParenthesisPos();

        while (true) {
            // Use full arithmetic/comparison parser for each argument
            Object v = parseComparisonOrArithmetic();
            args.add(v);

            if (peek().type == Type.COMMA) {
                consume(); // skip comma
                continue;
            }
            break;
        }

        // pad with null if fewer args than parameters
        while (args.size() < method.getParameterCount()) args.add(null);
        return args.toArray();
    }

    private Object convertArgument(Object v, Class<?> target) {
        if (v == null) return null;
        if (target == BigDecimal.class) {
            if (v instanceof BigDecimal) {
                return v;  // explicit cast
            } else if (v instanceof Number) {
                return BigDecimal.valueOf(((Number) v).doubleValue());
            } else if (v instanceof String) {
                return new BigDecimal((String) v);
            } else if (v instanceof Boolean b) {
                // explicit cast
                return b ? BigDecimal.ONE : BigDecimal.ZERO;
            } else {
                throw new IllegalArgumentException("Unsupported type: " + v.getClass());
            }
        }
        if (target == String.class) return v.toString();
        if (target == Boolean.class || target == boolean.class) {
            if (v instanceof Boolean) {
                return v;  // explicit cast
            } else if (v instanceof BigDecimal bigDecimal) {
                // explicit cast
                return bigDecimal.compareTo(BigDecimal.ZERO) != 0;
            } else if (v instanceof String s) {
                // explicit cast
                return Boolean.parseBoolean(s);
            } else {
                throw new IllegalArgumentException("Unsupported type: " + v.getClass());
            }
        }
        return mapper.convertValue(v, target);
    }

    private String stripQuotes(String s) {
        if (s == null) return null;
        if ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\""))) {
            String inner = s.substring(1, s.length() - 1);
            return inner.replace("\\'", "'").replace("\\\"", "\"");
        }
        return s;
    }

    private String getStringBeforeException() {
        if (pos < tokens.size()) {
            StringBuilder builder = new StringBuilder(" in ");

            for (int i = 0; i <= pos; i++) {
                builder.append(tokens.get(i).prettierText);
            }

            return builder.toString();
        }

        return "EOF";
    }

    private void addMethodRightParenthesisPos() {
        int position = this.pos - 1;
        int leftParenthesis = 1;
        int rightParenthesis = 0;

        while (leftParenthesis > rightParenthesis) {
            Token token = tokens.get(++position);

            if (token.is(LEFT_PARENTHESIS)) leftParenthesis++;
            if (token.is(RIGHT_PARENTHESIS)) rightParenthesis++;
        }

        methodRightParenthesisPos.add(position);
    }

    private boolean notMethodRightParenthesis() {
        return !methodRightParenthesisPos.contains(pos);
    }
}
