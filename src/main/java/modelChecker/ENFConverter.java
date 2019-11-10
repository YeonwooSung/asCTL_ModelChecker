package modelChecker;

import java.util.HashSet;

import formula.pathFormula.Always;
import formula.pathFormula.Eventually;
import formula.pathFormula.Next;
import formula.pathFormula.Until;
import formula.stateFormula.And;
import formula.stateFormula.AtomicProp;
import formula.stateFormula.BoolProp;
import formula.stateFormula.ForAll;
import formula.stateFormula.Not;
import formula.stateFormula.Or;
import formula.stateFormula.StateFormula;
import formula.stateFormula.ThereExists;


/**
 * This class converts the given CTL formula to the StateFormula
 *
 */
public class ENFConverter {

    /**
     * Returns the ENF of the given asCTL formulae.
     * ENF is an Existential Normal Form, which replaces 
     * the universial quantifiers (A) to existential quatifiers (E).
     *
     * @param formula the original formula
     * @return The generated ENF formula
     */
    public StateFormula convertToENF(StateFormula formula) {
        /* Use "instanceof" to check the type of instances */
        if (formula instanceof AtomicProp) {
            return formula;
        } else if (formula instanceof BoolProp) {
            return convertToENF((BoolProp) formula);
        } else if (formula instanceof Not) {
            return convertToENF((Not) formula);
        } else if (formula instanceof And) {
            return convertToENF((And) formula);
        } else if (formula instanceof Or) {
            return convertToENF((Or) formula);
        } else if (formula instanceof ThereExists) {
            return convertToENF((ThereExists) formula);
        } else if (formula instanceof ForAll) {
            return convertToENF((ForAll) formula);
        }
        return null;
    }

    /**
     * ENF for Exists: 
     * Exists(P U Q) = Exists(ENF(P) U ENF(Q))
     * Exists(Next(P)) = Exists(Next(ENF(P)))
     * Exists(Always(P)) = Exists(Always(ENF(P)))
     * Exists(Eventually(P)) = Exists(true U ENF(P))
     * 
     * @param formula the original formula
     * @return The generated ENF formula
     */
    private StateFormula convertToENF(ThereExists formula) {
        // use "instanceof" to check the type of instance of the path formula
        if (formula.pathFormula instanceof Until) {
            // Exists(P U Q) = Exists(ENF(P) U ENF(Q))

            Until originalUntil = (Until) formula.pathFormula;
            StateFormula newLeft = convertToENF(originalUntil.left);
            StateFormula newRigth = convertToENF(originalUntil.right);

            Until newUntil = new Until(newLeft, newRigth, originalUntil.getLeftActions(), originalUntil.getRightActions());
            return new ThereExists(newUntil);

        } else if (formula.pathFormula instanceof Next) {
            // Exists(Next(P)) = Exists(Next(ENF(P)))

            Next originalNext = (Next) formula.pathFormula;
            return new ThereExists(new Next(convertToENF(originalNext.stateFormula), originalNext.getActions()));

        } else if (formula.pathFormula instanceof Always) {
            // Exists(Always(P)) = Exists(Always(ENF(P)))

            Always originalAlways = (Always) formula.pathFormula;
            return new ThereExists(new Always(convertToENF(originalAlways.stateFormula), originalAlways.getActions()));

        } else if (formula.pathFormula instanceof Eventually) {
            // Exists(Eventually(P)) = Exists(true U ENF(P))

            Eventually originalEventually = (Eventually) formula.pathFormula;
            Until until = new Until(new BoolProp(true), convertToENF(originalEventually.stateFormula), originalEventually.getLeftActions(), originalEventually.getRightActions());

            return new ThereExists(until);
        }
        return null;
    }

    /**
     * ENF for ForAll: 
     * ForAll(P U Q) = (not(Exists(not(ENF(Q)) U (not(ENF(P)) and not(ENF(Q))))) and (not(Exists(Always(not(ENF(Q)))))) 
     * ForAll(Next(P)) = not(Exists(Next(not(ENF(P)))))
     * ForAll(Always(P)) = not(Exists(true U ENF(P)))
     * ForAll(Eventually(P)) = not(Exists(Always(not(ENF(P)))))
     *
     * @param formula orignal formula
     * @return The generated ENF formula
     */
    private StateFormula convertToENF(ForAll formula) {

        if (formula.pathFormula instanceof Until) {
            // ForAll(P U Q) = (not(Exists(not(ENF(Q)) U (not(ENF(P)) and not(ENF(Q))))) and (not(Exists(Always(not(ENF(Q))))))

            Until originalUntil = (Until) formula.pathFormula;

            StateFormula enfL = convertToENF(originalUntil.left);
            StateFormula enfR = convertToENF(originalUntil.right);


            /* Left part of the final AND */

            // Until Right
            StateFormula untilRight = new And(new Not(enfL), new Not(enfR));
            // Until Left
            StateFormula untilLeft = new Not(enfR);

            Until rightUntil = new Until(untilLeft, untilRight, originalUntil.getLeftActions(), originalUntil.getRightActions());

            StateFormula leftAnd = new Not(new ThereExists(rightUntil));


            /* Left part of the final AND */
            StateFormula rightAnd = new Not(new ThereExists(new Always(new Not(enfR), originalUntil.getRightActions())));

            /* Final Formula */
            return new And(leftAnd, rightAnd);

        } else if (formula.pathFormula instanceof Next) {
            // ForAll(Next(P)) = not(Exists(Next(not(ENF(P)))))

            Next originalNext = (Next) formula.pathFormula;
            Next newNext = new Next(new Not(convertToENF(originalNext.stateFormula)), originalNext.getActions());
            return new Not(new ThereExists(newNext));

        } else if (formula.pathFormula instanceof Always) {
            // ForAll(Always(P)) = not(Exists(true U ENF(P)))

            Always originalAlways = (Always) formula.pathFormula;
            Until newUntil = new Until(new BoolProp(true), new Not(convertToENF(originalAlways.stateFormula)), new HashSet<String>(), originalAlways.getActions());
            return new Not(new ThereExists(newUntil));

        } else if (formula.pathFormula instanceof Eventually) {
            // ForAll(Eventually(P)) = not(Exists(Always(not(ENF(P)))))

            Eventually originalEventually = (Eventually) formula.pathFormula;
            Always newAlways = new Always(new Not(convertToENF(originalEventually.stateFormula)), originalEventually.getRightActions());
            return new Not(new ThereExists(newAlways));
        }
        return null;
    }

    /**
     * ENF for BooleanProp: true = true false = not(true)
     *
     * @param formula original formula
     * @return The generated ENF formula
     */
    private StateFormula convertToENF(BoolProp formula) {
        // Check the value of BoolProp
        if (formula.value) {
            return formula;
        }

        // If false, return the neg of true
        BoolProp trueProp = new BoolProp(true);
        Not not = new Not(trueProp);
        return not;
    }

    /**
     * ENF for Not: not(P) = not(ENF(P))
     *
     * @param formula original formula
     * @return The generated ENF formula
     */
    private StateFormula convertToENF(Not formula) {
        return new Not(convertToENF(formula.stateFormula));
    }

    /**
     * ENF for AND: P and Q = ENF(P) and ENF(Q)
     *
     * @param formula original formula
     * @return The generated ENF formula
     */
    private StateFormula convertToENF(And formula) {
        return new And(convertToENF(formula.left), convertToENF(formula.right));
    }

    /**
     * ENF for OR: P or Q = not( not(ENF(P)) AND not(ENF(Q)) )
     *
     * @param formula original formula
     * @return The generated ENF formula
     */
    private StateFormula convertToENF(Or formula) {
        StateFormula newLeft = new Not(convertToENF(formula.left));
        StateFormula newRight = new Not(convertToENF(formula.right));

        return new Not(new And(newLeft, newRight));
    }
}
