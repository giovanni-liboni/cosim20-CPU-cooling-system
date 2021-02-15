package fr.univcotedazur.kairos.glose.cosim20.example.cpuheatmanagement.coordinator;

import org.eclipse.gemoc.execution.commons.commands.StopCondition;
import org.eclipse.gemoc.execution.commons.commands.StopReason;
import org.eclipse.gemoc.execution.commons.predicates.BinaryPredicate;
import org.eclipse.gemoc.execution.commons.predicates.EventPredicate;
import org.eclipse.gemoc.execution.commons.predicates.ReadyToReadPredicate;
import org.javafmi.modeldescription.ScalarVariable;
import org.javafmi.wrapper.Simulation;

public class Coordinator {

    public Simulation boxSU = null;
    public Simulation fanController = null;
    public OverHeatControllerSimulationUnit controlerSU = null;
    public double now = 0;


    public Coordinator() {
        boxSU = new Simulation("/home/jdeanton/boulot/recherche/articles/2020/cosim-CPS/workspace/fr.univcotedazur.kairos.glose.cosim20.example.cpuheatmanagement/src/fmu/CPUinBoxWithFanHeatModel.fmu");
        fanController = new Simulation("/home/jdeanton/boulot/recherche/articles/2020/cosim-CPS/workspace/fr.univcotedazur.kairos.glose.cosim20.example.cpuheatmanagement/src/fmu/FanController.fmu");
        controlerSU = new OverHeatControllerSimulationUnit("localhost", 39635);
    }

    public static void main(String[] args) {
        Coordinator c = new Coordinator();
        c.loadBoxSU();
        c.loadFanControllerSU();
        c.controlerSU.load();
        boolean initialIsStopped = false;

        c.now = 0;
        System.out.println("time,cpuTemp,boxTemp,fanCmd");
        c.coSimulate(initialIsStopped, 30000);

        c.controlerSU.terminate();
        c.boxSU.terminate();
        c.fanController.terminate();


    }

    public void loadBoxSU() {
        // Retrieve the FMU
        boxSU.init(0);
        boxSU.write("isStopped").with(false);
        boxSU.write("CPUfanSpeed").with(1);
//        // Print FMU info
//        System.out.println(boxSU.getModelDescription().getModelName() + " FMU I/O");
//        for (ScalarVariable p : boxSU.getModelDescription().getModelVariables()) {
//            if (p.getCausality().compareTo("input") == 0 || p.getCausality().compareTo("output") == 0) {
//                System.out.println("\t" + p.getCausality() + " " + p.getName() + ":" + p.getTypeName());
//            }
//        }
    }

    public void loadFanControllerSU() {
        // Retrieve the FMU
        fanController.init(0);

        fanController.write("targetTemperature").with(65);
        fanController.write("Kp").with(5.0);
//        // Print FMU info
//        System.out.println(fanController.getModelDescription().getModelName() + " FMU I/O");
//        for (ScalarVariable p : fanController.getModelDescription().getModelVariables()) {
//            if (p.getCausality().compareTo("input") == 0 || p.getCausality().compareTo("output") == 0) {
//                System.out.println("\t" + p.getCausality() + " " + p.getName() + ":" + p.getTypeName());
//            }
//        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        controlerSU.finalize();
        boxSU.terminate();
    }

    private void coSimulate(boolean localIsStopped, double endtime) {
        while (now < endtime) {
            ReadyToReadPredicate r2rp = new ReadyToReadPredicate("currentValue", "CPUprotection::cpuTemperature");
            EventPredicate ep = new EventPredicate("occurs", "CPUprotection::switchCPUState");
            BinaryPredicate bp = new BinaryPredicate(r2rp, ep);
            StopCondition sc = controlerSU.doStep(bp);
            if (sc.stopReason == StopReason.READYTOREAD) {
                int res= simulateBoxAndFanControlUntilExpectedTime(sc.timeValue);
                double cpuTemperature = boxSU.read("CPUTemperature").asDouble();
                double boxTemperature = boxSU.read("BoxTemperature").asDouble();
                if (res == 1) System.out.println(now + "," + cpuTemperature + "," + boxTemperature + ", ");
                controlerSU.setVariable("CPUprotection::cpuTemperature::currentValue", new Integer((int) cpuTemperature));
            } else { //event occured
                simulateBoxAndFanControlUntilExpectedTime(sc.timeValue);
                localIsStopped = !localIsStopped;
                boxSU.write("isStopped").with(localIsStopped);
            }
        }
    }

    private int simulateBoxAndFanControlUntilExpectedTime(double expectedTime) {
        double delta = expectedTime - now;
        while (delta + (now % 5) >= 5) { //\Delta t == 5 for each connector from boxSU to fanControllerSU
            double stepToDo = (5 - (now % 5));
            boxSU.doStep(stepToDo);
            fanController.doStep(5);
            double cpuTemperature = boxSU.read("CPUTemperature").asDouble();
            fanController.write("CPUTemperature").with(cpuTemperature);
            int fanCommand = fanController.read("CPUfanSpeed").asInteger();
            boxSU.write("CPUfanSpeed").with(fanCommand);
            double boxTemperature = boxSU.read("BoxTemperature").asDouble();
            now += stepToDo;
            delta = expectedTime - now;
            System.out.println(now + "," + cpuTemperature + "," + boxTemperature + "," + fanCommand);
        }
        if (delta > 0) {
            boxSU.doStep(delta);
            now += delta;
            return 1;// time elapsed since last sysout
        }
        return 0;
    }

}
