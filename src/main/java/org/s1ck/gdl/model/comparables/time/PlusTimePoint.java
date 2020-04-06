package org.s1ck.gdl.model.comparables.time;

/**
 * Represents a +(p1,...,pn) term, where p1...pn are TimePoints
 */
public class PlusTimePoint extends TimeTerm{

    /**
     * Operator name
     */
    private final static String operator = "PLUS";

    /**
     * Creates a +(args[0],...,args[args.length-1]) term
     * @param args the arguments for the + term (TimePoints)
     */
    public PlusTimePoint(TimePoint...args){
        super(args);
    }

    @Override
    public long evaluate(){
        long ret = 0;
        for (TimePoint p: args){
            long p_val = p.evaluate();
            if(p_val == -1){
                return -1;
            }
            ret +=p_val;
        }
        return ret;
    }

    @Override
    public long getLowerBound(){
        // lower bound of a sum of positive elements is the sum of all known elements
        long ret = 0;
        for (TimePoint p: args){
            ret += p.getLowerBound();
        }
        return ret;
    }

    @Override
    public long getUpperBound(){
        // upper bound of a sum of positive elements is either the sum of them (if all known)
        // or the maximum long value (if not all known)
        long ret = 0;
        for (TimePoint p: args){
            long up_p = p.getUpperBound();
            if(up_p == Long.MAX_VALUE){
                return Long.MAX_VALUE;
            }
            ret += up_p;
        }
        return ret;
    }


}
