/*
 * @Class : RegisterClientThread
 * @Author : Ronak Patel
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class RegisterClientThread implements Runnable{

	private Socket client;
	private Hashtable htClientsFiles = new Hashtable();;

	//Constructor
	RegisterClientThread(Socket client,Hashtable<String,ArrayList<String>> htClientsFiles) {
		this.client = client;
		this.htClientsFiles = htClientsFiles;
	}

	public void run(){
		String clientOperation;

		try{
				ObjectInputStream objectInput = new ObjectInputStream(client.getInputStream());
				/*
				 * reads input from client.
				 * 1 for registration
				 * 2 for file search
				 */
				clientOperation = (String)objectInput.readObject();
				//registration
				if(clientOperation.equals("1")) {
					Object object = objectInput.readObject();
					//Calls method to register client
					registerClient(object);
				}
				//search
				else if(clientOperation.equals("2")) {
					//retrieves file name which is being searched by client. 
					String fileName = (String)objectInput.readObject();
					//calls method to search file and returns list of peers who are having that file
					ArrayList returnToClientList = searchFile(fileName);
					ObjectOutputStream objectOutput = new ObjectOutputStream(client.getOutputStream());
					objectOutput.writeObject(returnToClientList);
					objectOutput.flush();
					objectOutput.close();;
					objectInput.close();
				}
				else if(clientOperation.equals("3")) {
					System.out.println(client.getInetAddress().getHostName() + " - " + client.getInetAddress().getHostAddress()  + "left");
				}
			
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Exception occured in" + e);
		}
	}

	/*
	 * @Method : registerClient
	 * @Param : Object (Instance of HashMap)
	 * @Returns : void
	 */

	private void registerClient(Object object) {
		HashMap clientMap = new HashMap();
		PrintWriter out = null;

		try {

			if(object instanceof HashMap) {
				clientMap =  (HashMap) object;
				//Following line was added for IP adrress based indexing.
				//htClientsFiles.put((((InetAddress)clientMap.get("IP")).getHostAddress()).toString(),clientMap);

				//registers a peer client info into hash table.
				//clientMap is a HashMap which is sent by peer client with its info

				htClientsFiles.put(clientMap.get("PeerId").toString(),clientMap);
				System.out.println("================================================");
				System.out.println("Recently registered client's info is as follows");
				System.out.println("Host Name : " + (((InetAddress)clientMap.get("IP")).getHostName()).toString());
				System.out.println("IP Address : " + clientMap.get("IpAdr").toString());
				System.out.println("================================================");
				out = new PrintWriter(client.getOutputStream(), true);
				out.println("We have read your filelist.");
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * @Method : searchFile
	 * @Param : String
	 * @Returns : ArrayList
	 */

	private ArrayList searchFile(String fileName) {
		ArrayList returnToClientList = null;
		HashMap returnToClientMap = null;
		HashMap clientInfoMap = null;

		try {
			returnToClientList = new ArrayList();
			returnToClientMap = new HashMap();
			clientInfoMap = new HashMap();

			//Obtaining iterator over set entries
			Set<String> keysLevel1 = htClientsFiles.keySet();
			Iterator<String> itrLevel1 = keysLevel1.iterator();

			//Looks up in hash table for clients which has particular file 
			while (itrLevel1.hasNext()) { 

				ArrayList searchedFiles = new ArrayList();
				String strKey1 = itrLevel1.next();
				clientInfoMap = (HashMap) htClientsFiles.get(strKey1);

				ArrayList filesList = (ArrayList) clientInfoMap.get("ClientFiles");//List of files each peer is having.
				for (int i= 0; i<filesList.size(); i++) {
					//if searched file matches then it is added into result ArrayList
					if(filesList.get(i).toString().contains(fileName)) {
						searchedFiles.add(filesList.get(i));
					}
				}
				//if return list's size is more than zero then it means file is found.
				clientInfoMap.put("ClientSearchedFile", fileName);
				if(searchedFiles.size() > 0) {
					clientInfoMap.put("SearchedFiles", searchedFiles);
					returnToClientList.add(clientInfoMap);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnToClientList;
	}
}