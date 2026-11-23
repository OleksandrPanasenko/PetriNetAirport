import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ua.stetsenkoinna.petriobj.ExceptionInvalidNetStructure;
import ua.stetsenkoinna.petriobj.ExceptionInvalidTimeDelay;
import ua.stetsenkoinna.petriobj.PetriSim;

public class ValidateAirport {
    public static void main(String[] args)throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure{
        SimConfig base = new SimConfig();

        List<String> csvLines = new ArrayList<>();
        csvLines.add("Scenario,Time,SmallPlanes,BigPlanes,ArrivalDelay,LoadTime,AvgStoreWait,AvgSystemWait,SmallUtil,BigUtil,AvgStore,DeliveredCargo");

        csvLines.add(runValidation("0_Baseline", base, 5));
        csvLines.add(runValidation("1_1_LessTime", base.withTime(500), 5));
        csvLines.add(runValidation("1_2_MoreTime", base.withTime(1500), 5));

        csvLines.add(runValidation("2_1_MoreSmallPlanes", base.withSmallPlanes(5), 5));
        csvLines.add(runValidation("2_2_LessSmallPlanes", base.withSmallPlanes(1), 5));
        csvLines.add(runValidation("2_3_MoreBigPlanes", base.withBigPlanes(3), 5));
        csvLines.add(runValidation("2_4_LessBigPlanes", base.withBigPlanes(1), 5));
        csvLines.add(runValidation("3_1_BiggerSmallPlanes", base.withSmallPlanesSize(100), 5));
        csvLines.add(runValidation("3_2_SmallerSmallPlanes", base.withSmallPlanesSize(60), 5));
        csvLines.add(runValidation("3_3_BiggerBigPlanes", base.withBigPlanesSize(160), 5));
        csvLines.add(runValidation("3_4_SmallerBigPlanes", base.withBigPlanesSize(120), 5));
        csvLines.add(runValidation("4_1_FasterArrivals", base.withArrivalTime(0.25), 5));
        csvLines.add(runValidation("4_2_SlowerArrivals", base.withArrivalTime(0.75), 5));
        
        csvLines.add(runValidation("5_1_SmallerAvgFlight", base.withTimeFly(120., 120.,240.,60.), 5));
        csvLines.add(runValidation("5_2_BiggerAvgFlight", base.withTimeFly(180., 120.,240.,60.), 5));
        csvLines.add(runValidation("5_3_SmallerMinFlight", base.withTimeFly(180., 60.,240.,60.), 5));
        csvLines.add(runValidation("5_4_BiggerMinFlight", base.withTimeFly(180., 180.,240.,60.), 5));
        csvLines.add(runValidation("5_5_SmallerMaxFlight", base.withTimeFly(180., 120.,180.,60.), 5));
        csvLines.add(runValidation("5_6_BiggerMaxFlight", base.withTimeFly(180., 120.,300.,60.), 5));
        csvLines.add(runValidation("5_7_SmallerStdFlight", base.withTimeFly(180., 120.,240.,30.), 5));
        csvLines.add(runValidation("5_8_BiggerStdFlight", base.withTimeFly(180., 120.,240.,90.), 5));
        csvLines.add(runValidation("5_9_BiggerBoundsFlight", base.withTimeFly(180., 0.,360.,60.), 5));
        csvLines.add(runValidation("5_10_SmallerBoundsFlight", base.withTimeFly(180., 150.,210.,60.), 5));
        csvLines.add(runValidation("5_11_SmallerSpreadFlight", base.withTimeFly(180., 150.,210.,30.), 5));
        csvLines.add(runValidation("5_12_BiggerSpreadFlight", base.withTimeFly(180., 150.,210.,90.), 5));
        csvLines.add(runValidation("5_13_SmallShiftFlight", base.withTimeFly(120., 60.,180.,60.), 5));
        csvLines.add(runValidation("5_14_BiggerShiftFlight", base.withTimeFly(240., 180.,300.,60.), 5));
        csvLines.add(runValidation("5_15_SmallScaleFlight", base.withTimeFly(90., 60.,120.,30.), 5));
        csvLines.add(runValidation("5_16_BiggerScaleFlight", base.withTimeFly(270., 180.,360.,90.), 5));

        csvLines.add(runValidation("6_WithLoadingTime", base.withTimeLoad(1.0), 5));
        csvLines.add(runValidation("6_1_WithSmallerLoadingTime", base.withTimeLoad(0.5), 5));

        try (PrintWriter pw = new PrintWriter(new FileWriter("ValidationResults.csv"))) {
            for (String line : csvLines) {
                pw.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Validation completed successfully. Results saved to ValidationResults.csv");
    }
    public static class SimConfig {
    double timeModeling = 1000.0;
    int smallPlanes = 3;
    int bigPlanes = 2;
    double timeCargoArrival = 0.5;
    int smallPlaneSize = 80;
    int bigPlaneSize = 140;
    double timeLoad = 0.0;
    double avgFlight = 180.0;
    double minFlight = 120.0;
    double maxFlight = 240.0;
    double stdFlight = 60.0;

    // Copy constructor
    public SimConfig copy() {
        SimConfig c = new SimConfig();
        c.timeModeling = this.timeModeling;
        c.smallPlanes = this.smallPlanes;
        c.bigPlanes = this.bigPlanes;
        c.timeCargoArrival = this.timeCargoArrival;
        c.smallPlaneSize = this.smallPlaneSize;
        c.bigPlaneSize = this.bigPlaneSize;
        c.timeLoad = this.timeLoad;
        c.avgFlight = this.avgFlight;
        c.minFlight = this.minFlight;
        c.maxFlight = this.maxFlight;
        c.stdFlight = this.stdFlight;
        return c;
    }

    public SimConfig withTime(int val) { SimConfig c = copy(); c.timeModeling = val; return c; }
    public SimConfig withSmallPlanes(int val) { SimConfig c = copy(); c.smallPlanes = val; return c; }
    public SimConfig withSmallPlanesSize(int val) { SimConfig c = copy(); c.smallPlaneSize = val; return c; }
    public SimConfig withBigPlanesSize(int val) { SimConfig c = copy(); c.bigPlaneSize = val; return c; }
    public SimConfig withBigPlanes(int val) { SimConfig c = copy(); c.bigPlanes = val; return c; }
    public SimConfig withTimeFly(double mean, double min, double max, double std) { SimConfig c = copy(); c.avgFlight = mean; c.minFlight = min; c.maxFlight = max; c.stdFlight = std; return c; }
    public SimConfig withArrivalTime(double val) { SimConfig c = copy(); c.timeCargoArrival = val; return c; }
    
    public SimConfig withTimeLoad(double val) { SimConfig c = copy(); c.timeLoad = val; return c; }
    public SimConfig withTimeLoad(int time, double val) { SimConfig c = copy(); c.timeModeling = time; c.timeLoad = val; return c; }
    public SimConfig withTimeLoadAndExtraPlane(int time, double val) { SimConfig c = copy(); c.timeModeling = time; c.timeLoad = val; c.smallPlanes++; return c; }

    }
    public static String runValidation(String scenarioName, SimConfig cfg, int repeats) 
        throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure {
    
    double sumStoreWait = 0, sumSystemWait = 0, sumSmallUtil = 0, sumBigUtil = 0, sumBacklog = 0, sumDeliveredCargo = 0;

    for (int i = 0; i < repeats; i++) {
        PetriSim model = Airport.CreateAirportNet(
            cfg.smallPlanes, cfg.bigPlanes, cfg.timeCargoArrival,
            cfg.smallPlaneSize, cfg.bigPlaneSize, cfg.timeLoad,
            cfg.avgFlight, cfg.minFlight, cfg.maxFlight, cfg.stdFlight
        );
        //model.setStatysticsInterval(10.0);
        model.go(cfg.timeModeling);

        // Accumulate final run metrics
        sumStoreWait += Airport.getAverageStoreWaitTime(model);
        sumSystemWait += Airport.getAverageSystemWaitTime(model);
        sumSmallUtil += Airport.getSmallPlanesLoad(model);
        sumBigUtil += Airport.getBigPlanesLoad(model);
        sumBacklog += Airport.getAvgStore(model);
        sumDeliveredCargo += Airport.getDeliveredCargo(model);
    }

    return String.format(java.util.Locale.US, "%s,%.2f,%d,%d,%.2f,%.2f,%.2f,%.2f,%.3f,%.3f,%.1f,%.1f",
        scenarioName, cfg.timeModeling, cfg.smallPlanes, cfg.bigPlanes, cfg.timeCargoArrival, cfg.timeLoad,
        sumStoreWait / repeats, sumSystemWait / repeats, sumSmallUtil / repeats, sumBigUtil / repeats, sumBacklog / repeats, sumDeliveredCargo / repeats
    );
}
}
