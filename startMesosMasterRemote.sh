#!/bin/bash
nohup mesos-master --ip=<publicIP> cluster=Biobox-Mesos-Cluster --authenticate_slaves --authenticate --authenticators=crammd5 --credentials=mesosCredentials.cred --work_dir=/tmp --log_dir=/tmp > mesos-master.log 2>&1 &
