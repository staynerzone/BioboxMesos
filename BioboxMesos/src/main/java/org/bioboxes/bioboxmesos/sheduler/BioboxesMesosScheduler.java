
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bioboxes.bioboxmesos.sheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jsteiner
 */
public class BioboxesMesosScheduler implements Scheduler {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BioboxesMesosScheduler.class);

    /**
     * Docker image name e..g. "fedora/apache".
     */
    private final String imageName;

    /**
     * Number of instances to run.
     */
    private final int desiredInstances;

    /**
     * List of pending instances.
     */
    private final List<String> pendingInstances = new ArrayList<>();

    /**
     * List of running instances.
     */
    private final List<String> runningInstances = new ArrayList<>();

    /**
     * Task ID generator.
     */
    private final AtomicInteger taskIDGenerator = new AtomicInteger();

    public BioboxesMesosScheduler(String imageName, int desiredInstances) {
        this.imageName = imageName;
        this.desiredInstances = desiredInstances;
    }

    @Override
    public void registered(SchedulerDriver schedulerDriver, Protos.FrameworkID frameworkID, Protos.MasterInfo masterInfo) {
        LOGGER.info("registered() master={}:{}, framework={}", masterInfo.getIp(), masterInfo.getPort(), frameworkID);
    }

    @Override
    public void reregistered(SchedulerDriver schedulerDriver, Protos.MasterInfo masterInfo) {
        LOGGER.info("reregistered()");
    }

    @Override
    public void resourceOffers(SchedulerDriver schedulerDriver, List<Protos.Offer> offers) {

        LOGGER.info("resourceOffers() with {} offers", offers.size());

        for (Protos.Offer offer : offers) {

            List<Protos.TaskInfo> tasks = new ArrayList<>();

            if (runningInstances.size() + pendingInstances.size() < desiredInstances) {

                LOGGER.info("[INFO] RunningInstances: {} -- PendingInstances: {} -- DesiredInstances: {}", runningInstances.size(), pendingInstances.size(), desiredInstances);

                // generate a unique task ID
                Protos.TaskID taskId = Protos.TaskID.newBuilder()
                        .setValue(Integer.toString(taskIDGenerator.incrementAndGet())).build();

                LOGGER.info("Launching task {}", taskId.getValue());
                pendingInstances.add(taskId.getValue());

                // docker image info
                Protos.ContainerInfo.DockerInfo.Builder dockerInfoBuilder = Protos.ContainerInfo.DockerInfo.newBuilder();
                dockerInfoBuilder.setImage(imageName);
                dockerInfoBuilder.setNetwork(Protos.ContainerInfo.DockerInfo.Network.BRIDGE);

                // container info
                Protos.ContainerInfo.Builder containerInfoBuilder = Protos.ContainerInfo.newBuilder();
                containerInfoBuilder.setType(Protos.ContainerInfo.Type.DOCKER);
                containerInfoBuilder.setDocker(dockerInfoBuilder.build());

                // create task to run
                Protos.TaskInfo task = Protos.TaskInfo.newBuilder()
                        .setName("task " + taskId.getValue())
                        .setTaskId(taskId)
                        .setSlaveId(offer.getSlaveId())
                        .addResources(Protos.Resource.newBuilder()
                                .setName("cpus")
                                .setType(Protos.Value.Type.SCALAR)
                                .setScalar(Protos.Value.Scalar.newBuilder().setValue(1)))
                        .addResources(Protos.Resource.newBuilder()
                                .setName("mem")
                                .setType(Protos.Value.Type.SCALAR)
                                .setScalar(Protos.Value.Scalar.newBuilder().setValue(512)))
                        .setContainer(containerInfoBuilder)
                        .setCommand(Protos.CommandInfo.newBuilder().setShell(false))
                        .build();

                tasks.add(task);
            }
            if (tasks.isEmpty() && runningInstances.isEmpty()) {

                LOGGER.info("[ADDITIONAL-INFO] RunningInstances: {} -- PendingInstances: {} -- DesiredInstances: {}", runningInstances.size(), pendingInstances.size(), desiredInstances);

                Protos.Status ps = schedulerDriver.stop();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    ;
                }
                
                switch (ps) {
                    case DRIVER_STOPPED:
                        LOGGER.info("[SUCCESS] Driver stopped successful!");
                        BioboxesMesos.slavePID.destroy();
                        BioboxesMesos.masterPID.destroy();
                        break;
                    case DRIVER_RUNNING:
                        LOGGER.info("[FAILURE] Driver is running already...trying to stop again!");
                        schedulerDriver.stop();
                        break;
                    default:
                        LOGGER.info("[INFO] Driver is not running anymore!");
                        break;
                }

            } else {
                Protos.Filters filters = Protos.Filters.newBuilder().setRefuseSeconds(1).build();
                schedulerDriver.launchTasks(offer.getId(), tasks, filters);
            }
        }
    }

    @Override
    public void offerRescinded(SchedulerDriver schedulerDriver, Protos.OfferID offerID) {
        LOGGER.info("offerRescinded()");
    }

    @Override
    public void statusUpdate(SchedulerDriver driver, Protos.TaskStatus taskStatus) {

        final String taskId = taskStatus.getTaskId().getValue();

        LOGGER.info("statusUpdate() task {} is in state {}",
                taskId, taskStatus.getState());

        switch (taskStatus.getState()) {
            case TASK_RUNNING:
                pendingInstances.remove(taskId);
                runningInstances.add(taskId);
                break;
            case TASK_FAILED:
            case TASK_FINISHED:
                pendingInstances.remove(taskId);
                runningInstances.remove(taskId);
                break;
        }

        LOGGER.info("Number of instances: pending={}, running={}",
                pendingInstances.size(), runningInstances.size());
    }

    @Override
    public void frameworkMessage(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, byte[] bytes) {
        LOGGER.info("frameworkMessage()");
    }

    @Override
    public void disconnected(SchedulerDriver schedulerDriver) {
        LOGGER.info("disconnected()");
    }

    @Override
    public void slaveLost(SchedulerDriver schedulerDriver, Protos.SlaveID slaveID) {
        LOGGER.info("slaveLost()");
    }

    @Override
    public void executorLost(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, int i) {
        LOGGER.info("executorLost()");
    }

    @Override
    public void error(SchedulerDriver schedulerDriver, String s) {
        LOGGER.error("error() {}", s);
    }

}
