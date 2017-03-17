package controller;

import socket.SocketController;
import weight.IWeightInterfaceController;
import weight.gui.WeightInterfaceControllerGUI;
import socket.ISocketController;
/**
 * Simple class to fire up application and inject implementations
 * @author Christian
 *
 */
public class Main {
	private static boolean gui= true;

	public static void main(String[] args) {
		ISocketController socketHandler = new SocketController();
		IWeightInterfaceController weightController = new WeightInterfaceControllerGUI();
		IMainController mainCtrl = new MainController(socketHandler, weightController);
		mainCtrl.start();
	}
}
