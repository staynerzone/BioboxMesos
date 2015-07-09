/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.bielefeld.cebitec.mesosDocker.controller;

import de.uni.bielefeld.cebitec.mesosDocker.SchedulerStarter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.apache.mesos.Protos;

/**
 *
 * @author jsteiner
 */
@SessionScoped
@ManagedBean
public class SchedulerWatcher {

    @ManagedProperty(value = "#{schedulerStarter}")
    private SchedulerStarter scheduler;

    List<Protos.TaskInfo> pendingTasks;
    Map<String, Protos.TaskInfo> runningTasks;
    Map<String, Protos.TaskInfo> finishedTasks;
    List<Protos.TaskInfo> finished;
    List<Protos.TaskInfo> running;
    List<Protos.TaskInfo> pending;
    
    private int maxSystemCPUCores;

    private boolean masterReachable = false;
    private boolean slaveReachable = false;

    private String taskResult;
    private Protos.TaskInfo selectedTask;

    public SchedulerWatcher() {

    }

    @PostConstruct
    public void init() {
        masterReachable = masterReachable();
        slaveReachable = slaveReachable();
        maxSystemCPUCores = Runtime.getRuntime().availableProcessors();
    }
    
    private boolean pollerFinish = false;
    
    public boolean getPollerFinish() {
        return pollerFinish;
    }

    public void reloadLists() {
        finishedTasks = scheduler.getScheduler().getScheduler().getFinishedTasks();
        pendingTasks = scheduler.getScheduler().getScheduler().getPendingTasks();
        runningTasks = scheduler.getScheduler().getScheduler().getRunningTasks();
        
        finished = new ArrayList<>(finishedTasks.values());
        pending = new ArrayList<>(pendingTasks);
        running = new ArrayList<>(runningTasks.values());
        
        if (pending.isEmpty() && running.isEmpty() && !finished.isEmpty()) {
            pollerFinish = true;
        } else {
            pollerFinish = false;
        }
    }
    /**
     *
     * @param task
     * @return
     */
    public String getResultPath(Protos.TaskInfo task) {
        String slaveID = task.getSlaveId().getValue();
        String frameworkID = scheduler.getScheduler().getScheduler().getFramework().getValue();
        String taskID = task.getTaskId().getValue();
        StringBuilder sb = new StringBuilder();
        sb.append("/tmp/mesos/slaves/");
        sb.append(slaveID).append("/frameworks/");
        sb.append(frameworkID).append("/executors/");
        sb.append(taskID).append("/runs/");
        sb.append("latest").append("/stdout");
        return sb.toString();
    }

    public String readResult() {
        StringBuilder sb = null;
        if (selectedTask != null) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(getResultPath(selectedTask))));
                sb = new StringBuilder();
                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(SchedulerWatcher.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(SchedulerWatcher.class.getName()).log(Level.SEVERE, null, ex);
            }
            return sb.toString();
        } else {
            return "";
        }
    }

    /**
     * Checks if Mesos Master is reachable. (Using std adress 127.0.0.1:5050 as
     * default.
     *
     * @return true if up
     */
    private boolean masterReachable(String... urls) {
        InputStream is = null;
        try {
            URL url;
            if (urls.length == 0) {
                url = new URL("http://127.0.0.1:5050");
            } else {
                url = new URL(urls[0]);
            }
            URLConnection con = url.openConnection();

            is = con.getInputStream();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                is.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Cheacks if at least one(!) slave is up.
     *
     * @return true if up
     */
    private boolean slaveReachable(String... slaveID) {
        if (slaveID.length == 0) {
            if (new File("/tmp/mesos/meta/slaves/latest").exists()) {
                return true;
            }
        } else {
            for (String u : slaveID) {
                if (!new File(u).exists()) {
                    return false;
                }
            }
            return true;
        }
        // unnessecary return  -.-
        return false;
    }

    public SchedulerStarter getScheduler() {
        return scheduler;
    }

    public void setScheduler(SchedulerStarter scheduler) {
        this.scheduler = scheduler;
    }

    public List<Protos.TaskInfo> getPendingTasks() {
        return pendingTasks;
    }

    public List<String> getRunningTasks() {
        return null;
    }

    public List<String> getFinishedTasks() {
        return null;
    }

    public List<Protos.TaskInfo> getFinished() {
        return finished;
    }

    public void setFinished(List<Protos.TaskInfo> finished) {
        this.finished = finished;
    }

    public String getTaskResult() {
        return taskResult;
    }

    public void setTaskResult(String taskResult) {
        this.taskResult = taskResult;
    }

    public Protos.TaskInfo getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(Protos.TaskInfo selectedTask) {
        this.selectedTask = selectedTask;
    }

    public List<Protos.TaskInfo> getRunning() {
        return running;
    }

    public List<Protos.TaskInfo> getPending() {
        return pending;
    }

    public boolean isMasterReachable() {
        return masterReachable;
    }

    public boolean isSlaveReachable() {
        return slaveReachable;
    }

    public int getMaxSystemCPUCores() {
        return maxSystemCPUCores;
    }

}
