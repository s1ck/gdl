package org.s1ck.gdl.model.comparables.time;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Base class for more abstract TimestampExpressions that may combine several timestamps,
 * e.g. MIN(p1,p2) is a timestamp, while p1 and p2 are timestamps, too
 * Allows to build complex functions of timestamps that yield timestamps
 */
public abstract class TimeTerm extends TimePoint {

    /**
     * Operator name (e.g. "MIN", "MAX",...)
     */
    private final static String operator="";

    /**
     * List of arguments (i.g. more than one)
     */
    ArrayList<TimePoint> args;

    /**
     * Initialize a complex expression by its arguments (TimePoints)
     * @param args the arguments
     */
    protected TimeTerm(TimePoint...args){
        this.args = new ArrayList<>();
        Collections.addAll(this.args, args);
    }

    /**
     * Get the operator name
     * @return operator name
     */
    public String getOperator() {
        return operator;
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
    public ArrayList<String> getVariables(){
        ArrayList<String> vars = new ArrayList<>();
        for (TimePoint tp: args){
            vars.addAll(tp.getVariables());
        }
        return vars;
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
