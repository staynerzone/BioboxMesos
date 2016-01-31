/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bioboxes.bioboxmesossheduler.tasks;

import java.util.List;
import org.apache.mesos.Protos;

/**
 *
 * @author jsteiner
 * @param <T> TaskImplementation
 */
public interface Task<T> {

    public T createTask(int id,
            String dockerImage,
            int maxCPU,
            int maxMEM,
            String principal,
            List<String> hostVolumes,
            List<String> containerVolumes,
            String... arg);

    /**
     *
     * @param numberOfSlaves
     * @param currentSlave
     * @return
     */
    public T calculatePriority(int numberOfSlaves, Protos.Offer currentSlave);

    /**
     * Assign selected slave to task and prepare it to run.
     *
     * @param slave The selected Slave
     */
    public T prepareToRun(Protos.Offer slave);

}
