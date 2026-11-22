import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import ua.stetsenkoinna.petriobj.ExceptionInvalidNetStructure;
import ua.stetsenkoinna.petriobj.ExceptionInvalidTimeDelay;
import ua.stetsenkoinna.petriobj.PetriSim;

public class MeasureDistribution {
    static double timeModeling = 83500.;
    static int NumberRepeats=80;
    public static void main(String[] args) throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure{
        PetriSim modelFirst=Airport.CreateAirportNet(3,
                                            2,
                                            0.5,
                                            80,
                                            140,
                                            0.0,
                                            180.,
                                            120.,
                                            240.,
                                            60.0);
        modelFirst.go(timeModeling);
        Deque<Double> StoreWaitTimes = modelFirst.getListPositionsForTimeStatistica().get(0).getWaitTimes();
        Deque<Double> SystemWaitTimes = modelFirst.getListPositionsForTimeStatistica().get(1).getWaitTimes();
        exportEventsCsv("single_store_wait_time.csv", new ArrayList<Deque<Double>>(List.of(StoreWaitTimes)));
        exportEventsCsv("single_system_wait_time.csv", new ArrayList<Deque<Double>>(List.of(SystemWaitTimes)));
        
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
            model.setStatysticsInterval(100.);
            model.go(timeModeling);
            simulations.add(model);
        }
    ArrayList<Deque<Double>> allStoreWaitTimes = new ArrayList<>();
    ArrayList<Deque<Double>> allSystemWaitTimes = new ArrayList<>();
    

    for (PetriSim model : simulations) {
        allStoreWaitTimes.add(model.getListPositionsForTimeStatistica().get(0).getWaitTimes());
        allSystemWaitTimes.add(model.getListPositionsForTimeStatistica().get(1).getWaitTimes());
    }

    // Export to csv files
    exportEventsCsv("80_store_wait_time.csv", allStoreWaitTimes);
    exportEventsCsv("80_system_wait_time.csv", allSystemWaitTimes);
    }
    public static void exportEventsCsv(String filename, ArrayList<Deque<Double>> dataPerRun) {
        if (dataPerRun == null || dataPerRun.size() == 0) {
            return;
        }

        int numRepeats = dataPerRun.size();

        // Header: Run_0, Run_1, ..., Run_N
        // Find the longest deque
        int maxEvents = 0;
        List<List<Double>> indexedRuns = new ArrayList<>(numRepeats);
        for (int r = 0; r < numRepeats; r++) {
            Deque<Double> deque = dataPerRun.get(r);
            List<Double> list = (deque != null) ? new ArrayList<>(deque) : List.of();
            indexedRuns.add(list);
            if (list.size() > maxEvents) {
                maxEvents = list.size();
            }
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            for (int i = 0; i < numRepeats; i++) {
                if (i > 0) {
                    pw.print(",");
                }
                pw.print("Run_" + i);
            }
            pw.println();

            // Event rows (iterating front-to-back in FIFO order)
            for (int e = 0; e < maxEvents; e++) {
                for (int r = 0; r < numRepeats; r++) {
                    if (r > 0) {
                        pw.print(",");
                    }
                    List<Double> runData = indexedRuns.get(r);
                    if (e < runData.size()) {// Empty cell if less
                        pw.print(runData.get(e));
                    }
                    
                }
                pw.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
