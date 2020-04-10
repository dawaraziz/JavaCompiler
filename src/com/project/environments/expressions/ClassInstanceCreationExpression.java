package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
import com.project.environments.scopes.ConstructorScope;
import com.project.environments.scopes.Scope;
import com.project.scanner.structure.Kind;

public class ClassInstanceCreationExpression extends Expression {
    final Expression classType;
    final Expression argList;

    public ClassInstanceCreationExpression(final ASTHead head, final Scope parentScope) {
        this.ast = head;
        this.parentScope = parentScope;
        this.name = null;

        if (head.getChildren().size() == 5) {
            argList = generateExpressionScope(head.getChild(1), this);
            classType = generateExpressionScope(head.getChild(3), this);
        } else if (head.getChildren().size() == 4) {
            argList = null;
            classType = generateExpressionScope(head.getChild(2), this);
        } else {
            argList = generateExpressionScope(head.generateClassInstanceSubHead(), this);
            classType = generateExpressionScope(head.getChild(head.getChildren().size() - 2), this);
        }
    }

    @Override
    public boolean isVariableNameUsed(final String variableName) {
        return false;
    }

    @Override
    public void linkTypesToQualifiedNames(final ClassScope rootClass) {
        this.classType.linkTypesToQualifiedNames(rootClass);
        if (argList != null) this.argList.linkTypesToQualifiedNames(rootClass);

        type = classType.type;
    }

    @Override
    public void checkTypeSoundness() {
        if (argList != null) this.argList.checkTypeSoundness();
        this.classType.checkTypeSoundness();
        //TODO: Check that parameters match a constructor
    }

    @Override
    // I guess a new Object() is an expressionName? Doesn't really matter, anything but Bool
    public Kind evaluatesTo() {
        return booleanOrKind(Kind.EXPRESSIONNAME);
    }


    @Override
    public String code() {
        StringBuilder assembly = new StringBuilder();
        ClassScope parentClass = ((ClassScope) ((NameExpression) classType).namePointer);
        assembly.append("mov eax, 4"); // sizeof class ?
        assembly.append("\n");
        assembly.append("call __malloc");
        assembly.append("\n");
        assembly.append("mov [eax], " + parentClass.callVtableLabel());
        assembly.append("\n");
        assembly.append("push eax");
        assembly.append('\n');
        assembly.append(pushArguments(argList));
        assembly.append('\n');
        assembly.append("call " + parentClass.getConstructorLabel(argList));
        assembly.append('\n');
        assembly.append(popArguments(argList));
        assembly.append('\n');
        assembly.append("pop eax");
        assembly.append('\n');

        return assembly.toString();
    }

    public String pushArguments(Expression arguments) {
        String label = "";

        if (arguments instanceof NameExpression) {

            label = "push " + arguments.code() + ";\n";

        } else if (arguments instanceof ArgumentListExpression) {

            for (Expression arg : ((ArgumentListExpression) arguments).arguments) {
                label += "push " + arg.code() + ";\n";
            }

        } else {
            System.out.println("Why is it anything else");
            System.exit(42);
        }


        return label;
    }

    public String popArguments(Expression arguments) {
        String label = "";

        if (arguments instanceof NameExpression) {

            label = "pop " + arguments.code() + ";\n";

        } else if (arguments instanceof ArgumentListExpression) {

            for (Expression arg : ((ArgumentListExpression) arguments).arguments) {
                label += "pop " + arguments.code() + ";\n";
            }

        } else {
            System.out.println("Why is it anything else");
            System.exit(42);
        }


        return label;
    }

}
