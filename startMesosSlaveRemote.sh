#!/bin/bash
LIBPROCESS_IP=<myPublicIp> nohup mesos-slave --master=<masterPublicIp>:5050 --authenticatee=crammd5 --credential=/home/jsteiner/mesosCredentials.cred --containerizers=mesos,docker > mesos-slave.log 2>&1 &
