package fr.univcotedazur.kairos.glose.cosim20.example.cpuheatmanagement.coordinator;

import org.eclipse.gemoc.execution.commons.commands.*;
import org.eclipse.gemoc.execution.commons.predicates.CoordinationPredicate;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;


public class OverHeatControllerSimulationUnit {
    Socket socket = null;
    ObjectOutputStream cout = null;
    ObjectInputStream cin = null;
    Process controllerExec = null;
    OutputStream out = null;

    public OverHeatControllerSimulationUnit(String host, int port) {

        try {
            controllerExec = Runtime.getRuntime().exec("java -jar " + "/home/jdeanton/boulot/recherche/articles/2020/cosim-CPS/workspace/fr.univcotedazur.kairos.glose.cosim20.example.cpuheatmanagement/src/fmu/overHeatControler.jar");
            //out = controllerExec.getOutputStream();
            //			ProcessBuilder builder = new ProcessBuilder("java -jar /home/jdeanton/boulot/recherche/articles/2020/cosim-CPS/workspace/fr.univcotedazur.kairos.glose.cosim20.example.cpuheatmanagement/src/fmu/overHeatControler.jar");
//			Process process = builder.start();
            Thread.sleep(2500);
            socket = new Socket(host, port);
            cout = new ObjectOutputStream(socket.getOutputStream());
            cin = new ObjectInputStream(socket.getInputStream());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main() {

/*
			if (myint == 1) {
				ReadyToReadPredicate p = new ReadyToReadPredicate("currentValue", "CPUprotection::cpuTemperature");
				DoStepCommand doStep = new DoStepCommand(p);
				System.out.println("ask for doStep(read(CPUprotection::cpuTemperature::currentValue))");
				cout.writeObject(doStep);
				StopCondition sc = (StopCondition) cin.readObject();

				System.out.println(sc.stopReason+" "+sc.propertyName+" @ "+sc.timeValue);
			}
			else
			if (myint == 2) {
				String varQN = "CPUprotection::cpuTemperature::currentValue";
				GetVariableCommand getVar = new GetVariableCommand(varQN);
				System.out.println("ask for getVariable");

				cout.writeObject(getVar);
				Object varValue = (Object) cin.readObject();

				System.out.println("value of "+varQN+" is "+varValue);
			}
			else
			if (myint == 3) {
				String varQN = "CPUprotection::cpuTemperature::currentValue";
				System.out.println("please enter the value to set");
				int newValue = keyboard.nextInt();
				SetVariableCommand setVar = new SetVariableCommand(varQN, new Integer(newValue));
				System.out.println("ask for setVariable to a fifo with "+newValue);

				cout.writeObject(setVar);
				Boolean resValue = (Boolean) cin.readObject();

				System.out.println("value  is set correctly ?: "+resValue);
			}else
			if (myint == 4) {
				LogicalStepPredicate p = new LogicalStepPredicate(80);
				DoStepCommand doStep = new DoStepCommand(p);
				System.out.println("ask for doStep(80 logicalSteps)");
				cout.writeObject(doStep);
				StopCondition sc = (StopCondition) cin.readObject();
				System.out.println(sc.stopReason+" "+sc.propertyName+" @ "+sc.timeValue);
			}else
			if (myint == 5) {
				EventPredicate p = new EventPredicate("occurs", "CPUprotection::switchCPUState");
				DoStepCommand doStep = new DoStepCommand(p);
				System.out.println("ask for doStep(ticks(CPUprotection::switchCPUState::occurs))");
				cout.writeObject(doStep);
				StopCondition sc = (StopCondition) cin.readObject();
				System.out.println(sc.stopReason+" "+sc.propertyName+" @ "+sc.timeValue);
			}else
			if (myint == 6) {
				TemporalPredicate p = new TemporalPredicate(10);
				DoStepCommand doStep = new DoStepCommand(p);
				System.out.println("ask for doStep(10s)");
				cout.writeObject(doStep);
				StopCondition sc = (StopCondition) cin.readObject();
				System.out.println(sc.stopReason+" "+sc.propertyName+" @ "+sc.timeValue);
			}else
			if (myint == 88) {
				ReadyToReadPredicate p = new ReadyToReadPredicate("notExisting", "justRun");
				DoStepCommand doStep = new DoStepCommand(p);
				System.out.println("ask for doStep infinite");
				cout.writeObject(doStep);
				StopCondition sc = (StopCondition) cin.readObject();

				System.out.println(sc.stopReason+" "+sc.propertyName+" @ "+sc.timeValue);
			}
			myint = keyboard.nextInt();
		}*/


    }

    public void load() {

    }

    public StopCondition doStep(CoordinationPredicate p) {

//		UpgradedPredicate p = new UpgradedPredicate("currentValue", "CPUprotection::cpuTemperature");
        DoStepCommand doStep = new DoStepCommand(p);
        try {
            cout.writeObject(doStep);
//			Thread.sleep(250);
            return (StopCondition) cin.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getVariable(String varQN) {
        //String varQN = "CPUprotection::cpuTemperature::currentValue";
        GetVariableCommand getVar = new GetVariableCommand(varQN);
//		System.out.println("ask for getVariable");
        try {
            cout.writeObject(getVar);
            Object varValue = (Object) cin.readObject();
            return varValue;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Boolean setVariable(String varQN, Object newValue) { //work only with integer value for now
        //String varQN = "CPUprotection::cpuTemperature::currentValue";
        SetVariableCommand setVar = new SetVariableCommand(varQN, new Integer(newValue.toString()));
//		System.out.println("ask for setVariable to "+varQN+" = "+newValue);
        Boolean resValue = false;
        try {
            cout.writeObject(setVar);
            resValue = (Boolean) cin.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
//		System.out.println("value  is set correctly ?: "+resValue);
        return resValue;
    }

    public void terminate() {
        StopCommand stop = new StopCommand();
        try {
            cout.writeObject(stop);
            socket.close();
            Thread.sleep(500);
            controllerExec.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        terminate();
    }

}
