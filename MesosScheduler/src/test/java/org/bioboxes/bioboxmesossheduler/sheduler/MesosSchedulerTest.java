/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bioboxes.bioboxmesossheduler.sheduler;

import java.util.ArrayList;
import java.util.List;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.bioboxes.bioboxmesossheduler.tasks.DockerTask;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jsteiner
 */
public class MesosSchedulerTest {

    public MesosSchedulerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of resourceOffers method, of class MesosScheduler.
     */
    @Test
    public void testResourceOffers() {
        System.out.println("resourceOffers");

        MesosScheduler instance = new MesosScheduler();

        // ############## Image,mem,cpu,   user   , mounts,mounts
        instance.addTask("test", 2, 2, "jsteiner1", null, null);
        instance.addTask("test", 1, 1, "jsteiner2", null, null);
        instance.addTask("test", 3, 2, "jsteiner3", null, null);
        instance.addTask("test", 1, 4, "jsteiner4", null, null);

        List<Protos.Offer> offers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            offers.add(Protos.Offer.newBuilder()
                    .addResources(Protos.Resource.newBuilder()
                            .setName("cpus")
                            .setType(Protos.Value.Type.SCALAR)
                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(4)))
                    .addResources(Protos.Resource.newBuilder()
                            .setName("mem")
                            .setType(Protos.Value.Type.SCALAR)
                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(4)))
                    .setId(Protos.OfferID.newBuilder().setValue("dasdsadsdadsad").build())
                    .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("klkklkkll").build())
                    .setSlaveId(Protos.SlaveID.newBuilder().setValue(i + " 12345z6t").build())
                    .setHostname("tetzkatlipoklan")
                    .build());
        }

        instance.resourceOffers(new MesosSchedulerDriver(instance,
                Protos.FrameworkInfo.newBuilder()
                .setHostname("test")
                .setName("tests")
                .setUser("jsteiner")
                .build(),
                "127.0.0.1:5050"), offers);

        System.out.println("Pending: " + instance.getPendingTasks().size());
        System.out.println("Running: " + instance.getRunningTasks().size());
        System.out.println("Finished: " + instance.getFinishedTasks().size());

        /**
         * Expected not empty running and empty pending list at the end to
         * ensure a correct working scheduler. There can't be a status update to
         * finished so far cause of a scheduler emulation only
         */
        boolean expected = true, result = instance.getPendingTasks().isEmpty()
                && instance.getRunningTasks().size() == 4; // use-only with no mesos cluster

        // use only with working mesos cluster!
//        boolean expected = true, result = instance.getRunningTasks().isEmpty() 
//                && instance.getPendingTasks().isEmpty() 
//                && !instance.getFinishedTasks().isEmpty();
        assertEquals(expected, result);
    }

    /**
     * Test of statusUpdate method, of class MesosScheduler.
     */
    @Test
    public void testStatusUpdate() {
        System.out.println("statusUpdate");
        SchedulerDriver driver = null;
        Protos.TaskStatus taskStatus = null;
//        MesosScheduler instance = new MesosScheduler();
//        instance.statusUpdate(driver, taskStatus);
        // TODO review the generated test code and remove the default call to fail.
        System.out.println("Not implemented yet");
    }

}
