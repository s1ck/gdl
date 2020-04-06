package org.s1ck.gdl.model.comparables.time;

import java.util.ArrayList;

/**
 * Represents a fixed number of milliseconds
 * Not really a timestamp, but needed for certain related computations (e.g. deltas)
 */
public class TimeConstant extends TimePoint{

    /**
     * The number of miliseconds wrapped by this class
     */
    private long millis;


    public TimeConstant(long absoluteMillis){
        if(absoluteMillis<0){
            throw new IllegalArgumentException("expected positive value");
        }
        this.millis = absoluteMillis;
    }

    /**
     * Create a constant of size days+hours+minutes+seconds+millis
     * @param days number of days
     * @param hours number of hours [0-23]
     * @param minutes number of minutes [0-59]
     * @param seconds number of seconds [0-59]
     * @param millis number of millis [0-999]
     */
    public TimeConstant(int days, int hours, int minutes, int seconds, int millis){
        if( (hours<0 || hours>23) || (minutes<0 || minutes>59) || (seconds<0||seconds>59) || (millis<0 || millis>999)){
            throw new IllegalArgumentException("not a valid timestamp");
        }
        this.millis = millis;
        this.millis+=1000*seconds;
        this.millis+=1000*60*minutes;
        this.millis+=1000*60*60*hours;
        this.millis+=1000*60*60*24*days;
    }

    /**
     * Return the wrapped number of milliseconds
     * @return number of milliseconds
     */
    public long getMillis(){
        return millis;
    }

    @Override
    public ArrayList<String> getVariables(){
        return new ArrayList<>();
    }

    @Override
    public long evaluate(){
        return millis;
    }

    @Override
    public long getLowerBound(){
        return millis;
    }

    @Override
    public long getUpperBound(){
        return millis;
    }

}
