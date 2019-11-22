package modelChecker;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import formula.*;
import formula.stateFormula.*;
import modelChecker.SimpleModelChecker;
import model.Model;

public class ModelCheckerTest {
    private Model model;
    private Model mutexModel;
    private StateFormula fairnessConstraint_mutex;
    private StateFormula fairnessConstraint;
    private StateFormula query;
    private SimpleModelChecker mc;

    @Before
    public void instantiateEnvironment(){
        try {
            mutexModel = Model.parseModel("src/test/resources/mtxmodel.json");
            fairnessConstraint_mutex = new FormulaParser("src/test/resources/mtxconstraint.json").parse();

            model = Model.parseModel("src/test/resources/model1.json");
            fairnessConstraint = new FormulaParser("src/test/resources/constraint1-pass.json").parse();
            query = new FormulaParser("src/test/resources/ctl1.json").parse();
            mc = new SimpleModelChecker();
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /*
     * Each test methods test the model with corresponding constraint and query.
     * If the sat model checker fails, it will print out the counter example for the tracing.
     */

    @Test
    public void buildAndCheckModel() {
        // Constraint passes, query passes
        setEnvironment("model1.json", "ctl1.json", "constraint1-pass.json");
        assertTrue(mc.check(model, fairnessConstraint, query));
    }

    @Test
    public void buildAndCheckModel2() {
        // Constraint fails Trivially True
        setEnvironment("model1.json", "ctl1.json", "constraint1.json");
        assertTrue(mc.check(model, fairnessConstraint, query));
    }

    @Test
    public void buildAndCheckModel3() {
        // Constraint passes, query fails, False
        setEnvironment("model1.json", "ctl3.json", "constraint1-pass.json");
        assertFalse(mc.check(model, fairnessConstraint, query));
    }

    @Test
    public void buildAndCheckModel4() {
        // Constraint fails Trivially True
        setEnvironment("model2.json", "ctl2.json", "constraint2.json");
        assertTrue(mc.check(model, fairnessConstraint, query));
    }

    @Test
    public void buildAndCheckModel5() {
        // Constraint passes, query passes
        setEnvironment("mtxmodel.json", "mtxctl.json", "mtxconstraint.json");
        assertTrue(mc.check(model, fairnessConstraint, query));
    }

    @Test
    public void buildAndCheckModelWithENF1() {
        // Constraint passes, query passes
        setEnvironment("model1.json", "ctl1.json", "constraint1-pass.json");
        assertTrue(mc.check(model, fairnessConstraint, query));
    }

    @Test
    public void buildAndCheckModelWithENF2() {
        // Constraint fails Trivially True
        setEnvironment("model1.json", "ctl1.json", "constraint1.json");
        assertTrue(mc.check(model, fairnessConstraint, query));
    }

    @Test
    public void buildAndCheckModelWithENF3() {
        // Constraint passes, query fails, False
        setEnvironment("model1.json", "ctl3.json", "constraint1-pass.json");
        assertFalse(mc.check(model, fairnessConstraint, query));
    }

    @Test
    public void buildAndCheckModelWithENF4() {
        // Constraint fails Trivially True
        setEnvironment("model2.json", "ctl2.json", "constraint2.json");
        assertTrue(mc.check(model, fairnessConstraint, query));
    }

    @Test
    public void buildAndCheckModelWithENF5() {
        // Constraint passes, query passes
        setEnvironment("mtxmodel.json", "mtxctl.json", "mtxconstraint.json");
        assertTrue(mc.check(model, fairnessConstraint, query));
    }

    @Test
    public void buildAndChecKMutexModel1() {
        try {
            // sat model checking passes
            StateFormula query1 = new FormulaParser("src/test/resources/ctl1.json").parse();
            assertTrue(mc.check(mutexModel, fairnessConstraint, query1));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void buildAndChecKMutexModel2() {
        try {
            // sat model checking passes
            StateFormula query2 = new FormulaParser("src/test/resources/ctl2.json").parse();
            assertTrue(mc.check(mutexModel, fairnessConstraint, query2));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void buildAndChecKMutexModel3() {
        try {
            // constraint pass, query fails, False -> print out trace message
            StateFormula query2 = new FormulaParser("src/test/resources/ctl2.json").parse();
            assertFalse(mc.check(mutexModel, fairnessConstraint_mutex, query2));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    private void setEnvironment(String modelName, String queryName, String constraintName){
        try {
            String resourcePath = "src/test/resources/";
            model = Model.parseModel(resourcePath + modelName);
            fairnessConstraint = new FormulaParser(resourcePath + constraintName).parse();
            query = new FormulaParser(resourcePath + queryName).parse();
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
