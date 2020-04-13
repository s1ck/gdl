package org.s1ck.gdl.model.comparables.time.util;

/**
 * Represents a constant duration via a fixed number of milliseconds
 * Not really a timestamp, but needed for certain related computations (e.g. deltas)
 */
public class TimeConstant{

    /**
     * The number of milliseconds wrapped by this class
     */
    private long millis;


    /**
     * Create a constant of size days+hours+minutes+seconds+millis (in millis)
     * @param days number of days
     * @param hours number of hours [0-23]
     * @param minutes number of minutes [0-59]
     * @param seconds number of seconds [0-59]
     * @param millis number of millis [0-999]
     */
    public TimeConstant(int days, int hours, int minutes, int seconds, int millis){
        if( (hours<0 || hours>23) || (minutes<0 || minutes>59) || (seconds<0||seconds>59) || (millis <0 || millis >999)){
            throw new IllegalArgumentException("not a valid timestamp");
        }
        long sum = millis;
        sum +=1000*seconds;
        sum +=1000*60*minutes;
        sum +=1000*60*60*hours;
        sum +=1000*60*60*24*days;
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

}
