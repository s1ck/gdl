package org.s1ck.gdl.model.comparables.time;

import org.s1ck.gdl.model.comparables.ComparableExpression;
import org.s1ck.gdl.model.predicates.Predicate;
import org.s1ck.gdl.model.predicates.booleans.And;
import org.s1ck.gdl.model.predicates.booleans.Not;
import org.s1ck.gdl.model.predicates.booleans.Or;
import org.s1ck.gdl.model.predicates.expressions.Comparison;
import org.s1ck.gdl.utils.Comparator;

import java.util.ArrayList;
import java.util.List;

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
            if (eval==UNDEFINED){
                return UNDEFINED;
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
            if(val!=UNDEFINED && val < res){
                res = val;
            }
        }
        return res;
    }

    @Override
    public String getVariable() {
        return null;
    }

    @Override
    public boolean containsSelectorType(TimeSelector.TimeField type){
        for(TimePoint p: args){
            if(p.containsSelectorType(type)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected Predicate unfoldEQ(TimePoint arg){
        //MIN(p1...pn) == x    <=>        exists pi: (pi<=p1 AND pi<=p2...AND pi<=pn) AND (pi==x)

        // the predicates (pi<=p1 AND pi<=p2...AND pi<=pn) AND (pi==x)
        Predicate[] piPredicates = new Predicate[args.size()];
        for(int i= 0; i<args.size(); i++){
            // list of (pi<=p1), (pi<=p2),..., (pi<=pn)
            ArrayList<Comparison> comparisons = new ArrayList<>();
            for(int j=0; j<args.size(); j++){
                if(j==i){
                    continue;
                }
                comparisons.add(new Comparison(args.get(i), Comparator.LTE, args.get(j)));
            }
            // conjoin (pi==x) with (pi<=p1 AND pi<=p2...AND pi<=pn)
            And conj = new And(new Comparison(args.get(i), Comparator.EQ, arg),
                    comparisons.get(0));
            for(int k=1; k<comparisons.size(); k++){
                conj = new And(conj, comparisons.get(k));
            }
            piPredicates[i]=conj;
        }
        // disjunction over all ((pi<=p1 AND pi<=p2...AND pi<=pn) AND (pi==x))
        Or result = new Or(piPredicates[0], piPredicates[1]);
        for(int l=2; l<piPredicates.length; l++){
            result = new Or(result, piPredicates[l]);
        }
        return result;
    }

    @Override
    protected Predicate unfoldNEQ(TimePoint arg){
        // could be implemented as
        //return new Not(unfoldEQ(arg));
        // not done for simplicity (Nots will be eliminated for temporal predicates later)

        //MIN(p1...pn) != x    <=>        for all pi: (pi>p1 OR pi>p2...OR pi>pn) OR (pi!=x)

        // the predicates (pi>p1 OR pi>p2...OR pi>pn) OR (pi!=x)
        Predicate[] piPredicates = new Predicate[args.size()];
        for(int i= 0; i<args.size(); i++){
            // list of (pi>p1), (pi>p2),..., (pi>pn)
            ArrayList<Comparison> comparisons = new ArrayList<>();
            for(int j=0; j<args.size(); j++){
                if(j==i){
                    continue;
                }
                comparisons.add(new Comparison(args.get(i), Comparator.GT, args.get(j)));
            }
            // disjunction of (pi!=x) and (pi>p1 OR pi>p2...OR pi>pn)
            Or disj = new Or(new Comparison(args.get(i), Comparator.NEQ, arg),
                    comparisons.get(0));
            for(int k=1; k<comparisons.size(); k++){
                disj = new Or(disj, comparisons.get(k));
            }
            piPredicates[i]=disj;
        }
        // conjunction over all ((pi>p1 OR pi>p2...OR pi>pn) OR (pi!=x))
        And result = new And(piPredicates[0], piPredicates[1]);
        for(int l=2; l<piPredicates.length; l++){
            result = new And(result, piPredicates[l]);
        }
        return result;
    }

    @Override
    protected Predicate unfoldGT(TimePoint arg){
        //MIN(p1...pn) > x    <=>        p1>x AND p2>x...AND pn>x
        And conjGt = new And(new Comparison(args.get(0), Comparator.GT, arg),
                new Comparison(args.get(1), Comparator.GT, arg));
        for (int i=2; i<args.size(); i++){
            conjGt = new And(conjGt, new Comparison(args.get(i), Comparator.GT, arg));
        }
        return conjGt;
    }

    @Override
    protected Predicate unfoldGTE(TimePoint arg){
        //MIN(p1...pn) >= x    <=>        p1>=x AND p2>=x...AND pn>=x
        And conjGte = new And(new Comparison(args.get(0), Comparator.GTE, arg),
                new Comparison(args.get(1), Comparator.GTE, arg));
        for(int i=2; i<args.size(); i++){
            conjGte = new And(conjGte, new Comparison(args.get(i), Comparator.GTE, arg));
        }
        return conjGte;
    }

    @Override
    protected Predicate unfoldLT(TimePoint arg){
        //MIN(p1...pn) < x <=> p1<x OR p2<x ... OR pn<x
        Or disjLt = new Or(new Comparison(args.get(0), Comparator.LT, arg),
                new Comparison(args.get(1), Comparator.LT, arg));
        for(int i=2; i<args.size(); i++){
            disjLt = new Or(disjLt, new Comparison(args.get(i), Comparator.LT, arg));
        }
        return disjLt;
    }

    @Override
    protected Predicate unfoldLTE(TimePoint arg){
        //MIN(p1...pn) <= x <=> p1<=x OR p2<=x ... OR pn<=x
        Or disjLte = new Or(new Comparison(args.get(0), Comparator.LTE, arg),
                new Comparison(args.get(1), Comparator.LTE, arg));
        for(int i=2; i<args.size(); i++){
            disjLte = new Or(disjLte, new Comparison(args.get(i), Comparator.LTE, arg));
        }
        return disjLte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MinTimePoint that = (MinTimePoint) o;

        if(args.size()!=that.args.size()){
            return false;
        }

        for(TimePoint arg: args){
            boolean foundEq = false;
            for(TimePoint arg2: that.args){
                if(arg.equals(arg2)){
                    foundEq = true;
                    break;
                }
            }
            if(!foundEq){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isGlobal(){
        for(TimePoint arg: args){
            if(arg.isGlobal()){
                return true;
            }
        }
        return false;
    }

    @Override
    public ComparableExpression replaceGlobalByLocal(List<String> variables) {
        TimePoint[] newArgs = new TimePoint[args.size()];
        for(int i=0; i<args.size(); i++){
            newArgs[i] = (TimePoint) args.get(i).replaceGlobalByLocal(variables);
        }
        return new MinTimePoint(newArgs);
    }


}
