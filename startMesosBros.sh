#!/bin/bash

rm -rf /tmp/mesos

nohup mesos-master --ip=127.0.0.1 --authenticate_slaves --authenticate --authenticators=crammd5 --credentials=mesosCredentials.cred --work_dir=/tmp --log_dir=/tmp > mesos-master.log 2>&1 &
nohup mesos-slave --master=127.0.0.1:5050 --authenticatee=crammd5 --credential=mesosCredentials.cred --containerizers=mesos,docker > mesos-slave.log 2>&1 &
