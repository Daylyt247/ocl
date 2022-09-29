package de.monticore.ocl2smt;


import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import de.monticore.cd2smt.context.CDContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CommonExpressionTest extends ExpressionAbstractTest {
    protected List<BoolExpr> res = new ArrayList<>();
    protected CDContext cdContext = new CDContext(new Context());

    @BeforeEach
    public void setup() throws IOException {
       parse("MinAuction.cd", "CommonExpr.ocl");
        OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        ocl2SMTGenerator.ocl2smt(oclAST.getOCLArtifact()).forEach(b-> res.add(b.getRight()));
    }


    @Test
    public void testComparisonConverter() {
        Assertions.assertEquals(res.get(12).getSExpr(), "(< 10 3)");
        Assertions.assertEquals(res.get(13).getSExpr(), "(> 10 4)");
        Assertions.assertEquals(res.get(14).getSExpr(), "(<= 10 4)");
        Assertions.assertEquals(res.get(15).getSExpr(), "(>= 10 4)");
        Assertions.assertEquals(res.get(16).getSExpr(), "(= 10 4)");
        Assertions.assertEquals(res.get(17).getSExpr(), "(not (= 10 4))");
    }

    @Test
    public void testArithmeticExpressionConverter() {
        Assertions.assertEquals(res.get(8).getSExpr(), "(= (+ 10 12) 22)");
        Assertions.assertEquals(res.get(9).getSExpr(), "(= (div 10 5) 2)");
        Assertions.assertEquals(res.get(10).getSExpr(), "(= (* 10 5) 50)");
        Assertions.assertEquals(res.get(11).getSExpr(), "(= (mod 10 2) 0)");
        Assertions.assertEquals(res.get(18).getSExpr(), "(= (- 10 12) (* (- 1) 2))");
    }

    @Test
    public void testLogicExpressionConverter() {
        Assertions.assertEquals(res.get(0), cdContext.getContext().mkBool(true));
        Assertions.assertEquals(res.get(1), cdContext.getContext().mkFalse());
        Assertions.assertEquals(res.get(2).getSExpr(), "(not true)");
        Assertions.assertEquals(res.get(3).getSExpr(), "(not false)");
        Assertions.assertEquals(res.get(4).getSExpr(), "(and false false)");
        Assertions.assertEquals(res.get(5).getSExpr(), "(and false true)");
        Assertions.assertEquals(res.get(6).getSExpr(), "(or true false)");
        Assertions.assertEquals(res.get(7).getSExpr(), "(or true true)");
    }


}


