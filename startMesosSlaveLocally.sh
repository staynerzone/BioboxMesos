#!/bin/bash

rm -rf /tmp/mesos

nohup mesos-slave --master=127.0.0.1:5050 --authenticatee=crammd5 --credential=mesosCredentials.cred --containerizers=mesos,docker > mesos-slave.log 2>&1 &
