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
        d_P.add(new PetriP("Small fly-ready",0));//3
        d_P.add(new PetriP("Bigger planes",bigPlanes));//4
        d_P.add(new PetriP("Big fly-ready",0));//5
        d_P.add(new PetriP("Small flights done",0));//6
        d_P.add(new PetriP("Big flights done",0));//7
        d_P.add(new PetriP("Store total",0));//8
        d_P.add(new PetriP("Cargo delivered",0));//9
        d_P.add(new PetriP("In System",0));//10
        d_P.add(new PetriP("Cargo delivered final",0));//11 //new, to verify all path time
        

		d_T.add(new PetriT("Arrival",timeCargoArrival));
		d_T.get(0).setDistribution("exp", d_T.get(0).getTimeServ());
		d_T.get(0).setParamDeviation(0.0);
		d_In.add(new ArcIn(d_P.get(0),d_T.get(0),1));//Source->Arrival
		d_Out.add(new ArcOut(d_T.get(0),d_P.get(0),1));//Arrival->Source
		d_Out.add(new ArcOut(d_T.get(0),d_P.get(1),1));//Arrival->Store
        d_Out.add(new ArcOut(d_T.get(0),d_P.get(8),1));//Arrival->Store totals
        d_Out.add(new ArcOut(d_T.get(0),d_P.get(10),1));//Arrival->In System //new, to verify all path time

        d_T.add(new PetriT("Loading small",timeLoad));
        d_T.get(1).setPriority(1);//give priority to small planes
		d_T.get(1).setDistribution("expsum", d_T.get(1).getTimeServ());
		d_T.get(1).setParamDeviation(smallPlaneSize);//put number of containers
		d_In.add(new ArcIn(d_P.get(1),d_T.get(1),smallPlaneSize));//Store->Loading small
        d_In.add(new ArcIn(d_P.get(2),d_T.get(1),1));//Smaller planes->Loading small
		d_Out.add(new ArcOut(d_T.get(1),d_P.get(3),1));//Loading small->Small fly-ready

        d_T.add(new PetriT("Small planes flight",avgFlight));
        
		d_T.get(2).setDistribution("normlimited", d_T.get(2).getTimeServ());
		d_T.get(2).setParamDeviation(stdFlight);
        d_T.get(2).setParametrMin(minFlight);
        d_T.get(2).setParametrMax(maxFlight);
		d_In.add(new ArcIn(d_P.get(3),d_T.get(2),1));//Small fly-ready->Small planes flight
        
		d_Out.add(new ArcOut(d_T.get(2),d_P.get(6),1));//Small planes flight->Small flights done
        d_Out.add(new ArcOut(d_T.get(2),d_P.get(2),1));//Small planes flight->Small planes
        d_Out.add(new ArcOut(d_T.get(2),d_P.get(9),smallPlaneSize));//Small planes flight->Cargo delivered

        d_T.add(new PetriT("Loading big",timeLoad));
		d_T.get(3).setDistribution("expsum", d_T.get(3).getTimeServ());
		d_T.get(3).setParamDeviation(bigPlaneSize);//put number of containers
		d_In.add(new ArcIn(d_P.get(1),d_T.get(3),bigPlaneSize));//Store->Loading big
        d_In.add(new ArcIn(d_P.get(4),d_T.get(3),1));//Big planes->Loading big
		d_Out.add(new ArcOut(d_T.get(3),d_P.get(5),1));//Loading big->Big fly-ready

        d_T.add(new PetriT("Big planes flight",avgFlight));
        
		d_T.get(4).setDistribution("normlimited", d_T.get(4).getTimeServ());
		d_T.get(4).setParamDeviation(stdFlight);
        d_T.get(4).setParametrMin(minFlight);
        d_T.get(4).setParametrMax(maxFlight);
		d_In.add(new ArcIn(d_P.get(5),d_T.get(4),1));//Big fly-ready->Big planes flight
        
		d_Out.add(new ArcOut(d_T.get(4),d_P.get(7),1));//Big planes flight->Big flights done
        d_Out.add(new ArcOut(d_T.get(4),d_P.get(4),1));//Big planes flight->Big planes
        d_Out.add(new ArcOut(d_T.get(4),d_P.get(9),bigPlaneSize));//Big planes flight->Cargo delivered

        d_T.add(new PetriT("Output",0));
        d_In.add(new ArcIn(d_P.get(9),d_T.get(5),1));//Cargo delivered->Output
        d_In.add(new ArcIn(d_P.get(10),d_T.get(5),1));//Big fly-ready->Output
        d_Out.add(new ArcOut(d_T.get(5),d_P.get(11),1));//Output->Cargo delivered final


		PetriNet d_Net = new PetriNet("Petri Airport",d_P,d_T,d_In,d_Out);
		PetriP.initNext();
		PetriT.initNext();
		ArcIn.initNext();
		ArcOut.initNext();

        PetriSim petriSim = new PetriSim(d_Net, new StateTime());
        petriSim.addPositionForStatistica(d_P.get(1));//Store, to verify
        petriSim.addPositionForStatistica(d_P.get(2));//Smaller planes
        petriSim.addPositionForStatistica(d_P.get(4));//Bigger planes
        petriSim.addPositionForStatistica(d_P.get(11));//Delivered cargo, to verify
        petriSim.addPositionForTimeStatistica(d_P.get(1));//Store wait time
        petriSim.addPositionForTimeStatistica(d_P.get(10));//In System wait time

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
    public static double getAverageSystemWaitTime(PetriSim model){
        //get parametre of average wait time for System position
        return model.getListPositionsForTimeStatistica().get(1).getMeanWaitTime();
    }
    public static double getStdSystemWaitTime(PetriSim model){
        //get parametre of std wait time for System position
        return model.getListPositionsForTimeStatistica().get(1).getSTDWaitTime();
    }
    public static double getMinSystemWaitTime(PetriSim model){
        //get parametre of min wait time for System position
        return model.getListPositionsForTimeStatistica().get(1).getMinWaitTime();
    }
    public static double getMaxSystemWaitTime(PetriSim model){
        //get parametre of max wait time for System position
        return model.getListPositionsForTimeStatistica().get(1).getMaxWaitTime();
    }
}
