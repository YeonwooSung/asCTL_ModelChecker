package model;

import java.io.FileReader;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.Gson;

/**
 * A model is consist of states and transitions
 */
public class Model {
    private Set<State> initialSet;
    State[] states;
    Transition[] transitions;

    public static Model parseModel(String filePath) throws IOException {
        Gson gson = new Gson();
        Model model = gson.fromJson(new FileReader(filePath), Model.class);
        for (Transition t : model.transitions) {
            System.out.println(t);
        }
        model.generateInitialSet();
        return model;
    }

    /**
     * Generates the set of initial states.
     */
    private void generateInitialSet() {
        initialSet = new HashSet<State>();

        for (State state : states) {
            if (state.isInit()) {
                initialSet.add(state);
            }
        }
    }

    /**
     * Getter for initialSet.
     * @return initialSet
     */
    public Set<State> getInitialSet() {
        return initialSet;
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

    public HashMap<String, List<Transition>> getStateMap() {
        HashMap<String, List<Transition>> stateMap = new HashMap<>();

        for (State state : states) {
            List<Transition> list = new ArrayList<Transition>();
            String name = state.getName();

            for (Transition transition : transitions) {
                if (transition.getSource().equals(name)) {
                    list.add(transition);
                }
            }

            stateMap.put(name, list);
        }

        return stateMap;
    }

}
