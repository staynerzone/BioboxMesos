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
import java.util.Random;
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
public class DockerMesos {

    private static final Logger logger = LoggerFactory.getLogger(DockerMesos.class);
    private final MesosSchedulerDriver driver;
    private final MesosScheduler scheduler;

    /**
     * Command-line entry point.
     * <br/>
     * Example usage: java ExampleFramework 127.0.0.1:5050 fedora/apache 2
     *
     * @param args
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        Credential credential = Credential.newBuilder()
                .setPrincipal("hannes")
                .setSecret(ByteString.copyFrom("jojo".getBytes()))
                .build();

        final DockerMesos ef = new DockerMesos("127.0.0.1:5050", credential);
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                ef.start();
            }
        });
        t.start();
//        ef.getScheduler().addTask("hello-world", 1, 256, "jsteiner", null, null);

//        System.exit(0);
    }

    public DockerMesos(String masterIP, Credential mesosFrameworkCredentials) {
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

        int frameworkFailoverTimeout = 25;

        Random r = new Random();
        FrameworkInfo mesosFramework = FrameworkInfo.newBuilder()
                .setName("BioBox-Mesos-Framework_" + r.nextInt(10000))
                .setUser("")// Have Mesos fill in the current user.
                .setFailoverTimeout(frameworkFailoverTimeout)
                .build(); // timeout in seconds

        scheduler = new MesosScheduler();

        if (mesosFrameworkCredentials == null) {
            driver = new MesosSchedulerDriver(scheduler, mesosFramework, masterIP);
        } else {
            driver = new MesosSchedulerDriver(scheduler, mesosFramework, masterIP, mesosFrameworkCredentials);
        }
    }

    public void start() {
        driver.run();
    }

    public MesosScheduler getScheduler() {
        return this.scheduler;
    }

}
