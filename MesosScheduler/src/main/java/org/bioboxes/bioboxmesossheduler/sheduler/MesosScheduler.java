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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.mesos.Protos.CommandInfo;
import org.apache.mesos.Protos.FrameworkID;
import org.apache.mesos.Protos.Volume;
import org.bioboxes.bioboxmesossheduler.comparator.DockerTaskComparator;
import org.bioboxes.bioboxmesossheduler.tasks.DockerTask;

/**
 * Example scheduler to launch Docker containers.
 */
public class MesosScheduler implements Scheduler {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(MesosScheduler.class);

    /**
     * Task ID generator.
     */
    private final AtomicInteger taskIDGenerator = new AtomicInteger();

    private final List<DockerTask> pendingTasks = new ArrayList<>();
    private final List<DockerTask> stagingTasks = new ArrayList<>();
    private final List<DockerTask> runningTasks = new ArrayList<>();
    private final List<DockerTask> finishedTasks = new ArrayList<>();
    private final List<DockerTask> failedTasks = new ArrayList<>();

    private Protos.FrameworkID frameworkID;

    /**
     * C'tor with List of docker images.
     *
     */
    public MesosScheduler() {
    }

    public Protos.TaskInfo addTask(String dockerImage, int maxCPU, int maxMEM, String principal, List<String> hostVolumes, List<String> containerVolumes, String... arg) {
        DockerTask newDT = new DockerTask()
                .createTask(taskIDGenerator.incrementAndGet(), dockerImage, maxCPU, maxMEM, principal, hostVolumes, containerVolumes, arg);
        pendingTasks.add(newDT);
        logger.info("PendingTasks Size {}", pendingTasks.size());
        return newDT.getTaskContent();
    }

    @Override
    public void registered(SchedulerDriver schedulerDriver, Protos.FrameworkID frameworkID, Protos.MasterInfo masterInfo) {
        logger.info("registered() master={}:{}, framework={}", masterInfo.getIp(), masterInfo.getPort(), frameworkID);
        this.frameworkID = frameworkID;
    }

    @Override
    public void reregistered(SchedulerDriver schedulerDriver, Protos.MasterInfo masterInfo) {
        logger.info("reregistered()");
    }

    @Override
    public void resourceOffers(SchedulerDriver schedulerDriver, List<Protos.Offer> offers) {

        logger.info("resourceOffers() with {} offers", offers.size());

        /**
         * Adding rule to devide resources. Maybe ( numberOfOffers / #containers
         * ) = amount per offer
         */
        for (final Protos.Offer offer : offers) {

            if (pendingTasks.isEmpty()) {

                schedulerDriver.declineOffer(offer.getId()); // no work

            } else {

                for (DockerTask d : pendingTasks) {
                    d.calculatePriority(offers.size(), offer); // calculate priority
                }
                
                Collections.sort(pendingTasks, new DockerTaskComparator()); // sort by priority

                double available_CPU = getResource("cpus", offer); // slave cpu
                double available_MEM = getResource("mem", offer); // slave mem

                stagingTasks.clear();

                while (!pendingTasks.isEmpty()) {

                    DockerTask actualTask = pendingTasks.get(0);

                    double needed_CPU = actualTask.getNeeded_CPU();
                    double needed_MEM = actualTask.getNeeded_MEM();

                    if (available_CPU >= needed_CPU
                            && available_MEM >= needed_MEM) {

                        available_CPU -= needed_CPU;
                        available_MEM -= needed_MEM;

                        pendingTasks.remove(actualTask);
                        stagingTasks.add(actualTask.prepareToRun(offer));

                        logger.debug("Added Task: " + actualTask.getTaskContent().getTaskId().getValue());
                    } else {
                        logger.warn("BREAK: Task execution not possible. Offer resources exhausted");
                        break;
                    }
                }
                if (!stagingTasks.isEmpty()) {

                    Protos.Filters filters = Protos.Filters.newBuilder().setRefuseSeconds(10).build();
                    List<Protos.TaskInfo> tmp = new ArrayList<>();

                    for (DockerTask t : stagingTasks) {
                        tmp.add(t.getTaskContent());
                        runningTasks.add(t);
                    }

                    schedulerDriver.launchTasks(offer.getId(), tmp, filters);

                    logger.info("Started Tasks: {} on Slave: ({})", tmp.size(), offer.getId().getValue());
                } else {
                    schedulerDriver.declineOffer(offer.getId());
                }
            }
        }
    }

    /**
     *
     * @param type - 'mem' or 'cpus'
     * @param offer
     * @return
     */
    private double getResource(String type, Protos.Offer offer) {
        for (Protos.Resource r : offer.getResourcesList()) {
            if (r.getName().equals(type)) {
                return r.getScalar().getValue();
            }
        }
        return -1;
    }

    @Override
    public void offerRescinded(SchedulerDriver schedulerDriver, Protos.OfferID offerID) {
        logger.info("offerRescinded()");
    }

    @Override
    public void statusUpdate(SchedulerDriver driver, Protos.TaskStatus taskStatus) {

        final String taskId = taskStatus.getTaskId().getValue();

        DockerTask actualTask = null;

        for (DockerTask t : runningTasks) {
            if (t.getTaskContent().getTaskId().getValue().equals(taskId)) {
                actualTask = t;
                break;
            }
        }

        actualTask.setStatus(taskStatus.getState());

        logger.info("statusUpdate() task {} is in state {}",
                taskId, actualTask.getStatus());

        switch (actualTask.getStatus()) {
            case TASK_RUNNING:
                logger.info("Task [{}] running ( {} seconds | {} minutes )", actualTask.getTaskContent().getTaskId().getValue(), actualTask.getRuntimeSeconds(), actualTask.getRuntimeMinutes());
                break;
            case TASK_FINISHED:
                logger.info("Task {} FINISHED (in {} seconds)", actualTask.getTaskContent().getTaskId().getValue(), actualTask.getRuntimeSeconds());
                finishedTasks.add(actualTask);
                runningTasks.remove(actualTask);
                break;
            default:
                logger.warn("Task-Problem: Cause = {}", taskStatus.getReason());
                if (actualTask.getExecutionErrors() <= 3) {
                    switch (taskStatus.getReason()) {
                        case REASON_COMMAND_EXECUTOR_FAILED:
                            actualTask.setExecutionErrors(actualTask.getExecutionErrors() + 1);
                            logger.info("Retrying Task Execution ...");
                            logger.debug("REASON: {}", taskStatus.getReason());
                            pendingTasks.add(actualTask);
                            runningTasks.remove(actualTask);
                            break;
                    }
                } else {
                    logger.warn("Task [{}] gets dequeued in case of a three-timed failed execution.", actualTask.getTaskContent().getTaskId());
                    logger.warn("Task [{}] -> latest Reason [{}]", taskStatus.getReason());
                    runningTasks.remove(actualTask);
                }
                break;
        }

        logger.info("RUNNING Tasks={}", runningTasks.size());
        logger.info("PENDING Tasks={}", pendingTasks.size());
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

    public List<DockerTask> getPendingTasks() {
        return pendingTasks;
    }

    public List<DockerTask> getRunningTasks() {
        return runningTasks;
    }

    public List<DockerTask> getFinishedTasks() {
        return finishedTasks;
    }

    public List<DockerTask> getFailedTasks() {
        return failedTasks;
    }

    public FrameworkID getFramework() {
        return this.frameworkID;
    }
}
