import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ua.stetsenkoinna.petriobj.ExceptionInvalidNetStructure;
import ua.stetsenkoinna.petriobj.ExceptionInvalidTimeDelay;

public class MeasureParametres {
    public static void main(String[] args)throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure{
        int num_runs=80;
        int sim_time=83500;
        ValidateAirport.SimConfig base = new ValidateAirport.SimConfig().withTime(sim_time);

        List<String> csvLines = new ArrayList<>();
        csvLines.add("Scenario,Time,SmallPlanes,BigPlanes,ArrivalDelay,LoadTime,AvgStoreWait,AvgSystemWait,SmallUtil,BigUtil,AvgStore,DeliveredCargo");

        for(int i=0;i<num_runs;i++){
            csvLines.add(ValidateAirport.runValidation("Run_"+i, base, 1));
            System.out.println("Run "+i+" done;");
        }

        try (PrintWriter pw = new PrintWriter(new FileWriter("80_parametre_runs.csv"))) {
            for (String line : csvLines) {
                pw.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Parametres gathered. Results saved to 80_parametre_runs.csv");
    }
}
