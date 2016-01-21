
public class WebServerTest {
	public static void main(String[] args) {
		MockWebServer webserver = new MockWebServer("127.0.0.1", 9001);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		webserver.start();
	}
}
