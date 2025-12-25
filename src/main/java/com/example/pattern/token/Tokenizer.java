package com.example.pattern.token;

import java.util.ArrayList;
import java.util.List;

public class Tokenizer {

	public enum Type {
		LEFT_PARENTHESIS, RIGHT_PARENTHESIS, COMMA, PLUS, MINUS, MULTIPLY, DIVIDE,
		GREATER_THAN, LESS_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN_OR_EQUAL, EQUALS, NOT_EQUALS,
		AND, OR, NOT, IDENTIFIER, NUMBER, STRING, DOT, EOF
	}

	public static class Token {
		public final Type type;
		public final String text;
		public final String prettierText;

		public Token(Type type, String text) {
			this.type = type;
			this.text = text;
			this.prettierText = text;
		}

		public Token(Type type, String text, String prettierText) {
			this.type = type;
			this.text = text;
			this.prettierText = prettierText;
		}

		@Override
		public String toString() {
			return type + "('" + text + "')";
		}
	}

	private final String input;
	private final int length;
	private int pos = 0;

	public Tokenizer(String input) {
		this.input = input == null ? "" : input;
		this.length = this.input.length();
	}

	public List<Token> tokenize() {
		List<Token> tokens = new ArrayList<>();
		while (pos < length) {
			char c = input.charAt(pos);
			// skip whitespace
			if (Character.isWhitespace(c)) {
				pos++;
				continue;
			}

			switch (c) {
				case '(' -> {
					tokens.add(new Token(Type.LEFT_PARENTHESIS, "("));
					pos++;
				}
				case ')' -> {
					tokens.add(new Token(Type.RIGHT_PARENTHESIS, ")"));
					pos++;
				}
				case ',' -> {
					tokens.add(new Token(Type.COMMA, ",", ", "));
					pos++;
				}
				case '+' -> {
					tokens.add(new Token(Type.PLUS, "+", " + "));
					pos++;
				}
				case '-' -> {
					tokens.add(new Token(Type.MINUS, "-", " - "));
					pos++;
				}
				case '*' -> {
					tokens.add(new Token(Type.MULTIPLY, "*", " * "));
					pos++;
				}
				case '/' -> {
					tokens.add(new Token(Type.DIVIDE, "/", " / "));
					pos++;
				}
				case '.' -> {
					tokens.add(new Token(Type.DOT, "."));
					pos++;
				}
				case '>' -> {
					if (peekNext() == '=') {
						tokens.add(new Token(Type.GREATER_THAN_OR_EQUAL, ">=", " >= "));
						pos += 2;
					} else {
						tokens.add(new Token(Type.GREATER_THAN, ">", " > "));
						pos++;
					}
				}
				case '<' -> {
					if (peekNext() == '=') {
						tokens.add(new Token(Type.LESS_THAN_OR_EQUAL, "<=", " <= "));
						pos += 2;
					} else {
						tokens.add(new Token(Type.LESS_THAN, "<", " < "));
						pos++;
					}
				}
				case '=' -> {
					if (peekNext() == '=') {
						tokens.add(new Token(Type.EQUALS, "==", " == "));
						pos += 2;
					} else throw new IllegalArgumentException("Unexpected '=' at " + pos);
				}
				case '!' -> {
					if (peekNext() == '=') {
						tokens.add(new Token(Type.NOT_EQUALS, "!=", " != "));
						pos += 2;
					} else throw new IllegalArgumentException("Unexpected '!' at " + pos);
				}
				case '"', '\'' -> tokens.add(readString());
				default -> {
					if (Character.isDigit(c)) tokens.add(readNumber());
					else if (Character.isLetter(c) || c == '_') tokens.add(readIdentOrKeyword());
					else throw new IllegalArgumentException("Unexpected char: " + c + " at " + pos);
				}
			}
		}
		tokens.add(new Token(Type.EOF, ","));
		return tokens;
	}

	private char peekNext() {
		return pos + 1 < length ? input.charAt(pos + 1) : '\0';
	}

	private Token readString() {
		char quote = input.charAt(pos++);
		int start = pos;
		while (pos < length && input.charAt(pos) != quote) {
			if (input.charAt(pos) == '\\' && pos + 1 < length) pos += 2; // allow escapes
			else pos++;
		}
		if (pos >= length)
			throw new IllegalArgumentException("Unterminated string starting at " + start);
		String raw = input.substring(start, pos);
		pos++; // consume closing quote
		// keep quotes around text in token text for easier strip later
		return new Token(Type.STRING, quote + raw + quote);
	}

	private Token readNumber() {
		int start = pos;
		boolean dotSeen = false;
		while (pos < length) {
			char c = input.charAt(pos);
			if (Character.isDigit(c)) pos++;
			else if (c == '.' && !dotSeen) {
				dotSeen = true;
				pos++;
			} else break;
		}
		return new Token(Type.NUMBER, input.substring(start, pos));
	}

	private Token readIdentOrKeyword() {
		int start = pos;
		while (pos < length) {
			char c = input.charAt(pos);
			if (Character.isLetterOrDigit(c) || c == '_') pos++;
			else break;
		}
		String txt = input.substring(start, pos);

		return switch (txt.toUpperCase()) {
			case "AND" -> new Token(Type.AND, txt, " AND ");
			case "OR" -> new Token(Type.OR, txt, " OR ");
			case "NOT" -> new Token(Type.NOT, txt, " NOT ");
			default -> new Token(Type.IDENTIFIER, txt);
		};
	}
}
