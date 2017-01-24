package main

import (
	"github.com/pebbe/util"

	"bufio"
	"fmt"
	"net"
	"time"
)

var (
	x   = util.CheckErr
	w   = util.WarnErr
	TAG = "VRC1.0"
)

func main() {
	ln, err := net.Listen("tcp", ":8448")
	x(err)
	defer ln.Close()

	fmt.Println("ADRES: 192.168.178.24")
	fmt.Println("BEGIN:", TAG)
	fmt.Println("EIND:  quit")

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

	queue := make(chan string, 10000)

	scanner := bufio.NewScanner(conn)
	scanner.Scan()
	if scanner.Text() != TAG {
		return
	}

	for scanner.Scan() {
		line := scanner.Text()
		fmt.Println(name, line)
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
