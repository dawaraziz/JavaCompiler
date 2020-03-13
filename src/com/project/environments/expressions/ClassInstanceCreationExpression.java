package com.project.environments.expressions;

import com.project.environments.ast.ASTHead;
import com.project.environments.scopes.ClassScope;
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
    public Kind evaluatesTo(){
        return booleanOrKind(Kind.EXPRESSIONNAME);
    }

}
