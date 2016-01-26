import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import org.json.JSONObject;

import com.sun.corba.se.spi.orb.DataCollector;

public class Main {
	
	private static final int MK_II_PORT = 1338;
	private static boolean startImmediately;

	/**Checks if given input String is of the type "a.b.c.d" where 0 <= a, b, c, d <= 255.
	 * 
	 * @param address The IP address that shall be verified
	 * @return True iff above condition is met.
	 */
	private static boolean checkIfStringIsIPAddress(String address) {
		String[] addressParts = address.split("[.]");
		
		if (addressParts.length != 4) return false;
		
		for (String part : addressParts) {
			int tmp;
			try {
				tmp = Integer.parseInt(part);
			} catch (NumberFormatException e) {
				return false;
			}
			if (tmp > 255 || tmp < 0) {
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		
		SensorClientMkII client = null;
		String ipAddress = null;
		boolean requireSensorServerAvailable = false;
		if (args.length == 3) {
			requireSensorServerAvailable = Boolean.parseBoolean(args[1]);
			ipAddress = args[0];
			startImmediately = Boolean.parseBoolean(args[2]);
		}else if (args.length == 1) {
			ipAddress = args[0];
		}

		boolean sensorServerAvailable = true;
		
		if (!startImmediately) {
			try {
				System.out.println("Looking for sensor server.");
				client = new SensorClientMkII(ipAddress, 1338);
			} catch (Exception e) {
				System.out.println("Could not find host.");
				e.printStackTrace();
				sensorServerAvailable = false;
			}
		} else {
			sensorServerAvailable = false;
		}
		DataBaseManager dbMan = new DataBaseManager(client, sensorServerAvailable);
		System.out.println("created DBMan");
	}

	private static SensorClientInterface standByForIP(SensorClientInterface client) {
		Scanner sysInScanner = new Scanner(System.in);
		String consoleInput;
		while (true) {
			System.out.println("Please input SensorServer IP address:");
			consoleInput = sysInScanner.nextLine();
			if (!checkIfStringIsIPAddress(consoleInput)) {
				System.out.println("Given input is not a valid IP address. Should look as follows:\n 'a.b.c.d' where 0 <= a, b, c, d <= 255");
				continue;
			}
			
			try {
				client = new SensorClient(consoleInput);
			} catch (UnknownHostException e){
				System.out.println("Could not find host:");
				e.printStackTrace();
				System.out.println("Please verify IP address and availability of the SensorServer.");
				continue;
			} catch (IOException e) {
				System.out.println("Detected IO exception:");
				e.printStackTrace();
				System.out.println("Please verify IP address and availability of the SensorServer.");
				continue;
			}
			break;
		}
		sysInScanner.close();
		return client;
	}
}
