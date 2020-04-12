package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.Scope;
import com.project.environments.structure.Type;
import com.project.scanner.structure.Kind;

import java.util.ArrayList;
import java.util.HashMap;

public class BaseExpression extends Expression {
    final Expression LHS;
    final Expression singular;
    final Expression RHS;

    final String symbol;

    HashMap<String, String> instructionMap = new HashMap<>();

    private static long labelCounter = 0;

    public BaseExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        instructionMap.put("LESS", "jl ");
        instructionMap.put("LESSEQUAL", "jle ");
        instructionMap.put("GREATER", "jg ");
        instructionMap.put("GREATEREQUAL", "jge ");
        instructionMap.put("EQUALEQUAL", "je ");
        instructionMap.put("NOTEQUAL", "jne ");
        instructionMap.put("UPARROW", null);
        instructionMap.put("BAR", null);
        instructionMap.put("AND", null);
        instructionMap.put("AMPERSAND", null);
        instructionMap.put("INSTANCEOF", null);


        if (head.getChildren().size() == 3
                && head.getChild(0).getKind() == Kind.PAREN_CLOSE
                && head.getChild(2).getKind() == Kind.PAREN_OPEN) {
            LHS = null;
            singular = generateExpressionScope(head.getChild(1), this);
            RHS = null;
            symbol = null;
        } else if (head.getChildren().size() == 2) {
            LHS = null;
            singular = generateExpressionScope(head.getChild(0), this);
            RHS = null;
            symbol = null;
        } else {
            LHS = generateExpressionScope(head.getChild(2), this);
            singular = null;
            RHS = generateExpressionScope(head.getChild(0), this);
            symbol = head.getChild(1).getKind().toString();
        }
    }

    @Override
    public Kind evaluatesTo() {
        return booleanOrKind(Kind.BOOLEAN);
    }

    @Override
    public boolean isVariableNameUsed(String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(ClassScope rootClass) {
        if (LHS != null) {
            LHS.linkTypesToQualifiedNames(rootClass);
            RHS.linkTypesToQualifiedNames(rootClass);

            switch (this.ast.getLexeme()) {
                case "RELATIONALEXPRESSION":
                case "EQUALITYEXPRESSION":
                case "ANDEXPRESSION":
                case "EXCLUSIVEOREXPRESSION":
                case "CONDITIONALEXPRESSION":
                case "CONDITIONALOREXPRESSION":
                case "INCLUSIVEOREXPRESSION":
                case "CONDITIONALANDEXPRESSION":
                    this.type = new Type(Type.PRIM_TYPE.BOOLEAN);
                    break;
                case "MULTIPLICATIVEEXPRESSION":
                    this.type = new Type(Type.PRIM_TYPE.INT);
                    break;
                case "SUBBASEEXPRESSION":
                    this.type = RHS.type;
                    break;
                default:
                    System.err.println("Could not type Base Expr!");
                    System.exit(42);
            }
        } else {
            singular.linkTypesToQualifiedNames(rootClass);
            this.type = singular.type;
        }

        if (this.type == null) {
            System.err.println("Could not type Base Expr after!");
            System.exit(42);
        }
    }

    @Override
    public void checkTypeSoundness() {
        if (LHS != null) {
            LHS.checkTypeSoundness();
            RHS.checkTypeSoundness();

            if (this.ast.getChild(1).getLexeme().equals("COMMA")) {
                System.err.println("Found comma in BaseExpression.");
                System.exit(42);
            }

            if (LHS.type.equals(RHS.type)) return;

            if (RHS.type.equals(Type.generateObjectType()) && LHS.type.isArray) {

            } else if ((LHS.type.isReferenceType() && RHS.type.isNullType())
                    || (RHS.type.isReferenceType() && LHS.type.isNullType())) {

            } else if (LHS.type.isReferenceType() && RHS.type.isReferenceType()) {
                final ClassScope parentClass = this.getParentClass();
                final ClassScope LHSClass = parentClass.classMap.get(LHS.type.name.getDefaultlessQualifiedName());
                final ClassScope RHSClass = parentClass.classMap.get(RHS.type.name.getDefaultlessQualifiedName());

                if (!RHSClass.isSubClassOf(LHSClass) && !LHSClass.isSubClassOf(RHSClass)) {
                    System.err.println("Unsound type: Base Expression, differing types");
                    System.exit(42);
                }
            } else if (LHS.type.isNumericType() && RHS.type.isNumericType()) {

            } else {
                System.err.println("Unsound type: Base Expression, differing types");
                System.exit(42);
            }
        }
    }

    @Override
    public ArrayList<String> generatei386Code() {
        labelCounter += 1;

        final long count = labelCounter;

        final ArrayList<String> code = new ArrayList<>();

        // If there is only one argument, just generate the code.
        if (singular != null) {
            code.addAll(singular.generatei386Code());
            return code;
        }

        // Evaluate the LHS.
        code.addAll(LHS.generatei386Code());

        // Store the LHS.
        code.add("push eax");

        // Evaluate the RHS.
        code.addAll(RHS.generatei386Code());

        // Pop the RHS.
        code.add("pop ebx");

        // Compare the LHS and RHS.
        code.add("cmp eax, ebx");

        if (symbol.equals("INSTANCEOF")) {

            // Move both of the object pointers to the vtable.
            code.add("mov dword eax, [eax]");
            code.add("mov dword ebx, [ebx]");

            // Compare the v-table by value. Should point to the same vtables.
            code.add("cmp eax, ebx");

            // Treat this as an equality expression otherwise.
            code.add("je " + callMidLabel(count));
            code.add("mov eax, 0");
            code.add("jmp " + callEndLabel(count));
            code.add(setMidLabel(count));
            code.add("mov eax, 1");
            code.add(setEndLabel(count));

            return code;
        }

        switch (this.ast.getLexeme()) {
            case "RELATIONALEXPRESSION":
            case "EQUALITYEXPRESSION":
                if (!instructionMap.containsKey(symbol) || instructionMap.get(symbol) == null) {
                    System.err.println("Could not id " + symbol + " in instruction map.");
                    System.exit(42);
                }
                code.add(instructionMap.get(symbol) + callMidLabel(count));
                code.add("mov eax, 0");
                code.add("jmp " + callEndLabel(count));
                code.add(setMidLabel(count));
                code.add("mov eax, 1");
                code.add(setEndLabel(count));
                break;
            case "EXCLUSIVEOREXPRESSION":
                code.add("je " + callMidLabel(count));
                code.add("mov eax, 1");
                code.add("jmp " + callEndLabel(count));
                code.add(setMidLabel(count));
                code.add("mov eax, 0");
                code.add(setEndLabel(count));
                break;
            case "CONDITIONALEXPRESSION":
            case "CONDITIONALOREXPRESSION":
            case "INCLUSIVEOREXPRESSION":
                code.add("je " + callMidLabel(count));
                code.add("mov eax, 1");
                code.add("jmp " + callEndLabel(count));
                code.add(setMidLabel(count));
                code.add("mov eax, ebx");
                code.add(setEndLabel(count));
                break;
            case "CONDITIONALANDEXPRESSION":
            case "ANDEXPRESSION":
                code.add("je " + callMidLabel(count));
                code.add("mov eax, 0");
                code.add("jmp " + callEndLabel(count));
                code.add(setMidLabel(count));
                code.add("mov eax, ebx");
                code.add(setEndLabel(count));
                break;
            case "MULTIPLICATIVEEXPRESSION":
                code.add("mul ebx");
                break;
            case "SUBBASEEXPRESSION":
                if (symbol.equals("PLUS")) {
                    code.add("add eax, ebx");
                } else if (symbol.equals("MINUS")) {
                    code.add("sub eax, ebx");
                } else {
                    System.err.println("Un-implemented sub base expr.");
                    System.exit(42);
                }
                break;
            default:
                System.err.println("Could not write Base Expr!");
                System.exit(42);
        }

        return code;
    }

    private String setMidLabel(final long count) {
        return "bexpr_mid_" + count + ":";
    }

    private String callMidLabel(final long count) {
        return "bexpr_mid_" + count;
    }

    private String setEndLabel(final long count) {
        return "bexpr_end_" + count + ":";
    }

    private String callEndLabel(final long count) {
        return "bexpr_end_" + count;
    }
}




