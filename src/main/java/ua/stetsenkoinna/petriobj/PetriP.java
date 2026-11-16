package ua.stetsenkoinna.petriobj;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;


/**
 * This class for creating the place of Petri net.
 *
 *  @author Inna V. Stetsenko
 */
public class PetriP extends PetriMainElement implements Cloneable, Serializable {


    private final PetriElementId id;
    private String name;
    private int number;
    private int mark;
    private double mean;
    private static int next = 0;// лічильник об"єктів
    private int observedMax;
    private int observedMin;
    // arrived, but not yet pocessed
    private final Deque<Double>timesArrived=new ArrayDeque<>();
    // processed
    private Deque<Double>waitTimes=new ArrayDeque<>();
    // whether mark is a parameter
    private boolean markIsParam = false;
    // param name
    private String markParamName = null;

    /**
     *
     * @param n name of place
     * @param m quantity of markers
     */
    public PetriP(String n, int m) {
        id = PetriElementId.forPlace(n);
        name = n;
        number = next;
        next++;
        mark = m;
        mean = 0;
        observedMax = m;
        observedMin = m;
    }
    
     /**
     *
     * @param n - the name of place
     */
    public PetriP(String n) { //changed by Inna 21.03.2018
        this(n, 0);
        
    }
 
     /**
     *
     * @param id unique number for saving in server
     * @param n name of place
     * @param m quantity of markers
     */
    public PetriP(String id, String n, int m) {
        this.id = PetriElementId.fromString(id);
        name = n;
        number = next;
        next++;
        mark = m;
        mean = 0;
        observedMax = m;
        observedMin = m;
    }

    /**
     *
     * @param id unique number for saving in server
     * @param n - the name of place
     */
    public PetriP(String id, String n) {
        this(id, n, 0);
        
    }

    public PetriP(PetriP position) {
        this(position.getName(), position.getMark());
        number = next;
        next++;
    }

    public boolean markIsParam() {
        return markIsParam;
    }
    
    public String getMarkParamName() {
        return markParamName;
    }
    
    public void setMarkParam(String paramName) {
        if (paramName == null) {
            markIsParam = false;
            markParamName = null;
        } else {
            markIsParam = true;
            markParamName = paramName;
            mark = 0;
        }
    }

    /**
     * Set the counter of places to zero.
     */
    public static void initNext(){ //ініціалізація лічильника нульовим значенням

        next = 0;
    }

    /**
     * Raises the counter to {@code floor} if it currently sits lower — never lowers it.
     * Used to keep newly-built places from reusing numbers already on a canvas they're
     * about to join, without disturbing numbers already handed out.
     */
    public static void ensureNextAtLeast(int floor) {
        if (next < floor) {
            next = floor;
        }
    }

    /**
     * /**
     * Recalculates the mean value
     *
     * @param a value for recalculate of mean value (value equals product of
     * marking and time divided by time modeling)
     */
    public void changeMean(double a) {
        mean = mean + (mark - mean) * a;
    }

    /**
     *
     * @return mean value of quantity of markers
     */
    public double getMean() {
        return mean;
    }

    //NEW:
    public double getMeanWaitTime(){
        Double meanWaitTime=0.0;
        int N=waitTimes.size();
        for(Double waitTime: waitTimes){
            meanWaitTime+= waitTime/N;
        }
        return meanWaitTime;
    }

    public double getMinWaitTime(){
        Double minWaitTime=Double.MAX_VALUE;
        for(Double waitTime: waitTimes){
            if(waitTime<minWaitTime){
                minWaitTime=waitTime;
            }
        }
        return minWaitTime;
    }

    public double getMaxWaitTime(){
        Double maxWaitTime=Double.MIN_VALUE;
        for(Double waitTime: waitTimes){
            if(waitTime>maxWaitTime){
                maxWaitTime=waitTime;
            }
        }
        return maxWaitTime;
    }

    //NEW:
    public double getSTDWaitTime(){
        int N=waitTimes.size();
        Double meanWaitTime=getMeanWaitTime();
        Double stdWaitTime=0.0;
        for(Double waitTime: waitTimes){
            stdWaitTime+= Math.pow(waitTime-meanWaitTime,2)/N;
        }
        return Math.sqrt(stdWaitTime);
    }
    public Deque<Double> getWaitTimes() {
        return waitTimes;
    }

    /**
     *
     * @param a value on which increase the quantity of markers
     */
    public void increaseMark(int a) {
        mark += a;
        if (observedMax < mark) {
            observedMax = mark;
        }
        if (observedMin > mark) {
            observedMin = mark;
        }
        
    }

    /**
     *
     * @param a value on which decrease the quantity of markers
     */
    public void decreaseMark(int a) {
        mark -= a;
        if (observedMax < mark) {
            observedMax = mark;
        }
        if (observedMin > mark) {
            observedMin = mark;
        }
    }

    /**
     *
     * @return current quantity of markers
     */
    public int getMark() {
        return mark;
    }

    /**
     * Set quantity of markers
     *
     * @param a quantity of markers
     */
    public void setMark(int a) {
        mark = a;
        if (observedMax < mark) {
            observedMax = mark;
        }
        if (observedMin > mark) {
            observedMin = mark;
        }
    }

    public void addToQueue(double time, int num) {
        for (int i = 0; i < num; i++) {
            timesArrived.add(time);
        }

    }
    public void removeFromQueue(double time, int num) {
        for (int i = 0; i < num; i++) {
            double arrivedTime = timesArrived.remove();
            waitTimes.add(time - arrivedTime);
        }
    }

    public int getObservedMax() {
        return observedMax;
    }

    public int getObservedMin() {
        return observedMin;
    }

    /**
     *
     * @return name of the place
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param s - the new name of place
     */
    public void setName(String s) {
        name = s;
    }

    /**
     *
     * @return number of the place
     */
    public int getNumber() {
        return number;
    }

    /**
     *
     * @param n - the new number of place
     */
    public void setNumber(int n) {
        number = n;
    }

    public void printParameters() {
        System.out.println("Place " + name + "has such parametrs: \n"
                + " number " + number + ", mark " + mark);
    }

    /**
     * @return the id
     */
    public String getId() {
        return id != null ? id.getValue() : null;
    }

    /**
     * @return the id wrapper
     */
    public PetriElementId getIdWrapper() {
        return id;
    }

}
