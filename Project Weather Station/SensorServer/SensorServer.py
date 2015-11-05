import socket
import thread
import Adafruit_BMP.BMP085 as BMP085


def readline(): # ignores \r
    line = "";
    while True:
        byte = socket.recv(1)
        if "\n" == byte:
            return line
        elif "\r" == byte:
            continue
        else:
            line += byte


def handler():
    try:
        print "Got connection from:", addr
        while True:
            msg = readline(conn)
            if msg == "TEMP":
                conn.send('{0:0.2f}\n'.format(sensor.read_temperature()))
            elif msg == "PRES":
                conn.send('{0:0.2f}\n'.format(sensor.read_pressure()))
            elif msg == "ALTI":
                conn.send('{0:0.2f}\n'.format(sensor.read_altitude()))
            elif msg == "SEAL":
                conn.send('{0:0.2f}\n'.format(sensor.read_sealevel_pressure()))
            else:
                print "received message in conflict with our protocol"
    except Exception as e:
        print e.message
        print "closed connection to:", addr


if __name__ == '__main__':
    sensor = BMP085.BMP085()
    soc = socket.socket()
    soc.bind(("localhost", 1337))
    soc.listen(5)  # max 5 waiting connections
    while True:
        conn, addr = soc.accept()
        thread.start_new_thread(handler, (conn, addr))
