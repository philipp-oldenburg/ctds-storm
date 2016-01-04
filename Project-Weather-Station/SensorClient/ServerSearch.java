package SensorClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServerSearch {
	public static void main(String[] args) {
//	    try {
//			Socket echoSocket = new Socket("192.168.2.222", 8080);
//			PrintWriter out =
//			    new PrintWriter(echoSocket.getOutputStream(), true);
//			BufferedReader in =
//			    new BufferedReader(
//			        new InputStreamReader(echoSocket.getInputStream()));
//			out.write("GET / HTTP/1.0\\n\\n");
//			out.flush();
//			System.out.println(in.read(new char[1024], 0, 1024));
//			
//			echoSocket.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		try {
			String url = "http://192.168.2.222:8080";
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			//add request header
			con.setRequestProperty("User-Agent", "Mozilla/5.0");

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			//print result
			System.out.println(response.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
