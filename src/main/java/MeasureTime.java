import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ua.stetsenkoinna.petriobj.ExceptionInvalidNetStructure;
import ua.stetsenkoinna.petriobj.ExceptionInvalidTimeDelay;
import ua.stetsenkoinna.petriobj.PetriSim;

public class MeasureTime {
    static double timeModeling = 10000.;
    static int NumberRepeats=4;
    public static void main(String[] args) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure{
        
        
        ArrayList<PetriSim> simulations=new ArrayList<>();
        for(int i=0;i<NumberRepeats;i++){
            PetriSim model=Airport.CreateAirportNet(3,
                                            2,
                                            0.5,
                                            80,
                                            140,
                                            0.0,
                                            180.,
                                            120.,
                                            240.,
                                            60.0);
            model.setStatysticsInterval(10.);
            model.go(timeModeling);
            simulations.add(model);
        }

    List<List<Double>> allMeanWaitTimes = new ArrayList<>();
    List<List<Double>> allStdWaitTimes = new ArrayList<>();

    for (PetriSim model : simulations) {
        // We only measure one element
        allMeanWaitTimes.add(model.getAverageWaitTimes().get(0)); 
        allStdWaitTimes.add(model.getStdWaitTimes().get(0));     
    }

    // Export to two csv files
    exportTimeSeriesCsv("wait_times_mean.csv", simulations.get(0).getTimes(), allMeanWaitTimes);
    exportTimeSeriesCsv("wait_times_std.csv", simulations.get(0).getTimes(), allStdWaitTimes);
    System.out.println("Export completed successfully: wait_times_mean.csv & wait_times_std.csv");

    }
    public static void exportTimeSeriesCsv(String filename, List<Double> timesRun, List<List<Double>> dataPerRun) {
    try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
        int numRepeats = dataPerRun.size();
        if (numRepeats == 0) return;

        // Header: Time_Step, Run_0, Run_1, ..., Run_N
        pw.print("Time_Step");
        for (int i = 0; i < numRepeats; i++) {
            pw.print(",Run_" + i);
        }
        pw.println();

        // Checkpoints
        int numCheckpoints = dataPerRun.get(0).size();

        for (int t = 0; t < numCheckpoints; t++) {
            double currentTime = timesRun.get(t);
            pw.print(currentTime);

            for (int r = 0; r < numRepeats; r++) {
                List<Double> runData = dataPerRun.get(r);//get checkpoint
                double val = (t < runData.size()) ? runData.get(t) : 0.0;
                pw.print("," + val);
            }
            pw.println();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    }
}
