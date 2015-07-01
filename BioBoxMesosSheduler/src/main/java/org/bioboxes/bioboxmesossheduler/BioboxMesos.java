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
package org.bioboxes.bioboxmesossheduler;

import com.google.protobuf.ByteString;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import org.apache.mesos.*;
import org.apache.mesos.Protos.*;
import org.bioboxes.bioboxmesossheduler.sheduler.MesosScheduler;
import org.bioboxes.bioboxmesossheduler.tools.LibMesosFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example framework that will run a scheduler that in turn will cause Docker
 * containers to be launched.
 *
 * Source code adapted from the example that ships with Mesos.
 */
public class BioboxMesos {

    private static final Logger logger = LoggerFactory.getLogger(BioboxMesos.class);

    /**
     * Show command-line usage.
     */
    private static void usage() {
        String name = BioboxMesos.class.getName();
        System.err.println("Usage: " + name + " master-ip-and-port docker-image-name number-of-instances");
    }

    /**
     * Command-line entry point.
     * <br/>
     * Example usage: java ExampleFramework 127.0.0.1:5050 fedora/apache 2
     *
     * @param args
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {

        BioboxMesos ef = new BioboxMesos(args);

        System.exit(0);
    }

    public BioboxMesos(String[] args) {
        /**
         * Need to find libmesos.so.
         */
        Path startingDir = Paths.get("/usr/");
        String pattern = "libmesos.so";
        LibMesosFinder finder = new LibMesosFinder(pattern);
        try {
            Files.walkFileTree(startingDir, finder);
            System.load(finder.getResult());
        } catch (IOException ex) {
            System.load("/usr/local/lib/libmesos.so");
        }

        /**
         * Get all images to process.
         */
        final List<String> imageNames = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            imageNames.add(args[i]);
        }

        // If the framework stops running, mesos will terminate all of the tasks that
        // were initiated by the framework but only once the fail-over timeout period
        // has expired. Using a timeout of zero here means that the tasks will
        // terminate immediately when the framework is terminated. For production
        // deployments this probably isn't the desired behavior, so a timeout can be
        // specified here, allowing another instance of the framework to take over.
        final int frameworkFailoverTimeout = 25;

        Random r = new Random();
        FrameworkInfo mesosFramework = FrameworkInfo.newBuilder()
                .setName("BioBox-Mesos-Framework_" + r.nextInt(10000))
                .setPrincipal("hannes")
                .setUser("")// Have Mesos fill in the current user.
                .setCheckpoint(true)
                .setFailoverTimeout(frameworkFailoverTimeout)
                .build(); // timeout in seconds

        final Scheduler scheduler = new MesosScheduler(imageNames, mesosFramework);

        Credential credential = Credential.newBuilder()
                .setPrincipal("hannes")
                .setSecret(ByteString.copyFrom("jojo".getBytes()))
                .build();

        MesosSchedulerDriver driver = new MesosSchedulerDriver(scheduler, mesosFramework, args[0], credential);

        driver.run();

    }

}
