package com.example.rule.condition.parser;

import com.example.rule.common.MethodDescriptor;
import com.example.rule.common.registry.MethodDescriptionRegistry;
import com.example.rule.model.Element;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.example.rule.token.Tokenizer.Token;
import static com.example.rule.token.Tokenizer.Type;

public class ConditionParser {

	private int pos = 0;
	private final List<Token> tokens;
	private final ObjectMapper mapper;
	private final List<Element> elements;
	private final MethodDescriptionRegistry registry;

	public ConditionParser(List<Element> elements, List<Token> tokens,
	                       MethodDescriptionRegistry registry, ObjectMapper mapper) {
		this.tokens = tokens;
		this.mapper = mapper;
		this.elements = elements;
		this.registry = registry;
	}

	private Token peek() {
		if (pos == 15) {
			System.out.println();
		}

		return tokens.get(pos);
	}

	private Token consume() {
		if (pos == 27 || pos == 74 || pos == 9) {
			System.out.println();
		}

		return tokens.get(pos++);
	}

	private boolean match(Type t) {
		if (pos == 27 || pos == 74 || pos == 9) {
			System.out.println();
		}

		if (peek().type == t) {
			pos++;
			return true;
		}
		return false;
	}

	// Entry
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
			consume();

			// parse full comparison + arithmetic + boolean inside parentheses
			Object val = parseComparisonOrArithmeticOrBoolean();

			if (!match(Type.RIGHT_PARENTHESIS))
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
				Object res = parseFunctionCall(joinChain(chain));

				if (res instanceof Boolean bool) {
					return bool;
				} else {
					throw new RuntimeException();
				}
			}

			if (chain.size() == 2 && chain.get(0).matches("E\\d+") &&
				(chain.get(1).equals("first") || chain.get(1).equals("second"))) {
				BigDecimal bd = resolveElementRef(chain.get(0) + "." + chain.get(1));
				return bd.compareTo(BigDecimal.ZERO) != 0;
			}
			throw new IllegalArgumentException(
				"Unexpected identifier in boolean context: " + joinChain(chain));
		}

		// fallback: parse comparison / arithmetic, then convert to boolean
		return parseComparisonOrArithmeticAsBoolean();
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

	// Updated method to parse comparison or arithmetic expression
	private Object parseComparisonOrArithmetic() throws Exception {
		Token token = peek();

		if ("true".equalsIgnoreCase(token.text) || "false".equalsIgnoreCase(token.text)) {
			consume();
			return Boolean.parseBoolean(token.text);
		}

		Object left = parseArithmeticExpression(); // parse arithmetic first

		token = peek();
		if (token.type == Type.GREATER_THAN || token.type == Type.LESS_THAN || token.type == Type.GREATER_THAN_OR_EQUAL ||
			token.type == Type.LESS_THAN_OR_EQUAL || token.type == Type.EQUALS || token.type == Type.NOT_EQUALS) {
			consume();
			Object right = parseArithmeticExpression();
			return compare(left, right, token.type);
		}

		return left;
	}

	// Treat numeric result as boolean if needed
	private boolean parseComparisonOrArithmeticAsBoolean() throws Exception {
		Object left = parseArithmeticExpression(); // only arithmetic
		Token op = peek();
		if (op.type == Type.GREATER_THAN || op.type == Type.LESS_THAN || op.type == Type.GREATER_THAN_OR_EQUAL ||
			op.type == Type.LESS_THAN_OR_EQUAL || op.type == Type.EQUALS || op.type == Type.NOT_EQUALS) {
			consume();
			Object right = parseArithmeticExpression();
			return compare(left, right, op.type);
		}
		return toBoolean(left);
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

	// Arithmetic expression (returns BigDecimal)
	private Object parseArithmeticExpression() throws Exception {
		Object left = parseArithmeticTerm();
		while (peek().type == Type.PLUS || peek().type == Type.MINUS) {
			Token op = consume();
			Object right = parseArithmeticTerm();
			if (!(left instanceof BigDecimal) || !(right instanceof BigDecimal))
				throw new IllegalArgumentException("Arithmetic expects numbers");
			left = op.type == Type.PLUS ? ((BigDecimal) left).add(
				(BigDecimal) right) : ((BigDecimal) left).subtract((BigDecimal) right);
		}
		return left;
	}

	private Object parseArithmeticTerm() throws Exception {
		Object left = parseArithmeticFactor();
		while (peek().type == Type.MULTIPLY || peek().type == Type.DIVIDE) {
			Token op = consume();
			Object right = parseArithmeticFactor();
			if (!(left instanceof BigDecimal) || !(right instanceof BigDecimal))
				throw new IllegalArgumentException("Arithmetic expects numbers");
			left = op.type == Type.MULTIPLY ? ((BigDecimal) left).multiply(
				(BigDecimal) right) : ((BigDecimal) left).divide((BigDecimal) right);
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
				return parseFunctionCall(joinChain(chain));
			}
			if (chain.size() == 2 && chain.get(0).matches("E\\d+") &&
				(chain.get(1).equals("first") || chain.get(1).equals("second"))) {
				return resolveElementRef(chain.get(0) + "." + chain.get(1));
			}
			throw new IllegalArgumentException(
				"Unexpected identifier in arithmetic: " + joinChain(chain));
		}

		if (t.type == Type.LEFT_PARENTHESIS) {
			consume();
			Object v = parseArithmeticExpression();
			if (!match(Type.RIGHT_PARENTHESIS)) {
				throw new IllegalArgumentException(
					"Missing ) but have " + getStringBeforeException());
			}
			return v;
		}

		if (t.type == Type.MINUS) {
			consume();
			Object v = parseArithmeticFactor();
			if (!(v instanceof BigDecimal))
				throw new IllegalArgumentException("Unary - on non-number");
			return ((BigDecimal) v).negate();
		}

		throw new IllegalArgumentException("Unexpected token in factor: " + t);
	}

	private BigDecimal resolveElementRef(String ref) {
		String[] parts = ref.split("\\.");
		String e = parts[0];
		int idx = Integer.parseInt(e.substring(1));
		if (idx < 0 || idx >= elements.size())
			throw new IllegalArgumentException("Element index out of range: " + idx);
		Element el = elements.get(idx);
		return "first".equals(parts[1]) ? el.first() : el.second();
	}

	private boolean compare(Object leftVal, Object rightVal, Type op) {
		if (!(leftVal instanceof BigDecimal a) || !(rightVal instanceof BigDecimal b)) {
			throw new IllegalArgumentException("Comparison expects numeric expressions");
		}

		int cmp = a.compareTo(b);

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
		if (val instanceof BigDecimal) return ((BigDecimal) val).compareTo(BigDecimal.ZERO) != 0;
		if (val instanceof String) return Boolean.parseBoolean((String) val);
		throw new IllegalArgumentException("Cannot convert to boolean: " + val);
	}

	// ---------------- Function handling ----------------
	// fullIdent is qualified name like "weather.isCold" or "custom.math.call"
	private Object parseFunctionCall(String fullIdent) throws Exception {
		if (!fullIdent.contains("."))
			throw new IllegalArgumentException("Function must be bean.method, got: " + fullIdent);
		// only call when next token is LPAREN (caller ensures this)
		if (!match(Type.LEFT_PARENTHESIS)) throw new IllegalArgumentException(
			"Expected ( after function " + fullIdent + " at position " + pos);

		// resolve bean & method: use everything except last segment as bean, last as method
		String[] segs = fullIdent.split("\\.");
		if (segs.length < 2)
			throw new IllegalArgumentException("Invalid function name: " + fullIdent);
		String method = segs[segs.length - 1];
		String bean = String.join(".", java.util.Arrays.copyOf(segs, segs.length - 1));

		MethodDescriptor desc = registry.resolve(bean, method);
		if (desc == null) throw new IllegalArgumentException("Unknown function: " + fullIdent);

		Object[] parsedArgs = buildArgsForMethod(desc.method());

		if (!match(Type.RIGHT_PARENTHESIS))
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
		if (peek().type == Type.RIGHT_PARENTHESIS) return new Object[0];

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
			switch (v) {
				case BigDecimal ignored -> {
					return v;
				}
				case Number number -> {
					return BigDecimal.valueOf((Double) number);
				}
				case String s -> {
					return new BigDecimal(s);
				}
				case Boolean b -> {
					return b ? BigDecimal.ONE : BigDecimal.ZERO;
				}
				default -> {
				}
			}
		}
		if (target == String.class) return v.toString();
		if (target == Boolean.class || target == boolean.class) {
			switch (v) {
				case Boolean b -> {
					return b;
				}
				case BigDecimal bigDecimal -> {
					return bigDecimal.compareTo(BigDecimal.ZERO) != 0;
				}
				case String s -> {
					return Boolean.parseBoolean(s);
				}
				default -> {
				}
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
			StringBuilder builder = new StringBuilder(tokens.get(pos).text + " in ");

			for (int i = 0; i <= pos; i++) {
				builder.append(tokens.get(i).prettierText);
			}

			return builder.toString();
		}

		return "EOF";
	}
}
