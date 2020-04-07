package org.s1ck.gdl.model.comparables.time;

/**
 * Represents a MAX(p1,...,pn) term, where p1...pn are TimePoints
 */
public class MinTimePoint extends TimeTerm {

    /**
     * Creates a MIN(args[0],...,args[args.length-1]) term
     * @param args the arguments for the MIN term (TimePoints)
     */
    public MinTimePoint(TimePoint...args){
        super(args);
        operator = "MIN";
    }

    @Override
    public long evaluate(){
        long mn = Long.MAX_VALUE;
        for (TimePoint p:args){
            long eval = p.evaluate();
            if (eval==-1){
                return -1;
            }
            if (eval < mn){
                mn = eval;
            }
        }
        return mn;
    }

    @Override
    public long getLowerBound(){
        // lower bound of a min term is the minimum lower bound of all its arguments
        long res = Long.MAX_VALUE;
        for (TimePoint p: args){
            long val = p.getLowerBound();
            if(val==0){
                return 0;
            }
            if(val< res){
                res = val;
            }
        }
        return res;
    }

    @Override
    public long getUpperBound(){
        // upper bound of a min term is the minimum upper bound of all its arguments
        long res = Long.MAX_VALUE;
        for (TimePoint p: args){
            long val = p.getUpperBound();
            if(val>-1 && val < res){
                res = val;
            }
        }
        return res;
    }
}
