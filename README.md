#How to run
Run tu.berlin.run.Simulation as MainClass  
I tried to make it executable via maven but failed to do so for now, see pom.xml

#Notes
* the router has basically 0 functionality
* NetworkNode.TYPE currently identifies the node, it could be a transparent (invisible) field to the adversary - could alternatively work with IDs or whatever
* NetworkTopology now correctly displays only Remote Services of connected/accessible hosts
* Knowledge interfaces added which can be used to determine possible actions
* TODO: Knowledge interfaces still needs all necessary methods and Implementations
* TODO: When all knowledge interfaces are set, AdversaryAction methods can be implemented with pre & postconditions
* State, Actions, Percept etc. are only rudimentary placeholders inspired by Linus prototype, feel free to implement


#Bugs
* Router doesnt relay services of webserver and adminpc yet