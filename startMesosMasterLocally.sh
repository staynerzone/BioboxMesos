#!/bin/bash

rm -rf /tmp/mesos

nohup mesos-master --ip=127.0.0.1 --cluster=Biobox-Mesos-Cluster --authenticate_slaves --authenticate --authenticators=crammd5 --credentials=mesosCredentials.cred --work_dir=/tmp --log_dir=/tmp > mesos-master.log 2>&1 &
