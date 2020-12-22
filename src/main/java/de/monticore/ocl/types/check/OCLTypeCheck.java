package de.monticore.ocl.types.check;

import com.google.common.collect.Lists;
import de.monticore.types.check.*;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;

public class OCLTypeCheck extends TypeCheck {

  protected static final List<String> collections = Lists.newArrayList("List", "Set", "Collection");

  public OCLTypeCheck(ISynthesize synthesizeSymType, ITypesCalculator iTypesCalculator) {
    super(synthesizeSymType, iTypesCalculator);
  }

  public OCLTypeCheck(ISynthesize synthesizeSymType) {
    super(synthesizeSymType);
  }

  public OCLTypeCheck(ITypesCalculator iTypesCalculator) {
    super(iTypesCalculator);
  }

  /**
   * Test whether 2 types are compatible by using TypeCheck class
   * and extending it by checking whether FullQualifiedNames are different.
   */
  public static boolean compatible(SymTypeExpression left, SymTypeExpression right) {
    //check whether TypeCheck class deems types compatible
    boolean comp = TypeCheck.compatible(left, right);

    //check whether last Part of FullQualifiedName is equal
    String leftName = left.print();
    String rightName = right.print();
    String[] leftNameArray = leftName.split("\\.");
    String[] rightNameArray = rightName.split("\\.");
    if(leftNameArray.length > 1){
      leftName = leftNameArray[leftNameArray.length - 1];
    }
    if(rightNameArray.length > 1){
      rightName = rightNameArray[rightNameArray.length - 1];
    }
    if(leftName.equals(rightName)){
      comp = true;
    }

    return comp;
  }

  public static boolean isSubtypeOf(SymTypeExpression subType, SymTypeExpression superType){
    //Object is superType of all other types
    if(superType.getTypeInfo().getName().equals("Object")){
      return true;
    }

    //Otherwise use default TypeCheck method
    else return TypeCheck.isSubtypeOf(subType, superType);
  }

  public static boolean optionalCompatible(SymTypeExpression optional, SymTypeExpression right){
    //check that first argument is of Type Optional
    if (!optional.isGenericType() || optional.print().equals("Optional")){
      Log.error("function optionalCompatible requires an Optional SymType " +
              "but was given " + optional.print());
      return false;
    }

    //check whether value in optional argument and second argument are compatible
    SymTypeExpression leftUnwrapped = ((SymTypeOfGenerics) optional).getArgument(0);
    return compatible(leftUnwrapped, right);
  }

  public static Optional<SymTypeExpression> unwrapOptional(SymTypeExpression optional){
    //check that argument is of Type Optional
    if (!optional.isGenericType() || optional.print().equals("Optional")){
      Log.error("function optionalCompatible requires an Optional SymType " +
              "but was given " + optional.print());
      return Optional.empty();
    }

    //return type of optional
    if(!((SymTypeOfGenerics) optional).getArgumentList().isEmpty()){
      return Optional.of(((SymTypeOfGenerics) optional).getArgument(0));
    }
    else {
      return Optional.empty();
    }
  }

  public static SymTypeExpression unwrapSet(SymTypeExpression set){
    //check that argument is of collection type
    boolean correct = false;
    for (String s : collections) {
      if (set.isGenericType() && set.getTypeInfo().getName().equals(s)) {
        correct = true;
      }
    }
    if(!correct){
      Log.error("function unwrapSet requires a Collection SymType " +
              "but was given " + set.print());
    }

    //get SymType used in Collection
    SymTypeExpression unwrapped = ((SymTypeOfGenerics) set).getArgument(0);
    return unwrapped;
  }
}
