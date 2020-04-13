package org.s1ck.gdl.model.comparables.time;

import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import java.util.ArrayList;

/**
 * Represents a timestamp selection of a graph variable, e.g. v.VAL_FROM selects the VAL_FROM value of a graph element v
 */
public class TimeSelector extends TimeAtom{

    /**
     * The variable name
     */
    private String variable;

    /**
     * The time property selected (VAL_FROM, VAL_TO, TX_FROM, TX_TO)
     */
    private TimeField timeProp;

    /**
     *
     * All time properties defined by TPGM
     */
    public enum TimeField{
        VAL_FROM,
        VAL_TO,
        TX_FROM,
        TX_TO
    }

    /**
     * Initializes a TimeSelector given a variable and a time property (VAL_FROM, VAL_TO, TX_FROM, TX_TO)
     * @param variable the variable name
     * @param field the time property as defined by the TPGM
     */
    public TimeSelector(String variable, TimeField field){
        this.variable =variable;
        timeProp = field;
    }

    /**
     * Initializes a TimeSelector given a variable and the string representation of a  time property
     * (VAL_FROM, VAL_TO, TX_FROM, TX_TO)
     * @param variable the variable name
     * @param field the time property as defined by the TPGM. Must be one of "val_from", "val_to", "tx_from", "tx_to"
     *              (cases irrelevant)
     */
    public TimeSelector(String variable, String field){
        this(variable, stringToField(field));
    }

    @Override
    public String getVariable() {
        return variable;
    }

    @Override
    public ArrayList<String> getVariables(){
        ArrayList<String> ls = new ArrayList<>();
        ls.add(variable);
        return ls;
    }

    /**
     * Returns the TimeField (VAL_FROM, VAL_TO, TX_FROM, TX_TO)
     * @return the TimeField
     */
    public TimeField getTimeProp(){
        return timeProp;
    }

    @Override
    public long evaluate(){
        return UNDEFINED;
    }

    @Override
    public long getLowerBound(){
        return 0;
    }

    @Override
    public long getUpperBound(){
        return Long.MAX_VALUE;
    }

    @Override
    protected Predicate unfoldEQ(TimePoint arg){
        return new Comparison(this, Comparator.EQ, arg);
    }

    @Override
    protected Predicate unfoldNEQ(TimePoint arg){
        return new Comparison(this, Comparator.NEQ, arg);
    }

    @Override
    protected Predicate unfoldGT(TimePoint arg){
        return new Comparison(this, Comparator.GT, arg);
    }

    @Override
    protected Predicate unfoldGTE(TimePoint arg){
        return new Comparison(this, Comparator.GTE, arg);
    }

    @Override
    protected Predicate unfoldLT(TimePoint arg){
        return new Comparison(this, Comparator.LT, arg);
    }

    @Override
    protected Predicate unfoldLTE(TimePoint arg){
        return new Comparison(this, Comparator.LTE, arg);
    }

    /**
     * Parses a string to a TimeField
     * @param field a string equal to "tx_from", "tx_to", "val_from", "val_to" (cases irrelevant)
     * @return the corresponding TimeField
     */
    private static TimeField stringToField(String field){
        field = field.trim().toLowerCase();
        TimeField time = null;
        if (field.equals("val_from")){
            time = TimeField.VAL_FROM;
        }
        else if (field.equals("val_to")){
            time = TimeField.VAL_TO;
        }
        else if (field.equals("tx_from")){
            time = TimeField.TX_FROM;
        }
        else if (field.equals("tx_to")){
            time = TimeField.TX_TO;
        }
        return time;
    }

    @Override
    public String toString() {
        return variable + "." + timeProp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeSelector that = (TimeSelector) o;

        if (variable != null ? !variable.equals(that.variable) : that.getVariable() != null) return false;
        return timeProp != null ? timeProp.equals(that.timeProp) : that.timeProp == null;

    }

    @Override
    public int hashCode() {
        int result = variable != null ? variable.hashCode() : 0;
        result = 31 * result + (timeProp != null ? timeProp.hashCode() : 0);
        return result;
    }
}
