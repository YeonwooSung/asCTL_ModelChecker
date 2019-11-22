package modelChecker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import formula.pathFormula.*;
import formula.stateFormula.*;
import model.*;

public class SATChecker {
    private Model model;

    public SATChecker(Model model) {
        this.model = model;
    }

    /**
     * Computes the satisfaction model checking for the given formula.
     * @param formula the state formula
     * @param states the set of all states that should be checked
     * @return The satisfaction set that contains the all states that satisfy the given formula.
     */
    public Set<State> getSat(StateFormula formula, Set<State> states) {
        // Use "instanceof" to check the type of StateFormula instance
        if (formula instanceof BoolProp) {
            return getSatBool((BoolProp) formula, states);
        } else if (formula instanceof AtomicProp) {
            return getSatAtomicProp((AtomicProp) formula, states);
        } else if (formula instanceof And) {
            // "(a And b) = Not(Not a Or Not b)" by CNF propery
            Or or = new Or(new Not(((And) formula).left), new Not(((And) formula).right));
            return getSatNot(new Not(or), states);
        } else if (formula instanceof Or) {
            return getSatOr((Or) formula, states);
        } else if (formula instanceof Not) {
            return getSatNot((Not) formula, states);
        } else if (formula instanceof ThereExists) {
            return getSatThereExists((ThereExists) formula, states);
        } else if (formula instanceof ForAll) {
            return getSatForAll((ForAll) formula, states);
        }

        return new HashSet<>();
    }

    /**
     * Computes the satisfaction model checking for the path formula "ForAll".
     * @param formula path formula "ForAll"
     * @param states the set of all states that should be checked
     * @return The satisfaction set that contains the all states that satisfy the given formula.
     */
    private Set<State> getSatForAll(ForAll formula, Set<State> states) {
        PathFormula pathFormula = formula.pathFormula;
        ThereExists exists = new ThereExists(pathFormula);
        Set<State> sat = this.getSatThereExists(exists, states);

        // check if the satisfaction set contains all states
        if (sat.containsAll(states)) {
            return sat;
        }

        return new HashSet<>();
    }

    /**
     * This method computes the satisfaction model checking for the path formula "ThereExists".
     * @param formula path formula "ThereExists"
     * @param states the set of all states that should be checked
     * @return The satisfaction set that contains the all states that satisfy the given formula.
     */
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
            // "Exists Eventually = Exists True Until" by min-set
            // Fφ == [trueU(φ)]
            Eventually e = (Eventually) pathFormula;
            Until u = new Until(new BoolProp(true), e.stateFormula, e.getLeftActions(), e.getRightActions());
            return getSatExistsUntil(new ThereExists(u), states);
        }

        return new HashSet<>();
    }

    /**
     * The aim of this method is to compute the satisfaction model checking for the formula "ThereExists Until".
     * @param formula path formula "ThereExists"
     * @param states the set of all states that should be checked
     * @return The satisfaction set that contains the all states that satisfy the given formula.
     */
    private Set<State> getSatExistsUntil(ThereExists formula, Set<State> states) {
        PathFormula pathFormula = formula.pathFormula;

        // check if the pathFormula is an instace of Until class
        if (!(pathFormula instanceof Until))
            return getSatThereExists(formula, states);

        StateFormula left = ((Until) pathFormula).left;
        StateFormula right = ((Until) pathFormula).right;
        Set<State> leftStates = getSat(left, new HashSet<State>(states));
        Set<State> rightStates = getSat(right, new HashSet<State>(states));
        Set<String> rightActions = ((Until) pathFormula).getRightActions();
        Set<String> leftActions = ((Until) pathFormula).getLeftActions();

        boolean rightAction_notEmpty = !rightActions.isEmpty();  // check if the rightActions is not empty
        boolean leftAction_notEmpty = !leftActions.isEmpty();    // check if the leftActions is not empty

        // check if the set of right actions of the until formula is empty
        if (rightAction_notEmpty) {
            rightStates = this.getSetOfStatesByCheckingActionConstraints(rightStates, rightActions);
        }

        // check if the set of left actions of the until formula is empty
        if (leftAction_notEmpty) {
            leftStates = this.getSetOfStatesByCheckingActionConstraints(leftStates, rightStates, leftActions);
        }

        Set<State> newSetOfStates = new HashSet<>(rightStates);

        // run infinite loop to check all paths
        while (true) {
            Set<State> statesToRemove = new HashSet<>();
            // subtract 2 sets to avoid the cycle
            Set<State> s = CollectionHelper.substraction(leftStates, newSetOfStates);

            for (State state : s) {
                Set<State> nextStates = getSetOfNextStates(state, states);
                Set<State> nextStatesToRemove = new HashSet<>();

                for (State nextState : nextStates) {
                    // validate the set of right states
                    if (rightAction_notEmpty && rightStates.contains(nextState)) {
                        // Get set of transitions by checking the target name
                        String targetName = nextState.getName();

                        // get the set of transitions, where the target of the transition is the nextState and the source of the transition is the current state
                        Set<Transition> transitions = model.getTargetTransition(targetName).stream().filter(x -> x.getSource().equals(state.getName())).collect(Collectors.toSet());

                        // check if the set of transitions is empty
                        if (transitions.isEmpty())
                            continue;

                        int count = 0; // to count the number of transitions that does not have the actions that are defined in the constraint

                        for (Transition tr : transitions) {
                            // get the actions that the transition contains
                            Set<String> setOfActions_TargetIsNextState = Arrays.stream(tr.getActions()).collect(Collectors.toSet());

                            // Check if the intersection of the setOfActions_TargetIsNextState and rightActions is empty.
                            // The rightActions contains all right actions, and the setOfActions_TargetIsNextState contains all actions
                            // that the current transition has.
                            // Thus, the intersection of them will not be an empty set if the current transition has the action that we are looking for
                            boolean isEmpty = CollectionHelper.intersection(setOfActions_TargetIsNextState, rightActions).isEmpty();

                            if (isEmpty) count += 1;
                        }

                        // check if all transitions do not have the actions that we are looking for
                        if (count == transitions.size())
                            nextStatesToRemove.add(nextState);
                    }
                }

                nextStates.removeAll(nextStatesToRemove); // remove all unnecessary states
                nextStates.retainAll(newSetOfStates); // retain all states those who satisfy the formula

                if (nextStates.isEmpty()) statesToRemove.add(state);
            }

            s.removeAll(statesToRemove); // remove all states that do not satisfy the action constraints

            // if there is no more states to add, then break the inifinte loop
            if (s.isEmpty()) break;

            newSetOfStates.addAll(s);
        }

        return newSetOfStates;
    }

    /**
     * Computes the satisfaction model checking for the formula "ThereExists Next".
     * @param formula formula "ThereExists Next"
     * @param states the set of all states that should be checked
     * @return The satisfaction set that contains the all states that satisfy the given formula.
     */
    private Set<State> getSatExistsNext(ThereExists formula, Set<State> states) {
        PathFormula pathFormula = formula.pathFormula;

        // check if the pathFormula is an instace of Next class
        if (!(pathFormula instanceof Next)) return getSatThereExists(formula, states);

        StateFormula stateFormula = ((Next) pathFormula).stateFormula;
        Set<State> fStates = getSat(stateFormula, states); //gets the set of states that satisfy the given formula
        Set<String> actions = ((Next)pathFormula).getActions();

        if (!actions.isEmpty()) {
            // update the set of satisfaction states by checking the action constraints
            fStates = getSetOfStatesByCheckingActionConstraints(fStates, ((Next) pathFormula).getActions());
        }

        Set<State> newSetOfStates = new HashSet<>();

        // use for loop to iterate states in the set
        for(State state : states){
            Set<State> nextStates = getSetOfNextStates(state, states);
            nextStates.retainAll(fStates);

            if (!nextStates.isEmpty()) newSetOfStates.add(state);
        }

        return newSetOfStates;
    }

    /**
     * This method returns the set of next states of the current state.
     * @param state The current state
     * @param states the set of all states that should be checked
     * @return The set of states that are next state of the current state.
     */
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
            // update the set of satisfaction states by checking the action constraints
            fStates = getSetOfStatesByCheckingActionConstraints(fStates, ((Always) pathFormula).getActions());
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

    /**
     * Gets all the states with transitions that have the specified actions we are looking for.
     * Basically, this method is used for the path formula that we need to care about the left states
     * and right states, such as Until or Eventually.
     *
     * @param leftStates the set of left states
     * @param rightStates the set of right states
     * @param actions the set of actions that we are looking for.
     * @return The set of states who are the target of the transitions that have the specified actions we are looking for.
     */
    private Set<State> getSetOfStatesByCheckingActionConstraints(Set<State> leftStates, Set<State> rightStates, Set<String> actions) {
        Set<State> statesToRemove = new HashSet<State>();
        Set<State> newLeftStates = new HashSet<State>(leftStates); //clone the set "leftStates"

        // use the for loop to check all left states
        for (State state : leftStates) {
            int count = 0;

            Set<Transition> transitions = model.getTransition(state.getName()).stream().filter(x -> !x.getSource().equals(x.getTarget())).collect(Collectors.toSet());
            if (transitions.isEmpty()) continue;

            for (Transition tr : transitions) {
                // check if the current transition has the action that we are looking for
                boolean isEmpty = CollectionHelper.intersection(Arrays.stream(tr.getActions()).collect(Collectors.toSet()), actions).isEmpty();

                //if (isEmpty && !nextIsFromRightStates) count += 1;
                if (isEmpty) count += 1;
            }

            if (count == transitions.size()) statesToRemove.add(state);
        }

        newLeftStates.removeAll(statesToRemove);
        return newLeftStates;
    }

    /**
     * Gets all the states with transitions that have the specified actions we are looking for.
     * @param states the set of states.
     * @param actions the set of actions that we are looking for.
     * @return The set of states who are the target of the transitions that have the specified actions we are looking for.
     */
    private Set<State> getSetOfStatesByCheckingActionConstraints(Set<State> states, Set<String> actions) {
        Set<State> statesToRemove = new HashSet<State>();
        Set<State> newStates = new HashSet<State>(states); //clone the set "states"

        // use for loop to check all states in the given set.
        for (State state : states) {
            int count = 0;
            // get the set of transitions by comparing the target state of the transition
            Set<Transition> transitions = model.getTargetTransition(state.getName()).stream().filter(x -> !x.getSource().equals(x.getTarget())).collect(Collectors.toSet());

            // check if the set of transitions is empty
            if (transitions.isEmpty()) continue;

            // use for loop to iterate transitions
            for (Transition tr : transitions) {
                boolean isEmpty = CollectionHelper.intersection(Arrays.stream(tr.getActions()).collect(Collectors.toSet()), actions).isEmpty();

                if (isEmpty) {
                    count++;
                }
            }

            // check if all transitions do not have the action that we are looking for
            if (count == transitions.size()) {
                statesToRemove.add(state);
            }
        }

        newStates.removeAll(statesToRemove);
        return newStates;
    }

    /**
     * Compute the satisfaction checking for boolean proposition.
     * @param formula - boolean proposition
     * @param states - set of current states
     * @return Set of states that are passed the satisfaction check
     */
    private Set<State> getSatBool(BoolProp formula, Set<State> states) {
        if (!formula.value) return new HashSet<State>(); //return the empty set if the boolean value is false

        return states;
    }

    /**
     * Compute the satisfaction checking for atomic propositions.
     * @param formula - Atomic Proposition formulae
     * @param states - set of current states
     * @return Set of states that are passed the satisfaction check
     */
    private Set<State> getSatAtomicProp(AtomicProp formula, Set<State> states){
        String targetLabel = formula.label;

        // Use the stream filter to get the set of states that has the target label.
		return states.stream().filter(x->Arrays.asList(x.getLabel()).contains(targetLabel)).collect(Collectors.toSet());
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
