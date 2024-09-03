# Project Title

Cluster Auto-Healer using Zookeeper and Load Balncing between Workers with Herd Effect Avoidance

## Description

in order to keep a distribued system or app running correctly with load balancing, certain characteristics must be acheived. we present simple idea of keeping a fixed number of wrokers in the cluster by watching the cluster by running master using zookeeper so when a worker fails, we launch a new worker. wrokers join the cluster register as znode in zookeeper and master keeps an eye on them. workers here are simplem, just printing some lines on conole.
work is balanced among workers.

## Getting Started
### SSH
this app uses internally ssh commands between master and slaves so ssh settings must be configured before
and can oppen connections with no errors via exchanging keys.
ssh command has username@host_ip to specify which host to connect and what username we login under so keys must be provided to this username.

### Dependencies

* zookeeper-3.9.1-dependency
* zookeeper server running in a machine (Virtual Machine or container) with identical verison of its dependency

### Installing
* change zookeeper connection info (IP + Port) as the connection to the pre-mentioned server.

### Executing program 
java -jar AutoHealer.jar <port_number> <number_of_workers_to_maintain_on_cluster> <username@host_ip>
and Worker.jar must be with in the same directory as AutoHealer.jar
in linux systems AutoHealer.jar and Worker.jar must be on linux at : /root/AutoHealer/.

### Logging is used
logs are written in logs/app.log

## Authors

Contributors names and contact info

* Name: Mohammed Salameh
* email : mohammedsalameh37693@gmail.com
* LinkedIn : www.linkedin.com/in/mohammed-salameh-8b4811313
