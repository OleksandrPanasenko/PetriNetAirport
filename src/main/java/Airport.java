import java.util.ArrayList;
import ua.stetsenkoinna.petriobj.*;

public class Airport {
    public static void main(String[] args) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure{
        double timeModeling = 100000.;

        PetriSim model=CreateAirportNet(3,
                                        2,
                                        0.5,
                                        80,
                                        140,
                                        0.0,
                                        180.,
                                        120.,
                                        240.,
                                        60.0);
        model.setStatysticsInterval(100.);
        model.go(timeModeling);
        model.printStatistics();
        
    }

    public static PetriSim CreateAirportNet(int smallPlanes, 
                                            int bigPlanes, 
                                            double timeCargoArrival, 
                                            int smallPlaneSize, 
                                            int bigPlaneSize, 
                                            double timeLoad, 
                                            double avgFlight, 
                                            double minFlight, 
                                            double maxFlight, 
                                            double stdFlight) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
        ArrayList<PetriP> d_P = new ArrayList<>();
		ArrayList<PetriT> d_T = new ArrayList<>();
		ArrayList<ArcIn> d_In = new ArrayList<>();
		ArrayList<ArcOut> d_Out = new ArrayList<>();
		d_P.add(new PetriP("Source",1));//0
		d_P.add(new PetriP("Store",0));//1
		d_P.add(new PetriP("Smaller planes",smallPlanes));//2
        d_P.add(new PetriP("Bigger planes",bigPlanes));//3
        d_P.add(new PetriP("Small flights done",0));//4
        d_P.add(new PetriP("Big flights done",0));//5
        d_P.add(new PetriP("Store total",0));//6
        d_P.add(new PetriP("Cargo delivered",0));//7
        

		d_T.add(new PetriT("Arrival",timeCargoArrival));
		d_T.get(0).setDistribution("exp", d_T.get(0).getTimeServ());
		d_T.get(0).setParamDeviation(0.0);
		d_In.add(new ArcIn(d_P.get(0),d_T.get(0),1));//Source->Arrival
		d_Out.add(new ArcOut(d_T.get(0),d_P.get(0),1));//Arrival->Source
		d_Out.add(new ArcOut(d_T.get(0),d_P.get(1),1));//Arrival->Store
        d_Out.add(new ArcOut(d_T.get(0),d_P.get(6),1));//Arrival->Store totals

        
        d_T.add(new PetriT("Small planes flight",avgFlight+timeLoad*smallPlaneSize));
        
		d_T.get(1).setDistribution("normlimited", d_T.get(1).getTimeServ());
		d_T.get(1).setParamDeviation(stdFlight);
        d_T.get(1).setParametrMin(minFlight+timeLoad*smallPlaneSize);
        d_T.get(1).setParametrMax(maxFlight+timeLoad*smallPlaneSize);
		d_In.add(new ArcIn(d_P.get(1),d_T.get(1),smallPlaneSize));//Store->Small planes flight
        d_In.add(new ArcIn(d_P.get(2),d_T.get(1),1));//Small planes->Small planes flight
        
		d_Out.add(new ArcOut(d_T.get(1),d_P.get(2),1));//Small planes flight ->Small planes
        d_Out.add(new ArcOut(d_T.get(1),d_P.get(4),1));//Small planes flight->Small planes total
        d_Out.add(new ArcOut(d_T.get(1),d_P.get(7),smallPlaneSize));//Small planes flight->Cargo delivered


        d_T.add(new PetriT("Big planes flight",avgFlight+timeLoad*bigPlaneSize));
        
		d_T.get(2).setDistribution("normlimited", d_T.get(2).getTimeServ());
		d_T.get(2).setParamDeviation(stdFlight);
        d_T.get(2).setParametrMin(minFlight+timeLoad*bigPlaneSize);
        d_T.get(2).setParametrMax(maxFlight+timeLoad*bigPlaneSize);

		d_In.add(new ArcIn(d_P.get(1),d_T.get(2),bigPlaneSize));//Store->Big planes flight
        d_In.add(new ArcIn(d_P.get(3),d_T.get(2),1));//Big planes->Big planes flight
        
		d_Out.add(new ArcOut(d_T.get(2),d_P.get(3),1));//Big planes flight ->Big planes
        d_Out.add(new ArcOut(d_T.get(2),d_P.get(5),1));//Big planes flight->Big planes total
        d_Out.add(new ArcOut(d_T.get(2),d_P.get(7),bigPlaneSize));//Big planes flight->Cargo delivered



		PetriNet d_Net = new PetriNet("Petri Airport",d_P,d_T,d_In,d_Out);
		PetriP.initNext();
		PetriT.initNext();
		ArcIn.initNext();
		ArcOut.initNext();

        PetriSim petriSim = new PetriSim(d_Net, new StateTime());
        petriSim.addPositionForStatistica(d_P.get(1));//Store, to verify
        petriSim.addPositionForStatistica(d_P.get(2));//Smaller planes
        petriSim.addPositionForStatistica(d_P.get(3));//Bigger planes
        petriSim.addPositionForStatistica(d_P.get(7));//Delivered cargo, to verify
        petriSim.addPositionForTimeStatistica(d_P.get(1));//Store wait time

		return petriSim;
    }
    public static double getDeliveredCargo(PetriSim model){
        return model.getListPositionsForStatistica().get(3).getMark();
    }
    public static double getAvgStore(PetriSim model){
        return model.getListPositionsForStatistica().get(0).getMean();
    }
    public static double getSmallPlanesLoad(PetriSim model){
        return model.getListPositionsForStatistica().get(1).getObservedMax()-
        model.getListPositionsForStatistica().get(1).getMean();
    } 
    public static double getBigPlanesLoad(PetriSim model){
        return model.getListPositionsForStatistica().get(2).getObservedMax()-
        model.getListPositionsForStatistica().get(2).getMean();
    } 
    public static double getAverageStoreWaitTime(PetriSim model){
        //get parametre of average wait time for Store position
        return model.getListPositionsForTimeStatistica().get(0).getMeanWaitTime();
    }
    public static double getStdStoreWaitTime(PetriSim model){
        //get parametre of std wait time for Store position
        return model.getListPositionsForTimeStatistica().get(0).getSTDWaitTime();
    }
    public static double getMinStoreWaitTime(PetriSim model){
        //get parametre of min wait time for Store position
        return model.getListPositionsForTimeStatistica().get(0).getMinWaitTime();
    }
    public static double getMaxStoreWaitTime(PetriSim model){
        //get parametre of max wait time for Store position
        return model.getListPositionsForTimeStatistica().get(0).getMaxWaitTime();
    }
}
