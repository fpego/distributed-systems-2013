Replicated data storage
Implement a replicated data storage. For simplicity assume that the data to be replicated are integer numbers,
each identified by a unique id. N servers keep a copy of the data shared by clients, offering two primitives:
int read(dataId) and void write(dataId, newValue).
The client may connect to any of these servers, which cooperate to keep a consistent, duplicate copy of
the shared data.
Assuming servers are reliable but channels are not, implement the system to guarantee a sequential consistency model.