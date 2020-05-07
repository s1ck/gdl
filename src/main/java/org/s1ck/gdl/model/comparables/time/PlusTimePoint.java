package org.s1ck.gdl.model.comparables.time;

import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.model.comparables.time.util.TimeConstant;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.s1ck.gdl.model.comparables.time.TimeSelector.GLOBAL_SELECTOR;
import static org.s1ck.gdl.utils.Comparator.*;
import static org.s1ck.gdl.utils.Comparator.GTE;

/**
 * Represents an addition of a constant to a given TimePoint
 */
public class PlusTimePoint extends TimeAtom{

    /**
     * The wrapped TimePoint
     */
    private TimePoint timePoint;

    /**
     * The constant to be added to the wrapped TimePoint
     */
    private TimeConstant constant;

    /**
     * Initializes a Sum of a TimePoint and a constant
     * @param timePoint the TimePoint
     * @param constant the constant to be added to the TimePoint
     */
    public PlusTimePoint(TimePoint timePoint, TimeConstant constant){
        this.timePoint = timePoint;
        this.constant = constant;
    }

    @Override
    public Set<String> getVariables() {
        return timePoint.getVariables();
    }

    @Override
    public String getVariable() {
        return timePoint.getVariable();
    }

    @Override
    public long evaluate(){
        long tp = timePoint.evaluate();
        if(tp==UNDEFINED){
            return UNDEFINED;
        }
        return tp + constant.getMillis();
    }

    @Override
    public long getLowerBound(){
        long tp_lb = timePoint.getLowerBound();
        if(tp_lb == UNDEFINED){
            return constant.getMillis();
        }
        return tp_lb + constant.getMillis();
    }

    @Override
    public long getUpperBound(){
        long tp_ub = timePoint.getUpperBound();
        if(tp_ub == Long.MAX_VALUE){
            return Long.MAX_VALUE;
        }
        return tp_ub + constant.getMillis();
    }

    @Override
    public boolean containsSelectorType(TimeSelector.TimeField type){
        return timePoint.containsSelectorType(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlusTimePoint that = (PlusTimePoint) o;

        return timePoint.equals(that.timePoint) && constant.equals(that.constant);

    }

    @Override
    public String toString(){
        return "Plus("+timePoint+", "+constant+")";
    }


    @Override
    public Predicate unfoldGlobal(Comparator comp, ComparableExpression rhs, List<String> variables) {
        if(!(timePoint instanceof TimeSelector) || !timePoint.getVariable().equals(GLOBAL_SELECTOR)){
            return new Comparison(this, comp, rhs);
        }
        if(comp.equals(EQ)){
            return unfoldGlobalEQ(rhs, variables);
        }
        else if(comp.equals(Comparator.NEQ)){
            return unfoldGlobalNEQ(rhs, variables);
        }
        else if(comp.equals(Comparator.LT)){
            return unfoldGlobalLT(rhs, variables);
        }
        else if(comp.equals(Comparator.LTE)){
            return unfoldGlobalLTE(rhs, variables);
        }
        else if(comp.equals(Comparator.GT)){
            return unfoldGlobalGT(rhs, variables);
        }
        else if(comp.equals(Comparator.GTE)){
            return unfoldGlobalGTE(rhs, variables);
        }
        return null;
    }

    /**
     * Translates a comparison {@code (this == rhs)} into an equivalent predicate that does not contain
     * global time selectors/intervals anymore.
     * Basically the same method as in {@link TimeSelector}, but adapted for {@link PlusTimePoint}.
     * @param rhs the right hand side of the comparison to translate
     * @param variables all query variables
     * @return translated comparison
     */
    private Predicate unfoldGlobalEQ(ComparableExpression rhs, List<String> variables){
        // exists var: var.from==rhs
        Predicate exists = existsVariable(EQ, rhs, variables);

        TimeSelector.TimeField timeProp = ((TimeSelector)timePoint).getTimeProp();

        if(timeProp.equals(TimeSelector.TimeField.TX_FROM) || timeProp.equals(TimeSelector.TimeField.VAL_FROM)){
            //globalfrom==rhs <=> (exists var: var.from==rhs) AND (forall var: var.from<=rhs)
            return new And(exists,forAllVariables(LTE, rhs, variables));
        }

        else{
            //globalto == rhs <=> (exists var: var.to ==rhs) AND (forall var: var.to>=rhs)
            return new And(exists,forAllVariables(GTE, rhs, variables));
        }
    }

    /**
     * Translates a comparison {@code (this != rhs)} into an equivalent predicate that does not contain
     * global time selectors/intervals anymore.
     * Basically the same method as in {@link TimeSelector}, but adapted for {@link PlusTimePoint}
     * @param rhs the right hand side of the comparison to translate
     * @param variables all query variables
     * @return translated comparison
     */
    private Predicate unfoldGlobalNEQ(ComparableExpression rhs, List<String> variables){
        return forAllVariables(NEQ, rhs, variables);
    }

    /**
     * Translates a comparison {@code (this < rhs)} into an equivalent predicate that does not contain
     * global time selectors/intervals anymore.
     * Basically the same method as in {@link TimeSelector}, but adapted for {@link PlusTimePoint}
     * @param rhs the right hand side of the comparison to translate
     * @param variables all query variables
     * @return translated comparison
     */
    private Predicate unfoldGlobalLT(ComparableExpression rhs, List<String> variables){
        TimeSelector.TimeField timeProp = ((TimeSelector)timePoint).getTimeProp();
        if(timeProp.equals(TimeSelector.TimeField.TX_FROM) || timeProp.equals(TimeSelector.TimeField.VAL_FROM)){
            // globalfrom < rhs   <=>   forall var: var.from < rhs
            return forAllVariables(LT, rhs, variables);
        }
        else{
            //globalto < rhs   <=>   exists var: var.to < rhs
            return existsVariable(LT, rhs, variables);
        }
    }

    /**
     * Translates a comparison {@code (this <= rhs)} into an equivalent predicate that does not contain
     * global time selectors/intervals anymore.
     * Basically the same method as in {@link TimeSelector}, but adapted for {@link PlusTimePoint}
     * @param rhs the right hand side of the comparison to translate
     * @param variables all query variables
     * @return translated comparison
     */
    private Predicate unfoldGlobalLTE(ComparableExpression rhs, List<String> variables){
        TimeSelector.TimeField timeProp = ((TimeSelector)timePoint).getTimeProp();
        if(timeProp.equals(TimeSelector.TimeField.TX_FROM) || timeProp.equals(TimeSelector.TimeField.VAL_FROM)){
            // globalfrom <= rhs   <=>   forall var: var.from <= rhs
            return forAllVariables(LTE, rhs, variables);
        }
        else{
            //globalto <= rhs   <=>   exists var: var.to <= rhs
            return existsVariable(LTE, rhs, variables);
        }
    }

    /**
     * Translates a comparison {@code (this > rhs)} into an equivalent predicate that does not contain
     * global time selectors/intervals anymore.
     * Basically the same method as in {@link TimeSelector}, but adapted for {@link PlusTimePoint}
     * @param rhs the right hand side of the comparison to translate
     * @param variables all query variables
     * @return translated comparison
     */
    private Predicate unfoldGlobalGT(ComparableExpression rhs, List<String> variables){
        TimeSelector.TimeField timeProp = ((TimeSelector)timePoint).getTimeProp();
        if(timeProp.equals(TimeSelector.TimeField.TX_FROM) || timeProp.equals(TimeSelector.TimeField.VAL_FROM)){
            // globalfrom > rhs   <=>   exists var: var.from > rhs
            return existsVariable(GT, rhs, variables);
        }
        else{
            //globalto > rhs   <=>   forall var: var.to > rhs
            return forAllVariables(GT, rhs, variables);
        }
    }

    /**
     * Translates a comparison {@code (this >= rhs)} into an equivalent predicate that does not contain
     * global time selectors/intervals anymore.
     * Basically the same method as in {@link TimeSelector}, but adapted for {@link PlusTimePoint}
     * @param rhs the right hand side of the comparison to translate
     * @param variables all query variables
     * @return translated comparison
     */
    private Predicate unfoldGlobalGTE(ComparableExpression rhs, List<String> variables){
        TimeSelector.TimeField timeProp = ((TimeSelector)timePoint).getTimeProp();
        if(timeProp.equals(TimeSelector.TimeField.TX_FROM) || timeProp.equals(TimeSelector.TimeField.VAL_FROM)){
            // globalfrom >= rhs   <=>   exists var: var.from >= rhs
            return existsVariable(GTE, rhs, variables);
        }
        else{
            //globalto >= rhs   <=>   forall var: var.to >= rhs
            return forAllVariables(GTE, rhs, variables);
        }
    }

    /**
     * Returns a predicate equivalent to {@code exists v in variables s.t. (v comp rhs) holds}
     * Basically the same method as in {@link TimeSelector}, but adapted for {@link PlusTimePoint}
     * @param comp the comparator
     * @param rhs the rhs in the comparison
     * @param variables the query variables to "iterate" over (the domain)
     * @return predicate equivalent to {@code exists v in variables s.t. (v comp rhs) holds}
     */
    private Predicate existsVariable(Comparator comp, ComparableExpression rhs, List<String> variables){
        TimeSelector.TimeField timeProp = ((TimeSelector)timePoint).getTimeProp();
        Comparison c0 = new Comparison(
                new PlusTimePoint(new TimeSelector(variables.get(0),timeProp), constant), comp, rhs);
        if(variables.size()==1){
            return c0;
        }
        Or exists = new Or(c0, new Comparison(
                new PlusTimePoint(new TimeSelector(variables.get(1),timeProp), constant), comp, rhs));
        for(int i=2; i<variables.size(); i++){
            exists = new Or(exists, new Comparison(
                    new PlusTimePoint(new TimeSelector(variables.get(i),timeProp), constant), comp, rhs));
        }
        return exists;
    }

    /**
     * Returns a predicate equivalent to {@code forall v in variables: (v comp rhs) holds}
     * Basically the same method as in {@link TimeSelector}, but adapted for {@link PlusTimePoint}
     * @param comp the comparator
     * @param rhs the rhs in the comparison
     * @param variables the query variables to "iterate" over (the domain)
     * @return predicate equivalent to {@code forall v in variables: (v comp rhs) holds}
     */
    private Predicate forAllVariables(Comparator comp, ComparableExpression rhs, List<String> variables){
        TimeSelector.TimeField timeProp = ((TimeSelector)timePoint).getTimeProp();
        Comparison c0 = new Comparison(
                new PlusTimePoint(new TimeSelector(variables.get(0),timeProp), constant), comp, rhs);
        if(variables.size()==1){
            return c0;
        }

        And forall = new And(c0, new Comparison(
                new PlusTimePoint(new TimeSelector(variables.get(1),timeProp), constant), comp, rhs));
        for(int i=2; i<variables.size(); i++){
            forall = new And(forall,
                    new Comparison(
                            new PlusTimePoint(new TimeSelector(variables.get(i),timeProp), constant),
                            comp,rhs));
        }

        return forall;
    }

    @Override
    public boolean isGlobal(){
        return timePoint.isGlobal();
    }

    @Override
    public ComparableExpression replaceGlobalByLocal(List<String> variables) {
        return new PlusTimePoint((TimePoint)timePoint.replaceGlobalByLocal(variables), constant);
    }
}
