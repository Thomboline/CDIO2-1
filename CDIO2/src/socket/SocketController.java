package socket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import socket.SocketInMessage.SocketMessageType;

public class SocketController implements ISocketController 
{
	Set<ISocketObserver> observers = new HashSet<ISocketObserver>();
	Map<String, String> connectedClients = new HashMap<String, String>(); //Answer to = TODO Maybe add some way to keep track of multiple connections?
	int Count = 0;
	private DataOutputStream outStream; 
	

	public void viewAllClients()
	{
		
		try 
		{
			for(Entry<String, String> entry : connectedClients.entrySet()) 
			{
			    String test = ("Client Ip adress: " + entry.getKey() + " Numbers of clients: " + entry.getValue());
			    OutputStreamWriter osw = new OutputStreamWriter(outStream);
				BufferedWriter bw = new BufferedWriter(osw);
				bw.write(test);
				bw.flush();
			}
			
		} catch (IOException e1) 
		{
			e1.printStackTrace();
		} 
		
	}
	
	public void viewClient(SocketThread Client)
	{
		try 
		{
			OutputStreamWriter osw = new OutputStreamWriter(outStream);
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(connectedClients.get(Client).toString());
			bw.flush();
		} catch (IOException e1) 
		{
			e1.printStackTrace();
		} 
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

		//TODO send something over the socket! // Done
		} else 
		{
			try 
			{
				String MessageClosed = "Connection is closed";
				OutputStreamWriter osw = new OutputStreamWriter(outStream);
				BufferedWriter bw = new BufferedWriter(osw);
				bw.write(MessageClosed);
				bw.flush();
			} catch (IOException e1) 
			{
				e1.printStackTrace();
			} 
			
			//TODO maybe tell someone that connection is closed? //Done
		}
	}

	@Override
	public void run() 
	{
		//TODO some logic for listening to a socket //(Using try with resources for auto-close of socket)
		try (ServerSocket listeningSocket = new ServerSocket(Port))
		{ 
			while (true)
			{
				waitForConnections(listeningSocket); 	
			}		
		} catch (IOException e1) 
		{
			// TODO Maybe notify MainController?
			e1.printStackTrace();
		} 
	}

	private void waitForConnections(ServerSocket listeningSocket) 
	{
		try 
		{
			++Count;
			String clientCount = Integer.toString(Count);
			Socket activeSocket = listeningSocket.accept();
			
			String Addr = activeSocket.getLocalAddress().toString();
			connectedClients.put(Addr, clientCount);
			outStream = new DataOutputStream(activeSocket.getOutputStream());
			
			new SocketThread(activeSocket, this).start();
		
		} 
		catch (IOException e) 
		{
			//TODO maybe notify mainController?
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

	  Socket activeSocket;
	  SocketController SC;
	  
	  private BufferedReader inStream;
	  
	  public SocketThread(Socket activeSocket, SocketController SC ) 
	  {
	    this.activeSocket = activeSocket;
	    this.SC = SC;
	  }

	  public void run() 
	  {
		  String inLine;
		  
		  try 
		  {
	    	inStream = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));
	   	    SC.viewAllClients();
	   	   
	   	    while (true)
	    	{
	    		inLine = inStream.readLine();
	    		System.out.println(inLine);
	    		if (inLine==null) break;
	    		switch (inLine.split(" ")[0])
	    		{
				case "RM20": // Display a message in the secondary display and wait for response
					//TODO implement logic for RM command
					if(inLine.split(" ")[1].equals("8"))
					{
						try 
						{
							SC.notifyObservers(new SocketInMessage(SocketMessageType.RM208, inLine.split("8")[1]));
							System.out.println("Du har skrevet RM208");
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
				case "D":// Display a message in the primary display
					//TODO Refactor to make sure that faulty messages doesn't break the system					
						SC.notifyObservers(new SocketInMessage(SocketMessageType.D, inLine.split(" ")[1])); 
					break;
				case "DW": //Clear primary display
					SC.notifyObservers(new SocketInMessage(SocketMessageType.DW, "DW"));
					//TODO implement
					break;
				case "P111": //Show something in secondary display
					SC.notifyObservers(new SocketInMessage(SocketMessageType.P111, inLine.split(" ")[1]));
					//TODO implement
					break;
				case "T": // Tare the weight
					SC.notifyObservers(new SocketInMessage(SocketMessageType.T, "T"));
					//TODO implement
					break;
				case "S": // Request the current load
					SC.notifyObservers(new SocketInMessage(SocketMessageType.S, "S"));
					//TODO implement
					break;
				case "K":
					if (inLine.split(" ").length>1){
						SC.notifyObservers(new SocketInMessage(SocketMessageType.K, inLine.split(" ")[1]));
					}
					break;
				case "B": // Set the load
					//TODO implement
					if(SC.isItANumber(inLine.split(" ")[1])){
						SC.notifyObservers(new SocketInMessage(SocketMessageType.B, inLine.split(" ")[1])); 
					}
					break;
				case "Q": // Quit
					SC.notifyObservers(new SocketInMessage(SocketMessageType.Q, "Q"));
					this.interrupt();
					
					//TODO implement
					break;
				default: //Something went wrong?
					//TODO implement
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

