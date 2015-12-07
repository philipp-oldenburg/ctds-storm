import struct, array, time, io, fcntl


class HDC1008:

    def __init__(self, port):
        self.HDC1008_ADDR = port
        self.I2C_SLAVE = 0x0703

    def pollsensordata(self):
        ret = {}
        bus = 1
        fr = io.open("/dev/i2c-"+str(bus), "rb", buffering=0)
        fw = io.open("/dev/i2c-"+str(bus), "wb", buffering=0)

        # set device address
        fcntl.ioctl(fr, self.I2C_SLAVE, self.HDC1008_ADDR)
        fcntl.ioctl(fw, self.I2C_SLAVE, self.HDC1008_ADDR)
        time.sleep(0.015)               # 15ms startup time

        s = [0x02, 0x02, 0x00]
        s2 = bytearray(s)
        fw.write(s2)                    # sending config register bytes
        time.sleep(0.015)               # From the data sheet

        s = [0x00]                      # temp
        s2 = bytearray(s)
        fw.write(s2)
        time.sleep(0.0625)              # From the data sheet

        data = fr.read(2)               # read 2 byte temperature data
        buf = array.array('B', data)
        ret['temp'] = "%f" % (((((buf[0] << 8) + (buf[1])) / 65536.0) * 165.0) - 40.0)

        time.sleep(0.015)               # From the data sheet

        s = [0x01]                      # hum
        s2 = bytearray(s)
        fw.write(s2)
        time.sleep(0.0625)              # From the data sheet

        data = fr.read(2)               # read 2 byte temperature data
        buf = array.array('B', data)
        ret['humi'] = "%f" % ((((buf[0] << 8) + (buf[1])) / 65536.0) * 100.0)

        return ret

