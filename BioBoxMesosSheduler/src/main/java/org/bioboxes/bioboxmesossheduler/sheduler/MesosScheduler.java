/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.bioboxes.bioboxmesossheduler.sheduler;

import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mesos.Protos.CommandInfo;
import org.apache.mesos.Protos.ExecutorID;
import org.apache.mesos.Protos.ExecutorInfo;
import org.apache.mesos.Protos.Volume;

/**
 * Example scheduler to launch Docker containers.
 */
public class MesosScheduler implements Scheduler {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(MesosScheduler.class);

    /**
     * List of running instances.
     */
    private final List<String> runningInstances = new ArrayList<>();

    /**
     * Task ID generator.
     */
    private final AtomicInteger taskIDGenerator = new AtomicInteger();
    
    private final LinkedList<Protos.TaskInfo> tasks = new LinkedList<>();
    private final LinkedList<Protos.TaskInfo> tasksCopy;
    
    private final Map<Integer, Protos.TaskState> taskMap;
    
    private final int numberOfTasks;
    private int finishedTasks;

    /**
     * C'tor with List of docker images.
     *
     * @param imagesNames
     * @param framework
     */
    public MesosScheduler(final List<String> imagesNames, final Protos.FrameworkInfo framework) {
        
        for (String dockerImage : imagesNames) {
            
            ExecutorID execID = ExecutorID.newBuilder()
                    .setValue(dockerImage + "_executor")
                    .build();
            
            Protos.TaskID taskId = Protos.TaskID.newBuilder()
                    .setValue(Integer.toString(taskIDGenerator.incrementAndGet())).build();
            logger.info("Preparing task {}", taskId.getValue());
            // Docker Builder
            Protos.ContainerInfo.DockerInfo.Builder dockerInfoBuilder = Protos.ContainerInfo.DockerInfo.newBuilder();
            dockerInfoBuilder.setImage(dockerImage);
            dockerInfoBuilder.setNetwork(Protos.ContainerInfo.DockerInfo.Network.BRIDGE);
            // Container Builder
            Protos.ContainerInfo.Builder containerInfoBuilder = Protos.ContainerInfo.newBuilder();
            containerInfoBuilder.setType(Protos.ContainerInfo.Type.DOCKER);
            containerInfoBuilder.setDocker(dockerInfoBuilder.build());
            containerInfoBuilder.addVolumes(Volume.newBuilder()
                    .setContainerPath("/bbx/input")
                    .setHostPath("/tmp/input")
                    .setMode(Volume.Mode.RO)
                    .build());
            containerInfoBuilder.addVolumes(Volume.newBuilder()
                    .setContainerPath("/bbx/output")
                    .setHostPath("/tmp/output")
                    .setMode(Volume.Mode.RW)
                    .build());
//            ExecutorInfo executor = ExecutorInfo.newBuilder()
//                    .setContainer(containerInfoBuilder)
//                    .setExecutorId(execID)
//                    .addResources(Protos.Resource.newBuilder()
//                            .setName("cpus")
//                            .setType(Protos.Value.Type.SCALAR)
//                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(2)))
//                    .addResources(Protos.Resource.newBuilder()
//                            .setName("mem")
//                            .setType(Protos.Value.Type.SCALAR)
//                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(1024)))
//                    .setCommand(CommandInfo.newBuilder().setShell(false).build())
//                    .setFrameworkId(framework.getId())
//                    .setName("BioBoxes-Mesos-Executor")
//                    .build();
            // Task Builder
            Protos.TaskInfo task = Protos.TaskInfo.newBuilder()
                    .setName("task " + taskId.getValue())
                    .setTaskId(taskId)
                    .addResources(Protos.Resource.newBuilder()
                            .setName("cpus")
                            .setType(Protos.Value.Type.SCALAR)
                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(2)))
                    .addResources(Protos.Resource.newBuilder()
                            .setName("mem")
                            .setType(Protos.Value.Type.SCALAR)
                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(1024)))
                    .setContainer(containerInfoBuilder)
                    .setCommand(CommandInfo.newBuilder().setShell(false).build())
//                    .setExecutor(executor)
                    //                    .setCommand(Protos.CommandInfo.newBuilder().setShell(false))
                    .buildPartial(); // partialBuild, because we'll add the slaveID later
            tasks.add(task);
        }
        tasksCopy = new LinkedList<>(tasks);
        taskMap = new HashMap<>(tasksCopy.size());
        numberOfTasks = tasksCopy.size();
    }
    
    @Override
    public void registered(SchedulerDriver schedulerDriver, Protos.FrameworkID frameworkID, Protos.MasterInfo masterInfo) {
        logger.info("registered() master={}:{}, framework={}", masterInfo.getIp(), masterInfo.getPort(), frameworkID);
    }
    
    @Override
    public void reregistered(SchedulerDriver schedulerDriver, Protos.MasterInfo masterInfo) {
        logger.info("reregistered()");
    }
    
    private synchronized void addTaskMapEntry(String key, Protos.TaskState state) {
        this.taskMap.put(Integer.valueOf(key), state);
    }
    
    private final boolean cleanTaskMap() {
        boolean clean = true;
        if (taskMap.size() != tasksCopy.size()) {
            return false;
        }
        for (Protos.TaskState ts : taskMap.values()) {
            if (ts.equals(Protos.TaskState.TASK_ERROR)
                    || ts.equals(Protos.TaskState.TASK_FAILED)
                    || ts.equals(Protos.TaskState.TASK_KILLED)) {
                clean = false;
            }
        }
        return clean;
    }
    
    @Override
    public void resourceOffers(SchedulerDriver schedulerDriver, List<Protos.Offer> offers) {
        
        synchronized (this) {
            if (numberOfTasks == finishedTasks) {
                schedulerDriver.abort();
                while (!schedulerDriver.join().equals(Protos.Status.DRIVER_ABORTED)) {
                }
                schedulerDriver.stop(true);
                while (!schedulerDriver.join().equals(Protos.Status.DRIVER_STOPPED)) {
                }
                disconnected(schedulerDriver);
            }
        }
        
        logger.info("resourceOffers() with {} offers", offers.size());
        
        
        
        for (final Protos.Offer offer : offers) {
            List<Protos.OfferID> offerIDs = new ArrayList<>();
            offerIDs.add(offer.getId());
            /**
             * Add the needed slaveID from the Protos Offer.
             */
            List<Protos.TaskInfo> tasksToRun = new ArrayList<>();
            List<Protos.TaskInfo> tasksClone = new ArrayList<>(tasks);
            for (Protos.TaskInfo task : tasksClone) {
                Protos.TaskInfo readTask = Protos.TaskInfo.newBuilder(task).mergeSlaveId(offer.getSlaveId()).build();
                tasksToRun.add(readTask);
                tasks.remove(task);
            }
            
            Protos.Filters filters = Protos.Filters.newBuilder().setRefuseSeconds(10).build();
            schedulerDriver.launchTasks(offerIDs, tasksToRun, filters);
            
        }
    }
    
    @Override
    public void offerRescinded(SchedulerDriver schedulerDriver, Protos.OfferID offerID) {
        logger.info("offerRescinded()");
    }
    
    @Override
    public void statusUpdate(SchedulerDriver driver, Protos.TaskStatus taskStatus) {
        
        final String taskId = taskStatus.getTaskId().getValue();
        
        logger.info("statusUpdate() task {} is in state {}",
                taskId, taskStatus.getState());
        
        switch (taskStatus.getState()) {
            case TASK_RUNNING:
                runningInstances.add(taskId);
                addTaskMapEntry(taskId, taskStatus.getState());
                break;
            case TASK_FINISHED:
                runningInstances.remove(taskId);
                finishedTasks++;
                addTaskMapEntry(taskId, taskStatus.getState());
                break;
            default:
                logger.info("Task-Problem: Cause = {}", taskStatus.getState());
                addTaskMapEntry(taskId, taskStatus.getState());
                break;
        }
        
        logger.info("Number of instances: running={} Content={}", runningInstances.size(), runningInstances);
        
        if (!runningInstances.isEmpty()) {
            List<String> nextRuns = new ArrayList<>();
            for (String i : runningInstances) {
                nextRuns.add(tasksCopy.get(Integer.valueOf(i) - 1).getContainer().getDocker().getImage());
            }
            logger.info("dockerImages to Run: {}", nextRuns);
        }
    }
    
    @Override
    public void frameworkMessage(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, byte[] bytes) {
        logger.info("frameworkMessage()");
    }
    
    @Override
    public void disconnected(SchedulerDriver schedulerDriver) {
        logger.info("disconnected()");
    }
    
    @Override
    public void slaveLost(SchedulerDriver schedulerDriver, Protos.SlaveID slaveID) {
        logger.info("slaveLost()");
    }
    
    @Override
    public void executorLost(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, int i) {
        logger.info("executorLost()");
    }
    
    @Override
    public void error(SchedulerDriver schedulerDriver, String s) {
        logger.error("error() {}", s);
    }
    
}
