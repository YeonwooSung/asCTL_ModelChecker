package modelChecker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
            //TODO
            return states;
        } else if (formula instanceof ForAll) {
            //TODO
            return states;
        }

        return null;
    }

    /**
     * Compute the satisfaction checking for boolean proposition.
     * @param formula - boolean proposition
     * @param states - set of current states
     * @return Set of states that are passed the satisfaction check
     */
    private Set<State> getSatBool(BoolProp formula, Set<State> states) {
        return states; //TODO
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
