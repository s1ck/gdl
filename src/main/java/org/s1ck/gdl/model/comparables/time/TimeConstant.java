package org.s1ck.gdl.model.comparables.time;

import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.expressions.Comparison;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a constant duration via a fixed number of milliseconds
 * Not really a timestamp, but needed for certain related computations (e.g. deltas)
 */
public class TimeConstant extends TimePoint {

    /**
     * The number of milliseconds wrapped by this class
     */
    private Long millis;

    /**
     * Create a constant of size days+hours+minutes+seconds+millis (in millis)
     * @param days number of days
     * @param hours number of hours [0-23]
     * @param minutes number of minutes [0-59]
     * @param seconds number of seconds [0-59]
     * @param millis number of millis [0-999]
     */
    public TimeConstant(int days, int hours, int minutes, int seconds, int millis){
        long sum = (long)millis;
        sum +=1000L*(long)seconds;
        sum +=1000L*60L*(long)minutes;
        sum +=1000L*60L*60L*(long)hours;
        sum +=1000*60L*60L*24L*(long)days;
        this.millis = sum;
    }

    /**
     * Creates a constant from the given milliseconds
     * @param millis size of the constant in milliseconds
     */
    public TimeConstant(long millis){
        this.millis = millis;
    }

    /**
     * Return the wrapped number of milliseconds
     * @return number of milliseconds
     */
    public long getMillis(){
        return millis;
    }

    @Override
    public String toString(){
        return "Constant("+getMillis()+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeConstant that = (TimeConstant) o;
        return getMillis()==that.getMillis();
    }

    @Override
    public Optional<Long> evaluate() {
        return Optional.of(getMillis());
    }

    @Override
    public long getLowerBound() {
        return getMillis();
    }

    @Override
    public long getUpperBound() {
        return getMillis();
    }

    @Override
    protected Predicate unfoldEQ(TimePoint arg) {
        return null;
    }

    @Override
    protected Predicate unfoldNEQ(TimePoint arg) {
        return null;
    }

    @Override
    protected Predicate unfoldGT(TimePoint arg) {
        return null;
    }

    @Override
    protected Predicate unfoldGTE(TimePoint arg) {
        return null;
    }

    @Override
    protected Predicate unfoldLT(TimePoint arg) {
        return null;
    }

    @Override
    protected Predicate unfoldLTE(TimePoint arg) {
        return null;
    }

    @Override
    public Set<String> getVariables() {
        return new HashSet<>();
    }

    @Override
    public String getVariable() {
        return null;
    }

    @Override
    public boolean containsSelectorType(TimeSelector.TimeField type) {
        return false;
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    @Override
    public ComparableExpression replaceGlobalByLocal(List<String> variables) {
        return this;
    }

}
