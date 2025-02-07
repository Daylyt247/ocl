/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types.check.SymTypePrimitive;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

public abstract class AbstractPrinter {

  protected static final String NO_TYPE_DERIVED_ERROR =
      "0xAB846 Could not calculate type of expression";

  protected static final String INNER_TYPE_NOT_DERIVED_ERROR =
      "0xFC921 could not derive inner type (container expected)";

  protected static final String UNEXPECTED_STATE_AST_NODE =
      "0xFC922 ASTNode has an unexpected state";

  protected VariableNaming naming;

  protected VariableNaming getNaming() {
    return this.naming;
  }

  protected IDerive deriver;

  protected IDerive getDeriver() {
    return this.deriver;
  }

  protected ISynthesize syntheziser;

  protected ISynthesize getSynthesizer() {
    return this.syntheziser;
  }

  protected IndentPrinter printer;

  protected IndentPrinter getPrinter() {
    return this.printer;
  }

  // common functions

  /**
   * boxes the type e.g. {@code List<int>} to {@code java.util.List<Integer>}
   *
   * @param type type to be printed
   * @return String of type, boxed
   */
  protected static String boxType(TypeCheckResult type) {
    if (!type.isPresentResult()) {
      Log.error(NO_TYPE_DERIVED_ERROR);
    }
    if (type.getResult().isGenericType()) {
      return SymTypeOfGenerics.box((SymTypeOfGenerics) type.getResult());
    } else {
      return SymTypePrimitive.box(type.getResult().printFullName());
    }
  }

  /**
   * prints an expression which returns the result of a Java code block, which is opened by this
   * s.a. {@link AbstractPrinter#printExpressionEndLambda()}
   *
   * @param type the type of the expression
   */
  protected void printExpressionBeginLambda(TypeCheckResult type) {
    this.getPrinter().print("((java.util.function.Supplier<");
    this.getPrinter().print(boxType(type));
    this.getPrinter().println(">)()->{");
    this.getPrinter().indent();
  }

  /**
   * prints the end of the expression which returns the result of a Java code block s.a. {@link
   * AbstractPrinter#printExpressionBeginLambda}
   */
  protected void printExpressionEndLambda() {
    this.getPrinter().unindent();
    this.getPrinter().print("}).get()");
  }
}
