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

A7. Processes must use a deterministic identifier (e.g., meaningful name or number assigned to that process)

A8. A process must report an error if it attempts to join a group that already has a correct process sharing the same identifier

A9. If a process fails and restarts, it must be able to rejoin the group using the same identifier, but the fail/restart must be reported even if no other process detected the failure

A10. If a leader process fails and restarts, an election must take place

