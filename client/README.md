# Client

It is a set of communication techniques between peers. Every technique is described 
below.

## TurnAwareClientProtocol "framework"

**_NOTE: before you read this, please refer to [udp-core](../udp-core/README.md) 
module description, to get full understanding_**

**_TurnAwareClientProtocol_** is an abstract implementation of **_ClientProtocol_** 
interface. It encapsulates all communication nuances, like switching protocols and 
packet exchanges via TURN server, so developer focuses primarily only on protocol 
implementation.

In terms of nuances, **_TurnAwareClientProtocol_** will define NAT behaviour, if 
it is not specified explicitly. Depending on NAT behaviour, it will establish a 
TURN session and communicate with opponent peers through it. There is no need to 
refresh session, create channels / permissions: everything is handled inside 
this abstraction.

For example implementation, see **_PureProtocol_** implementation in this module.

## Usage

No matter, what usage scenario is suitable for you. Every client initialization 
starts with creating of worker thread container.

```java
int workersCount = 1;

UdpSockets sockets = new UdpSockets(workersCount);
sockets.start();
```

For example purposes, there is a sample protocol exists, which exchanges between 
peers with pure string, it is called **_PureProtocol_**. Additionally to default 
required constructor arguments, it accepts boolean flag, which determines, whether 
it should reply to incoming messages, or not.

### Direct Communication

Such scenario is useful only in case you don't need mechanisms for traversing NATs 
and firewalls, so peers are located in the same network. Pretty straightforward:

```java
boolean shouldReply = false;

PureProtocol pureProtocol = new PureProtocol(
        sockets, NatBehaviour.NO_NAT, clientProperties, shouldReply
);
pureProtocol.start();

// Send Packet
InetSocketAddress targetAddress = new InetSocketAddress("127.0.0.1", 40001);

pureProtocol.getClient().send(targetAddress, "Ping");
```

### Communication with NAT / firewall bypass (Only for **_TurnAwareClientProtocol_** implementations)

**_ClientBootstrap_** is a factory class for specific protocol implementations. 
Depending on incoming parameters, it will use or not specific NAT / firewall 
bypass procedures. Below is initialization example with full set of available 
arguments

```java
ClientBootstrap<PureProtocol> clientBootstrap = new ClientBootstrap<PureProtocol>(sockets)
        .withHostId("test_initiator")
        .useSignaling(URI.create("ws://178.13.28.131:8080/ws"))
        .useStun(new InetSocketAddress("95.174.88.11", 3478))
        .useTurn(new InetSocketAddress("95.174.88.11", 3478))
        .useSocketTimeout(Duration.ofMillis(400L))
        .build();
```

From client bootstrap you now have access to connection procedure, creating 
**_ClientSession_** instance.

```java
ClientSession<PureProtocol> clientSession = new ClientSession<>(clientBootstrap)
        .as(IceRole.CONTROLLING) // or IceRole.CONTROLLED, if it is receiving peer  
        .connectTo("test_receiver")
        .mapEstablishedConnection(it -> new PureProtocol(it, false));

ConnectResult<PureProtocol> connectResult = clientSession.connect().join();
PureProtocol pureProtocol = connectResult.getProtocol();

// Send Packet
InetSocketAddress targetAddress = connectResult.getOpponentAddress();

pureProtocol.getClient().send(targetAddress, "Ping");
```