///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.bioboxes.bioboxmesossheduler.comparator;
//
//import java.util.Collections;
//import java.util.List;
//import org.apache.mesos.Protos;
//import org.bioboxes.bioboxmesossheduler.DockerMesos;
//import org.bioboxes.bioboxmesossheduler.tasks.DockerTask;
//import org.junit.After;
//import org.junit.AfterClass;
//import org.junit.Before;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import static org.junit.Assert.*;
//
///**
// *
// * @author jsteiner
// */
//public class DockerTaskComparatorTest {
//
//    public DockerTaskComparatorTest() {
//    }
//
//    @BeforeClass
//    public static void setUpClass() {
//    }
//
//    @AfterClass
//    public static void tearDownClass() {
//    }
//
//    @Before
//    public void setUp() {
//    }
//
//    @After
//    public void tearDown() {
//    }
//
//    /**
//     * Test of compare method, of class DockerTaskComparator.
//     */
//    @Test
//    public void testCompare() {
//        System.out.println("compare");
//        DockerMesos dm = new DockerMesos("127.0.0.1:5050", null);
//        dm.getScheduler().addTask("test", 2, 2, "jsteiner1", null, null);
//        dm.getScheduler().addTask("test", 1, 1, "jsteiner2", null, null);
//        dm.getScheduler().addTask("test", 3, 2, "jsteiner3", null, null);
//        dm.getScheduler().addTask("test", 1, 4, "jsteiner4", null, null);
//
//        List<DockerTask> tmp = dm.getScheduler().getPendingTasks();
//        for (DockerTask t : tmp) {
//            t.calculatePriority(2, Protos.Offer.newBuilder()
//                    .addResources(Protos.Resource.newBuilder()
//                            .setName("cpus")
//                            .setType(Protos.Value.Type.SCALAR)
//                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(4)))
//                    .addResources(Protos.Resource.newBuilder()
//                            .setName("mem")
//                            .setType(Protos.Value.Type.SCALAR)
//                            .setScalar(Protos.Value.Scalar.newBuilder().setValue(4)))
//                    .setId(Protos.OfferID.newBuilder().setValue("dasdsadsdadsad").build())
//                    .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("klkklkkll").build())
//                    .setSlaveId(Protos.SlaveID.newBuilder().setValue("12345z6t").build())
//                    .setHostname("tetzkatlipoklan")
//                    .build());
//        }
//
//        Collections.sort(tmp, new DockerTaskComparator());
//        DockerTask selected = tmp.get(0);
//
//        System.out.println(selected.getTaskContent().getTaskId().getValue());
//
//        /**
//         * Expected is the task with the highest priority as first element.
//         */
//        boolean expected = true;
//        boolean actual = selected.getTaskContent().getTaskId().getValue().contains("jsteiner3");
//
//        assertEquals(expected, actual);
//    }
//
//}
