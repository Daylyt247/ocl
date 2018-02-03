/**
 * ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package ocl.monticoreocl.ocl._visitors;

import de.monticore.commonexpressions._ast.*;
import de.monticore.expressionsbasis._ast.ASTExpression;
import de.monticore.oclexpressions._ast.ASTOCLQualifiedPrimary;
import de.monticore.oclexpressions._ast.ASTParenthizedExpression;
import de.monticore.symboltable.MutableScope;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._ast.*;
import ocl.monticoreocl.ocl._visitor.OCLVisitor;


public class OCLTypeCheckingVisitor implements OCLVisitor {

    private boolean isTypeCorrect;
    private OCLVisitor realThis = this;
    private MutableScope scope;

    public OCLTypeCheckingVisitor(MutableScope scope) {
        this.isTypeCorrect = true;
        this.scope = scope;
    }

    public boolean isTypeCorrect() {
        return isTypeCorrect;
    }

    public static void checkInvariants(ASTOCLInvariant node, MutableScope scope) {
        OCLTypeCheckingVisitor checkingVisitor = new OCLTypeCheckingVisitor(scope);

        for(ASTExpression expr : node.getStatements()){
            expr.accept(checkingVisitor);
            if(!checkingVisitor.isTypeCorrect()) {
                Log.warn("0xOCLT0 Could not infer type from this expression:" + expr.get_SourcePositionStart());
            }
        }
    }

    @Override
    public void visit(ASTParenthizedExpression node){
        OCLExpressionTypeInferingVisitor.getTypeFromExpression(node, scope);
    }

    @Override
    public void visit(ASTOCLQualifiedPrimary node){
        OCLExpressionTypeInferingVisitor.getTypeFromExpression(node, scope);
    }



    /**
     *  ********** math expressions **********
     */

    public void checkInfixExpr(ASTInfixExpression node){
        CDTypeSymbolReference leftType = OCLExpressionTypeInferingVisitor.getTypeFromExpression(node.getLeftExpression(), scope);
        CDTypeSymbolReference rightType = OCLExpressionTypeInferingVisitor.getTypeFromExpression(node.getRightExpression(), scope);

        if (!leftType.getReferencedSymbol().isSameOrSuperType(rightType.getReferencedSymbol())) {
            if (!rightType.getReferencedSymbol().isSameOrSuperType(leftType.getReferencedSymbol())) {
                Log.error("0xCET01 left and right type of infix expression do not match: " + node.get_SourcePositionStart());
            }
        }
    }

    public void checkPrefixExpr(ASTExpression node){
        CDTypeSymbolReference exprType = OCLExpressionTypeInferingVisitor.getTypeFromExpression(node, scope);

        if (!exprType.getName().equals("Boolean")) {
            Log.error("0xCET02 type of prefix expression is not boolean: " + node.get_SourcePositionStart());
        }
    }

    @Override
    public void visit(ASTModuloExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTDivideExpression node) {
        // Todo Amount or Number
    }

    @Override
    public void visit(ASTMultExpression node) {
        // Todo Amount or Number
    }

    @Override
    public void visit(ASTPlusExpression node){
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTMinusExpression node){
        checkInfixExpr(node);
    }

    /**
     *  ********** boolean expressions **********
     */

    @Override
    public void visit(ASTEqualsExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTBooleanNotExpression node) {
        checkPrefixExpr(node.getExpression());
    }

    @Override
    public void visit(ASTLogicalNotExpression node) {
        checkPrefixExpr(node.getExpression());
    }

    @Override
    public void visit(ASTEquivalentExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTLessEqualExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTGreaterEqualExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTLessThanExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTGreaterThanExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTNotEqualsExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTBooleanAndOpExpression node) {
        checkInfixExpr(node);
    }

    @Override
    public void visit(ASTBooleanOrOpExpression node) {
        checkInfixExpr(node);
    }
}
