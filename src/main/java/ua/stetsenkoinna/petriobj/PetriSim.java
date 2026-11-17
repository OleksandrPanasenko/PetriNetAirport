package ua.stetsenkoinna.petriobj;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;


/**
 * This class is Petri simulator. <br>
 * The object of this class simulates dynamics of functioning according to Petri
 * net, given in his data field. Such object is named Petri-object.
 *
 *  @author Inna V. Stetsenko
 */
public class PetriSim implements Cloneable, Serializable {


    private static int next = 1; //лічильник створених об"єктів

    private final PetriNet net;
    private final ArrayList<PetriP> listPositionsForStatistica = new ArrayList<>();
    private final ArrayList<PetriP> listPositionsForTimeStatistica = new ArrayList<>();
    
    private final ArrayList<Double> listTimeForStatistica = new ArrayList<>();
    private final ArrayList<ArrayList<Double>> averageWaitTimesForPositions = new ArrayList<>();
    private final ArrayList<ArrayList<Double>> stdWaitTimesForPositions = new ArrayList<>();
    
    private final ArrayList<ArrayList<Double>> averageMarkersTimesForPositions = new ArrayList<>();


    private final PetriP[] listP;
    private final PetriT[] listT;

    private StateTime timeState;
    private String name;
    private int numObj; //поточний номер створюваного об"єкта
    private int priority;
    private ArcIn[] listIn;
    private ArcOut[] listOut;
    private String id; //unique number of object for server

    protected PetriT eventMin; // modifier is edited on protected for the subclass access
    protected double timeMin; // modifier is edited on protected for the subclass access
    protected double statisticsInterval = 0.0; // modifier is edited on protected for the subclass access
    protected double timeLastStatistics = 0.0; // modifier is edited on protected for the subclass access
    protected boolean doIntervalStatistics = false; // modifier is edited on protected for the subclass access
    /**
     * Transient because a deserialized Petri-object belongs to no model yet, and it is the
     * model — not the object — that decides who observes a firing.
     */
    private transient TransitionFiringListener firingListener;

    /**
     * Constructs the Petri simulator with given Petri net and time modeling
     *
     * @param net Petri net that describes the dynamics of object
     */
   public PetriSim(PetriNet net) {
        this(net, new StateTime());
    }
   
   public PetriSim(String id, PetriNet net, StateTime timeState) {
        this(net, new StateTime());
        this.id = id; // server set id
   }
    
    public PetriSim(PetriNet net, StateTime timeState) {
        this.net = net;
        this.timeState = timeState;
        name = net.getName();
        numObj = next; 
        next++;        
        timeMin = Double.MAX_VALUE;
            
        listP = net.getListP();
        listT = net.getListT();
        listIn = net.getArcIn();
        listOut = net.getArcOut();
        eventMin = this.getEventMin();
        priority = 0;
        
        id = null; // server set id
    }

    
    /**
     *
     * @return PetriNet
     */
    public PetriNet getNet() {
        return net;
    }

    /**
     *
     * @return name of Petri-object
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return list of places for statistics which use for statistics
     */
    public ArrayList<PetriP> getListPositionsForStatistica() {
        return listPositionsForStatistica;
    }
    public void addPositionForStatistica(PetriP position) {
        listPositionsForStatistica.add(position);
        averageMarkersTimesForPositions.add(new ArrayList<Double>());
    }
    public ArrayList<PetriP> getListPositionsForTimeStatistica() {
        return listPositionsForTimeStatistica;
    }
    public void addPositionForTimeStatistica(PetriP position) {
        listPositionsForTimeStatistica.add(position);
        averageWaitTimesForPositions.add(new ArrayList<Double>());
        stdWaitTimesForPositions.add(new ArrayList<Double>());
    }

    /**
     * Get priority of Petri-object
     *
     * @return value of priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     *
     * @return the number of object
     */
    public int getNumObj() {
        return numObj;
    }


    

    /**
     * Set priority of Petri-object
     *
     * @param a value of priority
     */
    public void setPriority(int a) {
        priority = a;
    }

    /**
     * This method uses for describing other actions associated with transition
     * markers output.<br>
     * Such as the output markers into the other Petri-object.<br>
     * The method is overridden in subclass.
     */
    public void doT() {

    }

    /**
     * Hook called just before a transition fires its input arc (tokens enter).
     * Override in subclass to add pre-firing behaviour (e.g. animation).
     */
    protected void beforeActIn(PetriT tr) {}

    /**
     * Hook called just after a transition fires its input arc (tokens enter).
     * Override in subclass to add post-firing behaviour (e.g. animation, delay).
     */
    protected void afterActIn(PetriT tr) {
        //if gather statistics for wait time in position, then calculate wait time for each place
        for (int i: tr.getInP()) {
            if (getListPositionsForTimeStatistica().contains(listP[i])) {
                listP[i].removeFromQueue(this.getCurrentTime(), tr.getInputQuantity(listP[i]));
            }
        }
    }

    /**
     * Hook called just before a transition fires its output arc (tokens leave).
     */
    protected void beforeActOut(PetriT tr) {}

    /**
     * Hook called just after a transition fires its output arc (tokens leave).
     */
    protected void afterActOut(PetriT tr) {
        //if gather statistics for wait time in position, then calculate incoming time for each place
        for (int i: tr.getOutP()) {
            if (getListPositionsForTimeStatistica().contains(listP[i])) {
                listP[i].addToQueue(this.getCurrentTime(), tr.getOutputQuantity(listP[i]));
            }
        }
    }

    /**
     * Returns true if the current simulation step should be interrupted.
     * Override to implement pause/halt support (e.g. in animated subclass).
     */
    protected boolean shouldInterrupt() {
        return false;
    }

    /**
     * Installs the observer of this object's firings, or {@code null} to remove it.
     *
     * <p>Called by {@link PetriObjModel}; a Petri-object never installs one on itself, which is
     * what keeps a clone — built by {@link #clone()} into a model that has no collector yet —
     * from inheriting an observer that belongs to another run. A headless or Swing run leaves
     * it {@code null} and pays one null check per firing.
     *
     * @param listener the observer of the four firing phases
     */
    public void setFiringListener(TransitionFiringListener listener) {
        this.firingListener = listener;
    }

    private void notifyPhase(FiringPhase phase, PetriT tr) {
        if (firingListener != null) {
            firingListener.onFiringPhase(phase, tr, this.getCurrentTime());
        }
    }

    /**
     * Runs the tokens-in half of a firing: reports it, lets a subclass animate around it, and
     * tells the caller whether the run may go on.
     *
     * <p>Each phase is reported before the corresponding hook, because a hook is where an
     * animating subclass sleeps or blocks on a pause monitor — an observer that only learned
     * of the phase afterwards would be reporting a state the simulation has already left.
     *
     * @return {@code false} when the caller must return from {@code input()} straight away
     */
    private boolean fireActIn(PetriT tr) {
        notifyPhase(FiringPhase.BEFORE_ACT_IN, tr);
        beforeActIn(tr);
        tr.actIn(listP, this.getCurrentTime());
        notifyPhase(FiringPhase.AFTER_ACT_IN, tr);
        afterActIn(tr);
        return !shouldInterrupt();
    }

    /**
     * Runs the tokens-out half of a firing, the same way {@link #fireActIn(PetriT)} runs the
     * other one.
     *
     * <p>{@link PetriT#actOut(PetriP[], double)} does nothing at all for a transition with an
     * empty buffer, so an empty one is not reported: an observer would otherwise see a pair of
     * phases across which neither the marking nor the buffer moved.
     *
     * @return {@code false} when the caller must return from {@code output()} straight away
     */
    private boolean fireActOut(PetriT tr) {
        boolean real = tr != null && tr.getBuffer() > 0;
        if (real) {
            notifyPhase(FiringPhase.BEFORE_ACT_OUT, tr);
        }
        beforeActOut(tr);
        tr.actOut(listP, this.getCurrentTime());
        if (real) {
            notifyPhase(FiringPhase.AFTER_ACT_OUT, tr);
        }
        afterActOut(tr);
        return !shouldInterrupt();
    }

    /**
     * Determines the next event and its moment.
     */
    public void eventMin() {
        PetriT event = null; //пошук часу найближчої події
        // якщо усі переходи порожні, то це означає зупинку імітації, 
        // отже за null значенням eventMin можна відслідковувати зупинку імітації
        double min = Double.MAX_VALUE;
        for (PetriT transition : listT) {
            if (transition.getMinTime() < min) {
                event = transition;
                min = transition.getMinTime();
            }
        }
        if(min>getSimulationTime()){
            min=getSimulationTime();
            event=null;
        }
        timeMin = min;
        eventMin = event;
    }

    /**
     *
     * @return moment of next event
     */
    public double getTimeMin() {
        return timeMin;
    }

    /**
     * Finds the set of transitions for which the firing condition is true and
     * sorts it on priority value
     *
     * @return the sorted list of transitions with the true firing condition
     */
    public ArrayList<PetriT> findActiveT() {
        ArrayList<PetriT> aT = new ArrayList<>();

        for (PetriT transition : listT) {
            if ((transition.condition(listP)) && (transition.getProbability() != 0)) {
                aT.add(transition);

            }
        }

        if (aT.size() > 1) {
            // сортування переходів за спаданням пріоритету
            aT.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));
        }
        return aT;
    }
       /**
     * @return the timeCurr
     */
    public double getCurrentTime() {
        return getTimeState().getCurrentTime();
    }

    /**
     * @param aTimeCurr the timeCurr to set
     */
    public void setTimeCurr(double aTimeCurr) {
        getTimeState().setCurrentTime(aTimeCurr);
    }
    /**
     * @return the timeMod
     */
    public double getSimulationTime() {
        return getTimeState().getSimulationTime();
    }

    /**
     * @param aTimeMod the timeMod to set
     */
    public void setSimulationTime(double aTimeMod) {
        getTimeState().setSimulationTime(aTimeMod);
       
    }
    //NEW: interval between statystics measurements
    public double getStatysticsInterval(){
        return statisticsInterval;
    }
    public void setStatysticsInterval(double interval){
        statisticsInterval = interval;
        doIntervalStatistics = true;
    }
    
    public static Comparator<PetriSim> getComparatorByPriority() {
        return (o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority());
    }
    
    public static Comparator<PetriSim> getComparatorByNum() {
        return Comparator.comparingInt(PetriSim::getNumObj);
    }

    

    /**
     * Do one event
     *
     * <p>This is the standalone single-object loop, reached only from {@link #go()}; a model
     * drives its objects through {@link #input()} and {@link #output()} instead. It fires
     * transitions directly and therefore reports no {@link FiringPhase} to a
     * {@link TransitionFiringListener}.
     */
    //NOT USED
    public void step() {
        // використовується для одного об'єкту мережа Петрі
        System.out.println("Next Step  time={}" + this.getCurrentTime());

        this.printMark(); //друкувати поточне маркування
        ArrayList<PetriT> activeT = this.findActiveT(); //формування списку активних переходів

        if ((activeT.isEmpty() && isBufferEmpty()) || this.getCurrentTime() >= getSimulationTime()) {
            //зупинка імітації за умови, що немає переходів, які запускаються, і немає маркерів у переходах, або вичерпаний час моделювання
            System.out.println("STOP in Net  {} "+this.getName());
            timeMin = getSimulationTime();
            for (PetriP p : listP) {
                p.changeMean((timeMin - this.getCurrentTime()) / getSimulationTime());
            }

            for (PetriT transition : listT) {
                transition.changeMean((timeMin - this.getCurrentTime()) / getSimulationTime());
            }
            setTimeCurr(timeMin);//просування часу
        } else {
            while (!activeT.isEmpty()) { //вхід маркерів в переходи доки можливо
                this.doConflikt(activeT).actIn(listP, this.getCurrentTime()); //розв'язання конфліктів
                activeT = this.findActiveT(); //оновлення списку активних переходів
            }

            this.eventMin();//знайти найближчу подію та ії час
            
            for (PetriP position : listP) {
                position.changeMean((timeMin - this.getCurrentTime()) / getSimulationTime());
            }

            for (PetriT transition : listT) {
                transition.changeMean((timeMin - this.getCurrentTime()) / getSimulationTime());
            }

            setTimeCurr(timeMin);         //просування часу

            if (this.getCurrentTime() <= getSimulationTime()) {

                //Вихід маркерів
                eventMin.actOut(listP, this.getCurrentTime() );//Вихід маркерів з переходу, що відповідає найближчому моменту часу

                if (eventMin.getBuffer() > 0) {
                    boolean u = true;
                    while (u) {
                        eventMin.minEvent();
                        if (eventMin.getMinTime() == this.getCurrentTime()) {
                            eventMin.actOut(listP, this.getCurrentTime());
                        } else {
                            u = false;
                        }
                    }
                }

                for (PetriT transition : listT) {
                    //ВАЖЛИВО!!Вихід з усіх переходів, що час виходу маркерів == поточний момент час.
                    if (transition.getBuffer() > 0 && transition.getMinTime() == this.getCurrentTime()) {
                        transition.actOut(listP,this.getCurrentTime());
                        //Вихід маркерів з переходу, що відповідає найближчому моменту часу
                        if (transition.getBuffer() > 0) {
                            boolean u = true;
                            while (u) {
                                transition.minEvent();
                                if (transition.getMinTime() == this.getCurrentTime()) {
                                    transition.actOut(listP, this.getCurrentTime());
                                } else {
                                    u = false;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
   
    /**
     * It does the transitions input markers
     */
    public void input() {
        ArrayList<PetriT> activeT = this.findActiveT();
        if (activeT.isEmpty() && isBufferEmpty()) {
            timeMin = Double.MAX_VALUE;
        } else {
            while (!activeT.isEmpty()) {
                PetriT tr = this.doConflikt(activeT);
                if (!fireActIn(tr)) return;
                activeT = this.findActiveT();
            }
            this.eventMin();
        }
    }
    
      /**
     * It does the transitions output markers
     */
    public void output() {
        if (this.getCurrentTime() <= this.getSimulationTime()) {
            if (!fireActOut(eventMin)) return;
            if (eventMin.getBuffer() > 0) {
                boolean u = true;
                while (u) {
                    eventMin.minEvent();
                    if (eventMin.getMinTime() == this.getCurrentTime()) {
                        if (!fireActOut(eventMin)) return;
                    } else {
                        u = false;
                    }
                }
            }
            for (PetriT transition : listT) {
                if (transition.getBuffer() > 0 && transition.getMinTime() == this.getCurrentTime()) {
                    if (!fireActOut(transition)) return;
                    if (transition.getBuffer() > 0) {
                        boolean u = true;
                        while (u) {
                            transition.minEvent();
                            if (transition.getMinTime() == this.getCurrentTime()) {
                                if (!fireActOut(transition)) return;
                            } else {
                                u = false;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * It does the transitions input and output markers in the current moment
     */
    public void stepEvent() {
        //один крок,вихід та вхід маркерів в переходи Петрі-об"єкта, використовується для множини Петрі-об'єктів
        if(isStop()){
            timeMin = Double.MAX_VALUE;
             return; //зупинка імітації
        }
        output();
        input();  
        //hybrid approach for better accuracy
        doStatistics();
        if(doIntervalStatistics && (timeMin>=timeLastStatistics+statisticsInterval)){
            doStatisticsInterval();
        }
        
        setTimeCurr(timeMin);
    }

    /**
     * Calculates mean value of quantity of markers in places and quantity of
     * active channels of transitions
     */
    public void doStatistics() {
        if(timeMin>getSimulationTime()){
            for (PetriP position : listPositionsForStatistica) {
                position.changeMean((getSimulationTime() - this.getCurrentTime()) / getSimulationTime());
            }    
        }
        else{
            for (PetriP position : listPositionsForStatistica) {
                position.changeMean((timeMin - this.getCurrentTime()) / timeMin);
            }    
        }
        
    }
    public void doStatisticsInterval() {
        timeLastStatistics+=statisticsInterval;
        listTimeForStatistica.add(timeLastStatistics);
        for (int p=0;p<getListPositionsForStatistica().size();p++) {
            PetriP position= listPositionsForStatistica.get(p);
            averageMarkersTimesForPositions.get(p).add(position.getMean());
        }
        for(int p=0;p<getListPositionsForTimeStatistica().size();p++) {
            PetriP position=getListPositionsForTimeStatistica().get(p);
            averageWaitTimesForPositions.get(p).add(position.getMeanWaitTime());
            stdWaitTimesForPositions.get(p).add(position.getSTDWaitTime());
        }
    }

    /**
     *
     * @param dt - the time interval
     */
    public void doStatistics(double dt) {
        if (dt > 0) {
            for (PetriP position : listPositionsForStatistica) {
                position.changeMean(dt);
            }
        }
        if (dt > 0) {
            for (PetriT transition : listT) {
                transition.changeMean(dt);
            }
        }
    }
    public ArrayList<ArrayList<Double>> getAverageWaitTimes() {
        return averageWaitTimesForPositions;
    }
    public ArrayList<ArrayList<Double>> getStdWaitTimes() {
        return stdWaitTimesForPositions;
    }
    public ArrayList<Double> getTimes(){
        return listTimeForStatistica;
    }

    /**
     * This method use for simulating Petri-object
     */
    public void go() {
        setTimeCurr(0);
        input();

        while (this.getCurrentTime() <= getSimulationTime() && !isStop()) {
            this.stepEvent();
            if (isStop()) {
                System.out.println("STOP in net  {} "+this.getName());
            }
        }
    }

    /**
     * This method use for simulating Petri-object until current time less then
     * the momemt in parametr time
     *
     * @param time - the simulation time
     */
    public void go(double time) {
        setSimulationTime(time);
        input();
        while (this.getCurrentTime() < time && !isStop()) {
            this.stepEvent();
            if (isStop()) {
                System.out.println("STOP in net  {} "+this.getName());
            }
        }
    }

    /**
     * Determines is all of transitions has empty buffer
     *
     * @return true if buffer is empty for all transitions of Petri net
     */
    public boolean isBufferEmpty() {
        boolean c = true;
        for (PetriT e : listT) {
            if (e.getBuffer() > 0) {
                c = false;
                break;
            }
        }
        return c;
    }

    /**
     * Do printing the current marking of Petri net
     */
    public void printMark() {
        StringBuilder sb = new StringBuilder("Mark in Net  " + this.getName() + "   ");
        for (PetriP position : listP) {
            sb.append(position.getMark()).append("  ");
        }
        System.out.println(sb.toString());
    }
    public void printBuffer(){
        StringBuilder sb = new StringBuilder("Buffer in Net  " + this.getName() + "   ");
        for (PetriT transition : listT) {
            sb.append(transition.getBuffer()).append("  ");
        }
        System.out.println(sb.toString());
    }
    
        
    public void printMark(Consumer<String> output) {
        output.accept("\n Mark in Net  " + this.getName() + "   \n");
        for (PetriP position : listP) {
            output.accept(position.getMark() + "  ");
        }
        output.accept("\n");
    }
    public void printStatistics(){
       System.out.println("\nState of places and transitions: "+getCurrentTime());
        printMark();
        printBuffer();

        
        System.out.println("\nMean value of markers in places and mean value of buffers in transitions for "+this.getName()+" object");
        for(PetriP p: getListPositionsForStatistica()) {
            System.out.println(p.getName()+"  "+p.getMean());
        }
        System.out.println("\nWait time statistical parametres for "+this.getName()+" object");
        for(PetriP p: getListPositionsForTimeStatistica()) {
            System.out.println(p.getName()+"  m="+p.getMeanWaitTime()+";  min="+p.getMinWaitTime()+";  max="+p.getMaxWaitTime()+"; std="+p.getSTDWaitTime());
        }
    }
        
    
   
    /**
     *
     * @return the nearest event
     */
    public final PetriT getEventMin() {
        this.eventMin();
        return eventMin;
    }

    /**
     * This method solves conflict between transitions given in parametr transitions
     *
     * @param transitions the list of transitions
     * @return the transition - winner of conflict
     */
    public PetriT doConflikt(ArrayList<PetriT> transitions) {//
        PetriT aT = transitions.getFirst();
        if (transitions.size() > 1) {
            aT = transitions.getFirst();
            int i = 0;
            while (i < transitions.size() && transitions.get(i).getPriority() == aT.getPriority()) {
                i++;
            }
            if (i != 1){
                double r = Math.random();
                int j = 0;
                double sum = 0;
                double prob;
                while (j < transitions.size() && transitions.get(j).getPriority() == aT.getPriority()) {
                    if (transitions.get(j).getProbability() == 1.0) {
                        prob = 1.0 / i;
                    } else {
                        prob = transitions.get(j).getProbability();
                    }
                    sum = sum + prob;
                    if (r < sum) {
                        aT = transitions.get(j);
                        break;
                    } //вибір переходу за значенням ймовірності
                    else {
                        j++;
                    }
                }
            }
        }
        return aT;
    }

    /**
     * @return the stop
     */
    public boolean isStop() {
        this.eventMin();
        return (eventMin == null);
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the timeState
     */
    public StateTime getTimeState() {
        return timeState;
    }

    /**
     * @param timeState the timeState to set
     */
    public void setTimeState(StateTime timeState) {
        this.timeState = timeState;
    }
}
