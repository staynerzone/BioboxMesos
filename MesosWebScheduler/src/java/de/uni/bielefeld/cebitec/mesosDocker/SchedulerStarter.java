package de.uni.bielefeld.cebitec.mesosDocker;


import com.google.protobuf.ByteString;
import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.apache.mesos.Protos;
import org.bioboxes.bioboxmesossheduler.DockerMesos;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jsteiner
 */
@ApplicationScoped
@ManagedBean
public class SchedulerStarter {
    
    private DockerMesos scheduler;

    @PostConstruct
    public void init() {
        Protos.Credential credential = Protos.Credential.newBuilder()
                .setPrincipal("hannes")
                .setSecret(ByteString.copyFrom("jojo".getBytes()))
                .build();
        scheduler = new DockerMesos("127.0.0.1:5050", credential);
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                scheduler.start();

            }
        });
        t.start();
    }

    public DockerMesos getScheduler() {
        return scheduler;
    }
    
}
