/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bioboxes.bioboxmesossheduler.tasks;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.mesos.Protos;
import org.apache.mesos.Protos.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jsteiner
 */
public class DockerTask implements Task {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(DockerTask.class);

    /**
     * Starttime.
     */
    private Date startTime;
    
    /**
     * The overlying ProtosTask Content to execute.
     */
    private Protos.TaskInfo taskContent;

    /**
     * The needed amount of CPU resources.
     */
    private int needed_CPU;

    /**
     * The needed amount of Memory resources.
     */
    private double needed_MEM;

    /**
     * The actual task state.
     */
    private Protos.TaskState status;

    /**
     * The assigned slave this task will run on.
     */
    private Protos.Offer assignedSlave;

    /**
     * The task queue priority.
     */
    private double priority = 0.0;

    /**
     * The number of waitingtickets. Equals the number of times this task
     * couldn't be run.
     */
    private int waitingTickets = 0;

    /**
     * Number of times this task has failed.
     */
    private int executionErrors = 0;

    public DockerTask() {

    }

    @Override
    public DockerTask createTask(
            int id,
            String dockerImage,
            int maxCPU,
            int maxMEM,
            String principal,
            List hostVolumes,
            List containerVolumes,
            String... arg) {

        Protos.TaskID taskId = Protos.TaskID.newBuilder()
                .setValue(principal + "_" + Integer.toString(id)).build();

        /**
         * DockerContainer Builder.
         */
        Protos.ContainerInfo.DockerInfo.Builder dockerInfoBuilder = Protos.ContainerInfo.DockerInfo.newBuilder();
        dockerInfoBuilder.setImage(dockerImage);
        dockerInfoBuilder.setNetwork(Protos.ContainerInfo.DockerInfo.Network.BRIDGE);
        /**
         * Container Builder.
         */
        Protos.ContainerInfo.Builder containerInfoBuilder = Protos.ContainerInfo.newBuilder();
        containerInfoBuilder.setType(Protos.ContainerInfo.Type.DOCKER);
        containerInfoBuilder.setDocker(dockerInfoBuilder.build());
        // Mount volumes if needed
        if (hostVolumes != null && containerVolumes != null) {
            int nHostVolumes = hostVolumes.size();
            if (nHostVolumes != containerVolumes.size()) {
                logger.error("Volume declarations of host and container differ in length ...");
            }

            for (int i = 0; i < nHostVolumes; i++) {
                containerInfoBuilder.addVolumes(Protos.Volume.newBuilder()
                        .setContainerPath(containerVolumes.get(i).toString())
                        .setHostPath(hostVolumes.get(i).toString())
                        .setMode(Protos.Volume.Mode.RW)
                        .build());
            }
        }
        /**
         * Task Builder.
         */
        Protos.TaskInfo task = Protos.TaskInfo.newBuilder()
                .setName("task " + taskId.getValue())
                .setTaskId(taskId)
                .addResources(Protos.Resource.newBuilder()
                        .setName("cpus")
                        .setType(Protos.Value.Type.SCALAR)
                        .setScalar(Protos.Value.Scalar.newBuilder().setValue(maxCPU)))
                .addResources(Protos.Resource.newBuilder()
                        .setName("mem")
                        .setType(Protos.Value.Type.SCALAR)
                        .setScalar(Protos.Value.Scalar.newBuilder().setValue(maxMEM)))
                .setContainer(containerInfoBuilder)
                .setCommand(Protos.CommandInfo.newBuilder().setShell(false).build())
                .buildPartial(); // partialBuild, because we'll add the slaveID later

        taskContent = Protos.TaskInfo.newBuilder(task).mergeCommand(Protos.CommandInfo.newBuilder().addAllArguments(Arrays.asList(arg)).setShell(false).build()).buildPartial();
        
        this.needed_CPU = (int) getResource("cpus", taskContent.getResourcesList());
        this.needed_MEM = (int) getResource("mem", taskContent.getResourcesList());
        
        return this;
    }

    @Override
    public DockerTask calculatePriority(int numberOfSlaves, Protos.Offer currentSlave) {
        priority = (numberOfSlaves / ((getResource("cpus", currentSlave.getResourcesList()) / this.needed_CPU)
                + (getResource("mem", currentSlave.getResourcesList()) / this.needed_MEM))) + this.waitingTickets;
        return this;
    }

    /**
     *
     * @param type - 'mem' , 'cpus'
     * @param resources
     * @return -1 if error double if default
     */
    private double getResource(String type, List<Protos.Resource> resources) {
        for (Protos.Resource r : resources) {
            if (r.getName().toLowerCase().equals(type)) {
                return r.getScalar().getValue();
            }
        }
        return -1;
    }

    @Override
    public DockerTask prepareToRun(Protos.Offer slave) {
        this.assignedSlave = slave;
        taskContent = TaskInfo.newBuilder(taskContent).mergeSlaveId(slave.getSlaveId()).build();
        startTime = new Date();
        return this;
    }

    // #############################################
    // G and S's
    // #############################################
    public TaskInfo getTaskContent() {
        return taskContent;
    }

    public int getNeeded_CPU() {
        return needed_CPU;
    }

    public double getNeeded_MEM() {
        return needed_MEM;
    }

    public Protos.TaskState getStatus() {
        return status;
    }

    public void setStatus(Protos.TaskState state) {
        this.status = state;
    }

    public Protos.Offer getAssignedSlave() {
        return assignedSlave;
    }

    public double getPriority() {
        return priority;
    }

    public int getWaitingTickets() {
        return waitingTickets;
    }

    public int getExecutionErrors() {
        return executionErrors;
    }

    public void setExecutionErrors(int executionErrors) {
        this.executionErrors = executionErrors;
    }

    public long getRuntimeSeconds() {
        long diff = new Date().getTime() - startTime.getTime();
        return diff / 1000 % 60;
    }
    public long getRuntimeMinutes() {
        long diff = new Date().getTime() - startTime.getTime();
        return diff / (60 * 1000) % 60;
    }
    
}
