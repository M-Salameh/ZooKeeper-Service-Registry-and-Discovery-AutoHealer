to Run AutoHealer.jar:
java -jar AutoHealer.jar <port_number> <number_of_workers_to_maintain_on_cluster> <username@host_ip>

and Worker.jar must be with in the same directory as AutoHealer.jar


logs are written in logs/app.log

we must start windows first then virtual machines

SSH Command FAILURE !!:
from windows to windows
from linux to windows
from linux to linux

if we started at windows first then every thing works fine.

how Linux Must be configured:
AutoHealer.jar and Worker.jar must be on linux at : /root/AutoHealer/.

cd /root/AutoHealer/
java -jar AutoHealer.jar <port_number> <number_of_workers_to_maintain_on_cluster> <username@host_ip>