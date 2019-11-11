package modelChecker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import formula.pathFormula.PathFormula;
import formula.pathFormula.Always;
import formula.pathFormula.Eventually;
import formula.pathFormula.Next;
import formula.pathFormula.Until;
import formula.stateFormula.And;
import formula.stateFormula.Or;
import formula.stateFormula.AtomicProp;
import formula.stateFormula.BoolProp;
import formula.stateFormula.Not;
import formula.stateFormula.StateFormula;
import formula.stateFormula.ThereExists;
import formula.stateFormula.ForAll;
import model.Model;
import model.State;
import model.Transition;

public class SATChecker {
    private Model model;

    public SATChecker(Model model) {
        this.model = model;
    }

    public Set<State> getSat(StateFormula formula, Set<State> states) {
        // Use "instanceof" to check the type of StateFormula instance
        System.out.println(states);
        if (formula instanceof BoolProp) {
            return getSatBool((BoolProp) formula, states);
        } else if (formula instanceof AtomicProp) {
            return getSatAtomicProp((AtomicProp) formula, states);
        } else if (formula instanceof And) {
            return getSatAnd((And) formula, states);
        } else if (formula instanceof Or) {
            return getSatOr((Or) formula, states);
        } else if (formula instanceof Not) {
            return getSatNot((Not) formula, states);
        } else if (formula instanceof ThereExists) {
            return getSatThereExists((ThereExists) formula, states);
        } else if (formula instanceof ForAll) {
            //TODO
            return getSatForAll((ForAll) formula, states);
        }

        return null;
    }

    private Set<State> getSatForAll(ForAll formula, Set<State> states) {
        PathFormula pathFormula = formula.pathFormula;

        // Get all the state sets.
        if (pathFormula instanceof Next) {
            // Check if the formula satisfies every 2nd state.
            return getSatForAllNext((Next) pathFormula, states);

        } else if (pathFormula instanceof Until) {
            // return getSatExistsUntil(formula, states);
        } else if (pathFormula instanceof Always) {
            // return getSatExistsAlways(formula, states);
        } else if (pathFormula instanceof Eventually) {
            // return getSatExistsEventually(formula, states);
        }
        return null;
    }

    private Set<State> getSatForAllNext(Next formula, Set<State> states) {
        for (State state: states){
            // String stateName = state.getName();
            Set<Transition> transitions = findTransitionsBySource(state);
            for (Transition transition: transitions){

                String[] actions = transition.getActions();
            }
        }

        return null;
    }

    private Set<State> getSatThereExists(ThereExists formula, Set<State> states) {
        PathFormula pathFormula = formula.pathFormula;

        // use "instanceof" to check the type of PathFormula instance
        if (pathFormula instanceof Next) {
            return getSatExistsNext(formula, states);
        } else if (pathFormula instanceof Until) {
            return getSatExistsUntil(formula, states);
        } else if (pathFormula instanceof Always) {
            return getSatExistsAlways(formula, states);
        } else if (pathFormula instanceof Eventually) {
            return getSatExistsEventually(formula, states);
        }

        return null;
    }

    private Set<State> getSatExistsUntil(ThereExists formula, Set<State> states) {
        PathFormula pathFormula = formula.pathFormula;

        // check if the pathFormula is an instace of Until class
        if (!(pathFormula instanceof Until)) return getSatThereExists(formula, states);

        StateFormula left = ((Until) pathFormula).left;
        StateFormula right = ((Until) pathFormula).right;
        Set<State> leftStates = getSat(left, states);
        Set<State> rightStates = getSat(right, states);
        Set<String> rightActions = ((Until) pathFormula).getRightActions();
        Set<String> leftActions = ((Until) pathFormula).getLeftActions();

        // check if the set of right actions of the until formula is empty
        if (!rightActions.isEmpty()) {
            //TODO
        }

        // check if the set of left actions of the until formula is empty
        if(!leftActions.isEmpty()) {
            //TODO
        }

        //TODO
        return states;
    }

    private Set<State> getSatExistsNext(ThereExists formula, Set<State> states) {
        PathFormula pathFormula = formula.pathFormula;

        // check if the pathFormula is an instace of Next class
        if (!(pathFormula instanceof Next)) return getSatThereExists(formula, states);

        StateFormula stateFormula = ((Next) pathFormula).stateFormula;
        Set<State> fStates = getSat(stateFormula, states); //gets the set of states that satisfy the given formula
        Set<String> actions = ((Next)pathFormula).getActions();

        // check if the
        if (!actions.isEmpty()) {
            //TODO
        }

        Set<State> newSetOfStates = new HashSet<>();

        // use for loop to iterate states in the set
        for(State state : states){
            Set<State> nextStates = getSetOfNextStates(state, states);
            nextStates.retainAll(fStates);

            if (!nextStates.isEmpty()) newSetOfStates.add(state);
        }

        return states;
    }

    private Set<State> getSetOfNextStates(State state, Set<State> states) {
        // get the set of names of next states
        Set<String> nextStates_str = Arrays.asList(model.getTransitions()).stream().filter(x -> x.getSource().equals(state.getName())).map(x -> x.getTarget()).collect(Collectors.toSet());
        // get the set of next states
        Set<State> nextStates = states.stream().filter(x -> nextStates_str.contains(x.getName())).collect(Collectors.toSet());
        return nextStates;
    }

    /**
     * Compute the Exists Always formula for the satisfaction checking.
     * @param formula
     * @param states
     * @return
     */
    private Set<State> getSatExistsAlways(ThereExists formula, Set<State> states) {
        PathFormula pathFormula = formula.pathFormula;

        // check if the pathFormula is an instace of Always class
        if (!(pathFormula instanceof Always)) return getSatThereExists(formula, states);

        StateFormula stateFormula = ((Always) pathFormula).stateFormula;
        Set<State> fStates = getSat(stateFormula, states); // gets the set of states that satisfy the given formula
        Set<String> actions = ((Always) pathFormula).getActions();

        if (!actions.isEmpty()) {
            //TODO
        }

        Set<State> newSetOfStates = new HashSet<>(fStates);

        // use an infinite loop to check all states of all paths
        while (true) {
            Set<State> s = new HashSet<>(newSetOfStates);
            Set<State> statesToRemove = new HashSet<>();

            // use for loop to iterate the states in the set
            for (State state : s) {
                Set<State> nextStates = getSetOfNextStates(state, states);
                nextStates.retainAll(newSetOfStates); //retain the states that satisfy the state formula

                // check if all next states do not satisfy the given state formula
                if (nextStates.isEmpty()) statesToRemove.add(state);
            }

            // check if there is no more states to remove from the set
            if (statesToRemove.size() == 0) break;

            newSetOfStates.removeAll(statesToRemove);
        }

        return newSetOfStates;
    }

    private Set<State> getSatExistsEventually(ThereExists formula, Set<State> states) {
        PathFormula pathFormula = formula.pathFormula;

        // check if the pathFormula is an instace of Eventually class
        if (!(pathFormula instanceof Eventually)) return getSatThereExists(formula, states);

        StateFormula stateFormula = ((Eventually) pathFormula).stateFormula;
        Set<State> fStates = getSat(stateFormula, states); // gets the set of states that satisfy the given formula

        // check if set of left actions is empty
        if (!((Eventually) pathFormula).getLeftActions().isEmpty()) {
            //TODO
        }

        // check if set of right actions is empty
        if (!((Eventually) pathFormula).getRightActions().isEmpty()) {
            // TODO
        }

        //TODO
        return states;
    }

    /**
     * Compute the satisfaction checking for boolean proposition.
     * @param formula - boolean proposition
     * @param states - set of current states
     * @return Set of states that are passed the satisfaction check
     */
    private Set<State> getSatBool(BoolProp formula, Set<State> states) {
        return states;
    }

    /**
     * Gets the set of transitions whose source state is equal to the given state.
     * @param state
     * @return
     */
    private Set<Transition> findTransitionsBySource(State state) {
        return Arrays.asList(model.getTransitions()).stream().filter(x -> x.getSource().equals(state.getName())).collect(Collectors.toSet());
    }

    /**
     * Gets the set of transitions whose target state is equal to the given state.
     * @param state - target state
     * @return The set of transitions
     */
    private Set<Transition> findTransitionsByTarget(State state) {
        return Arrays.asList(model.getTransitions()).stream().filter(x -> x.getTarget().equals(state.getName())).collect(Collectors.toSet());
    }

    /**
     * Compute the satisfaction checking for atomic propositions.
     * @param formula - Atomic Proposition formulae
     * @param states - set of current states
     * @return Set of states that are passed the satisfaction check
     */
    private Set<State>getSatAtomicProp(AtomicProp formula, Set<State> states){
        String targetLabel = formula.label;

        // Use the stream filter to get the set of states that has the target label.
		return states.stream().filter(x->Arrays.asList(x.getLabel()).contains(targetLabel)).collect(Collectors.toSet());
    }

    /**
     * Compute the satisfaction checking for And formula.
     * @param formula - And formula
     * @param states - set of current states.
     * @return Set of states that are passed the satisfaction check
     */
    private Set<State> getSatAnd(And formula, Set<State> states) {
        Set<State> left = getSat(formula.left, states);
        Set<State> right = getSat(formula.right, states);

        // Use the stream filter to get states that are contained in both left and right sets
        Set<State> newStates = left.stream().filter(right::contains).collect(Collectors.toSet());
        return newStates;
    }

    /**
     * Compute the satisfaction checking for Or formula.
     * @param formula - Or formula
     * @param states - set of current states.
     * @return Set of states that are passed the satisfaction check
     */
    private Set<State> getSatOr(Or formula, Set<State> states) {
        Set<State> left = getSat(formula.left, states);
        Set<State> right = getSat(formula.right, states);

        left.removeAll(right); // remove all elements in right set, to remove the duplicating elements
        right.addAll(left); // add all elements in the modified left set to right set.

        return right;
    }

    /**
     * Compute the satisfaction checking for Not formula.
     * @param formula - Not formula
     * @param states  - set of current states.
     * @return Set of states that are passed the satisfaction check
     */
    private Set<State> getSatNot(Not formula, Set<State> states) {
        Set<State> tempSet = getSat(formula.stateFormula, states);
        Set<State> copied_original = new HashSet<>(states);
        copied_original.removeAll(tempSet);
        return copied_original;
    }

}
