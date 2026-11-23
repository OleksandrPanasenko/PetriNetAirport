import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ua.stetsenkoinna.petriobj.ExceptionInvalidNetStructure;
import ua.stetsenkoinna.petriobj.ExceptionInvalidTimeDelay;

public class ExperimentsAirport {
    public static void main(String[] args)throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure{
        //Experiment_1_Load_Time_1();
        //Experiment_2_Load_Time_0_1();
        Experiment_3_ExtraPlane_LoadTime();
    }
    
    public static void Experiment_1_Load_Time_1()throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure{
        int num_runs=80;
        int sim_time=83500;
        double timeLoad=1.0;
        ValidateAirport.SimConfig base = new ValidateAirport.SimConfig().withTime(sim_time);
        ValidateAirport.SimConfig withTimeLoad = new ValidateAirport.SimConfig().withTimeLoad(sim_time, timeLoad);

        List<String> csvLines = new ArrayList<>();
        csvLines.add("Scenario,Time,SmallPlanes,BigPlanes,ArrivalDelay,LoadTime,AvgStoreWait,AvgSystemWait,SmallUtil,BigUtil,AvgStore,DeliveredCargo");

        for(int i=0;i<num_runs;i++){
            csvLines.add(ValidateAirport.runValidation("No_Load_Time_Run_"+i, base, 1));
            System.out.println("1 Run "+i+" done;");
        }
        for(int i=0;i<num_runs;i++){
            csvLines.add(ValidateAirport.runValidation("With_Load_Time_Run_"+i, withTimeLoad, 1));
            System.out.println("2 Run "+i+" done;");
        }
        

        try (PrintWriter pw = new PrintWriter(new FileWriter("ExperimentLoadTime.csv"))) {
            for (String line : csvLines) {
                pw.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Parametres gathered. Results saved to ExperimentLoadTime.csv");
    }
    public static void Experiment_2_Load_Time_0_1()throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure{
        int num_runs=80;
        int sim_time=83500;
        double timeLoad=0.1;
        ValidateAirport.SimConfig base = new ValidateAirport.SimConfig().withTime(sim_time);
        ValidateAirport.SimConfig withTimeLoad = new ValidateAirport.SimConfig().withTimeLoad(sim_time, timeLoad);

        List<String> csvLines = new ArrayList<>();
        csvLines.add("Scenario,Time,SmallPlanes,BigPlanes,ArrivalDelay,LoadTime,AvgStoreWait,AvgSystemWait,SmallUtil,BigUtil,AvgStore,DeliveredCargo");

        for(int i=0;i<num_runs;i++){
            csvLines.add(ValidateAirport.runValidation("No_Load_Time_Run_"+i, base, 1));
            System.out.println("1 Run "+i+" done;");
        }
        for(int i=0;i<num_runs;i++){
            csvLines.add(ValidateAirport.runValidation("With_Load_Time_Run_"+i, withTimeLoad, 1));
            System.out.println("2 Run "+i+" done;");
        }
        

        try (PrintWriter pw = new PrintWriter(new FileWriter("ExperimentLoadTime_01.csv"))) {
            for (String line : csvLines) {
                pw.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Parametres gathered. Results saved to ExperimentLoadTime_01.csv");
    }
    public static void Experiment_3_ExtraPlane_LoadTime()throws ExceptionInvalidTimeDelay, ExceptionInvalidNetStructure{
        int num_runs=80;
        int sim_time=83500;
        double timeLoad=0.1;
        ValidateAirport.SimConfig base = new ValidateAirport.SimConfig().withTime(sim_time);
        ValidateAirport.SimConfig withExtraPlane = new ValidateAirport.SimConfig().withTimeLoadAndExtraPlane(sim_time, timeLoad);

        List<String> csvLines = new ArrayList<>();
        csvLines.add("Scenario,Time,SmallPlanes,BigPlanes,ArrivalDelay,LoadTime,AvgStoreWait,AvgSystemWait,SmallUtil,BigUtil,AvgStore,DeliveredCargo");

        for(int i=0;i<num_runs;i++){
            csvLines.add(ValidateAirport.runValidation("No_Load_Time_Run_"+i, base, 1));
            System.out.println("1 Run "+i+" done;");
        }
        for(int i=0;i<num_runs;i++){
            csvLines.add(ValidateAirport.runValidation("With_Load_Time_Run_"+i, withExtraPlane, 1));
            System.out.println("2 Run "+i+" done;");
        }
        

        try (PrintWriter pw = new PrintWriter(new FileWriter("ExperimentLoadTime_ExtraPlane.csv"))) {
            for (String line : csvLines) {
                pw.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Parametres gathered. Results saved to ExperimentLoadTime_ExtraPlane.csv");
    }
}
