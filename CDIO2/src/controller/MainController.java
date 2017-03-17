package controller;

import java.text.DecimalFormat;
import socket.ISocketController;
import socket.ISocketObserver;
import socket.SocketInMessage;
import socket.SocketOutMessage;
import weight.IWeightInterfaceController;
import weight.IWeightInterfaceObserver;
import weight.KeyPress;

/**
 * MainController - integrating input from socket
 *  and ui. Implements ISocketObserver and IUIObserver to handle this.
 * @author Christian Budtz
 * @version 0.1 2017-01-24
 *
 */
public class MainController implements IMainController, ISocketObserver, IWeightInterfaceObserver {

	private ISocketController socketHandler;
	private IWeightInterfaceController weightController;
	private KeyState keyState = KeyState.K1;
	private double currentWeight = 0.000;
	private double containerWeight;
	
	DecimalFormat df = new DecimalFormat ("0.000");
	

	public MainController(ISocketController socketHandler, IWeightInterfaceController weightInterfaceController) {
		this.init(socketHandler, weightInterfaceController);
	}

	//
	@Override
	public void init(ISocketController socketHandler, IWeightInterfaceController weightInterfaceController) {
		this.socketHandler = socketHandler;
		this.weightController=weightInterfaceController;
	}

	@Override
	public void start() {
		if (socketHandler!=null && weightController!=null){
			//Makes this controller interested in messages from the socket
			socketHandler.registerObserver(this);
			//Starts socketHandler & weightController in own threads
			new Thread(socketHandler).start();
			new Thread(weightController).start();
			weightController.registerObserver(this);

		} else {
			System.err.println("No controllers injected!");
		}
	}
	
	//Listening for socket input
	@Override
	public void notify(SocketInMessage message) {
		switch (message.getType()) {
		case B:
			double newWeight = Double.parseDouble(message.getMessage());
			notifyWeightChange(newWeight);
			break;
		case D:
			weightController.showMessagePrimaryDisplay(message.getMessage());
			break;
		case Q:
			weightController.unRegisterObserver(this);
			socketHandler.unRegisterObserver(this);
			System.exit(0); 
			break;
		case RM204:
			//Not specified
			break;
		case RM208: //Need work
			weightController.showMessageTernaryDisplay(message.getMessage());
			socketHandler.sendMessage(new SocketOutMessage("RM20 B \r\n"));
			//TODO Implement
			socketHandler.sendMessage(new SocketOutMessage("RM20 A " + /*input +*/ " \r\n"));
			break;
		case S:
			socketHandler.sendMessage(new SocketOutMessage("S S " + this.currentWeight + "\r\n"));
			break;
		case T:
			this.containerWeight += currentWeight;
			notifyWeightChange(0);
			weightController.showMessageSecondaryDisplay("Weight of tara: " + containerWeight + "kg");
			socketHandler.sendMessage(new SocketOutMessage("T S " + this.containerWeight +"kg \r\n"));
			break;
		case DW:
			resetWeightChange();
			weightController.showMessagePrimaryDisplay(df.format(this.currentWeight));
			weightController.showMessageSecondaryDisplay(null);
			weightController.showMessageTernaryDisplay(null);
			socketHandler.sendMessage(new SocketOutMessage("DW A\r\n"));
			break;
		case K:
			handleKMessage(message);
			break;
		case P111:
			String upToNCharacters = message.getMessage().substring(0, Math.min(message.getMessage().length(), 30));
			weightController.showMessageSecondaryDisplay(upToNCharacters);
			socketHandler.sendMessage(new SocketOutMessage("P111 A \r\n"));
			break;
		default:
			socketHandler.sendMessage(new SocketOutMessage("Wrong input.\n"
					+ "Commands: S\r\n"
					+ "T\r\n"
					+ "D\r\n"
					+ "DW\r\n"
					+ "P111\r\n"
					+ "RM20 8\r\n"
					+ "K\r\n"
					+ "B\r\n"
					+ "Q\r\n"));
				
		}

	}

	private void handleKMessage(SocketInMessage message) {
		switch (message.getMessage()) {
		case "1" :
			this.keyState = KeyState.K1;
			break;
		case "2" :
			this.keyState = KeyState.K2;
			break;
		case "3" :
			this.keyState = KeyState.K3;
			break;
		case "4" :
			this.keyState = KeyState.K4;
			break;
		default:
			socketHandler.sendMessage(new SocketOutMessage("ES"));
			break;
		}
	}
	
	//Listening for UI input
	@Override
	public void notifyKeyPress(KeyPress keyPress) {
		//TODO implement logic for handling input from ui
		System.out.println(keyPress.getCharacter());
		switch (keyPress.getType()) {
		case SOFTBUTTON:
			if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3) ){
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
			}
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4) ){

				if (keyPress.getKeyNumber() == 0) {

				}
				if (keyPress.getKeyNumber() == 1) {
				
				}
				if (keyPress.getKeyNumber() == 2) {
					weightController.showMessageTernaryDisplay(this.containerWeight + "kg");
				}
				if (keyPress.getKeyNumber() == 3) {
				
				}
				if (keyPress.getKeyNumber() == 4) {
				
				}
				if (keyPress.getKeyNumber() == 5) {
				
				}					
			}
			break;
		case TARA:
			if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3)) {
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
			}
			
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				String [] texts = {
					"", "", "Show stored wieght"	
				};
				weightController.setSoftButtonTexts(texts);
				this.containerWeight += this.currentWeight;
				notifyWeightChange(0);
			}
			
			break;
		case TEXT:
			if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3)) {
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
			}
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				
			}
			
			break;
		case ZERO:
			if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3)) {
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
			}
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				containerWeight = 0.000;
				notifyWeightChange(0);
			}
			break;
		case CANCEL:
			if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3)) {
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
			}
			//Suspect its to delete either the text in the console or on the display. 
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
			weightController.showMessageSecondaryDisplay(null);
			}
			break;
		case EXIT:
			if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3)) {
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
			}
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4)) {
				weightController.unRegisterObserver(this);
				socketHandler.unRegisterObserver(this);
				System.exit(0); 
			}
			break;
		case SEND:
			if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3) ){
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
			}
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4) ){
				socketHandler.sendMessage(new SocketOutMessage("" + this.currentWeight));
			}
			break;
		}

	}

	@Override
	public void notifyWeightChange(double newWeight) {
		this.currentWeight = (double) newWeight;
		weightController.showMessagePrimaryDisplay(df.format(currentWeight) + "kg");
	}
	
	public String resetWeightChange () {
		this.currentWeight = 0.000;
		String weight = this.currentWeight + "kg";
		return weight;
	}
	/*
	private String userName;
	private int userId;

	public void userLogin() {
	    this.userName = "Ryan";
	    this.userId = 12;
	    Scanner scan = new Scanner(System.in);

	    while(true) {
            socketHandler.sendMessage(new SocketOutMessage("Your name is " + this.userName + " confirm by pressing ENTER"));
            scan.nextLine();
            socketHandler.sendMessage(new SocketOutMessage("Your user ID is " + this.userId + " confirm by pressing ENTER"));
            scan.nextLine();
            break;
        }

    }
    */
	
	/*
	private String batchnummer;

	public void chooseBatch () {
	    Scanner batchScan = new Scanner(System.in);

	    while (true) {
	        socketHandler.sendMessage(new SocketOutMessage("Enter batch number: 1234"));
            batchnummer = batchScan.nextLine();
	        if (batchnummer.equals("1234")) {
	            break;
            }
            else
                socketHandler.sendMessage(new SocketOutMessage("Invalid entry, try again"));
        }
    }
	*/
	
	/*
	public String writeToCMD(KeyPress keyPress) {
		
		System.out.println("Du er en lort");
		
		char[] ms = new char[30];
		
		for(int i = 0; i < ms.length; i++) {
			ms[i] = keyPress.getCharacter();
			System.out.println(keyPress.getCharacter());
			weightController.showMessageSecondaryDisplay(ms.toString());	
		
		}
		
		return ms.toString();
	}
  
  	if(keyPress.getType().equals("SEND")) {
			socketHandler.sendMessage(new SocketOutMessage(ms));
		}
	*/
}
