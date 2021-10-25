# File-exchange-system
File exchange system based on UDP and TCP protocols. The system enables the configuration-free exchange of files between users of the same local network.

Instruction:

In the folder where Rumba.jar is located:

java -jar Rumba.jar

Next select network from the list of available networks. Then select where you want to save files from other users. 
After selecting the folder, the program will ask the network who supports the rumba protocol and all users who
answer affirmatively will be added to our list of users. This list will be updated periodically.

Now we have the possibility to enter the corresponding commands:

*exit - exit the program.

*showusers - shows the users that support the rumba protocol.

*showlist - shows a list of files that the user you specify is sharing.
            We will first be asked to select from the list of users, the user
            whose list of shared files we want to see.

*download - download the file, first we select the user number, from whom we want to download the file and then we enter the path 
	          to the file you want to download.

*addfile - a window pops up, in which you select the file to share with others.

*addfolder - a window pops up in which you select a folder to share with others.

*showmylist - shows a list of files that other users can download from you.


