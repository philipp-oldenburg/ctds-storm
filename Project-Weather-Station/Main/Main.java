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

//	for (int i = 0; i < 256; i++) {
//		new Thread(new Runnable() {
//			private int i;
//			
//			public Runnable initialize(int i) {
//				this.i = i;
//				return this;
//			}
//			@Override
//			public void run() {
//				try {
//					SensorClientInterface client;
//					System.out.println("Trying: "+i);
//					client = new SensorClient("192.168.2."+i);
//					System.out.println("Successful: " + i);
//				} catch (UnknownHostException e) {
//					System.out.println("unknown host: "+i);
//				} catch (IOException e) {
//					System.out.println("ioexception : "+i);
//					System.out.println(i);
//					e.printStackTrace();
//				}
//			}
//		}.initialize(i)).start();
//		
//	}

//		try {
//			SensorClient client = new SensorClient("192.168.2.123");
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

//		String consoleInput;
//		Scanner sysInScanner = new Scanner(System.in);
		SensorClientInterface client = null;
		String ipAddress = args[0];
		System.out.println(ipAddress);
		try {
			client = new SensorClient(ipAddress);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		while (true) {
//			System.out.println("Please input SensorServer IP address:");
//			consoleInput = sysInScanner.nextLine();
//			System.out.println("Is input a valid IP address? Answer: " + checkIfStringIsIPAddress(consoleInput));
//			
//			try {
//				client = new SensorClient(consoleInput);
//			} catch (UnknownHostException e){
//				System.out.println("Could not find host:");
//				e.printStackTrace();
//				System.out.println("Please verify IP address and availability of the SensorServer.");
//				continue;
//			} catch (IOException e) {
//				System.out.println("Detected IO error:");
//				e.printStackTrace();
//				System.out.println("Please verify IP address and availability of the SensorServer.");
//				continue;
//			}
//			break;
//		}
//
//		sysInScanner.close();
		
		DataBaseManager dbMan = new DataBaseManager(client, true);
		
	}
}
