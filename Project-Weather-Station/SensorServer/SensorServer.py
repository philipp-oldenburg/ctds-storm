import socket
import thread
import Adafruit_BMP.BMP085 as BMP085
from HDC1008 import HDC1008
from tsl2561 import TSL2561

def readline(): # ignores \r
    line = "";
    while True:
        byte = conn.recv(1)
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
            msg = readline()
            if msg == "TEMP":
		data = sensor2.pollsensordata()
                conn.send('{0}\n'.format(data["temp"]))
            elif msg == "PRES":
                conn.send('{0}\n'.format(sensor.read_pressure()))
            elif msg == "ALTI":
                conn.send('{0}\n'.format(sensor.read_altitude()))
            elif msg == "SEAL":
                conn.send('{0}\n'.format(sensor.read_sealevel_pressure()))
            elif msg == "HUMI":
                data = sensor2.pollsensordata()
                conn.send('{0}\n'.format(data["humi"]))
            elif msg == "LUMI":
                conn.send('{0}\n'.format(sensor3.lux()))
            elif msg == "PING":
                conn.send('{0}\n'.format("PONG"))
            else:
                print "received message in conflict with our protocol"
    except Exception as e:
        print e.message
        print "closed connection to:", addr


if __name__ == '__main__':
    sensor = BMP085.BMP085()
    sensor2 = HDC1008(0x40)
    sensor3 = TSL2561()
    soc = socket.socket()
    soc.bind(("", 1337))
    soc.listen(5)  # max 5 waiting connections
    while True:
        conn, addr = soc.accept()
        thread.start_new_thread(handler, ())
		
		
