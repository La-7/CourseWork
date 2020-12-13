package sample.classes;

import sample.Configuration;
import sample.Controller;
import sample.Main;
import sample.util.ITickEvent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class Resource implements ITickEvent
{
    private final String name;
    private final Queue<Process> queue;
    private Status status;

    private int processTime;
    private Process currentTask;
    private int timer;

    private Random random = new Random();

    public Resource(String name)
    {
        this.name = name;
        queue = new LinkedList<>();
        status = Status.READY;
    }

    public void addProcess(Process process)
    {
        process.setState(Process.State.WAITING);
        queue.add(process);
    }

    private boolean setProcess(Process process)
    {
        if(status == Status.BUSY || process == null) return false;

        timer = 0;
        currentTask = process;
        currentTask.setResource(name);
        process.setState(Process.State.WAITING);
        processTime = Math.floorDiv(process.getTimeRequired(), 100) * random.nextInt(20) + 5;

        Main.guiController.updateTable(Controller.Tables.RESOURCES);

        return true;
    }

    public String getName()
    {
        return name;
    }

    public String getInfo()
    {
        return "";
    }

    public void setStatus(Status value)
    {
        this.status = value;
    }

    public Status getStatus()
    {
        return status;
    }

    public void sendTaskToCPU()
    {
        currentTask.setResource("");
        Main.getTaskScheduler().scheduleTask(currentTask);
        status = Status.READY;
    }

    @Override
    public void tickEvent(int currentTime)
    {
        if(queue.isEmpty()) return;
        if(status == Status.READY)
        {
            if(setProcess(queue.poll()))
                setStatus(Status.BUSY);

            //System.out.println("Process " + currentTask.getName() + " is running on a resource " + getName());
        }
        else if(status == Status.BUSY)
        {
            if (timer < processTime)
            {
                if(Configuration.runtimeErrorsEnabled() && random.nextInt(Configuration.getProcessTerminationChance()) == 0)
                    simulateException();

                timer++;
            }
            else
            {
                sendTaskToCPU();
                setStatus(Status.READY);
            }
        }

        Main.guiController.updateTable(Controller.Tables.RESOURCES);
    }

    public void simulateException()
    {
        currentTask.setState(Process.State.TERMINATED);
        currentTask.setInterruptionReason("Runtime Error (" + name + ")");
        setStatus(Status.READY);
    }

    public ArrayList<Process> getTaskList()
    {
        return new ArrayList<>(queue);
    }

    public Process getCurrentTask()
    {
        return currentTask;
    }

    public void finishWork()
    {
        queue.clear();
        currentTask = null;
    }

    public enum Status
    {
        READY,
        BUSY
    }
}