/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.setexpressions._ast;

import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedNameBuilder;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedTypeBuilder;

import java.util.Optional;

public class ASTSetEnumerationBuilder extends ASTSetEnumerationBuilderTOP {

  @Override
  public ASTSetEnumeration build() {
    if (!isPresentMCType()) {
      if (getOpeningBracket().equals("{")) {
        mCType = Optional.of(new ASTMCQualifiedTypeBuilder().setMCQualifiedName(
            new ASTMCQualifiedNameBuilder().addParts("Set").build()
        ).build());
      }
      else if (openingBracket.get().equals("[")) {
        mCType = Optional.of(new ASTMCQualifiedTypeBuilder().setMCQualifiedName(
            new ASTMCQualifiedNameBuilder().addParts("List").build()
        ).build());
      }
    }
    return super.build();
  }

  @Override
  public ASTSetEnumeration uncheckedBuild() {
    if (!isPresentMCType()) {
      if (getOpeningBracket().equals("{")) {
        mCType = Optional.of(new ASTMCQualifiedTypeBuilder().setMCQualifiedName(
            new ASTMCQualifiedNameBuilder().addParts("Set").build()
        ).build());
      }
      else if (openingBracket.get().equals("[")) {
        mCType = Optional.of(new ASTMCQualifiedTypeBuilder().setMCQualifiedName(
            new ASTMCQualifiedNameBuilder().addParts("List").build()
        ).build());
      }
    }
    return super.build();
  }
}
