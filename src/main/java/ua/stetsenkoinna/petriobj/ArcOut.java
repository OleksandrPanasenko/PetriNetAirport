package ua.stetsenkoinna.petriobj;



/**
 * This class for creating the arc between transition and place of Petri
 * net (and directed from transion to place)
 *
 *  @author Inna V. Stetsenko
 */
public class ArcOut {

    private int numP;
    private int numT;
    private int k;
    private String nameT;
    private String nameP;
    private static int next = 0;

    // whether k is a parameter; added by Katya 08.12.2016
    private boolean kIsParam = false;
    // param name
    private String kParamName = null;

    public ArcOut() {
        next++;
        k = 1;
    }

    /**
     * @param T number of transition
     * @param P number of place
     * @param K arc multiplicity
     */
    public ArcOut(int T, int P, int K) {
        next++;
        numP = P;
        numT = T;
        k = K;
    }

    /**
     *
     * @param T number of transition
     * @param P number of place
     * @param K arc multiplicity
     */
    public ArcOut(PetriT T, PetriP P, int K) {
        next++;
        numP = P.getNumber();
        numT = T.getNumber();
        k = K;
        nameP = P.getName();
        nameT = T.getName();
    }


    public boolean kIsParam() {
        return kIsParam;
    }
    
    public String getKParamName() {
        return kParamName;
    }
    
    public void setKParam(String paramName) {
        if (paramName == null) {
            kIsParam = false;
            kParamName = null;
        } else {
            kIsParam = true;
            kParamName = paramName;
            k = 1;
        }
    }

    /**
     * Set the counter of output arcs to zero.
     */
    public static void initNext() //ініціалізація лічильника нульовим значенням
    {
        next = 0;
    }

    /**
     *
     * @return arc multiplicity
     */
    public int getQuantity() {
        return k;
    }

    /**
     *
     * @param K arc multiplicity
     */
    public void setQuantity(int K) {
        k = K;
    }

    /**
     *
     * @return the number of place that is end of the arc
     */
    public int getNumP() {
        return numP;
    }

    /**
     *
     * @param n the number of place that is end of the arc
     */
    public void setNumP(int n) {
        numP = n;
    }

    /**
     *
     * @return number of transition that is beginning of the arc
     */
    public int getNumT() {
        return numT;
    }

    /**
     *
     * @param n number of transition that is beginning of the arc
     */
    public void setNumT(int n) {
        numT = n;
    }

    /**
     *
     * @return name of transition that is the beginning of the arc
     */
    public String getNameT() {
        return nameT;
    }

    /**
     *
     * @param s name of transition that is the beginning of the arc
     */
    public void setNameT(String s) {
        nameT = s;
    }

    /**
     *
     * @return name of place that is the end of the arc
     */
    public String getNameP() {
        return nameP;
    }

    /**
     *
     * @param s name of place that is the end of the arc
     */
    public void setNameP(String s) {
        nameP = s;
    }

    /**
     *
     */
    public void print() {
        if (nameP != null && nameT != null) {
            System.out.println(" T=  " + nameT + ", P=  " + nameP + ", k= " + getQuantity());
        } else {
            System.out.println(" T= T" + numT + ", P= P" + numP + ", k= " + getQuantity());
        }
    }



    public void printParameters() {
        System.out.println("This arc has direction from  transition  with number " + numT + " to place with number " + numP
                + " and has " + k + " value of multiplicity");
    }

}
