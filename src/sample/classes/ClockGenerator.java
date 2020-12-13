package sample.classes;

import sample.Configuration;
import sample.Main;
import sample.util.ITickEvent;

import java.util.ArrayList;

public class ClockGenerator extends Thread
{
    ArrayList<ITickEvent> attachedComponents;

    private int currentTick = 0;

    private boolean running = false;

    public ClockGenerator(ITickEvent... attachedComponents)
    {
        this.attachedComponents = new ArrayList<>();

        for (ITickEvent item : attachedComponents)
        {
            this.attachedComponents.add(item);
        }
    }

    public void attachSystemComponent(ITickEvent component)
    {
        attachedComponents.add(component);
    }

    @Override
    public void run()
    {
        running = true;

        System.out.println("System clock is running.");

        while(running)
        {
            if(!Main.pauseActive())
            {
                try
                {
                    Thread.sleep(Math.floorDiv(1000, Configuration.getClockTps()));
                    for (ITickEvent item : attachedComponents)
                    {
                        item.tickEvent(currentTick);
                    }
                    currentTick++;
                    Main.guiController.updateTicks();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("System clock is stopped.");
    }

    public int getTime()
    {
        return currentTick;
    }

    public void finishWork() { running = false; }
}
