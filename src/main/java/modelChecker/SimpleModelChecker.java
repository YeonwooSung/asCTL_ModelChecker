package modelChecker;

import formula.stateFormula.StateFormula;
import model.Model;
import model.State;

public class SimpleModelChecker implements ModelChecker {

    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {
        State[] initStates = model.getStates(); //get the initial states
        //TODO get paths (generate path tree? or do something else?) -> check all states?
        //TODO check the initial states

        return false;
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

}
