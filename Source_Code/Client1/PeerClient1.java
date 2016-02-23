/*
 * @Class : PeerClient1
 * @Author : Ronak Patel
 */


import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.Timestamp;


public class PeerClient1 extends Thread{

	private static Scanner sc;
	private static ArrayList<File> files = new ArrayList<>();
	private static Socket regSocket;
	private static ObjectOutputStream objectOutput;
	private static String serverIpAddress;

	public static void main(String args[]) {

		try {

			/*
			 * Two threads are created. 
			 * (listenThread)One continuously listens to other peer client's request for file download.
			 * (searchThread)Another provides interface to user to search and download file. 
			 */

			Thread searchThread = new Thread () {
				public void run() {
					askForServerIPAddress();
					selectOperation();		
				}
			};

			Thread listenThread = new Thread () {
				public void run() {
					openFileDownloadConnection();		
				}
			};

			listenThread.start();
			searchThread.start();

			listenThread.join();
			searchThread.join();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * @Method : openFileDownloadConnection
	 * @Param : None
	 * @Returns : void
	 * @Description : Opens connection for other peer clients for file download.
	 */
	private static void openFileDownloadConnection() {
		ServerSocket peerServer = null;
		try {
			peerServer = new ServerSocket(5001);
			while(true){
				DownloadFileThread1 dlFile;
				//server.accept returns a client connection
				dlFile = new DownloadFileThread1(peerServer.accept());
				Thread t = new Thread(dlFile);
				t.start();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				peerServer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * @Method : askForServerIPAddress
	 * @Param : None
	 * @Returns : void
	 * @Description : asks user to input server's IP address to connect
	 */
	private static void askForServerIPAddress() {
		sc = new Scanner(System.in);
		System.out.println("Enter Server IP Adress to connect.");
		serverIpAddress = sc.nextLine();
	}
	
	/*
	 * @Method : connectToIndexServer
	 * @Param : None
	 * @Returns : boolean
	 * @Description : Asks user to server's IP address and connects to Server.
	 */
	private static boolean connectToIndexServer() {
		boolean isConnected = false;

		try {
			//Connects to indexing server
			regSocket = new Socket(serverIpAddress, 4445);
			if(regSocket.isConnected()) {
				isConnected = true;
			}
			else {
				System.out.println("Connection could not be established at " + serverIpAddress);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Connection could not be established at " + serverIpAddress);
			e.printStackTrace();
		}
		return isConnected;
	}

	/*
	 * @Method : selectOperation
	 * @Param : None
	 * @Returns : void
	 * @Description : Provides interface to user to select operation and performs operation accordingly.
	 */
	private static void selectOperation() {
		String inData = "";
		try {

			do {
				if(connectToIndexServer()) {

					objectOutput =
							new ObjectOutputStream(regSocket.getOutputStream());
					System.out.println("---------------------------------------------");
					System.out.println("Enter Input as follows.");
					System.out.println("Press 1 for registration.");
					System.out.println("Press 2 for file search.");
					System.out.println("Press any other key to discontinue.");
					sc = new Scanner(System.in);
					inData = sc.nextLine();
					if(inData.equals("1")) {
						objectOutput.writeObject("1");
						System.out.println("Registration started at : ");
						printCurrentTimeStamp();
						registerToServer();
						System.out.println("Registration ended at : ");
						printCurrentTimeStamp();
					}
					else if (inData.equals("2"))
					{
						objectOutput.writeObject("2");
						searchFile();
					}
					else {
						objectOutput.writeObject("3");
					}
					System.out.println("---------------------------------------------");
					objectOutput.flush();
					regSocket.close();
				}
			}
			while(inData.equals("1") || inData.equals("2"));
			System.out.println("You are now exiting.");
			System.exit(-1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	/*
	 * @Method : registerToServer
	 * @Param : None
	 * @Returns : void
	 * @Descriptions : Registers all information to server.
	 */
	private static void registerToServer() {
		try {

			String message = null;
			HashMap client1Map = new HashMap();
			//Gathers info such as IP address, post number and file list
			client1Map = gatherMyInfo();
			listFiles(new File("."));
			if(files.size() > 0) {
				client1Map.put("ClientFiles",files);
			}
			objectOutput.writeObject(client1Map);
			BufferedReader input =
					new BufferedReader(new InputStreamReader(regSocket.getInputStream()));
			message = input.readLine();
			System.out.println(message);
		}
		catch(Exception ioe) {
			ioe.printStackTrace();
			System.out.println("Failed");
		}
	}

	/*
	 * @Method : listFiles
	 * @Param : File
	 * @Returns : void
	 * @Description : Gathers lists of files for registration.
	 */
	private static void listFiles(File directory) {
		// get all the files from a directory
		try {
			File[] fList = directory.listFiles();
			for (File file : fList) {
				if (file.isFile()) {
					files.add(file);
				} else if (file.isDirectory()) {
					listFiles(file);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * @Method : searchFile
	 * @Param : None
	 * @Returns : void
	 * @Description : Searches for files and downloads from lists of peers
	 */
	private static void searchFile() {
		try {
			sc = new Scanner(System.in);
			String searchFile = null;
			System.out.println("Enter filename:");
			searchFile = sc.nextLine();
			System.out.println("Look up stared at : ");
			printCurrentTimeStamp();
			objectOutput.writeObject(searchFile);

			//Displays list of peers having the file
			ObjectInputStream objectInputStream = new ObjectInputStream(regSocket.getInputStream());
			Object inputObject = objectInputStream.readObject();
			System.out.println("Look up ended at : ");
			printCurrentTimeStamp();
			if(inputObject != null) {
				if(inputObject instanceof ArrayList) {
					ArrayList peerServersInfoList = (ArrayList) inputObject;
					//If list contains one or more peers then dispaly it else file does not exist.
					if(peerServersInfoList !=null && peerServersInfoList.size() > 0) {
						System.out.println("==========================================================");
						System.out.printf("%20s. %-20s $%.20s\n","Sequence Number","Host Address","HostName");
						
						for (int i = 0; i<peerServersInfoList.size(); i++) {
							InetAddress IP = (InetAddress)((HashMap)peerServersInfoList.get(i)).get("IP");
							String ipAddress = ((HashMap)peerServersInfoList.get(i)).get("IpAdr").toString();
							String name = (String)((HashMap)peerServersInfoList.get(i)).get("Name");
							System.out.printf("%20d. %-20s $%.20s\n", i + 1,ipAddress,IP.getHostName());
						}
						System.out.println("==========================================================");
						//Select peer server to download file.
						System.out.println("Enter sequence number to select source of file to download from.");
						String strSource = sc.nextLine();
						int iSource = Integer.parseInt(strSource);
						if(iSource > 0 && iSource <= peerServersInfoList.size()) {
							HashMap peerServerMap = (HashMap) peerServersInfoList.get(iSource - 1);
							System.out.println("Your file will be down loaded from " + ((InetAddress)peerServerMap.get("IP")).getHostName() + " - " + peerServerMap.get("IpAdr").toString());
							System.out.println("Download started at : ");
							printCurrentTimeStamp();
							downloadFile(peerServerMap);
							System.out.println("Download ended at : ");
							printCurrentTimeStamp();
						}
						else
						{
							System.out.println("Sorry! Wrong input.");
						}
					}
					else {
						System.out.println("File does not exist.");
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * @Method : gatherMyInfo
	 * @Params : None
	 * @Returns : HashMap
	 * @Description : Gathers all information to send to Server.
	 */
	private static HashMap gatherMyInfo() {
		HashMap client1Map = null;
		try {
			client1Map = new HashMap();
			InetAddress IP=InetAddress.getLocalHost();
			client1Map.put("IP",IP);
			String result = "";
			Enumeration<NetworkInterface> interfaces = null;

			try {
				interfaces = NetworkInterface.getNetworkInterfaces();
			} catch (SocketException e) {

			}

			if (interfaces != null) {
				while (interfaces.hasMoreElements() && "".equals(result)) {
					NetworkInterface i = interfaces.nextElement();
					Enumeration<InetAddress> addresses = i.getInetAddresses();
					while (addresses.hasMoreElements() && "".equals(result)) {
						InetAddress address = addresses.nextElement();
						if (!address.isLoopbackAddress()  &&  address.isSiteLocalAddress()) {
						result = address.getHostAddress();
						}
					}
				}
			}

			client1Map.put("IpAdr",result);
			client1Map.put("PortNo",5001);
			client1Map.put("PeerId","Client1");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return client1Map;
	}	

	/*
	 * @Method : downloadFile
	 * @Params : HashMap
	 * @Returns : void
	 * @Descriptions : downloads file from selected peer server.
	 */
	private static void downloadFile(HashMap peerServerMap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		int bytesRead;
		try {
			byte[] aByte = new byte[1];
			String ipAddress = peerServerMap.get("IpAdr").toString();
			int portNo = Integer.parseInt(peerServerMap.get("PortNo").toString());
			Socket downloadSocket = new Socket(ipAddress,portNo);//add params
			//file will be downloaded to 
			String fileOutput = peerServerMap.get("ClientSearchedFile").toString();
			ObjectOutputStream objectOutput = new ObjectOutputStream(downloadSocket.getOutputStream());
			String filePath = ((ArrayList) peerServerMap.get("SearchedFiles")).get(0).toString();
			objectOutput.writeObject(filePath);
			objectOutput.flush();
			InputStream is = downloadSocket.getInputStream();
			fos = new FileOutputStream(fileOutput);
			bos = new BufferedOutputStream(fos);

			bytesRead = is.read(aByte, 0, aByte.length);

			do {
				baos.write(aByte);
				bytesRead = is.read(aByte);
			} while (bytesRead != -1);

			bos.write(baos.toByteArray());
			String dlFilePath = new File(".").getAbsolutePath();
			System.out.println(peerServerMap.get("ClientSearchedFile").toString() + " is downloaded at " + dlFilePath.substring(0,dlFilePath.length() - 1));
			bos.flush();
			bos.close();
			downloadSocket.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	private static void printCurrentTimeStamp()
    {
	 java.util.Date date= new java.util.Date();
	 System.out.println(new Timestamp(date.getTime()));
    }
}
