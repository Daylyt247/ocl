// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.oclexpressions._symboltable;

import de.monticore.ocl.oclexpressions._ast.ASTInDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTInDeclarationVariable;
import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.oclexpressions._ast.ASTTypeIfExpression;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.types.check.OCLSynthesizer;
import de.monticore.ocl.types.check.OCLTypeCheck;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.basicsymbols._visitor.BasicSymbolsVisitor2;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.TypeCheckResult;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.se_rwth.commons.logging.Log;
import java.util.List;

public class OCLExpressionsSymbolTableCompleter
    implements BasicSymbolsVisitor2, OCLExpressionsVisitor2, OCLExpressionsHandler {
  protected static final String USED_BUT_UNDEFINED = "0xB0028: Type '%s' is used but not defined.";

  protected static final String DEFINED_MUTLIPLE_TIMES =
      "0xB0031: Type '%s' is defined more than once.";

  protected final List<ASTMCImportStatement> imports;

  protected final String packageDeclaration;

  protected OCLExpressionsTraverser traverser;

  protected IDerive deriver;

  protected ISynthesize synthesizer;

  public void setDeriver(IDerive deriver) {
    if (deriver != null) {
      this.deriver = deriver;
    } else {
      Log.error("0xA3201 The deriver has to be set");
    }
  }

  public void setSynthesizer(ISynthesize synthesizer) {
    if (synthesizer != null) {
      this.synthesizer = synthesizer;
    } else {
      Log.error("0xA3204 The synthesizer has to be set");
    }
  }

  public OCLExpressionsSymbolTableCompleter(
      List<ASTMCImportStatement> imports, String packageDeclaration) {
    this.imports = imports;
    this.packageDeclaration = packageDeclaration;
    deriver = new OCLDeriver();
    synthesizer = new OCLSynthesizer();
  }

  @Override
  public OCLExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void traverse(IOCLExpressionsScope node) {
    OCLExpressionsHandler.super.traverse(node);
    for (IOCLExpressionsScope subscope : node.getSubScopes()) {
      subscope.accept(this.getTraverser());
    }
  }

  @Override
  public void visit(ASTTypeIfExpression node) {
    // get the shadowing variable that has been added in OCLExpressionsScopesGenitor
    VariableSymbol shadowingSymbol =
        node.getThenExpression().getSpannedScope().getLocalVariableSymbols().get(0);
    TypeCheckResult shadowingSymbolType = synthesizer.synthesizeType(node.getMCType());
    if (!shadowingSymbolType.isPresentResult()) {
      Log.error(
          String.format(
              "The type (%s) of the object (%s) could not be calculated",
              node.getMCType(), node.getName()));
    } else {
      shadowingSymbol.setType(shadowingSymbolType.getResult());
      shadowingSymbol.setIsReadOnly(true);
    }
  }

  @Override
  public void visit(ASTInDeclaration ast) {
    for (ASTInDeclarationVariable node : ast.getInDeclarationVariableList()) {
      VariableSymbol symbol = node.getSymbol();
      symbol.setIsReadOnly(false);
      TypeCheckResult typeResult = new TypeCheckResult();
      typeResult.setResultAbsent();
      if (ast.isPresentMCType()) {
        typeResult = synthesizer.synthesizeType(ast.getMCType());
        if (!typeResult.isPresentResult()) {
          Log.error(
              String.format(
                  "The type (%s) of the object (%s) could not be calculated",
                  ast.getMCType(), symbol.getName()));
        } else {
          symbol.setType(typeResult.getResult());
        }
      }
      if (ast.isPresentExpression()) {
        TypeCheckResult tcr_expr = deriver.deriveType(ast.getExpression());
        if (tcr_expr.isPresentResult()) {
          // if MCType present: check that type of expression and MCType are compatible
          if (typeResult.isPresentResult()
              && !OCLTypeCheck.compatible(
                  typeResult.getResult(), OCLTypeCheck.unwrapSet(tcr_expr.getResult()))) {
            Log.error(
                String.format(
                    "The MCType (%s) and the expression type (%s) in Symbol (%s) are not compatible",
                    ast.getMCType(),
                    OCLTypeCheck.unwrapSet(tcr_expr.getResult()),
                    symbol.getName()));
          }
          // if no MCType present: symbol has type of expression
          if (!typeResult.isPresentResult()) {
            symbol.setType(OCLTypeCheck.unwrapSet(tcr_expr.getResult()));
          }
        } else {
          Log.error(
              String.format(
                  "The type of the object (%s) could not be calculated", symbol.getName()));
        }
      }
      // node has neither MCType nor expression
      if (!typeResult.isPresentResult() && !ast.isPresentExpression()) {
        symbol.setType(
            SymTypeExpressionFactory.createTypeObject("Object", ast.getEnclosingScope()));
      }
    }
  }

  @Override
  public void visit(ASTOCLVariableDeclaration ast) {
    VariableSymbol symbol = ast.getSymbol();
    symbol.setIsReadOnly(false);
    if (ast.isPresentMCType()) {
      ast.getMCType().setEnclosingScope(symbol.getEnclosingScope());
      ast.getMCType().accept(getTraverser());
      final TypeCheckResult typeResult = synthesizer.synthesizeType(ast.getMCType());
      if (!typeResult.isPresentResult()) {
        Log.error(
            String.format(
                "The type (%s) of the object (%s) could not be calculated",
                ast.getMCType(), ast.getName()));
      } else {
        symbol.setType(typeResult.getResult());
      }
    } else {
      if (ast.isPresentExpression()) {
        ast.getExpression().accept(getTraverser());
        TypeCheckResult tcr_expr = deriver.deriveType(ast.getExpression());
        if (tcr_expr.isPresentResult()) {
          symbol.setType(tcr_expr.getResult());
        } else {
          Log.error(
              String.format("The type of the object (%s) could not be calculated", ast.getName()));
        }
      } else {
        symbol.setType(
            SymTypeExpressionFactory.createTypeObject("Object", ast.getEnclosingScope()));
      }
    }
  }
}
