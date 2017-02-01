/*

This server expects connections from two apps. Coordinates
it gets from one app are send to the other, and vice verse.

*/

package main

import (
	"github.com/pebbe/util"

	"bufio"
	"fmt"
	"net"
	"strings"
	"sync"
)

var (
	x      = util.CheckErr
	w      = util.WarnErr
	TAG    = "VRC1.0"
	mu     sync.Mutex
	queue1 = make(chan string, 10)
	queue2 = make(chan string, 10)
	app1   string
	app2   string
)

func main() {
	ln, err := net.Listen("tcp", ":8448")
	x(err)
	defer ln.Close()

	for {
		conn, err := ln.Accept()
		if w(err) != nil {
			break
		}
		go handleConnection(conn)
	}
}

func handleConnection(conn net.Conn) {

	r := conn.RemoteAddr()
	name := r.Network() + "/" + r.String()

	fmt.Println("Open ", name)
	defer func() {
		fmt.Println("Close", name)
		conn.Close()
	}()

	scanner := bufio.NewScanner(conn)
	if !scanner.Scan() {
		return
	}
	a := strings.Fields(scanner.Text())
	if len(a) != 2 || a[0] != TAG {
		return
	}
	fmt.Println("     ", name, "=", a[1])

	var cIn, cOut chan string
	mu.Lock()
	if app1 == a[1] || app1 == "" {
		app1 = a[1]
		cIn = queue1
		cOut = queue2
	} else if app2 == a[1] || app2 == "" {
		app2 = a[1]
		cIn = queue2
		cOut = queue1
	} else {
		mu.Unlock()
		return
	}
	mu.Unlock()

	for scanner.Scan() {
		line := scanner.Text()
		if line == "quit" {
			return
		}

		select {
		case cOut <- line:
		default:
		}

		line = "nil"
		for busy := true; busy; {
			select {
			case line = <-cIn:
			default:
				busy = false
			}
		}

		fmt.Fprintln(conn, line)
	}
	w(scanner.Err())

}
