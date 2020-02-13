package com.project.Weeders;

import com.project.AST.ASTHead;
import com.project.AST.structure.CharacterLiteralHolder;
import com.project.AST.structure.IntegerLiteralHolder;
import com.project.AST.structure.StringLiteralHolder;

import java.util.ArrayList;
import java.util.HashMap;

import static com.project.AST.structure.IntegerLiteralHolder.ParentType.UNARY;

public class LiteralWeeder {

    private static final HashMap<String, String> escapes = new HashMap<>();

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

    public static void weed(final ASTHead ast) {
        final ArrayList<IntegerLiteralHolder> integerLiterals = ast.getIntegerLiterals();

        for (final IntegerLiteralHolder holder : integerLiterals) {
            if (holder.value > Integer.MAX_VALUE
                    && !(holder.parentType == UNARY && -holder.value == Integer.MIN_VALUE)) {
                System.err.println("Encountered INTEGER_LITERAL overflow: " + holder.value);
                System.exit(42);
            }
        }

        final ArrayList<CharacterLiteralHolder> characterLiterals = ast.getCharacterLiterals();

        for (final CharacterLiteralHolder characterLiteral : characterLiterals) {
            final String lexeme = characterLiteral.value;

            // Trims the quotes off.
            final String literal = lexeme.substring(1, lexeme.length() - 1);

            if (literal.charAt(0) != '\\' && literal.length() != 1) {
                System.err.println("Encountered too many character in CHARACTER_LITERAL: " + literal);
                System.exit(42);
            } else if (escapes.containsKey(literal)) {
                characterLiteral.setNodeLexeme(escapes.get(literal));
            } else if (isEscapedOctal(literal)) {
                characterLiteral.setNodeLexeme(Integer.toString(getOctalValue(literal)));
            } else if (literal.charAt(0) == '\\') {
                System.err.println("Encountered invalid character literal: " + literal);
                System.exit(42);
            }
        }

        final ArrayList<StringLiteralHolder> stringLiterals = ast.getStringLiterals();

        for (final StringLiteralHolder stringLiteral : stringLiterals) {
            final String lexeme = stringLiteral.value;

            // Trims the quotes off.
            final String literal = lexeme.substring(1, lexeme.length() - 1);
            final StringBuilder escapedString = new StringBuilder();

            for (int i = 0; i < literal.length(); ++i) {

                if (literal.charAt(i) == '\\') {

                    final String escapedChars = findEscapedChars(literal.substring(i).toCharArray());

                    if (escapes.containsKey(escapedChars)) {
                        escapedString.append(escapes.get(escapedChars));
                    } else if (isEscapedOctal(escapedChars)) {
                        escapedString.append(getOctalValue(escapedChars));
                    } else {
                        System.err.println("Encountered invalid STRING_LITERAL: " + lexeme);
                        System.exit(42);
                    }

                    i += escapedChars.length() - 1;
                } else {
                    escapedString.append(literal.charAt(i));
                }
            }

            stringLiteral.setNodeLexeme(escapedString.toString());
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

    private static boolean isOctal(final char c) {
        return c >= '0' && c <= '7';
    }

    private static boolean isZeroToThree(final char c) {
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

    private static String findEscapedChars(final char[] literal) {
        final StringBuilder escapedChars = new StringBuilder("\\");
        final int scanLength = Integer.min(literal.length, 4);

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
