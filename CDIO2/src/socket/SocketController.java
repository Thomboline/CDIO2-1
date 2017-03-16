package socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import socket.SocketInMessage.SocketMessageType;

public class SocketController implements ISocketController 
{
	Set<ISocketObserver> observers = new HashSet<ISocketObserver>();
	//TODO Maybe add some way to keep track of multiple connections?
	private BufferedReader inStream;
	private DataOutputStream outStream;


	@Override
	public void registerObserver(ISocketObserver observer) {
		observers.add(observer);
	}

	@Override
	public void unRegisterObserver(ISocketObserver observer) {
		observers.remove(observer);
	}

	@Override
	public void sendMessage(SocketOutMessage message) 
	{
		if (outStream!=null)
		{
			try 
			{
				OutputStreamWriter osw = new OutputStreamWriter(outStream);
				BufferedWriter bw = new BufferedWriter(osw);
				bw.write(message.getMessage());
				bw.flush();
			} catch (IOException e1) 
			{
				e1.printStackTrace();
			} 



			//TODO send something over the socket! 
		} else 
		{
			//TODO maybe tell someone that connection is closed?
		}
	}

	@Override
	public void run() 
	{
		//TODO some logic for listening to a socket //(Using try with resources for auto-close of socket)
		try (ServerSocket listeningSocket = new ServerSocket(Port)){ 
			while (true){
				waitForConnections(listeningSocket); 	
			}		
		} catch (IOException e1) {
			// TODO Maybe notify MainController?
			e1.printStackTrace();
		} 


	}

	private void waitForConnections(ServerSocket listeningSocket) {
		try {
			Socket activeSocket = listeningSocket.accept();
			String inLine;
			inStream = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
			outStream = new DataOutputStream(activeSocket.getOutputStream());
//			new Thread() 
//			{
//				public void run() 
//				{
//					try
//					{
//						inStream = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
//						outStream = new DataOutputStream(activeSocket.getOutputStream());
//
//					}
//					catch(Exception e)
//					{
//						System.err.println(e);
//					}
//				}
//			}.start();

			//.readLine is a blocking call 
			//TODO How do you handle simultaneous input and output on socket?
			//TODO this only allows for one open connection - how would you handle multiple connections?
			//ServerThread st = new ServerThread(activeSocket); // SKal m�ske laves en Thread //

			while (true){
				inLine = inStream.readLine();
				System.out.println(inLine);
				if (inLine==null) break;
				switch (inLine.split(" ")[0]) {
				case "RM20": // Display a message in the secondary display and wait for response
					//TODO implement logic for RM command
					if(inLine.split(" ")[1].equals("8"))
					{
						try {
							notifyObservers(new SocketInMessage(SocketMessageType.RM208, inLine.split("8")[1]));
							System.out.println("Du har skrevet RM208");
						}
						catch (ArrayIndexOutOfBoundsException e) {
							notifyObservers(new SocketInMessage(SocketMessageType.RM208, "INDTAST NR"));
						}
					}
					else if(inLine.split(" ")[1].equals("4"))
					{
						notifyObservers(new SocketInMessage(SocketMessageType.RM204, inLine.split("8")[1]));
						System.out.println("Du har skrevet RM204");
					}
					else 
						System.out.println("Du har tastet forkert.");
					break;
				case "D":// Display a message in the primary display
					//TODO Refactor to make sure that faulty messages doesn't break the system					
					notifyObservers(new SocketInMessage(SocketMessageType.D, inLine.split(" ")[1])); 
					break;
				case "DW": //Clear primary display
					notifyObservers(new SocketInMessage(SocketMessageType.DW, "DW"));
					//TODO implementation done!
					break;
				case "P111": //Show something in secondary display
					notifyObservers(new SocketInMessage(SocketMessageType.P111, inLine.split(" ")[1]));
					//TODO implementation done!
					break;
				case "T": // Tare the weight
					notifyObservers(new SocketInMessage(SocketMessageType.T, "T"));
					//TODO implementation done!
					break;
				case "S": // Request the current load
					notifyObservers(new SocketInMessage(SocketMessageType.S, "S"));
					//TODO implementation done!
					break;
				case "K":
					if (inLine.split(" ").length>1){
						notifyObservers(new SocketInMessage(SocketMessageType.K, inLine.split(" ")[1]));
					}
					break;
				case "B": // Set the load
					if(isItANumber(inLine.split(" ")[1])){
						notifyObservers(new SocketInMessage(SocketMessageType.D, inLine.split(" ")[1]));
					//TODO implementation done!
					}
					//TODO implement
					break;
				case "Q": // Quit
					notifyObservers(new SocketInMessage(SocketMessageType.DW, "Q"));
					//TODO implementation done!
					break;
				default: //Something went wrong?
					notifyObservers(new SocketInMessage(SocketMessageType.wrongCommand, "wrongCommand"));
					break;
				//TODO implementation done!
				}
			}
		} catch (IOException e) {
			//TODO maybe notify mainController?
			e.printStackTrace();
		}
	}

	private void notifyObservers(SocketInMessage message) {
		for (ISocketObserver socketObserver : observers) {
			socketObserver.notify(message);
		}
	}

	private static boolean isItANumber(String x){
		boolean b = true;
		String s = x;
		int dotCount = 0;
		for(int i = 0; i < s.length(); i++){
			if(s.charAt(i) >= 48 && s.charAt(i) <= 57) {
			}
			else if(s.charAt(i) == 46){
				dotCount++;
			}
			else b = false;
		}
		if(dotCount > 1){
			b = false;
		}
		return b;
	}
}

