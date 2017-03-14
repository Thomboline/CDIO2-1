package controller;

import socket.ISocketController;
import socket.ISocketObserver;
import socket.SocketInMessage;
import socket.SocketOutMessage;
import weight.IWeightInterfaceController;
import weight.IWeightInterfaceObserver;
import weight.KeyPress;
import weight.gui.WeightInterfaceControllerGUI;
/**
 * MainController - integrating input from socket and ui. Implements ISocketObserver and IUIObserver to handle this.
 * @author Christian Budtz
 * @version 0.1 2017-01-24
 *
 */
public class MainController implements IMainController, ISocketObserver, IWeightInterfaceObserver {

	private ISocketController socketHandler;
	private IWeightInterfaceController weightController;
	private KeyState keyState = KeyState.K1;
	private double currentWeight = 0.0000;
	private double containerWeight;
	

	public MainController(ISocketController socketHandler, IWeightInterfaceController weightInterfaceController) {
		this.init(socketHandler, weightInterfaceController);
	}

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
			WeightInterfaceControllerGUI.getInstance().onSliderValueChange(currentWeight);

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
			weightController.showMessagePrimaryDisplay(message.getMessage());
			try { 
				//User input
			} catch (Exception e) {
				e.printStackTrace();
			}
			socketHandler.sendMessage(new SocketOutMessage("RM20 A " + /*input +*/ " crlf"));
			break;
		case S:
			
			
			//Need to make notifyWeightChange()
			break;
		case T:
			
			//Need to make notifyWeightChange()
			break;
		case DW:
			weightController.showMessagePrimaryDisplay(null);
			socketHandler.sendMessage(new SocketOutMessage("A"));
			break;
		case K:
			handleKMessage(message);
			break;
		case P111:
			String upToNCharacters = message.getMessage().substring(0, Math.min(message.getMessage().length(), 30));
			weightController.showMessageSecondaryDisplay(upToNCharacters);
			break;
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
		WeightInterfaceControllerGUI.getInstance().showMessageSecondaryDisplay("Du har trykket på en knap");
		System.out.println(keyPress.getCharacter());
		switch (keyPress.getType()) {
		case SOFTBUTTON:
			break;
		case TARA:
			containerWeight = currentWeight;
			notifyWeightChange(-currentWeight);
			currentWeight = 0.0000;
			WeightInterfaceControllerGUI.getInstance().showMessagePrimaryDisplay("" + currentWeight);
			
			break;
		case TEXT:
			break;
		case ZERO:
			currentWeight = 0.0000;
			containerWeight = 0.0000;
			WeightInterfaceControllerGUI.getInstance().showMessageSecondaryDisplay("Vægten er nulstillet.");
			WeightInterfaceControllerGUI.getInstance().showMessagePrimaryDisplay("" + currentWeight);
			break;
		case C:
			//Suspect its to delete either the text in the console or on the display. 
			break;
		case EXIT:
			weightController.unRegisterObserver(this);
			socketHandler.unRegisterObserver(this);
			System.exit(0); 
			break;
		case SEND:
			if (keyState.equals(KeyState.K4) || keyState.equals(KeyState.K3) ){
				socketHandler.sendMessage(new SocketOutMessage("K A 3"));
			}
			if (keyState.equals(KeyState.K1) || keyState.equals(KeyState.K4) ){
				//Udfør funktion
			}
			if (keyState.equals(KeyState.K2) || keyState.equals(KeyState.K3) ){
				//Udfør ikke funktionen
			}
			break;
		}

	}

	@Override
	public void notifyWeightChange(double newWeight) {
		currentWeight += newWeight;
		WeightInterfaceControllerGUI.getInstance().showMessageSecondaryDisplay("Du har ændret vægten med " + newWeight + " gram.");
		// TODO Auto-generated method stub
		//Possibly need get & set methods for Tarér

	}

}
