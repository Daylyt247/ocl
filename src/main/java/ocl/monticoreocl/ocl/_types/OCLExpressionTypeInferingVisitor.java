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
package ocl.monticoreocl.ocl._types;

import de.monticore.ast.ASTNode;


import de.monticore.commonexpressions._ast.*;
import de.monticore.expressionsbasis._ast.ASTExpression;
import de.monticore.literals.literals._ast.*;
import de.monticore.numberunit._ast.ASTNumberWithUnit;
import de.monticore.numberunit.prettyprint.NumberUnitPrettyPrinter;
import de.monticore.numberunit.prettyprint.UnitsPrinter;
import de.monticore.oclexpressions._ast.*;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.types.TypesPrinter;
import de.monticore.umlcd4a.symboltable.*;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._symboltable.OCLVariableDeclarationSymbol;
import ocl.monticoreocl.ocl._visitor.OCLVisitor;

import javax.measure.unit.Unit;
import java.util.*;


/**
 * This visitor tries to infer the return type of an ocl expression
 */
public class OCLExpressionTypeInferingVisitor implements OCLVisitor {

    private CDTypeSymbolReference returnTypeRef;
    private OCLVisitor realThis = this;
    private MutableScope scope;

    public OCLExpressionTypeInferingVisitor(MutableScope scope) {
        this.returnTypeRef = null;
        this.scope = scope;
    }

    public static CDTypeSymbolReference getTypeFromExpression(ASTExpression node, MutableScope scope) {
        OCLExpressionTypeInferingVisitor exprVisitor = new OCLExpressionTypeInferingVisitor(scope);
        node.accept(exprVisitor);
        CDTypeSymbolReference typeReference = exprVisitor.getReturnTypeReference();
        if (typeReference==null) {
            Log.error("0xOCLI0 The variable type could not be resolved from this expression: " + node.get_SourcePositionStart());
            return new CDTypeSymbolReference("Class", exprVisitor.scope);
        } else {
            return typeReference;
        }
    }

    public static CDTypeSymbolReference getTypeFromExpression(ASTOCLComprehensionExpr node, MutableScope scope) {
        OCLExpressionTypeInferingVisitor exprVisitor = new OCLExpressionTypeInferingVisitor(scope);
        node.accept(exprVisitor);
        CDTypeSymbolReference typeReference = exprVisitor.getReturnTypeReference();
        if (typeReference==null) {
            Log.error("0xOCLI0 The variable type could not be resolved from this expression: " + node.get_SourcePositionStart());
            return new CDTypeSymbolReference("Class", exprVisitor.scope);
        } else {
            return typeReference;
        }
    }

    public CDTypeSymbolReference getReturnTypeReference() {
        return returnTypeRef;
    }

    private CDTypeSymbolReference createTypeRef(String typeName, ASTNode node) {
        // map int to Integer , etc.
        typeName = CDTypes.primitiveToWrapper(typeName);
        CDTypeSymbolReference typeReference = new CDTypeSymbolReference(typeName, this.scope);
        typeReference.setStringRepresentation(typeName);
        // Check if type was found in CD loaded CD models
        if (!typeReference.existsReferencedSymbol()) {
            Log.error("0xOCLI9 This type could not be found: " + typeName + " at " + node.get_SourcePositionStart());
        }
        return typeReference;
    }

    /**
     *  ********** traverse methods **********
     */

    @Override
    public void traverse(ASTIntLiteral node) {
        returnTypeRef = createTypeRef("Integer", node);
    }

    @Override
    public void traverse(ASTDoubleLiteral node) {
        returnTypeRef = createTypeRef("Double", node);
    }

    @Override
    public void traverse(ASTFloatLiteral node) {
        returnTypeRef = createTypeRef("Float", node);
    }

    @Override
    public void traverse(ASTNumberWithUnit node) {
        if (node.unIsPresent()) {
            NumberUnitPrettyPrinter printer = new NumberUnitPrettyPrinter(new IndentPrinter());
            printer.prettyprint(node.getUn().get());
            String unitString = printer.getPrinter().getContent();
            returnTypeRef = createTypeRef(UnitsPrinter.unitStringToUnitName(unitString), node);
        }
    }

    @Override
    public void traverse(ASTStringLiteral node) {
        returnTypeRef = createTypeRef("String", node);
    }

    @Override
    public void traverse(ASTCharLiteral node) {
        returnTypeRef = createTypeRef("char", node);
    }

    @Override
    public void traverse(ASTParenthizedExpression node) {
        returnTypeRef = getTypeFromExpression(node.getExpression(), scope);
        if (node.qualificationIsPresent()) {
            node.getQualification().get().accept(realThis);
        }
    }

    @Override
    public void traverse(ASTIfThenElseExpr node) {
        node.getThenExpression().accept(realThis);
    }

    @Override
    public void traverse(ASTConditionalExpression node) {
        node.getTrueExpression().accept(realThis);
    }

    @Override
    public void traverse(ASTOCLQualifiedPrimary node) {
        LinkedList<String> names = new LinkedList<>(node.getNames());

        CDTypeSymbolReference firstType = handlePrefixName(node, names);
        returnTypeRef = handleNames(names, firstType, node);

        if(node.postfixQualificationIsPresent()) {
            node.getPostfixQualification().get().accept(realThis);
        }

        // process following primaries
        if(node.oCLQualifiedPrimaryIsPresent()) {
           node.getOCLQualifiedPrimary().get().accept(realThis);
        }
    }

    @Override
    public void traverse(ASTOCLTransitivQualification node) {
        CDTypeSymbolReference setType = createTypeRef("Set", node);
        TypeInferringHelper.addActualArgument(setType, returnTypeRef);
        setType = TypeInferringHelper.flattenOnce(setType);
        returnTypeRef = setType;
    }

    @Override
    public void traverse(ASTOCLArrayQualification node) {
        List<ActualTypeArgument> arguments = returnTypeRef.getActualTypeArguments();
        if (arguments.size() == 0) {
            Log.error("0xOCLI4 Could not resolve container argument from: " + returnTypeRef + " at " + node.get_SourcePositionStart());
        }
        returnTypeRef = (CDTypeSymbolReference) arguments.get(0).getType();
    }

    @Override
    public void traverse(ASTOCLArgumentQualification node) {}

    @Override
    public void traverse(ASTOCLComprehensionPrimary node) {
        String typeName;
        if (node.typeIsPresent()) {
            typeName = TypesPrinter.printType(node.getType().get());
        } else {
            typeName = "Collection";
        }
        returnTypeRef = createTypeRef(typeName, node);

        if (node.expressionIsPresent()) {
            CDTypeSymbolReference innerType = getTypeFromExpression(node.getExpression().get(), scope);
            TypeInferringHelper.addActualArgument(returnTypeRef, innerType);
        }

        if (node.qualificationIsPresent()) {
            node.getQualification().get().accept(realThis);
        }
    }

    @Override
    public void traverse(ASTOCLComprehensionExpressionStyle node) {
        node.getExpression().accept(realThis);
    }

    @Override
    public void traverse(ASTOCLComprehensionEnumerationStyle node) {
        node.getOCLCollectionItems().get(0).getExpressions().get(0).accept(realThis);
    }

    @Override
    public void traverse(ASTInExpr node) {
        if(node.typeIsPresent()) {
            String typeName = TypesPrinter.printType(node.getType().get());
            returnTypeRef = createTypeRef(typeName, node);
        }
        else if(node.expressionIsPresent()) {
            CDTypeSymbolReference containerType = getTypeFromExpression(node.getExpression().get(), scope);
            if (containerType.getActualTypeArguments().size() == 0) {
                Log.error("0xOCLI5 Could not resolve type from InExpression, " + node.getVarNames() +
                        " in " + containerType + " at " +  node.get_SourcePositionStart());
            }
            returnTypeRef = (CDTypeSymbolReference) containerType.getActualTypeArguments().get(0).getType();
        }
    }

    /**
     *  ********** math expressions **********
     */

    @Override
    public void traverse(ASTPlusExpression node) {
        node.getLeftExpression().accept(realThis);
    }

    @Override
    public void traverse(ASTMinusExpression node) {
        node.getLeftExpression().accept(realThis);
    }

    @Override
    public void traverse(ASTModuloExpression node) {
        node.getLeftExpression().accept(realThis);
    }

    @Override
    public void traverse(ASTDivideExpression node) {
        CDTypeSymbolReference leftType = OCLExpressionTypeInferingVisitor.getTypeFromExpression(node.getLeftExpression(), scope);
        CDTypeSymbolReference rightType = OCLExpressionTypeInferingVisitor.getTypeFromExpression(node.getRightExpression(), scope);
        CDTypeSymbolReference amountType = createTypeRef("Amount", node);

        if (leftType.getName().equals("Number") && rightType.getName().equals("Number")) {
            returnTypeRef = createTypeRef("Number", node);
        } else if(leftType.getName().equals("Amount") || rightType.getName().equals("Amount")) {
            returnTypeRef = createTypeRef("Amount", node);
        } else if(amountType.isSameOrSuperType(leftType) && amountType.isSameOrSuperType(rightType)){
            Unit<?> leftUnit = UnitsPrinter.unitNameToUnit(leftType.getName());
            Unit<?> rightUnit = UnitsPrinter.unitNameToUnit(rightType.getName());
            Unit<?> resultUnit = leftUnit.divide(rightUnit);
            String resultUnitName = UnitsPrinter.unitToUnitName(resultUnit);

            returnTypeRef = createTypeRef(resultUnitName, node);
        }
    }

    @Override
    public void traverse(ASTMultExpression node) {
        CDTypeSymbolReference leftType = OCLExpressionTypeInferingVisitor.getTypeFromExpression(node.getLeftExpression(), scope);
        CDTypeSymbolReference rightType = OCLExpressionTypeInferingVisitor.getTypeFromExpression(node.getRightExpression(), scope);
        CDTypeSymbolReference amountType = createTypeRef("Amount", node);

        if (leftType.getName().equals("Number") && rightType.getName().equals("Number")) {
            returnTypeRef = createTypeRef("Number", node);
        } else if(leftType.getName().equals("Amount") || rightType.getName().equals("Amount")) {
            returnTypeRef = createTypeRef("Amount", node);
        } else if(amountType.isSameOrSuperType(leftType) && amountType.isSameOrSuperType(rightType)){
            Unit<?> leftUnit = UnitsPrinter.unitNameToUnit(leftType.getName());
            Unit<?> rightUnit = UnitsPrinter.unitNameToUnit(rightType.getName());
            Unit<?> resultUnit = leftUnit.times(rightUnit);
            String resultUnitName = UnitsPrinter.unitToUnitName(resultUnit);

            returnTypeRef = createTypeRef(resultUnitName, node);
        }
    }

    /**
     *  ********** boolean expressions **********
     */

    @Override
    public void traverse(ASTEqualsExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }

    @Override
    public void traverse(ASTBooleanNotExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }

    @Override
    public void traverse(ASTLogicalNotExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }

    @Override
    public void traverse(ASTEquivalentExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }

    @Override
    public void traverse(ASTLessEqualExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }

    @Override
    public void traverse(ASTGreaterEqualExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }

    @Override
    public void traverse(ASTLessThanExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }

    @Override
    public void traverse(ASTGreaterThanExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }

    @Override
    public void traverse(ASTNotEqualsExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }

    @Override
    public void traverse(ASTBooleanAndOpExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }

    @Override
    public void traverse(ASTBooleanOrOpExpression node) {
        returnTypeRef = createTypeRef("boolean", node);
    }


    /**
     *  ********** Handle Methods **********
     */

    private CDTypeSymbolReference handlePrefixName(ASTOCLQualifiedPrimary node, LinkedList<String> names) {
        // Try and look if name or this was declared as variable or try as ClassName of CD
        String prefixName = names.get(0);
        Optional<OCLVariableDeclarationSymbol> nameDecl = scope.resolve(prefixName, OCLVariableDeclarationSymbol.KIND);
        Optional<OCLVariableDeclarationSymbol> thisDecl = scope.resolve("this", OCLVariableDeclarationSymbol.KIND);
        Optional<CDTypeSymbol> typeName = scope.resolve(prefixName, CDTypeSymbol.KIND);

        CDTypeSymbolReference typeRef;
        if(returnTypeRef!=null) { //Previous Type present from prefix-qualification
            typeRef = returnTypeRef;
        } else if(nameDecl.isPresent()) { // firstName as defined variable
            names.pop();
            typeRef = nameDecl.get().getType();
        } else if (typeName.isPresent()) { // Class same as Class.allInstances()
            names.pop();
            typeRef = createTypeRef("Set", node);
            CDTypeSymbolReference argsTypeRef = createTypeRef(prefixName, node);
            TypeInferringHelper.addActualArgument(typeRef, argsTypeRef);
        } else if (thisDecl.isPresent()) { // implicit this
            typeRef = thisDecl.get().getType();
        } else {
            Log.error("0xOCLI2 Could not resolve name or type: " + prefixName + " at " + node.get_SourcePositionStart());
            typeRef = new CDTypeSymbolReference("Class", scope);
        }
        return typeRef;
    }

    /**
     * Takes a chain of names and recursivly traces back the return type: Class.field.association.method().
     * E.g. Auction.members.size() -> Set<int>
     * Implicit flattening is used: E.g a type of List<List<Person>>> is also looked at as List<Person>
     */
    private CDTypeSymbolReference handleNames(LinkedList<String> names, CDTypeSymbolReference previousType, ASTNode node) {
        if (names.size() > 0) {
            String name = names.pop();

            // Try name as method/field/assoc
            Scope elementsScope = previousType.getAllKindElements();
            Optional<CDTypeSymbolReference> newType = handleName(node, name, elementsScope);

            //If it failed try implicit flattening
            if (!newType.isPresent()) {
                CDTypeSymbolReference flattendType = TypeInferringHelper.flattenAll(previousType);
                elementsScope = flattendType.getAllKindElements();
                newType = handleName(node, name, elementsScope);
                // If it succeeded add container from previous type around it
                if(newType.isPresent()) {
                    CDTypeSymbolReference containerType = createTypeRef(previousType.getName(), node);
                    TypeInferringHelper.addActualArgument(containerType, newType.get());
                    // implicit flattening with . operator
                    containerType = TypeInferringHelper.flattenOnce(containerType);
                    newType = Optional.of(containerType);
                }
            }

            if(!newType.isPresent()) {
                Log.error("0xOCLI3 Could not resolve field/method/association: " + name + " on " + previousType.getName() + " at " + node.get_SourcePositionStart());
                return createTypeRef("Class", node);
            }

            return handleNames(names, newType.get(), node);
        } else {
            return previousType;
        }
    }

    /**
     * Takes a single name and tries to resolve it as association/field/method on a scope
     */
    private Optional<CDTypeSymbolReference> handleName(ASTNode node, String name, Scope elementsScope) {
        Optional<CDFieldSymbol> fieldSymbol = elementsScope.<CDFieldSymbol>resolve(name, CDFieldSymbol.KIND);
        Collection<CDAssociationSymbol> associationSymbol = elementsScope.<CDAssociationSymbol>resolveMany(name, CDAssociationSymbol.KIND);
        Optional<CDMethodSymbol> methodSymbol = elementsScope.<CDMethodSymbol>resolve(name, CDMethodSymbol.KIND);

        if(fieldSymbol.isPresent()) { // Try name as field
            return Optional.of(createTypeRef(fieldSymbol.get().getType().getName(), node));
        } else if (!associationSymbol.isEmpty()) { // Try name as association
            return Optional.of(handleAssociationSymbol(node, associationSymbol.iterator().next(), name));
        } else if (methodSymbol.isPresent()) { // Try name as method
            return Optional.of(createTypeRef(methodSymbol.get().getReturnType().getName(), node));
        } else {
            return Optional.empty();
        }
    }

    private CDTypeSymbolReference handleAssociationSymbol(ASTNode node, CDAssociationSymbol associationSymbol, String roleName) {
        CDTypeSymbolReference newType;
        CDTypeSymbolReference targetType = (CDTypeSymbolReference) associationSymbol.getTargetType();
        Cardinality cardinality = associationSymbol.getTargetCardinality();
        List<Stereotype> stereotypes = associationSymbol.getStereotypes();
        if(associationSymbol.getSourceRole().isPresent() && associationSymbol.getSourceRole().get().equals(roleName)) {
            targetType = (CDTypeSymbolReference) associationSymbol.getSourceType();
            cardinality = associationSymbol.getSourceCardinality();
        }

        if (cardinality.isMultiple()) {
            if(stereotypes.stream().filter(s -> s.getName().equals("ordered")).count() > 0) {
                newType = createTypeRef("List", node);
            } else {
                newType = createTypeRef("Set", node);
            }
            TypeInferringHelper.addActualArgument(newType, targetType);
        } else if (!cardinality.isDefault()) {
            newType = createTypeRef("Optional", node);
            TypeInferringHelper.addActualArgument(newType, targetType);
        } else {
            newType = targetType;
        }
        return newType;
    }



}
