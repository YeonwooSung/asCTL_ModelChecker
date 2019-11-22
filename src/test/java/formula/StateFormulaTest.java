package formula;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import formula.pathFormula.*;
import formula.stateFormula.*;
import modelChecker.SATChecker;
import model.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StateFormulaTest {
    private Model model;
    private SATChecker checker;
    private String[] actionSet1 = {"act1", "act2"};
    private String[] actionSet2 = {"act3"};
    private StateFormula wait1 = new AtomicProp("wait1");
    private StateFormula wait2 = new AtomicProp("wait2");
    private StateFormula crit1 = new AtomicProp("crit1");
    private StateFormula crit2 = new AtomicProp("crit2");

    @Before
    public void instantiateEnvironment(){
        try {
            model = Model.parseModel("src/test/resources/mtxmodel.json");
            checker = new SATChecker(model);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    /**
     * State Formula Tests
     */
    
    @Test
    public void testAND(){
        StateFormula formula = new And(wait1, wait2);

        Set<State> expected = new HashSet<>();
        expected.add(model.getState("s5"));
        Set<State> result = checker.getSat(formula, model.getStatesSet());

        assert(result.containsAll(expected));
        // Shouldn't contain any initial states.
        assertFalse(result.containsAll(model.getInitialSet()));
    }

    @Test
    public void testOR(){
        StateFormula formula = new Or(wait1, crit1);

        Set<State> expected = new HashSet<>();
        expected.add(model.getState("s5"));
        expected.add(model.getState("s1"));
        expected.add(model.getState("s2"));
        expected.add(model.getState("s6"));
        expected.add(model.getState("s7"));

        Set<State> result = checker.getSat(formula, model.getStatesSet());

        assert(result.containsAll(expected));
        // Shouldn't contain any initial states.
        assertFalse(result.containsAll(model.getInitialSet()));
    }

    @Test
    public void testNOT(){
        // Not (wait1 And wait2)
        StateFormula formula = new Not(new And(wait1, wait2));

        Set<State> expected = new HashSet<>();
        expected.addAll(model.getStatesSet());
        expected.remove(model.getState("s5"));

        Set<State> result = checker.getSat(formula, model.getStatesSet());

        assert(result.containsAll(expected));
        // Shouldn't contain s5.
        assertFalse(result.contains(model.getState("s5")));
    }

    @Test
    public void testExists(){
        Set<String> leftActions = new HashSet<>(Arrays.asList(actionSet1));
        Set<String> rightActions = new HashSet<>(Arrays.asList(actionSet2));

        PathFormula eventually = new Eventually(wait1, leftActions, rightActions);
        StateFormula exists = new ThereExists(eventually);


        Set<State> expected = new HashSet<>();
        expected.add(model.getState("s2"));
        expected.add(model.getState("s0"));
        expected.add(model.getState("s4"));
        expected.add(model.getState("s3"));
        expected.add(model.getState("s1"));
        expected.add(model.getState("s5"));

        Set<State> result = checker.getSat(exists, model.getStatesSet());
        assert(result.containsAll(expected));

        assertFalse(result.contains(model.getState("s6")));
        assertFalse(result.contains(model.getState("s7")));
    }

    @Test
    public void testforAll(){
        Set<String> leftActions = new HashSet<>(Arrays.asList(actionSet1));
        Set<String> rightActions = new HashSet<>(Arrays.asList(actionSet2));
        PathFormula eventually = new Eventually(wait1, leftActions, rightActions);
        StateFormula forall = new ForAll(eventually);
        
        Set<State> result = checker.getSat(forall, model.getStatesSet());
        // If not all states are satisfied, empty set is returned.
        assert(result.isEmpty());
        
        eventually = new Eventually(wait1, new HashSet<>(), new HashSet<>());
        forall = new ForAll(eventually);

        result = checker.getSat(forall, model.getStatesSet());
        // Contains all the states.
        assert(result.containsAll(model.getStatesSet()));
    }

    @Test
    public void testBoolProp(){
        StateFormula boolTrue = new BoolProp(true);
        StateFormula boolFalse = new BoolProp(false);

        Set<State> expected = new HashSet<>();
        expected.addAll(model.getStatesSet());

        Set<State> result = checker.getSat(boolTrue, model.getStatesSet());
        assert(result.containsAll(expected));

        result = checker.getSat(boolFalse, model.getStatesSet());
        assert(result.isEmpty());
    }

    @Test
    public void testAtomic(){
        Set<State> expected = new HashSet<>();
        expected.add(model.getState("s1"));
        expected.add(model.getState("s5"));
        expected.add(model.getState("s7"));

        Set<State> result = checker.getSat(wait1, model.getStatesSet());

        assert(result.containsAll(expected));
        assertFalse(result.containsAll(model.getInitialSet()));

        StateFormula empty = new AtomicProp("");

        result = checker.getSat(empty, model.getStatesSet());
        assert(result.contains(model.getState("s0")));
    }

    /**
     * Path Formula Tests
     */
    @Test
    public void testAlways(){
        StateFormula or1 = new Or(wait1, crit1);
        StateFormula or2 = new Or(wait2, crit2);
        StateFormula or = new Or(or1, or2);
        PathFormula always = new Always(or, new HashSet<>(Arrays.asList(actionSet1))); 
        StateFormula exists = new ThereExists(always);

        Set<State> result = checker.getSat(exists, model.getStatesSet());
        
        // These don't have valid paths to them or they don't have the appropriate labels.
        assertFalse(result.contains(model.getState("s0")));
        assertFalse(result.contains(model.getState("s5")));
    }

    @Test
    public void testEventually(){
        StateFormula and = new And(wait1, wait2);
        Set<String> leftActions = new HashSet<>(Arrays.asList(actionSet1));
        Set<String> rightActions = new HashSet<>(Arrays.asList(actionSet2));

        PathFormula eventually = new Eventually(and, leftActions, rightActions);
        StateFormula exists = new ThereExists(eventually);


        Set<State> expected = new HashSet<>();
        expected.add(model.getState("s0"));
        expected.add(model.getState("s5"));

        Set<State> result = checker.getSat(exists, model.getStatesSet());
        // s2, s4, s3, and s1, pass as well due to the loops.
        assert(result.containsAll(expected));
    }

    @Test
    public void testNext(){
        StateFormula and = new And(wait1, wait2);
        Set<String> rightActions = new HashSet<>(Arrays.asList(actionSet2));

        PathFormula next = new Next(and, rightActions);
        StateFormula exists = new ThereExists(next);

        Set<State> expected = new HashSet<>();
        expected.add(model.getState("s1"));
        expected.add(model.getState("s3"));

        Set<State> result = checker.getSat(exists, model.getStatesSet());
        assert(result.containsAll(expected));
    }

    @Test
    public void testUntil(){
        StateFormula or = new Or(wait1, crit1);
        StateFormula state0 = new AtomicProp("");

        Set<String> leftActions = new HashSet<>(Arrays.asList(actionSet1));
        Set<String> rightActions = new HashSet<>(Arrays.asList(actionSet2));

        PathFormula until = new Until(or, state0, leftActions, rightActions);
        StateFormula exists = new ThereExists(until);

        Set<State> expected = new HashSet<>();
        expected.add(model.getState("s0"));
        expected.add(model.getState("s1"));
        expected.add(model.getState("s2"));

        Set<State> result = checker.getSat(exists, model.getStatesSet());
        assert(result.containsAll(expected));
    }
}
