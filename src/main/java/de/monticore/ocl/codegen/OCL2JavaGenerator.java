/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen;

import com.google.common.base.Preconditions;
import de.monticore.expressions.prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.literals.prettyprint.MCCommonLiteralsPrettyPrinter;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.codegen.visitors.CommonExpressionsPrinter;
import de.monticore.ocl.codegen.visitors.OCLExpressionsPrinter;
import de.monticore.ocl.codegen.visitors.OCLPrinter;
import de.monticore.ocl.codegen.visitors.SetExpressionsPrinter;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.types.check.OCLSynthesizer;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.prettyprint.MCBasicsPrettyPrinter;
import de.monticore.types.prettyprint.MCBasicTypesPrettyPrinter;
import de.monticore.types.prettyprint.MCCollectionTypesPrettyPrinter;
import de.monticore.types.prettyprint.MCSimpleGenericTypesPrettyPrinter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class OCL2JavaGenerator {

  public static void generate(ASTOCLCompilationUnit ast, String outputFile) throws IOException {
    Preconditions.checkNotNull(ast);
    Preconditions.checkNotNull(ast.getEnclosingScope());
    Preconditions.checkNotNull(outputFile);
    Preconditions.checkArgument(!outputFile.isEmpty());
    File output = Paths.get(outputFile).toFile();
    output.getParentFile().mkdirs();

    FileOutputStream fos = new FileOutputStream(output, false);
    fos.write(generate(ast).getBytes());
    fos.close();
  }

  public static String generate(ASTOCLCompilationUnit ast) {
    Preconditions.checkNotNull(ast);
    return generate(ast, new IndentPrinter());
  }

  protected static String generate(ASTOCLCompilationUnit ast, IndentPrinter printer) {
    Preconditions.checkNotNull(ast);
    Preconditions.checkNotNull(printer);

    printer.println("/* (c) https://github.com/MontiCore/monticore */");
    ast.accept(new OCL2JavaGenerator(printer).getTraverser());
    return printer.getContent();
  }

  protected OCLTraverser traverser;

  protected IndentPrinter printer;

  protected OCLTraverser getTraverser() {
    return this.traverser;
  }

  protected OCL2JavaGenerator(IndentPrinter printer) {
    this(printer, new VariableNaming());
  }

  protected OCL2JavaGenerator(IndentPrinter printer, VariableNaming naming) {
    this(printer, naming, new OCLDeriver(), new OCLSynthesizer());
  }

  protected OCL2JavaGenerator(IndentPrinter printer, VariableNaming naming,
      OCLDeriver oclDeriver, OCLSynthesizer oclSynthesizer) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);
    Preconditions.checkNotNull(oclDeriver);
    Preconditions.checkNotNull(oclSynthesizer);

    this.traverser = OCLMill.traverser();

    // Expressions
    CommonExpressionsPrinter comExprPrinter = new CommonExpressionsPrinter(printer, naming,
        oclDeriver, oclSynthesizer);
    this.traverser.setCommonExpressionsHandler(comExprPrinter);
    this.traverser.add4CommonExpressions(comExprPrinter);
    ExpressionsBasisPrettyPrinter exprBasPrinter = new ExpressionsBasisPrettyPrinter(printer);
    this.traverser.setExpressionsBasisHandler(exprBasPrinter);
    this.traverser.add4ExpressionsBasis(exprBasPrinter);
    OCLExpressionsPrinter oclExprPrinter = new OCLExpressionsPrinter(printer, naming,
        oclDeriver, oclSynthesizer);
    this.traverser.setOCLExpressionsHandler(oclExprPrinter);
    this.traverser.add4OCLExpressions(oclExprPrinter);
    SetExpressionsPrinter setExprPrinter = new SetExpressionsPrinter(printer, naming, oclDeriver,
        oclSynthesizer);
    this.traverser.setSetExpressionsHandler(setExprPrinter);
    this.traverser.add4SetExpressions(setExprPrinter);

    // Types
    MCSimpleGenericTypesPrettyPrinter simpleGenericTypes = new MCSimpleGenericTypesPrettyPrinter(
        printer);
    traverser.setMCSimpleGenericTypesHandler(simpleGenericTypes);
    traverser.add4MCSimpleGenericTypes(simpleGenericTypes);
    MCCollectionTypesPrettyPrinter collectionTypes = new MCCollectionTypesPrettyPrinter(printer);
    traverser.setMCCollectionTypesHandler(collectionTypes);
    traverser.add4MCCollectionTypes(collectionTypes);
    MCBasicTypesPrettyPrinter basicTypes = new MCBasicTypesPrettyPrinter(printer);
    traverser.setMCBasicTypesHandler(basicTypes);
    traverser.add4MCBasicTypes(basicTypes);
    MCBasicsPrettyPrinter basics = new MCBasicsPrettyPrinter(printer);
    traverser.add4MCBasics(basics);

    MCCommonLiteralsPrettyPrinter comLitPrinter = new MCCommonLiteralsPrettyPrinter(printer);
    this.traverser.setMCCommonLiteralsHandler(comLitPrinter);
    this.traverser.add4MCCommonLiterals(comLitPrinter);

    // OCL
    OCLPrinter oclPrinter = new OCLPrinter(printer, naming, oclDeriver, oclSynthesizer);
    this.traverser.setOCLHandler(oclPrinter);
    this.traverser.add4OCL(oclPrinter);
  }
}
