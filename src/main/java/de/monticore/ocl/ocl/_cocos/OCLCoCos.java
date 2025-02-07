// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.oclexpressions._cocos.IterateExpressionVariableUsageIsCorrect;
import de.monticore.ocl.setexpressions._cocos.SetComprehensionHasGenerator;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.types.check.OCLSynthesizer;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;

public class OCLCoCos {

  public static OCLCoCoChecker createChecker() {
    return createChecker(new OCLDeriver(), new OCLSynthesizer());
  }

  public static OCLCoCoChecker createChecker(ISynthesize synthesizer) {
    return createChecker(new OCLDeriver(), synthesizer);
  }

  public static OCLCoCoChecker createChecker(IDerive deriver) {
    return createChecker(deriver, new OCLSynthesizer());
  }

  public static OCLCoCoChecker createChecker(IDerive deriver, ISynthesize synthesizer) {
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new MethSignatureStartsWithLowerCaseLetter());
    checker.addCoCo(new ConstructorNameStartsWithCapitalLetter());
    checker.addCoCo(new InvariantNameStartsWithCapitalLetter());
    checker.addCoCo(new ParameterNamesUnique());
    checker.addCoCo(new IterateExpressionVariableUsageIsCorrect());
    checker.addCoCo(new ConstructorNameReferencesType());
    checker.addCoCo(new ExpressionValidCoCo(deriver));
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ContextVariableNamesAreUnique());
    checker.addCoCo(new ContextHasOnlyOneType());
    checker.addCoCo(new SetComprehensionHasGenerator());
    checker.addCoCo(new UnnamedInvariantDoesNotHaveParameters());
    checker.addCoCo(new PreAndPostConditionsAreBooleanType(deriver));
    checker.addCoCo(new VariableDeclarationOfCorrectType(deriver, synthesizer));
    return checker;
  }
}
