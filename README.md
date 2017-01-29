https://play.google.com/store/apps/details?id=nl.xs4all.pebbe.vrkubus

In the file `MainActivity.java`, in the function `onCreate`, a provider
is chosen. A provider is a class that implements the `Provider`
interface, that has one method:

    boolean forward(float[] out, float[] in)

The method `forward` receives three floats as `in`, and return three
floats as `out` on success. When the return value is `false`, this means
there are no valid return values at the moment.

The three values are the x, y and z of the vector that is the direction
the user is looking at. X is to the right, y is up, z is forward. The
initial values are (0, 0, -1). The values are normalised, so the length
of the vector is always 1.

There are several classes that implement the `Provider` interface:

  - `dubbel` doubles the rotation along the y-axis
  - `vertraagd` delays by one second
  - `server` exchanges the vector with an external server
  - `server2` is like `server`, using multiple connections

The providers `server` and `server2` were tested on a private, local
network. The simple `server` is clearly too slow. The rotation of the
cube is jerky. With `server2` using eight simultaneous connections, the
cube turns smoothly. On a slower, non-local network, you may need to
increase the amount of connections. (Though the providers are called
`server` and `server2`, these turn the Android app into a client. The
actual server is running somewhere else on the internet.)

There are three values you may want to modify in the file `server2.class`:

    private static final String address = "192.168.178.24";
    private static final int port = 8448;

    private static final int NR_OF_CONNECTIONS = 8;

There is an example server in the directory `go`: `server.go`. It
returns the vector it gets to the same Android app it came from, with a
delay of one second. To compile this, run:

    export GOPATH=$HOME/go
	go get github.com/pebbe/util
	go build server.go

You may want to change the port number in `server.go`:

    ln, err := net.Listen("tcp", ":8448")

The behavior of the server is as follows:

The first line it gets from a connection consists of the string `VRC1.0`
followed by a space and an ID string (without spaces). The server
doesn't send a response. When the line is missing, the server terminates
the connection. The ID string is used to know which connections belong
to the same client. The Android app generates an ID string the first
time it is used. It is the current timestamp in milliseconds. (So don't
start the Android app for the first time on two phones at exactly the
same time.)

Then the server waits for lines consisting of three values, separated by
a space: the input vector. After each line it receives, the server
**immediately** sends a line with an output vector (three values
separated by a space), or with the string `nil` if there is currently no
output vector available.

There is no special link between the input vector, the output vector,
and the connection. An input vector on one connection may result in an
output vector that is later returned on another connection. There is
even no guarantee the output vectors are returned in the correct order,
but I didn't notice this to be a problem visually.

If the server reads the line `quit`, it terminates the connection. But
the Android app doesn't send a `quit` string, it just disconnects when
it is stopped.
