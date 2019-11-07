package modelChecker;

import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import formula.stateFormula.StateFormula;
import formula.pathFormula.*;

import model.Model;
import model.State;
import model.Transition;
import formula.stateFormula.*;

public class SimpleModelChecker implements ModelChecker {

    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {
        // The query should not be null!
        if (query == null) {
            System.out.println("Query should not be null!");
            return false;
        }

        Set<State> startingStates = model.getInitialSet();
        // StateFormula formulae = new And(constraint, query);
        // System.out.println(formulae);

        // Check at every step if the constraint is satisfied, and if it is not,
        // the check returns as false.

        HashMap<String, List<Transition>> stateMap = model.getStateMap();


        //TODO check initial states
        for (State state: startingStates){
            // System.out.println(state.getName());
            List<Transition> transitions = stateMap.get(state.getName());

            for (Transition transition: transitions) {
                System.out.println(transition.getSource() + "-" + transition.getTarget());
            }

        }

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
