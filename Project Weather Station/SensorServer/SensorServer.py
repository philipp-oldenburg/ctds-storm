import socket
import thread


def readline(socket):  # ignores \r
    line = "";
    while True:
        byte = socket.recv(1)
        if "\n" == byte:
            return line
        elif "\r" == byte:
            continue
        else:
            line += byte


def handler(conn, addr):
    try:
        print "Got connection from:", addr
        while True:
            msg = readline(conn)
            if msg == "TEMP":
                conn.send("0.001" + '\n')
            elif msg == "PRES":
                conn.send("0.002" + '\n')
            elif msg == "ALTI":
                conn.send("0.003" + '\n')
    except Exception as e:
        print e.message
        print "closed connection to:", addr


if __name__ == '__main__':
    soc = socket.socket()
    soc.bind(("localhost", 1337))
    soc.listen(5)  # max 5 waiting connections
    while True:
        conn, addr = soc.accept()
        thread.start_new_thread(handler, (conn, addr))
