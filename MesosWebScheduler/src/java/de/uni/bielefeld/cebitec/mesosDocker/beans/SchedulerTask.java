package de.uni.bielefeld.cebitec.mesosDocker.beans;

import de.uni.bielefeld.cebitec.mesosDocker.SchedulerStarter;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import org.apache.mesos.Protos;
import org.primefaces.context.RequestContext;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jsteiner
 */
@ManagedBean
@SessionScoped
public class SchedulerTask {

    private String docker;
    private int cpu = 1;
    private int mem = 256;
    private String parameter;
    private boolean paramNeeded = false;

    private String username = "jsteiner";

    private String pathToResult;
    private Protos.TaskInfo task;
    private Protos.TaskID taskID;

    private boolean mountVolumeOne;
    private String mountVolumeOneHost;
    private String mountVolumeOneContainer;

    private boolean mountVolumeTwo;
    private String mountVolumeTwoHost;
    private String mountVolumeTwoContainer;

    private String resultPath;

    @ManagedProperty(value = "#{schedulerStarter}")
    private SchedulerStarter scheduler;

    public void execute() {
        if (parameter != null && paramNeeded) {
            if (!parameter.isEmpty()) {
                if (mountVolumeOne && !mountVolumeTwo) {
                    List<String> vol1_HOST = new ArrayList<>();
                    List<String> vol1_CONTAINER = new ArrayList<>();
                    vol1_HOST.add(mountVolumeOneHost);
                    vol1_CONTAINER.add(mountVolumeOneContainer);
                    task = scheduler.getScheduler().getScheduler().addTask(docker, cpu, mem, username, vol1_HOST, vol1_CONTAINER, parameter);
                } else if (!mountVolumeOne && mountVolumeTwo) {
                    List<String> vol2_HOST = new ArrayList<>();
                    List<String> vol2_CONTAINER = new ArrayList<>();
                    vol2_HOST.add(mountVolumeTwoHost);
                    vol2_CONTAINER.add(mountVolumeTwoContainer);
                    task = scheduler.getScheduler().getScheduler().addTask(docker, cpu, mem, username, vol2_HOST, vol2_CONTAINER, parameter);
                } else if (!mountVolumeOne && !mountVolumeTwo) {
                    task = scheduler.getScheduler().getScheduler().addTask(docker, cpu, mem, username, null, null, parameter);
                } else if (mountVolumeOne && mountVolumeTwo) {
                    List<String> vol12_HOST = new ArrayList<>();
                    List<String> vol12_CONTAINER = new ArrayList<>();
                    vol12_HOST.add(mountVolumeOneHost);
                    vol12_HOST.add(mountVolumeTwoHost);
                    vol12_CONTAINER.add(mountVolumeOneContainer);
                    vol12_CONTAINER.add(mountVolumeTwoContainer);
                    task = scheduler.getScheduler().getScheduler().addTask(docker, cpu, mem, username, vol12_HOST, vol12_CONTAINER, parameter);
                }
                
            } else {
                FacesContext fc =FacesContext.getCurrentInstance();
                fc.addMessage(null, new FacesMessage("Empty Parameter Error!"));
            }
        } else {
            if (mountVolumeOne && !mountVolumeTwo) {
                    List<String> vol1_HOST = new ArrayList<>();
                    List<String> vol1_CONTAINER = new ArrayList<>();
                    vol1_HOST.add(mountVolumeOneHost);
                    vol1_CONTAINER.add(mountVolumeOneContainer);
                    task = scheduler.getScheduler().getScheduler().addTask(docker, cpu, mem, username, vol1_HOST, vol1_CONTAINER);
                } else if (!mountVolumeOne && mountVolumeTwo) {
                    List<String> vol2_HOST = new ArrayList<>();
                    List<String> vol2_CONTAINER = new ArrayList<>();
                    vol2_HOST.add(mountVolumeTwoHost);
                    vol2_CONTAINER.add(mountVolumeTwoContainer);
                    task = scheduler.getScheduler().getScheduler().addTask(docker, cpu, mem, username, vol2_HOST, vol2_CONTAINER);
                } else if (!mountVolumeOne && !mountVolumeTwo) {
                    task = scheduler.getScheduler().getScheduler().addTask(docker, cpu, mem, username, null, null);
                } else if (mountVolumeOne && mountVolumeTwo) {
                    List<String> vol12_HOST = new ArrayList<>();
                    List<String> vol12_CONTAINER = new ArrayList<>();
                    vol12_HOST.add(mountVolumeOneHost);
                    vol12_HOST.add(mountVolumeTwoHost);
                    vol12_CONTAINER.add(mountVolumeOneContainer);
                    vol12_CONTAINER.add(mountVolumeTwoContainer);
                    task = scheduler.getScheduler().getScheduler().addTask(docker, cpu, mem, username, vol12_HOST, vol12_CONTAINER);
                }
        }
    }

    public void paramChanger() {
        RequestContext rc = RequestContext.getCurrentInstance();
        if (paramNeeded == true) {
            rc.execute("PF('param').enable()");
        } else {
            rc.execute("PF('param').disable()");
        }
    }

    public void mountVolOneChanger() {
        RequestContext rc = RequestContext.getCurrentInstance();
        if (mountVolumeOne == true) {
            rc.execute("PF('mountV1H').enable()");
            rc.execute("PF('mountV1G').enable()");
        } else {
            rc.execute("PF('mountV1H').disable()");
            rc.execute("PF('mountV1G').disable()");
        }
    }

    public void mountVolTwoChanger() {
        RequestContext rc = RequestContext.getCurrentInstance();
        if (mountVolumeTwo == true) {
            rc.execute("PF('mountV2H').enable()");
            rc.execute("PF('mountV2G').enable()");
        } else {
            rc.execute("PF('mountV2H').disable()");
            rc.execute("PF('mountV2G').disable()");
        }
    }

    public String getDocker() {
        return docker;
    }

    public void setDocker(String docker) {
        this.docker = docker;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getMem() {
        return mem;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public String getPathToResult() {
        return pathToResult;
    }

    public void setPathToResult(String pathToResult) {
        this.pathToResult = pathToResult;
    }

    public Protos.TaskInfo getTask() {
        return task;
    }

    public void setTask(Protos.TaskInfo task) {
        this.task = task;
    }

    public Protos.TaskID getTaskID() {
        return taskID;
    }

    public void setTaskID(Protos.TaskID taskID) {
        this.taskID = taskID;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public SchedulerStarter getScheduler() {
        return scheduler;
    }

    public void setScheduler(SchedulerStarter scheduler) {
        this.scheduler = scheduler;
    }

    public String getUsername() {
        return username;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public boolean isParamNeeded() {
        return paramNeeded;
    }

    public void setParamNeeded(boolean paramNeeded) {
        this.paramNeeded = paramNeeded;
    }

    public boolean isMountVolumeOne() {
        return mountVolumeOne;
    }

    public void setMountVolumeOne(boolean mountVolumeOne) {
        this.mountVolumeOne = mountVolumeOne;
    }

    public String getMountVolumeOneHost() {
        return mountVolumeOneHost;
    }

    public void setMountVolumeOneHost(String mountVolumeOneHost) {
        this.mountVolumeOneHost = mountVolumeOneHost;
    }

    public String getMountVolumeOneContainer() {
        return mountVolumeOneContainer;
    }

    public void setMountVolumeOneContainer(String mountVolumeOneContainer) {
        this.mountVolumeOneContainer = mountVolumeOneContainer;
    }

    public boolean isMountVolumeTwo() {
        return mountVolumeTwo;
    }

    public void setMountVolumeTwo(boolean mountVolumeTwo) {
        this.mountVolumeTwo = mountVolumeTwo;
    }

    public String getMountVolumeTwoHost() {
        return mountVolumeTwoHost;
    }

    public void setMountVolumeTwoHost(String mountVolumeTwoHost) {
        this.mountVolumeTwoHost = mountVolumeTwoHost;
    }

    public String getMountVolumeTwoContainer() {
        return mountVolumeTwoContainer;
    }

    public void setMountVolumeTwoContainer(String mountVolumeTwoContainer) {
        this.mountVolumeTwoContainer = mountVolumeTwoContainer;
    }

}
