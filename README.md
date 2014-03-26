Proxied-Zombie-Port-Simulator
=============================

Purpose
-------
Simulates the behavior of a zombie port on the client server. This is an evolution of the Zombie-Port-Simulator project. This accepts connections on one port (listener port), opens up a connection to another port (destination port) and passes all communications between the two ports until a key on the keyboard is struck. At this point, if it is in "read" mode the listener port data will be read but not repeated to the destination port. Otherwise, if in "no read" mode, the listener port will be open, but no data sent to it will be read or repeated to the destination port. In both cases, destination port communications will be ignored and not repeated.


Author
------
David Williams
January 4, 2012
