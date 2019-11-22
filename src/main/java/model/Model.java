package model;

import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
// import java.util.List;
// import java.util.ArrayList;

import com.google.gson.Gson;

/**
 * A model is consist of states and transitions
 */
public class Model {
    private Set<State> initialSet;
    private Set<State> statesSet;
    private HashMap<String, State> stateMap;
    private HashMap<String, Set<Transition>> targetMap;
    private HashMap<String, Set<Transition>> transitionMap;
    State[] states;
    Transition[] transitions;

    public static Model parseModel(String filePath) throws IOException {
        Gson gson = new Gson();
        Model model = gson.fromJson(new FileReader(filePath), Model.class);
        model.generateInitialSet();
        model.generateStateMap();
        return model;
    }

    /**
     * Generates the set of initial states.
     */
    private void generateInitialSet() {
        initialSet = new HashSet<State>();
        statesSet = new HashSet<State>();

        for (State state : states) {
            statesSet.add(state);

            if (state.isInit()) {
                initialSet.add(state);
            }
        }
    }

    private void generateStateMap() {
        stateMap = new HashMap<>();
        targetMap = new HashMap<>();
        transitionMap = new HashMap<>();

        for (State state : states) {
            Set<Transition> list = new HashSet<>();
            String name = state.getName();

            for (Transition transition : transitions) {
                if (transition.getSource().equals(name)) {
                    list.add(transition);
                }

                String target = transition.getTarget();
                if (!targetMap.containsKey(target)) {
                    // Initialize the new set
                    Set<Transition> newSet = new HashSet<Transition>();
                    newSet.add(transition);
                    targetMap.put(target, newSet);

                } else {
                    // Populate the target set.
                    targetMap.get(target).add(transition);
                }
            }

            stateMap.put(name, state);
            transitionMap.put(name, list);
        }
    }

    /**
     * Getter for initialSet.
     * 
     * @return initialSet
     */
    public Set<State> getInitialSet() {
        return initialSet;
    }

    /**
     * Getter for statesSet.
     * 
     * @return statesSet
     */
    public Set<State> getStatesSet() {
        return statesSet;
    }

    /**
     * Returns the list of the states
     * 
     * @return list of state for the given model
     */
    public State[] getStates() {
        return states;
    }

    /**
     * Returns the list of transitions
     * 
     * @return list of transition for the given model
     */
    public Transition[] getTransitions() {
        return transitions;
    }

    public HashMap<String, State> getStateMap() {
        return stateMap;
    }

    public HashMap<String, Set<Transition>> getTransitionMap() {
        return transitionMap;
    }

    public HashMap<String, Set<Transition>> getTargetMap() {
        return targetMap;
    }

    public State getState(String name) {
        return stateMap.get(name);
    }

    /**
     * Gets the set of transitions whose target state is equal to the given state.
     * @param state - target state
     * @return May return null if the state is an initial state without any connecting transitions.
     */
    public Set<Transition> getTargetTransition(String name) {
        Set<Transition> transitions = targetMap.get(name);
        
        return (transitions != null)? transitions: new HashSet<>();
    }

    public Set<Transition> getTransition(String name) {
        return transitionMap.get(name);
    }

}
