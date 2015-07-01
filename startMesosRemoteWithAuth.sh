#!/bin/bash
# LIBPROCESS_IP = myip
LIBPROCESS_IP=10.68.162.155 nohup mesos-slave --master=129.70.80.111:5050 --authenticatee=crammd5 --credential=/home/jsteiner/mesosCredentials.cred --containerizers=mesos,docker > mesos-slave.log 2>&1 &
