package robotcontroller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Eine Klasse für die Kommunikation zum Nameserver.
 * 
 * @author wilhelm
 *
 */
public class Kommunikation {
	private static final Logger log = LoggerFactory
			.getLogger(RobotControllerSpeechlet.class);

	/**
	 * Representant der InetAdresse
	 */
	private InetAddress ia = null;

	/**
	 * Verbindungsendpunkt um Nachrichten zum Server zu schicken.
	 */
	private DatagramSocket socket = null;

	/**
	 * Portnummer für die Verbindung zum Server
	 */

	private int portnummer_Nameserver = -1;

	/**
	 * Builder zum Erstellen von Json-Nachrichten.
	 */
	private JsonObjectBuilder builder = null;

	/**
	 * Factory für Json
	 */
	private JsonBuilderFactory factory = null;

	public Kommunikation(String IpAdresse, int port) {
		portnummer_Nameserver = port;
		try {
			ia = InetAddress.getByName(IpAdresse);
		} catch (UnknownHostException e) {
			System.err.println("Unknown Host.");
			e.printStackTrace();
		}
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			System.err.println(
					"Error creating or accessing a Socket im Konstruktor.");
			e.printStackTrace();
		}

		factory = Json.createBuilderFactory(null);
		builder = factory.createObjectBuilder();
	}

	/**
	 * Fragt die Namen der Roboter ab.
	 * 
	 * @return
	 */
	public String getRobotNames() {
		builder.add("FunctionName", "lookup").add("Type", "Request");
		JsonArrayBuilder abuilder = Json.createArrayBuilder();
		abuilder.add(factory.createObjectBuilder().add("position", 1)
				.add("type", "String").add("value", "all"));
		builder.add("Parameter", abuilder);
		builder.add("ObjectName", ("keinRoboter."
				+ "InterfaceIDLCaDSEV3RMINameserverRegistration"));
		JsonObject object = builder.build();

		DatagramPacket packet = null;
		byte[] data = null;
		data = object.toString().getBytes();
		packet = new DatagramPacket(data, data.length, ia,
				portnummer_Nameserver);

		log.info(portnummer_Nameserver + "   " + ia.getHostAddress());
		try {
			socket.send(packet);
			log.info("Paket gesendet");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		data = new byte[2048];
		packet = new DatagramPacket(data, data.length);

		try {
			socket.setSoTimeout(5000);
			try {
				socket.receive(packet);
			} catch (SocketTimeoutException e) {
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SocketException e) {
			System.err.println("Error with a socket in the Sender");
			e.printStackTrace();
		}

		JsonObject obj = null;
		try (InputStream is = new ByteArrayInputStream(data, 0,
				packet.getLength()); JsonReader rdr = Json.createReader(is)) {
			obj = rdr.readObject();
		} catch (IOException e) {
			System.err
					.println("Fehler beim Lesen des JsonObjectes im Receiver");
			e.printStackTrace();
		}

		String returnWert = obj.getString("ReturnValue");

		return returnWert;
	}

	/**
	 * öffnet den Greifer
	 * 
	 * @return
	 */
	public boolean openGripper(String robotname) {
		boolean successful = true;
		builder.add("FunctionName", "openGripper").add("Type", "Request");
		JsonArrayBuilder abuilder = Json.createArrayBuilder();
		abuilder.add(factory.createObjectBuilder().add("position", 1)
				.add("type", "int").add("value", 1));
		builder.add("Parameter", abuilder);
		builder.add("ObjectName",
				robotname + ".InterfaceIDLCaDSEV3RMIMoveGripper");
		JsonObject object = builder.build();

		DatagramPacket packet = null;
		byte[] data = null;
		data = object.toString().getBytes();
		packet = new DatagramPacket(data, data.length, ia,
				portnummer_Nameserver);
		try {
			socket.send(packet);
		} catch (IOException e) {
			successful = false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		data = new byte[2048];
		packet = new DatagramPacket(data, data.length);

		try {
			socket.setSoTimeout(2000);
			try {
				socket.receive(packet);
			} catch (SocketTimeoutException e) {
				successful = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SocketException e) {
			System.err.println("Error with a socket in the Sender.");
			e.printStackTrace();
		}
		return successful;
	}

	/**
	 * schließt den Greifer
	 * 
	 * @return
	 */
	public boolean closeGripper(String robotname) {
		boolean successful = true;
		builder.add("FunctionName", "closeGripper").add("Type", "Request");
		JsonArrayBuilder abuilder = Json.createArrayBuilder();
		abuilder.add(factory.createObjectBuilder().add("position", 1)
				.add("type", "int").add("value", 1));
		builder.add("Parameter", abuilder);
		builder.add("ObjectName",
				robotname + ".InterfaceIDLCaDSEV3RMIMoveGripper");
		JsonObject object = builder.build();

		DatagramPacket packet = null;
		byte[] data = null;
		data = object.toString().getBytes();
		packet = new DatagramPacket(data, data.length, ia,
				portnummer_Nameserver);
		try {
			socket.send(packet);
		} catch (IOException e) {
			successful = false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		data = new byte[2048];
		packet = new DatagramPacket(data, data.length);

		try {
			socket.setSoTimeout(2000);
			try {
				socket.receive(packet);
			} catch (SocketTimeoutException e) {
				successful = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SocketException e) {
			System.err.println("Error with a socket in the Sender");
			e.printStackTrace();
		}
		return successful;
	}

	/**
	 * vertikale Bewegung des Armes
	 * 
	 * @return
	 */
	public boolean moveVertical(String robotname, int Percent) {
		boolean successful = true;
		builder.add("FunctionName", "moveVerticalToPercent").add("Type",
				"Request");
		JsonArrayBuilder abuilder = Json.createArrayBuilder();
		abuilder.add(factory.createObjectBuilder().add("position", 1)
				.add("type", "int").add("value", Percent));
		abuilder.add(factory.createObjectBuilder().add("position", 2)
				.add("type", "int").add("value", Percent));
		builder.add("Parameter", abuilder);
		builder.add("ObjectName",
				robotname + ".InterfaceIDLCaDSEV3RMIMoveVertical");
		JsonObject object = builder.build();

		DatagramPacket packet = null;
		byte[] data = null;
		data = object.toString().getBytes();
		packet = new DatagramPacket(data, data.length, ia,
				portnummer_Nameserver);
		try {
			socket.send(packet);
		} catch (IOException e) {
			successful = false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		data = new byte[2048];
		packet = new DatagramPacket(data, data.length);

		try {
			socket.setSoTimeout(2000);
			try {
				socket.receive(packet);
			} catch (SocketTimeoutException e) {
				successful = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SocketException e) {
			System.err.println("Error with a socket in the Sender");
			e.printStackTrace();
		}
		return successful;
	}

	/**
	 * vertikale Bewegung des Armes
	 * 
	 * @return
	 */
	public boolean moveHorizontal(String robotname, int Percent) {
		boolean successful = true;
		builder.add("FunctionName", "moveHorizontalToPercent").add("Type",
				"Request");
		JsonArrayBuilder abuilder = Json.createArrayBuilder();
		abuilder.add(factory.createObjectBuilder().add("position", 1)
				.add("type", "int").add("value", Percent));
		abuilder.add(factory.createObjectBuilder().add("position", 2)
				.add("type", "int").add("value", Percent));
		builder.add("Parameter", abuilder);
		builder.add("ObjectName",
				robotname + ".InterfaceIDLCaDSEV3RMIMoveHorizontal");
		JsonObject object = builder.build();

		DatagramPacket packet = null;
		byte[] data = null;
		data = object.toString().getBytes();
		packet = new DatagramPacket(data, data.length, ia,
				portnummer_Nameserver);
		try {
			socket.send(packet);
		} catch (IOException e) {
			successful = false;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		data = new byte[2048];
		packet = new DatagramPacket(data, data.length);

		try {
			socket.setSoTimeout(2000);
			try {
				socket.receive(packet);
			} catch (SocketTimeoutException e) {
				successful = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SocketException e) {
			System.err.println("Error with a socket in the Sender");
			e.printStackTrace();
		}
		return successful;
	}

	/**
	 * Schließt den Socket.
	 */
	public void teardown() {
		socket.close();
	}

}
