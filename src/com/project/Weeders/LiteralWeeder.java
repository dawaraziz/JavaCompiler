package com.project.Weeders;

import com.project.ParseTree.ParseTree;
import com.project.scanner.Kind;

import java.util.ArrayList;
import java.util.HashMap;

public class LiteralWeeder {

    static HashMap<String, String> escapes = new HashMap<>();

    static {
        escapes.put("\\b", "\b");
        escapes.put("\\t", "\t");
        escapes.put("\\n", "\n");
        escapes.put("\\f", "\f");
        escapes.put("\\r", "\r");
        escapes.put("\\\"", "\"");
        escapes.put("\\'", "'");
        escapes.put("\\\\", "\\");
    }

    public static void weed(ArrayList<ParseTree> tokenList) {
        Kind previousKind = Kind.VARIABLE_ID;

        for (ParseTree token : tokenList) {
            if (token.getKind() == Kind.INTEGER_LITERAL) {
                try {
                    long literal = Long.parseLong(token.getLexeme());
                    if (literal > Integer.MAX_VALUE &&
                            !(previousKind == Kind.MINUS && -literal == Integer.MIN_VALUE)) {
                        System.err.println("Encountered INTEGER_LITERAL overflow: " + token.getLexeme());
                        System.exit(42);
                    }
                } catch (NumberFormatException x) {
                    System.err.println("Encountered non-integer INTEGER_LITERAL: " + token.getLexeme());
                    System.exit(42);
                }
            } else if (token.getKind() == Kind.CHARACTER_LITERAL) {
                String literal = token.getLexeme().substring(1, token.getLexeme().length() - 1);

                if (literal.charAt(0) != '\\' && literal.length() != 1) {
                    System.err.println("Encountered too many character in CHARACTER_LITERAL: " + token.getLexeme());
                    System.exit(42);
                } else if (escapes.containsKey(literal)) {
                    token.setLexeme(escapes.get(literal));
                } else if (isEscapedOctal(literal)) {
                    token.setLexeme(Integer.toString(getOctalValue(literal)));
                } else if (literal.charAt(0) == '\\') {
                    System.err.println("Encountered invalid character literal: " + token.getLexeme());
                    System.exit(42);
                }

            } else if (token.getKind() == Kind.STRING_LITERAL) {
                String literal = token.getLexeme().substring(1, token.getLexeme().length() - 1);
                StringBuilder escapedString = new StringBuilder();

                for (int i = 0; i < literal.length(); ++i) {

                    if (literal.charAt(i) == '\\') {

                        String escapedChars = findEscapedChars(literal.substring(i).toCharArray());

                        if (escapes.containsKey(escapedChars)) {
                            escapedString.append(escapes.get(escapedChars));
                        } else if (isEscapedOctal(escapedChars)) {
                            escapedString.append(getOctalValue(escapedChars));
                        } else {
                            System.err.println("Encountered invalid STRING_LITERAL: " + token.getLexeme());
                            System.exit(42);
                        }

                        i += escapedChars.length() - 1;
                    } else {
                        escapedString.append(literal.charAt(i));
                    }
                }
                token.setLexeme(escapedString.toString());
            }

            previousKind = token.getKind();
        }
    }

    private static boolean isEscapedOctal(String literal) {
        if (literal.charAt(0) != '\\') return false;
        else literal = literal.substring(1);

        return (literal.length() == 1 && isOctal(literal.charAt(0))) ||
                (literal.length() == 2 && isOctal(literal.charAt(0)) && isOctal(literal.charAt(1))) ||
                (literal.length() == 3 && isZeroToThree(literal.charAt(0))
                        && isOctal(literal.charAt(1)) && isOctal(literal.charAt(2)));
    }

    private static boolean isOctal(char c) {
        return c >= '0' && c <= '7';
    }

    private static boolean isZeroToThree(char c) {
        return c >= '0' && c <= '3';
    }

    private static int getOctalValue(String literal) {
        literal = literal.substring(1);

        if (literal.length() == 1) {
            return Character.getNumericValue(literal.charAt(0));
        } else if (literal.length() == 2) {
            return Character.getNumericValue(literal.charAt(0)) * 8 +
                    Character.getNumericValue(literal.charAt(1));
        } else {
            return Character.getNumericValue(literal.charAt(0)) * 64 +
                    Character.getNumericValue(literal.charAt(1)) * 8 +
                    Character.getNumericValue(literal.charAt(2));
        }
    }

    private static String findEscapedChars(char[] literal) {
        StringBuilder escapedChars = new StringBuilder("\\");
        int scanLength = Integer.min(literal.length, 4);

        int maxLength = Integer.MAX_VALUE;
        for (int i = 1; i < Integer.min(scanLength, maxLength); ++i) {
            if (!(literal[i] > '0' && literal[i] < '8')) {
                if (i == 1) escapedChars.append(literal[i]);
                break;
            } else {
                escapedChars.append(literal[i]);

                if (i == 1 && literal[i] > '3') {
                    maxLength = 3;
                }
            }
        }
        return escapedChars.toString();
    }
}
