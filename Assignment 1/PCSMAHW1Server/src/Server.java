import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private static String FILELOCATION = "D://AccelerometerDataServerCopy.csv";
	private static int    PORTNO       = 8097;
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		serverSocket = new ServerSocket(Server.PORTNO);
		
		while (true) {
			  Socket sock              = null;
		      FileOutputStream fos     = null;
		      BufferedOutputStream bos = null;
		      InputStream inputStream  = null;
		      byte[] accelerometerData = new byte[10000000];
		      int bytesRead;
		      int currentTotal = 0;
		      
		      System.out.println("Waiting for Client....");
		      
		      try {
					sock = serverSocket.accept();
					System.out.println("Connection Accepted: " + sock);
					inputStream  = sock.getInputStream();
					fos          = new FileOutputStream(Server.FILELOCATION); 
			        bos          = new BufferedOutputStream(fos);
			        bytesRead    = inputStream.read(accelerometerData, 0, accelerometerData.length);
			        currentTotal = bytesRead;
			        do {
			            bytesRead = inputStream.read(accelerometerData, currentTotal, (accelerometerData.length - currentTotal));
			            if (bytesRead >= 0) {
			                currentTotal += bytesRead;
			            }
			        } while (bytesRead > -1);
			        
			        bos.write(accelerometerData, 0, currentTotal);
			        bos.flush();
			        System.out.println("AccelerometerDataServerCopy.csv file received on Server in D drive.");
		    }
		      catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					//release all resources
					
						try {
							if(fos != null)
							{
							  fos.close();
							}
							
							if(inputStream != null)
							{
								inputStream.close();
							}
							if(sock != null)
							{
								sock.close();
							}
						}
						catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			}

		}
	}
}
