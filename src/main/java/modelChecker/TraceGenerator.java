package modelChecker;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import formula.stateFormula.Not;
import formula.stateFormula.StateFormula;
import model.Model;
import model.State;
import model.Transition;

class TraceGenerator {
    private SATChecker checker;
    private Model model;

    public TraceGenerator(Model model, SATChecker checker) {
        this.checker = checker;
        this.model = model;
    }


    /**
     * Gets the counterexample for the trace.
     * What this method does is:
     *      1) negate the original formula
     *      2) do the satisfaciton model checking with the negated formula
     *      3) create the counterexample from the set of states that satisfy the negated formula
     *      4) returns the generated counterexample
     *
     * @param sat the satisfaction set for the original formula
     * @param formula the original formula
     * @return
     */
    public List<String> getCounterExampleForTrace(Set<State> sat, StateFormula formula) {
        ArrayList<String> result = new ArrayList<String>();
        Set<String> resultNames = new HashSet<String>();
        HashMap<String, State> stateMap = model.getStateMap();
        boolean foundCycle = false;

        Set<State> initialNotSat = new HashSet<State>(model.getInitialSet());
        initialNotSat.removeAll(sat);

        // Get the initial states that don't satisfy the formula
        Not negatedFormula = new Not(formula);
        Set<State> negated_sat = checker.getSat(negatedFormula, new HashSet<State>(model.getStatesSet()));
        Set<State> negated_sat_cloned = new HashSet<State>(negated_sat);
        
        // Remove the startingState from the set
        State currentState = initialNotSat.iterator().next();
        negated_sat_cloned.remove(currentState); 

        String name = currentState.getName();
        result.add(name);
        resultNames.add(name);

        while (true) {
            Set<Transition> transitions = model.getTransition(name).stream().filter(x -> !x.getSource().equals(x.getTarget())).collect(Collectors.toSet());;
            boolean checkIfFindNext = false;

            // if the current state is a terminal state, break the loop
            if (transitions.isEmpty()) break;

            // use for loop to check all states in the set
            for (State s : negated_sat) {
                Set<Transition> t = model.getTargetTransition(s.getName()).stream().filter(x -> !x.getSource().equals(x.getTarget())).collect(Collectors.toSet());
                Set<Transition> intersection = CollectionHelper.intersection(transitions, t);

                // check if the intersection set is empty
                if (!intersection.isEmpty()) {
                    checkIfFindNext = true;
                    Transition tr = intersection.iterator().next();
                    String targetName = tr.getTarget();

                    result.add(targetName); // add the name of the state to the result list

                    // The set does not allow the duplication, thus, by using contains() method of the set, we could detect the cyclic path.
                    if (resultNames.contains(targetName)) {
                        foundCycle = true;
                    } else {
                        resultNames.add(targetName);
                    }

                    currentState = stateMap.get(targetName);
                    name = currentState.getName();
                    break;
                }
            }

            // break the endless loop if we find the cycle
            if (foundCycle) {
                break;
            }

            // break the loop if we failed to find the next state from negated_sat
            if (!checkIfFindNext) {
                resultNames.remove(name);
                break;
            }
        }

        if (!foundCycle) {
            Set<State> allStates = model.getStatesSet();

            while (true) {
                Set<Transition> transitions = model.getTransition(name).stream().filter(x -> !x.getSource().equals(x.getTarget())).collect(Collectors.toSet());
                boolean checkIfFindNext = false;

                // if the current state is a terminal state, break the loop
                if (transitions.isEmpty()) break;

                // use for loop to check all states in the set
                for (State s : allStates) {
                    Set<Transition> t = model.getTargetTransition(s.getName()).stream().filter(x -> !x.getSource().equals(x.getTarget())).collect(Collectors.toSet());;
                    //Set<Transition> t = model.getTargetTransition(s.getName());
                    Set<Transition> intersection = CollectionHelper.intersection(transitions, t);

                    // check if the intersection set is empty
                    if (!intersection.isEmpty()) {
                        checkIfFindNext = true;
                        Transition tr = intersection.iterator().next();
                        String targetName = tr.getTarget();

                        result.add(targetName); //add the name of the state to the result list

                        // Check if the resultNames contains the name of the next state.
                        // If so, that means that the TraceGenerator found the cycle.
                        if (resultNames.contains(targetName)) {
                            foundCycle = true;
                        } else {
                            resultNames.add(targetName); //add the name of the next state to the list
                        }

                        currentState = stateMap.get(targetName);
                        name = currentState.getName();
                        break;
                    }
                }

                if (foundCycle || !checkIfFindNext) {
                    break;
                }
            }
        }

        return result;
    }
}
