package main

import (
	"github.com/pebbe/util"

	"bufio"
	"fmt"
	"net"
	"strings"
	"sync"
	"time"
)

var (
	x      = util.CheckErr
	w      = util.WarnErr
	TAG    = "VRC1.0"
	mu     sync.Mutex
	queues = make(map[string]chan string)
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

	mu.Lock()
	queue, ok := queues[a[1]]
	if !ok {
		queue = make(chan string, 10000)
		queues[a[1]] = queue
	}
	mu.Unlock()

	for scanner.Scan() {
		line := scanner.Text()
		if line == "quit" {
			return
		}

		go func(s string) {
			time.Sleep(1 * time.Second)
			select {
			case queue <- s:
			default:
			}
		}(line)

		line = "nil"
		for busy := true; busy; {
			select {
			case line = <-queue:
			default:
				busy = false
			}
		}

		fmt.Fprintln(conn, line)
	}
	w(scanner.Err())

}
