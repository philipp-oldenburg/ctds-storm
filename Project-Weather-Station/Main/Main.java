import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Main {
	
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

		SensorClientInterface client = null;
		String ipAddress = null;
		if (args.length != 0) {
			ipAddress = args[0];
		}
		
		if (checkIfStringIsIPAddress(ipAddress)) {
			try {
				client = new SensorClient(ipAddress);
			} catch (UnknownHostException e){
				System.out.println("Could not find host:");
				e.printStackTrace();
				client = standByForIP(client);
			} catch (IOException e) {
				System.out.println("Detected IO exception:");
				e.printStackTrace();
				client = standByForIP(client);
			}
		} else  {
			System.out.println("Given Parameter is not a valid IP address.");
			client = standByForIP(client);
		}
		
		DataBaseManager dbMan = new DataBaseManager(client, true);
		
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
