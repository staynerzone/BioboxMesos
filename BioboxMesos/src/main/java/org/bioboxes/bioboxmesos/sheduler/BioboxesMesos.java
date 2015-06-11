package org.bioboxes.bioboxmesos.sheduler;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.protobuf.ByteString;
import java.io.File;
import java.io.IOException;
import org.apache.mesos.*;
import org.apache.mesos.Protos.*;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jsteiner
 */
public class BioboxesMesos {

    public static Process masterPID;
    public static Process slavePID;

    /**
     * Logger.
     */
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(BioboxesMesos.class);

    static {
        try {
            System.load("/usr/local/lib/libmesos.so");
        } catch (UnsatisfiedLinkError e) {
            logger.info(e.getMessage());
        }
    }

    public static void startMesosMaster() {
        new File("/tmp/mesos").mkdir();
        Runtime r = Runtime.getRuntime();
        try {
            masterPID = r.exec("nohup mesos-master --ip=127.0.0.1 --work_dir=/tmp >/tmp/mesos-master.log 2>&1 &");
            logger.info("[SUCCESS] MesosMaster started successfuly at 127.0.0.1:5050");
        } catch (IOException ex) {
            logger.error("[FATAL] Error on starting MesosMaster");
        }
    }

    public static void startMesosSlave() {
        Runtime r = Runtime.getRuntime();
        try {
            slavePID = r.exec("nohup mesos-slave --master=127.0.0.1:5050 --containerizers=docker,mesos >/tmp/mesos-slave.log 2>&1 &");
            logger.info("[SUCCESS] MesosSlave (1/1) successfuly connected to 127.0.0.1:5050");
        } catch (IOException ex) {
            logger.error("[FATAL] Error on starting MesosSlave 1/1");
        }
    }

    public static void startMesosSlaves(int numberOfSlaves) {

    }

    public static void startMesosNetwork(String imageName, int tasks) {

        // If the framework stops running, mesos will terminate all of the tasks that
        // were initiated by the framework but only once the fail-over timeout period
        // has expired. Using a timeout of zero here means that the tasks will
        // terminate immediately when the framework is terminated. For production
        // deployments this probably isn't the desired behavior, so a timeout can be
        // specified here, allowing another instance of the framework to take over.
        final int frameworkFailoverTimeout = 0;

        FrameworkInfo.Builder frameworkBuilder = FrameworkInfo.newBuilder()
                .setName("CodeFuturesExampleFramework")
                .setUser("") // Have Mesos fill in the current user.
                .setFailoverTimeout(frameworkFailoverTimeout); // timeout in seconds

        if (System.getenv("MESOS_CHECKPOINT") != null) {
            System.out.println("Enabling checkpoint for the framework");
            frameworkBuilder.setCheckpoint(true);
        }

        // parse command-line args
        final String dockerImageName = imageName;
        final int totalTasks = tasks;

        // create the scheduler
        final Scheduler scheduler = new BioboxesMesosScheduler(
                dockerImageName,
                totalTasks
        );

        // create the driver
        String mesosMaster = "127.0.0.1:5050";

        MesosSchedulerDriver driver;
        if (System.getenv("MESOS_AUTHENTICATE") != null) {
            System.out.println("Enabling authentication for the framework");

            if (System.getenv("DEFAULT_PRINCIPAL") == null) {
                System.err.println("Expecting authentication principal in the environment");
                System.exit(1);
            }

            if (System.getenv("DEFAULT_SECRET") == null) {
                System.err.println("Expecting authentication secret in the environment");
                System.exit(1);
            }

            Credential credential = Credential.newBuilder()
                    .setPrincipal(System.getenv("DEFAULT_PRINCIPAL"))
                    .setSecret(ByteString.copyFrom(System.getenv("DEFAULT_SECRET").getBytes()))
                    .build();

            frameworkBuilder.setPrincipal(System.getenv("DEFAULT_PRINCIPAL"));

            driver = new MesosSchedulerDriver(scheduler, frameworkBuilder.build(), mesosMaster, credential);
        } else {
            frameworkBuilder.setPrincipal("test-framework-java");

            driver = new MesosSchedulerDriver(scheduler, frameworkBuilder.build(), mesosMaster);
        }

        int status = driver.run()== Status.DRIVER_STOPPED ? 0 : 1;
        // Ensure that the driver process terminates.

        driver.stop();
    }

}
