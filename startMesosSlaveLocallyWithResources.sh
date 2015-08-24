#!/bin/bash
nohup mesos-slave --master=127.0.0.1:5050 --resources='cpus:2;mem:2048' --authenticatee=crammd5 --credential=/home/jsteiner/mesosCredentials.cred --containerizers=mesos,docker > mesos-slave.log 2>&1 &
