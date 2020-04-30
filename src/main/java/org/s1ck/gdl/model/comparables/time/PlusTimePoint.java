package org.s1ck.gdl.model.comparables.time;

import org.s1ck.gdl.model.comparables.time.util.TimeConstant;

import java.util.ArrayList;

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
    public ArrayList<String> getVariables() {
        return timePoint.getVariables();
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


}
