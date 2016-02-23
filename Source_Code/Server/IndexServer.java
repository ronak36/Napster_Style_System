/*
 * @Class : IndexServer
 * @Author : Ronak Patel
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Hashtable;

public class IndexServer {

	//Following hastable is used to index multiple peer client's information
	private static Hashtable htClientsFiles = new Hashtable();

	public static void main(String args[]) {
		listenRegesteringClients();
	}

	/*
	 * @Method : listenRegesteringClients
	 * @Param : None
	 * @Returns : void
	 * @Purpose : Opens a server socket and creates thread to listen each peer client's request
	 */
	private static void listenRegesteringClients() {
		//TODO
		ServerSocket server = null;

		try{
			//creates server socket on port no 4445
			server = new ServerSocket(4445);
			if(!server.isClosed()) {
				System.out.println("Index server is now in listening mode.");
			}
			RegisterClientThread regClient;
			while(true){
				//thread is invoked with server.accept() which returns a client connection 
				regClient = new RegisterClientThread(server.accept(),htClientsFiles);
				Thread t = new Thread(regClient);
				t.start();
			} 
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				server.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
