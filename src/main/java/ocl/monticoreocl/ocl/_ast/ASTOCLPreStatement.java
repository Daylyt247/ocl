package ocl.monticoreocl.ocl._ast;

import java.util.Optional;

import static ocl.monticoreocl.oclexpressions._ast.ASTOCLQualifiedPrimary.name2String;
import static ocl.monticoreocl.oclexpressions._ast.ASTOCLQualifiedPrimary.name2StringOpt;

public class ASTOCLPreStatement extends ASTOCLPreStatementTOP {

  public ASTOCLPreStatement() {
    super();
  }

  protected  ASTOCLPreStatement (/* generated by template ast.ConstructorParametersDeclaration*/
      Optional<ocl.monticoreocl.oclexpressions._ast.ASTName2> name2
      ,
      java.util.List<de.monticore.expressionsbasis._ast.ASTExpression> statementss

  ) {
    super(name2, statementss);
  }

  public Optional<String> getNameOpt() {
    return name2StringOpt(getName2Opt());
  }

  public String getName() {
    return name2String(getName2());
  }

  public boolean isPresentName() {
    return isPresentName2();
  }
}
