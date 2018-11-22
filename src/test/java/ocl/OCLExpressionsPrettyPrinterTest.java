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
package ocl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import de.monticore.prettyprint.IndentPrinter;
import ocl.monticoreocl.oclexpressions.prettyprint.OCLExpressionsPrettyPrinter;
import oclexpressions.testoclexpressions._ast.ASTEDeclaration;
import oclexpressions.testoclexpressions._ast.ASTPrimaryExpression;
import oclexpressions.testoclexpressions._parser.TestOCLExpressionsParser;
import oclexpressions.testoclexpressions._visitor.TestOCLExpressionsVisitor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.monticore.expressionsbasis._ast.ASTExpression;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;

public class OCLExpressionsPrettyPrinterTest {
  
  @BeforeClass
  public static void init() {
    LogStub.init();
    Log.enableFailQuick(false);
  }
  
  @Before
  public void setUp() {
    Log.getFindings().clear();
  }
  
  static class PrimaryPrettyPrinter extends OCLExpressionsPrettyPrinter
      implements TestOCLExpressionsVisitor {
    
    private TestOCLExpressionsVisitor realThis;
    
    @Override
    public void visit(ASTPrimaryExpression node) {
      getPrinter().print((node.getName()));
    }
    
    public PrimaryPrettyPrinter(IndentPrinter printer) {
      super(printer);
      realThis = this;
    }
    
    @Override
    public TestOCLExpressionsVisitor getRealThis() {
      return realThis;
    }
   

    @Override
    public void visit(ASTEDeclaration node) {
      if (node.isPresentPublic()) {
        getPrinter().print(node.getPublic() + " ");
      }
      if (node.isPresentPrivate()) {
        getPrinter().print(node.getPrivate() + " ");
      }
      getPrinter().print(node.getType() + " " + node.getVarName());
    }
  }
  
  @Test
  public void testImpliesExpression() throws IOException {
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("a implies b"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }
  
  @Test
  public void testSingleLogicalORExpr() throws IOException {
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("a | b"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }
  

  @Test
  public void testForallExpr1() throws IOException {
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("forall a in A : exp"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }

  @Ignore
  @Test
  public void testForallExpr2() throws IOException {
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("forall a in List<Abc> : exp"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }

  @Ignore
  @Test
  public void testExistsExpr1() throws IOException {
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("exists :exp"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }
  
  @Test
  public void testExistsExpr2() throws IOException {
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("exists a in A:exp"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }

  @Ignore
  @Test
  public void testExistsExpr3() throws IOException {
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("exists a in List <Abc> : exp"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }
  
  @Test
  public void testAnyExpr() throws IOException {
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("any exp"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }
  
  @Test
  public void testLetinExpr1() throws IOException {
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("let public Int A; private Double B; in exp"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }
  
  @Test
  public void testLetDeclaration() throws IOException {
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("let public Int A;private Double B;"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }
  
  @Test
  public void testIterateExpr() throws IOException {    
    TestOCLExpressionsParser parser = new TestOCLExpressionsParser();
    Optional<ASTExpression> ast = parser.parseExpression(new StringReader("iterate { B in A; public Int A : Name = Value }"));
    assertTrue(ast.isPresent());
    assertFalse(parser.hasErrors());
    ASTExpression assignment = ast.get();
    PrimaryPrettyPrinter printer = new PrimaryPrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast.get());
    ast = parser.parseExpression(new StringReader(output));
    assertFalse(parser.hasErrors());
    assertTrue(ast.isPresent());
    assertTrue(assignment.deepEquals(ast.get()));
  }
  
}
