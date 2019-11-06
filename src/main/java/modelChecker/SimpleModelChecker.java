package modelChecker;

import java.util.Set;
import java.util.HashSet;

import formula.stateFormula.StateFormula;
import model.Model;
import model.State;
import formula.stateFormula.And;

public class SimpleModelChecker implements ModelChecker {

    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {
        // The query should not be null!
        if (query == null) {
            System.out.println("Query should not be null!");
            return false;
        }

        State[] initStates = model.getStates(); //get the initial states
        Set<State> startingStates = model.getInitialSet();

        //TODO generate Program Graph -> do the satisfaction checking

        Set<State> sat = new HashSet<State>(); //TODO get the satisfaction set -> use variable "formulae"

        // Check if satisfaction set contains all initial states to do the Model Checking for CTL
        boolean result = sat.containsAll(startingStates);

        return result; // return the result of satisfaction checking
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

}
