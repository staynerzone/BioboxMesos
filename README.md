# BioboxMesos
Mesos Testing Area for Cloud Computing Course

## 1. Contents and Preparing
* 1.1: Biobox Mesos - NetbeansProject with the corresponding jar @target
* 1.2: Mesos CredentialsFile containing just a single line with only a princial and a secret (e.g. hannes passwd)
  * 1.2.1: The CredentialsFile can be used for both, master AND slave (in case of only one single line containing)
  * 1.2.2: The CredentialsFile for the master has to containt ALL principals working with the cluster (multi-lines of principal->passwd)
  * 1.2.3: The CredentialsFile for the slave must not contain more than 1 file!!
* 1.3: Master and Slave StartScripts (locally and with public IPs)
  * 1.3.1: Within the scripts, the path to the CredentialsFile needs to be edit correctly
  * 1.3.2: Also the IPs marked as e.g. <myPublicIp> need to edit correctly
  
## 2. Starting Mesos (Master & Slave)
* 2.1: Make all needed scripts executable by you via: ``` chmod u+x *.sh ```
* 2.2: Chose either the local-mode or the remote-mode
  * 2.2.1: For both cases there are 2 corresponding scripts (one for master, one for slave)
    * 2.2.1.1: DON'T FORGET TO EDIT THE SCRIPTS AS MENTIONED AT 1.2!
* 2.3: You can check if your master and slave started correctly by visiting the mesos-dashboard at:
  * 2.3.1: Locally:``` 127.0.0.1:5050 ```in your browser
  * 2.3.2: Remote:``` <masterPublicIp>:5050 ``` in your browser
  * 2.3.3: If you can see an activated slave everything worked fine

## 3. Starting Bioboxes-Mesos-Framework with a docker container
* 3.1: ```cd``` into the BioboxMesos/BioboxMesosScheduler directory
* 3.2: Start the scheduler via ``` java -classpath target/bioboxMesosScheduler-0.9-jar-with-dependencies.jar org.bioboxes.bioboxmesossheduler.BioboxMesos 127.0.0.1:5050 "hello-world" ```
  * 3.2.1: The example above is for the LOCAL-MODE! ```(127.0.0.1:5050)```
  * 3.2.1.1: For the REMOTE-MODE you only have to alter the IP address to the masters-public-ip PLUS the LIBPROCESS_IP environment which stores your personal IP: ``` LIBPROCESS_IP=<myPublicIP> java -classpath target/bioboxMesosScheduler-0.9-jar-with-dependencies.jar org.bioboxes.bioboxmesossheduler.BioboxMesos <masterPublicIP>:5050 "hello-world" ```
  * 3.2.1.2: STARTING THE FRAMEWORK WILL ONLY BE ABLE FROM A SLAVE MASCHINE! (In local-mode its unimportant...)
  * 3.2.2: The "hello-world" parameters is just a placeholder for any docker container which is either locally on your maschine or gets pulled from dockerhub

## 4. Get the results
Over the (in 2.3) mentioned Mesos-Dashboard you are able to see if everythin worked fine so far and the stdout results of each slave

## 5. Problems
* 1: In some cases 1 of 3 started Tasks (docker containers) gets lost. This may attend to the standard executor each task gets. For this purpose we will write an own executor in the near future.
* 2: ...
