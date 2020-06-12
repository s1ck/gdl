package org.s1ck.gdl.model.comparables.time;

import java.util.*;

/**
 * Base class for more abstract TimestampExpressions that may combine several timestamps,
 * e.g. MIN(p1,p2) is a timestamp, while p1 and p2 are timestamps, too
 * Allows to build complex functions of timestamps that yield timestamps
 */
public abstract class TimeTerm extends TimePoint {

    /**
     * List of arguments (i.g. more than one)
     */
    protected ArrayList<TimePoint> args;

    /**
     * String representation of the operator, e.g. "MIN", "MAX", ...
     */
    protected String operator = "";

    /**
     * Initialize a complex expression by its arguments (TimePoints)
     * @param args the arguments
     */
    protected TimeTerm(TimePoint...args){
        if(args.length < 2){
            throw new IllegalArgumentException("need at least 2 arguments");
        }
        this.args = new ArrayList<>();
        Collections.addAll(this.args, args);
    }

    /**
     * Get the arguments list
     * @return list of arguments
     */
    public ArrayList<TimePoint> getArgs(){
        return args;
    }

    /**
     * set the list of arguments
     * @param args the desired list of arguments (not empty)
     */
    public void setArgs(ArrayList<TimePoint> args){
        if(args.size()==0){
            throw new IllegalArgumentException("There must be at least one argument");
        }
        this.args = args;
    }

    @Override
    public Set<String> getVariables(){
        HashSet<String> vars = new HashSet<>();
        for (TimePoint tp: args){
            vars.addAll(tp.getVariables());
        }
        return vars;
    }

    /**
     * String representation of the operator (e.g. "MIN", "MAX",...)
     * @return operator string
     */
    public String getOperator(){
        return operator;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder(operator+"(");
        sb.append(args.get(0).toString());
        for (int i=1; i<args.size(); i++){
            sb.append(", ");
            sb.append(args.get(i).toString());
        }
        sb.append(")");
        return new String(sb);
    }
}
