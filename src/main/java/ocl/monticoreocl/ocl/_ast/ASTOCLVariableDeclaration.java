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
package ocl.monticoreocl.ocl._ast;

import de.monticore.types.types._ast.ASTType;

import java.util.Optional;

import static ocl.monticoreocl.oclexpressions._ast.ASTOCLQualifiedPrimary.name2String;

public class ASTOCLVariableDeclaration extends ASTOCLVariableDeclarationTOP {
  public ASTOCLVariableDeclaration() {
    super();
  }

  public  ASTOCLVariableDeclaration (/* generated by template ast.ConstructorParametersDeclaration*/
      Optional<ASTType> type
      ,
      ocl.monticoreocl.oclexpressions._ast.ASTName2 name
      ,
      de.monticore.expressionsbasis._ast.ASTExpression expression

  )
    /* generated by template ast.ConstructorAttributesSetter*/
  {
    super(type, name, expression);
  }

  public String getName() {
    return name2String(getName2());
  }
}