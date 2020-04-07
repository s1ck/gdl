package org.s1ck.gdl.model.comparables.time;

/**
 * Represents a MAX(p1,...,pn) term, where p1...pn are TimePoints
 */
public class MaxTimePoint extends TimeTerm{

    /**
     * Creates a MAX(args[0],...,args[args.length-1]) term
     * @param args the arguments for the MAX term (TimePoints)
     */
    public MaxTimePoint(TimePoint...args){
        super(args);
        operator = "MAX";
    }




    @Override
    public long evaluate(){
        long mn = Long.MIN_VALUE;
        for (TimePoint p:args){
            long eval = p.evaluate();
            if(eval==-1){
                return -1;
            }
            if(eval > mn){
                mn = eval;
            }
        }
        return mn;
    }

    @Override
    public long getLowerBound(){
        // lower bound of a max term is the maximal lower bound of all its arguments
        long res = Long.MIN_VALUE;
        for (TimePoint p: args){
            long val = p.getLowerBound();
            if(val>-1 && val>res){
                res = val;
            }
        }
        return res;
    }

    @Override
    public long getUpperBound(){
        // upper bound of a max term is the maximal upper bound of all its arguments
        long res = Long.MIN_VALUE;
        for (TimePoint p: args){
            long val = p.getUpperBound();
            if(val==Long.MAX_VALUE){
                return Long.MAX_VALUE;
            }
            if(val > res){
                res = val;
            }
        }
        return res;
    }

}
