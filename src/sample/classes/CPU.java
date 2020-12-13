package sample.classes;

import sample.Main;
import sample.util.ITickEvent;

import java.util.ArrayList;

public class CPU implements ITickEvent {
    private Core[] cores;

    private int inactivityTicks = 0;

    public CPU(final int coresNumber) {
        cores = new Core[coresNumber];
        for (int i = 0; i < coresNumber; i++) {
            cores[i] = new Core(this);
        }
    }

    @Override
    public void tickEvent(int currentTime) {
        int freeCores = 0;
        for (Core core : cores) {
            core.tickEvent(currentTime);
            if (!core.isBusy()) freeCores++;
        }
        if (freeCores == cores.length) {
            inactivityTicks++;
            Main.guiController.updateCPUInactivity();
        }
    }

    public boolean beginProcess(Process process) {
        Core core = getFirstAvailableCore();
        if (core != null) {
            core.beginProcess(process);
            return true;
        }
        return false;
    }

    public void finishWork() {
        for (Core core : cores) {
            if (core.isBusy()) {
                core.finishProcess("CPU shutdown.");
            }
        }
    }

    public Core getFirstAvailableCore() {
        for (Core core : cores) {
            if (!core.isBusy()) return core;
        }
        return null;
    }

    public int getLowestPriorityIndex() {
        int lowestPriorityIndex = 0;
        for (int i = 1; i < cores.length; i++) {
            if (cores[i].getCurrentProcess().getPriority() > cores[lowestPriorityIndex].getCurrentProcess().getPriority()) {
                lowestPriorityIndex = i;
            }
        }
        return lowestPriorityIndex;
    }

    public Core getCore(int index) {
        if (index > cores.length || index < 0) return null;
        return cores[index];
    }

    public boolean hasFreeCore() {
        return getFirstAvailableCore() != null;
    }

    public ArrayList<Process> getCoresContent() {
        ArrayList<Process> result = new ArrayList<>();
        for (Core core : cores) {
            if (core.getCurrentProcess() != null)
                result.add(core.getCurrentProcess());
        }

        return result;
    }

    public int getInactivityTicks() {
        return inactivityTicks;
    }
}
