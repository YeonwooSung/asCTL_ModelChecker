package modelChecker;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import model.*;
import formula.stateFormula.*;

public class SimpleModelChecker implements ModelChecker {

    /**
     * This method computes the satisfaction model check with the ENF.
     * Basically, the ENFConverter converts the formula to the ENF to reduce the number of cases.
     *
     * @param model The model to check
     * @param constraint The constraint
     * @param query The query
     * @return The satisfaction set for the given constraint and query.
     */
    public boolean checkENF(Model model, StateFormula constraint, StateFormula query) {
        // The query should not be null!
        if (query == null) {
            System.out.println("Query should not be null!");
            return false;
        }

        Set<State> startingStates = model.getInitialSet();
        SATChecker satChecker = new SATChecker(model);
        Set<State> allStates = new HashSet<State>(model.getStatesSet());
        Set<State> satConstraint = null;

        ENFConverter enfConverter = new ENFConverter(); //enf converter will convert the formula to ENF

        if (constraint != null){
            StateFormula constraint_enf = enfConverter.convertToENF(constraint); //convert the constraint to ENF

            satConstraint = satChecker.getSat(constraint_enf, allStates); //compute the sat model check with the constraint

            // check if the satisfaction set for the constraint is empty
            Set<State> initialStates = new HashSet<State>(model.getInitialSet());
            initialStates.retainAll(satConstraint);
            if (initialStates.isEmpty()) {
                System.out.println("The model trivially satisfies the formula as there are no states that satisfy the constraint.");
                return true;
            }
        }

        StateFormula query_enf = enfConverter.convertToENF(query); //convert the query to ENF

        // If the model satisfies the constraint but not the query, then print a counter example with a trace and return false.
        Set<State> sat = satChecker.getSat(query_enf, allStates); 
        // Else, get the intersection of both and check if it contains the starting states.

        // check if the satConstraint is null
        if (satConstraint != null) {
            sat.retainAll(satConstraint);
        }

        if (!sat.containsAll(startingStates)) {
            System.out.println("The model does not satisfy the query given the constraint.");
            String[] trace = getTrace(model, satChecker, sat, query);
            printTrace(trace);
            return false;
        }

        System.out.println("The model satisfies the query and the constraint.");
        return true; // return the result of satisfaction checking
    }

    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {
        // The query should not be null!
        if (query == null) {
            System.out.println("Query should not be null!");
            return false;
        }

        Set<State> startingStates = model.getInitialSet();
        SATChecker satChecker = new SATChecker(model);
        Set<State> allStates = new HashSet<State>(model.getStatesSet());
        Set<State> satConstraint = null;

        // check if the constraint is null
        if (constraint != null){
            satConstraint = satChecker.getSat(constraint, allStates);
            Set<State> initialStates = new HashSet<State>(model.getInitialSet());
            initialStates.retainAll(satConstraint);

            if (initialStates.isEmpty()) {
                System.out.println("The model trivially satisfies the formula as there are no states that satisfy the constraint.");
                return true;
            }
        }

        // If the model satisfies the constraint but not the query, then 
        // print a counter example with a trace and return false.
        Set<State> sat = satChecker.getSat(query, allStates); 
        // Else, get the intersection of both and check if it contains the starting states. 

        if (satConstraint != null) {
            sat.retainAll(satConstraint);
        }

        if (!sat.containsAll(startingStates)) {
            System.out.println("The model does not satisfy the query given the constraint.");
            String[] trace = getTrace(model, satChecker, sat, query);
            printTrace(trace);     
            return false;
        }

        System.out.println("The model satisfies the query given the constraint.");
        return true; // return the result of satisfaction checking
    }

    @Override
    public String[] getTrace(Model model, SATChecker checker, Set<State> sat, StateFormula formula) {
        TraceGenerator generator = new TraceGenerator(model, checker);

        // use trace generator to get the counter example
        List<String> counterExamples = generator.getCounterExampleForTrace(sat, formula);

        return counterExamples.toArray(new String[counterExamples.size()]);
    }

    private void printTrace(String[] trace){
        System.out.println("Path Trace:");
        StringBuilder sb = new StringBuilder();
        String stateName = trace[0];
        sb.append(stateName);

        // use the for loop to iterate all states in the trace array
        for (int i = 1; i < trace.length; i++) {
            stateName = trace[i];
            sb.append("->");
            sb.append(stateName);
        }
        System.out.println(sb.toString());
    }
}
