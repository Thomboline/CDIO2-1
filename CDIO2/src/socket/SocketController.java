package socket;

import java.util.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import socket.SocketInMessage.SocketMessageType;

public class SocketController implements ISocketController 
{
	private Set<ISocketObserver> observers = new HashSet<ISocketObserver>();
	private Map<String, String> connectedClients = new HashMap<String, String>(); //Answer to = TODO Maybe add some way to keep track of multiple connections?
	private List<DataOutputStream> dout = new ArrayList<DataOutputStream>(); 
	
	private int runOnce = 0;
	private int Port = 8000;
	
	
	public void OutputCMD(String message)
	{
		try 
		{
			OutputStreamWriter osw = new OutputStreamWriter(dout.get(0));
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(message);
			bw.flush();
			
		} catch (IOException e1) 
		{
			e1.printStackTrace();
		} 
	}
  
  public void viewClient()
	{
		for(Entry<String, String> entry : connectedClients.entrySet()) 
		{
		    String ClientView = ("Client Ip adress: " + entry.getKey() + " Numbers of clients: " + entry.getValue() + "\n");
		    OutputCMD(ClientView);
		} 
	}

  	public void setPortNumber(int newPort) {
  		this.Port = newPort;
  	}
  	
	@Override
	public void registerObserver(ISocketObserver observer) 
	{
		observers.add(observer);
	}

	@Override
	public void unRegisterObserver(ISocketObserver observer) 
	{
		observers.remove(observer);
	}

	@Override
	public void sendMessage(SocketOutMessage message) 
	{
		if (!dout.isEmpty())
		{
			try 
			{	
				for(int i = 0; i < dout.size(); i++) 
				{
				OutputStreamWriter osw = new OutputStreamWriter(dout.get(i));
				BufferedWriter bw = new BufferedWriter(osw);
				bw.write(message.getMessage());
				bw.flush();
				}
			} catch (IOException e1) 
			{
				e1.printStackTrace();
			} 

		} else 
		{
			try 
			{
				String MessageClosed = "Connection is closed";
				for(int i = 0; i < dout.size(); i++) {
					OutputStreamWriter osw = new OutputStreamWriter(dout.get(i));
					BufferedWriter bw = new BufferedWriter(osw);
					bw.write(MessageClosed);
					bw.flush();
					}
				
			} catch (IOException e1) 
			{
				e1.printStackTrace();
			} 
			
		}
	}

	@Override
	public void run() 
	{
		try (ServerSocket listeningSocket = new ServerSocket(Port))
		{ 
			while (true)
			{
				waitForConnections(listeningSocket); 	
			}		
		} catch (IOException e1) 
		{
			e1.printStackTrace();
		} 
	}

	private void waitForConnections(ServerSocket listeningSocket) {
		try {
			Socket activeSocket = listeningSocket.accept();
			DataOutputStream temp = new DataOutputStream(activeSocket.getOutputStream());
			dout.add(temp);

			if (runOnce < 1) {
				String ChangePortMessage = "Do you wish to change the port number on the device, y/n?\n";
				OutputCMD(ChangePortMessage);

				BufferedReader inStream = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
				String inLine = inStream.readLine();

				switch (inLine.split(" ")[0]) {
				case "y":
					String ChangePortMessage2 = "Please type in the new port number\n";
					OutputCMD(ChangePortMessage2);
					int NewPort = Integer.parseInt(inStream.readLine());
					setPortNumber(NewPort);

					break;
				case "n":
					break;

				default:
					String ChangePortMessage3 = "Input error";
					OutputCMD(ChangePortMessage3);
					break;
				}

				runOnce++;

				String Addr = activeSocket.getInetAddress().toString();
				new SocketThread(activeSocket, this).start();

				int activeCount = SocketThread.activeCount() - 8;
				String clientCount = Integer.toString(activeCount);
				connectedClients.put(Addr, clientCount);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void notifyObservers(SocketInMessage message) 
	{
		for (ISocketObserver socketObserver : observers) 
		{
			socketObserver.notify(message);
		}
	}

	public boolean isItANumber(String x)//IT is!//
	{
		boolean b = true;
		String s = x;
		int dotCount = 0;
		for(int i = 0; i < s.length(); i++)
		{
			if(s.charAt(i) >= 48 && s.charAt(i) <= 57) 
			{
			}
			else if(s.charAt(i) == 46)
			{
				dotCount++;
			}
			else b = false;
		}
		if(dotCount > 1)
		{
			b = false;
		}
		return b;
	}
	
}
class SocketThread extends Thread 
{	
	
	private String userName;
	private int userID;
	
	Socket activeSocket;
	SocketController SC;
	  
	private BufferedReader inStream;
	 
	  
	  public SocketThread(Socket activeSocket, SocketController SC ) 
	  {
	    this.activeSocket = activeSocket;
	    this.SC = SC;
	  }
	  
	public boolean login() {
		
		this.userName = "Anders And";
		this.userID = 12;
		
		while (true) {
			try {
				SC.sendMessage(new SocketOutMessage("Please enter your userID\r\n"));

				int userID = Integer.parseInt(inStream.readLine());
				if (this.userID == userID) {
					SC.sendMessage(new SocketOutMessage("Your user ID is " + this.userID + "\r\n" + "Confirm: y/n\r\n"));
					String answer = inStream.readLine();
					if (answer.equals("y")) {
						SC.sendMessage(new SocketOutMessage("Your name is " + this.userName + "\r\n" + "Confirm: y/n\r\n"));
						answer = inStream.readLine();
						if (answer.equals("y")) {
							return true;
						} else {
							SC.sendMessage(new SocketOutMessage("Name incorrect. Try again."));
						}
					} else {
						SC.sendMessage(new SocketOutMessage("ID does not exist.\r\n"));
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			return false;
		}
		
	}

	  public void run() 
	  {	
		  String inLine;
		  
		  try 
		  {
	    	inStream = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
	    	while(!login()) {}
	    	
	   	    SC.viewClient();
	   	   
	   	    while (true)
	    	{
	    		inLine = inStream.readLine();
	    		System.out.println(inLine);
	    		if (inLine==null) break;
	    		switch (inLine.split(" ")[0])
	    		{
				case "RM20": // Display a message in the secondary display and wait for response
					if(inLine.split(" ")[1].equals("8"))
					{
						try 
						{
							SC.notifyObservers(new SocketInMessage(SocketMessageType.RM208, inLine.split("8")[1]));
						}
						catch (ArrayIndexOutOfBoundsException e) 
						{
							SC.notifyObservers(new SocketInMessage(SocketMessageType.RM208, "INDTAST NR"));
						}
					}
					else if(inLine.split(" ")[1].equals("4"))
					{
						SC.notifyObservers(new SocketInMessage(SocketMessageType.RM204, inLine.split("8")[1]));
						System.out.println("Du har skrevet RM204");
					}
					else 
						System.out.println("Du har tastet forkert.");
					break;
				case "D": // Write in secondary display
						SC.notifyObservers(new SocketInMessage(SocketMessageType.D, inLine.split(" ")[1])); 
					break;
				case "DW": //Clear primary display
					SC.notifyObservers(new SocketInMessage(SocketMessageType.DW, "DW"));
					break;
				case "P111": //Show something in secondary display
					SC.notifyObservers(new SocketInMessage(SocketMessageType.P111, inLine.split(" ")[1]));
					break;
				case "T": // Tare the weight
					SC.notifyObservers(new SocketInMessage(SocketMessageType.T, "T"));
					break;
				case "S": // Request the current load
					SC.notifyObservers(new SocketInMessage(SocketMessageType.S, "S"));
					break;
				case "K": // Choose keystate
					if (inLine.split(" ").length>1){
						SC.notifyObservers(new SocketInMessage(SocketMessageType.K, inLine.split(" ")[1]));
					}
					break;
				case "B": // Set the load
					try {
					if(SC.isItANumber(inLine.split(" ")[1])){
						SC.notifyObservers(new SocketInMessage(SocketMessageType.B, inLine.split(" ")[1])); 
					}
					}
					catch(ArrayIndexOutOfBoundsException ex) {
						System.out.println("You can't do that.");
					}
					
					break;
				case "Q": // Quit
					SC.notifyObservers(new SocketInMessage(SocketMessageType.Q, "Q"));
					this.interrupt();
					break;
				default: //Something went wrong?
					try {
						SC.notifyObservers(new SocketInMessage(SocketMessageType.DE, inLine.split(" ")[1]));
						} catch (ArrayIndexOutOfBoundsException e) 
						{
							SC.notifyObservers(new SocketInMessage(SocketMessageType.DE, " "));
						}
					break;
				}
			}
	    } 
	    catch (IOException e) 
	    {
	      System.out.println(e);
	    }

	 }
	  
}
