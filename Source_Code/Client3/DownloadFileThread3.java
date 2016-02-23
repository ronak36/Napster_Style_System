/*
 * @Class DownloadFileThread1
 * @Author Ronak Patel
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class DownloadFileThread3 implements Runnable{

	private Socket peerClient;

	//Constructor
	DownloadFileThread3(Socket peerClient) {
		this.peerClient = peerClient;
	}

	public void run(){
		ObjectInputStream objectInput;
		try{
			//Listens to peer clients and gives requested file.
			
			objectInput = new ObjectInputStream(peerClient.getInputStream());
			String filePath = objectInput.readObject().toString();
			filePath = filePath.replaceAll("/",File.separator);
			File file = new File(filePath);
			BufferedInputStream in = new BufferedInputStream(
					new FileInputStream(file));
			BufferedOutputStream out = new BufferedOutputStream(peerClient.getOutputStream());
			byte[] buffer = new byte[(int) file.length()];
			in.read(buffer, 0, buffer.length);
            out.write(buffer, 0, buffer.length);
            out.flush();
            out.close();
           // peerClient.close();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Exception occured in" + e);
		}
	}
}