iDectectProject
===============

20CS6063

Lab 1 Requirements
==================
The Scenario - Your established client, Mugscript, has been experiencing more failures as its data center grows. A failure detection system for its servers is needed for continuity of service and notification. You have been directed to build iDetect, a distributed crash-stop failure detection utility.
Functional Requirements:

<s>A1. iDetect shall support a minimum of 4 networked nodes.</s> - done

<s>A2. The failure of any single node shall be reported by at least one non-faulty node within 8 seconds. It may be assumed that RTCs among the nodes are synchronized (e.g., using NTP).</s> - done

<s>A3. The failure of any two nodes simultaneously shall be reported by at least one working node within 10 seconds.</s> - done

<s>A4. iDetect shall produce no false positive reports of failure when no packets are lost in network communications.</s> - done

<s>A5. iDetect shall produce no more than 1 false positive report in any 5 second period when 20% of packets are lost at random in network communications.</s> - done

<s>A6. The network overhead bandwidth used for failure detection should be minimized.</s> - done

Lab 2 Requirements
==================
The Scenario - Your established client, Mugscript, has begun using your failure detector from Lab 1 in their data center, and is now interested in extending some capabilities.  They want to create a general-purpose leader election framework iLead, that is integrated with the failure detector.
You are to team in groups of three (I think we have 21 students now), and select among your failure detector implementations as the starting point for this lab.
Functional Requirements:

<s>A1. iLead shall be tested with a minimum of 12 networked processes on at least two host machines.</s> - done

<s>A2. Upon startup, an iLead process must automatically seek an established group, if one exists, or establish a group if it does not.</s> - done

<s>A3. When the current leader process fails, iLead shall establish a new leader process in an average of 3 seconds.</s> - done

<s>A4. When the current leader process fails in combination with any number of other processes, iLead shall establish a new leader process in an average of 3 seconds.</s> - done

<s>A5. In an environment with 10% packet loss, iLead shall average no more than one unnecessary leader election per minute.</s> - done

<s>A6. Network bandwidth overhead should be minimized.</s> - done

Lab 3 Requirements
==================
As Mugscript’s data center operations become more complex, managing its services places a few new requirements on iDetect/iLeadAs Mugscript’s data center operations become more complex, managing its services places a few new requirements on iDetect/iLead.
Functional Requirements:

<s>A7. Processes must use a deterministic identifier (e.g., meaningful name or number assigned to that process)</s> - done

<s>A8. A process must report an error if it attempts to join a group that already has a correct process sharing the same identifier</s> - done

<s>A9. If a process fails and restarts, it must be able to rejoin the group using the same identifier, but the fail/restart must be reported even if no other process detected the failure</s> - done

<s>A10. If a leader process fails and restarts, an election must take place</s> - done

Lab 4 Requirements
==================
A1. Each process in the iTolerate system shall have a definition for the number of processes operating in the system configuration

     –“N” necessary to determine quorum quantities
     – does not fluctuate, even with network partitions, for our exercise

A2. iTolerate shall maintain consensus functionality so long as strictly more than 2/3 of the processes in the system are correct

     – I.e., int(2*N/3+1) or greater must be operating as designed, where N is the total number of processes in the system
     – This functionality must be maintained even if incorrect processes are sending erroneous and misleading messages 

A3. In the event of a network partition that leaves strictly more than 2/3 of the correct processes in the system in a single partition, the processes in that partition shall maintain consensus value functionality

A4. In the event of a network partition, all processes in a partition that does not contain strictly more than 2/3 of the correct processes in the system shall signal an inability to carry out consensus operations

    – This will include ether the “minority” side of a partition, or a “majority” side that contains enough Byzantine-failed processes to leave 2/3*N or fewer correct processes

A5. An incorrect leader shall be detected by correct processes within 1 minute and a new election held to elect a correct process

     – Need a “consensus value” – a value that the leader periodically announces – to determine whether the leader is acting properly
          - We already have a value (N) if you just want to use that, or make up your own
     – You do not need to defend against the “framed process” scenario that we discussed in class

Verify the requirements; this will necessitate the ability to simulate Byzantine fault behavior with a number of processes, as well as a network partition

