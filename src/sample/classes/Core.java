package sample.classes;

import sample.Configuration;
import sample.Controller;
import sample.Main;
import sample.util.ITickEvent;

import java.util.Random;

public class Core implements ITickEvent
{
    private CPU parent;

    private boolean busy = false;
    private Process currentProcess;

    private Random random = new Random();

    public Core(CPU parent)
    {
        this.parent = parent;
    }

    public void beginProcess(Process process)
    {
        currentProcess = process;
        currentProcess.setResource("CPU");
        currentProcess.setState(Process.State.RUNNING);
        busy = true;
        Main.guiController.updateTable(Controller.Tables.RESOURCES);
    }

    private void finishProcess()
    {
        if(currentProcess.getBurstTime() < currentProcess.getTimeRequired())
        {
            currentProcess.setState(Process.State.TERMINATED);
        }
        else
            currentProcess.setState(Process.State.FINISHED);

        this.currentProcess = null;
        busy = false;
    }

    public void finishProcess(String reason)
    {
        currentProcess.setInterruptionReason(reason);
        currentProcess.setResource("");
        finishProcess();
    }

    public void supplantProcess(Process newProcess)
    {
        currentProcess.setState(Process.State.READY);
        Main.getTaskScheduler().scheduleTask(currentProcess);

        beginProcess(newProcess);
    }

    public Process getCurrentProcess()
    {
        return currentProcess;
    }

    @Override
    public void tickEvent(int currentTime)
    {
        if(currentProcess == null) return;
        if(busy)
        {
            currentProcess.increaseBurstTime(1);

            int percent = Math.round(currentProcess.getTimeRequired() * 0.01f);
            if(currentProcess.getBurstTime() > percent*10 + 5)
            {
                if (random.nextInt(currentProcess.getBurstTime()) < percent*4)
                {
                    Resource r = Main.getSystemResources().get(random.nextInt(Configuration.getResourcesCount()));

                    r.addProcess(currentProcess);
                    this.currentProcess = null;
                    busy = false;
                    return;
                }
            }

            if(Configuration.runtimeErrorsEnabled() && random.nextInt(Configuration.getProcessTerminationChance()) == 0)
            {
                simulateException();
                return;
            }

            if(currentProcess.getTimeRequired() <= currentProcess.getBurstTime())
            {
                finishProcess("Completed.");
            }

            Main.guiController.updateTable(Controller.Tables.RESOURCES);
        }
    }

    public void simulateException()
    {
        finishProcess("Runtime Error (CPU)");
    }

    public boolean isBusy()
    {
        return busy;
    }
}
