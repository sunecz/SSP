package sune.ssp.p2p;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import sune.ssp.data.Data;
import sune.ssp.data.Message;
import sune.ssp.data.Status;
import sune.ssp.event.ClientEvent;
import sune.ssp.event.EventType;
import sune.ssp.event.Listener;
import sune.ssp.secure.SecureClient;
import sune.ssp.util.Randomizer;

public class MultiClient {
	
	private final List<SecureClient> clients;
	private final String username;
	private boolean autoConnect;
	
	public MultiClient() {
		this(null);
	}
	
	public MultiClient(String username) {
		if(username == null) {
			username = "username" + Randomizer.nextLong();
		}
		this.username = username;
		this.clients  = new LinkedList<>();
	}
	
	public void append(String serverIP, int serverPort) {
		SecureClient client = SecureClient.create(
			serverIP, serverPort);
		client.setUsername(username);
		if(autoConnect) {
			client.connect();
		}
		clients.add(client);
	}
	
	public void connectAll() {
		for(SecureClient client : clients) {
			client.connect();
		}
	}
	
	public void disconnectFrom(String serverIP, int serverPort) {
		for(SecureClient client : clients) {
			String ip = client.getServerIP();
			int port  = client.getServerPort();		
			if(ip.equals(serverIP) && port == serverPort) {
				client.disconnect();
				break;
			}
		}
	}
	
	public void disconnectAll() {
		List<SecureClient> backup = new LinkedList<>(clients);
		while(clients.size() > 0) {
			SecureClient client = clients.get(0);
			client.disconnect();
			clients.remove(0);
		}
		
		if(clients.isEmpty()) {
			clients.addAll(backup);
		}
	}
	
	public void send(Data data) {
		for(SecureClient client : clients) {
			client.send(data);
		}
	}
	
	public void send(Status status) {
		for(SecureClient client : clients) {
			client.send(status);
		}
	}
	
	public void send(String message) {
		Message msg = new Message(message, username);
		for(SecureClient client : clients) {
			client.send(msg);
		}
	}
	
	public void forEach(Consumer<SecureClient> listener) {
		for(SecureClient client : clients) {
			listener.accept(client);
		}
	}
	
	public boolean isEmpty() {
		return clients.isEmpty();
	}
	
	public List<SecureClient> getClients() {
		return clients;
	}
	
	public <E> void addListener(
			EventType<ClientEvent, E> type, Listener<E> listener) {
		forEach((client) -> client.addListener(type, listener));
	}
	
	public <E> void removeListener(
			EventType<ClientEvent, E> type, Listener<E> listener) {
		forEach((client) -> client.removeListener(type, listener));
	}
}