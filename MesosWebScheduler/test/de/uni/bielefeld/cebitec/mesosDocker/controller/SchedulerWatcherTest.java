/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni.bielefeld.cebitec.mesosDocker.controller;

import de.uni.bielefeld.cebitec.mesosDocker.SchedulerStarter;
import java.util.List;
import org.apache.mesos.Protos;
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
public class SchedulerWatcherTest {
    
    public SchedulerWatcherTest() {
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
     * Test of init method, of class SchedulerWatcher.
     */
    @Test
    public void testInit() {
        System.out.println("init");
        SchedulerWatcher instance = new SchedulerWatcher();
        instance.init();

        assertEquals(instance.isMasterReachable(), instance.isSlaveReachable());
    }

    /**
     * Test of getResultPath method, of class SchedulerWatcher.
     */
    @Test
    public void testGetResultPath() {
        System.out.println("getResultPath");
        Protos.TaskInfo task = null;
        SchedulerWatcher instance = new SchedulerWatcher();
        String expResult = "";
        String result = instance.getResultPath(task);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of readResult method, of class SchedulerWatcher.
     */
    @Test
    public void testReadResult() {
        System.out.println("readResult");
        SchedulerWatcher instance = new SchedulerWatcher();
        String expResult = "";
        String result = instance.readResult();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }


    /**
     * Test of isMasterReachable method, of class SchedulerWatcher.
     */
    @Test
    public void testIsMasterReachable() {
        System.out.println("isMasterReachable");
        SchedulerWatcher instance = new SchedulerWatcher();
        boolean expResult = false;
        boolean result = instance.isMasterReachable();
        assertEquals(expResult, result);
    }

    /**
     * Test of isSlaveReachable method, of class SchedulerWatcher.
     */
    @Test
    public void testIsSlaveReachable() {
        System.out.println("isSlaveReachable");
        SchedulerWatcher instance = new SchedulerWatcher();
        boolean expResult = false;
        boolean result = instance.isSlaveReachable();
        assertEquals(expResult, result);
    }
    
}
