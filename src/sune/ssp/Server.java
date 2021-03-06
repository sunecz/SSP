package sune.ssp;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import sune.ssp.data.ClientData;
import sune.ssp.data.Data;
import sune.ssp.data.FileData;
import sune.ssp.data.FileInfoData;
import sune.ssp.data.FinalData;
import sune.ssp.data.IdentificatorData;
import sune.ssp.data.Message;
import sune.ssp.data.Status;
import sune.ssp.data.StatusData;
import sune.ssp.data.TerminationData;
import sune.ssp.etc.DataList;
import sune.ssp.etc.DataListType;
import sune.ssp.etc.IPAddress;
import sune.ssp.etc.Identificator;
import sune.ssp.etc.ServerClientInfo;
import sune.ssp.event.EventRegistry;
import sune.ssp.event.EventType;
import sune.ssp.event.Listener;
import sune.ssp.event.ServerEvent;
import sune.ssp.file.FileReader;
import sune.ssp.file.FileSender;
import sune.ssp.file.FilesStorage;
import sune.ssp.file.FilesStorage.StorageFile;
import sune.ssp.file.Sender;
import sune.ssp.file.TransferType;
import sune.ssp.util.AntiSpamProtection;
import sune.ssp.util.ListMap;
import sune.ssp.util.PathSystem;
import sune.ssp.util.PortUtils;
import sune.ssp.util.Utils;

public class Server {
	
	private static final String RECEIVE_ALL = "";
	
	private IPAddress ipAddress;
	private ServerSocket server;
	private volatile boolean running;
	
	private Map<Identificator, ServerClient> clients;
	private Queue<FinalData> dataToSend;
	
	private Queue<File> filesToSend;
	private List<FileSender> senders;
	private ListMap<ServerClient, String> acceptedFiles;
	private ListMap<ServerClient, String> terminatedFiles;
	private ListMap<ServerClient, String> waitStateFiles;
	private List<ServerClient> responses;
	private Map<String, Integer> sending;
	
	private FilesStorage fstorage;
	private Identificator identificator;
	
	private int BUFFER_SIZE = 8192;
	public void setBufferSize(int size) {
		BUFFER_SIZE = size;
	}
	
	private int MAX_SEND_TRANSFERS = 5;
	public void setMaxSendTransfers(int max) {
		MAX_SEND_TRANSFERS = max;
	}
	
	public static int SEND_WAIT_TIME = 10000;
	public static void setSendWaitTime(int time) {
		SEND_WAIT_TIME = time;
	}
	
	private String SERVER_NAME = "SERVER";
	public void setServerName(String name) {
		SERVER_NAME = name;
	}
	
	private boolean forceSend;
	public void setForceSend(boolean value) {
		forceSend = value;
	}
	
	protected int TIMEOUT = 8000;
	public void setTimeout(int timeout) {
		TIMEOUT = timeout;
	}
	
	private AntiSpamProtection asp;
	public void setAntiSpamProtection(AntiSpamProtection asp) {
		this.asp = asp;
		if(asp != null) {
			int maxTime 	= asp.getMaxTime();
			int maxAttempts = asp.getMaxAttempts();
			synchronized(clients) {
				for(ServerClient client : clients.values()) {
					client.asp.setMaxTime(maxTime);
					client.asp.setMaxAttempts(maxAttempts);
				}
			}
		} else {
			synchronized(clients) {
				for(ServerClient client : clients.values()) {
					client.setAntiSpamProtection(null);
				}
			}
		}
	}
	public AntiSpamProtection getAntiSpamProtection() { return asp; }
	
	// Event registry
	protected final EventRegistry<ServerEvent> eventRegistry = new EventRegistry<>();
	
	private Thread threadAccept;
	private Runnable runAccept = (() -> {
		while(running) {
			try {
				Socket socket 	 	= server.accept();
				ServerClient client = createClient(socket);
				
				Identificator identificator = client.getIdentificator();
				if(canConnect(identificator.getUUID().toString())) {
					if(asp != null) {
						client.setAntiSpamProtection(
							new AntiSpamProtection(
								asp.getMaxTime(),
								asp.getMaxAttempts()));
					}
					client.send(Status.SUCCESSFULLY_CONNECTED);
					client.send(new ClientData(identificator));
					addClient(identificator, client);
					eventRegistry.call(
						ServerEvent.CLIENT_CONNECTED, client);
				} else {
					client.close(Status.CLIENT_ALREADY_CONNECTED);
					eventRegistry.call(
						ServerEvent.CLIENT_ALREADY_CONNECTED, client);
				}
			} catch(Exception ex) {
			}
		}
	});
	
	private Thread threadSend;
	private Runnable runSend = (() -> {
		while(running) {
			synchronized(dataToSend) {
				if(!dataToSend.isEmpty()) {
					FinalData data  = dataToSend.poll();
					String receiver = data.getReceiver();
					String uuid		= data.getUUID();
					boolean useRecv = !receiver.isEmpty();
					synchronized(clients) {
						for(ServerClient client : clients.values()) {
							String clientIP = client.getIP();
							Identificator i = client.getIdentificator();
							if((forceSend) || (
							   (useRecv  && clientIP.equals(receiver)) ||
							   (!useRecv && !i.equals(uuid)))) {
								client.send(data);
							}
						}
					}
				}
			}
			Utils.sleep(1);
		}
	});
	
	private Thread threadReceived;
	private Runnable runReceived = (() -> {
		while(running) {
			synchronized(clients) {
				if(!clients.isEmpty()) {
					for(ServerClient client : clients.values()) {
						FinalData fdata;
						if((fdata = client.nextData()) != null) {
							Data data = fdata.toData().cast();
							if(data instanceof StatusData) {
								Status status = ((StatusData) data).getStatus();
								switch(status) {
									case DISCONNECTED_BY_USER:
										String uuid 	 	 = data.getUUID();
										ServerClient sclient = getClient(uuid);
										sclient.close();
										removeClient0(uuid);
										eventRegistry.call(
											ServerEvent.CLIENT_DISCONNECTED, sclient);
										break;
									default:
										break;
								}
							} else {
								if(data instanceof FileInfoData) {
									FileInfoData info = (FileInfoData) data;
									try {
										StorageFile sf  = fstorage.createFile(
											info.getHash(), info.getName(), info.getSize());
										String sender = info.getUUID();
										for(ServerClient sclient : clients.values()) {
											String receiver = sclient.getUUID();
											if(!sender.equals(receiver)) {
												new Thread(() -> {
													if(waitTillAccepted(sclient, sf)) {
														String hash = info.getHash();
														long total 	= info.getSize();
														synchronized(sending) {
															sending.put(hash, sending.getOrDefault(hash, 0)+1);
														}
														FileReader reader = sf.getReader(receiver);
														byte[] bytes = new byte[BUFFER_SIZE];
														while(running && reader.read(bytes) != -1) {
															sclient.send(new FileData(hash, bytes, total));
															Utils.sleep(1);
														}
														int current = 0;
														synchronized(sending) {
															current = sending.getOrDefault(hash, 0);
															sending.put(hash, --current);	
														}
														if(reader.isRead() && current <= 0) {
															synchronized(sending) {
																sending.remove(hash);
															}
															fstorage.removeFile(hash);
														}
													}
												}).start();
											}
										}
									} catch(Exception ex) {
									}
								} else if(data instanceof FileData) {
									FileData fileData = (FileData) data;
									StorageFile sfile = fstorage.getFile(fileData.getHash());
									sfile.getWriter().write(fileData.getRawData());
								} else {
									send(data, client.getUUID());
								}
							}
							eventRegistry.call(ServerEvent.DATA_RECEIVED, data);
						}
					}
				}
			}
			Utils.sleep(1);
		}
	});
	
	private Thread threadSendFile;
	private Runnable runSendFile = (() -> {
		while(running) {
			synchronized(senders) {
				try {
					if(senders.size() < MAX_SEND_TRANSFERS) {
						synchronized(filesToSend) {
							if(!filesToSend.isEmpty()) {
								File file;
								if((file = filesToSend.poll()) != null) {
									createFileSenders(file);
								}
							}
						}
					}
					
					if(!senders.isEmpty()) {
						for(int i = 0, l = senders.size(); i < l; ++i) {
							senders.get(i).sendNext();
							Utils.sleep(1);
						}
					}
				} catch(Exception ex) {
				}
			}
			Utils.sleep(1);
		}
	});
	
	protected Server(String ipAddress, int port) {
		this.ipAddress    	 = new IPAddress(ipAddress, port);
		this.clients      	 = new LinkedHashMap<>();
		this.dataToSend   	 = new ConcurrentLinkedQueue<>();
		this.filesToSend  	 = new ConcurrentLinkedQueue<>();
		this.senders	  	 = new ArrayList<>();
		this.acceptedFiles	 = new ListMap<>();
		this.terminatedFiles = new ListMap<>();
		this.waitStateFiles	 = new ListMap<>();
		this.responses		 = new ArrayList<>();
		this.sending		 = new LinkedHashMap<>();
	}
	
	public static Server create(int port) {
		return new Server(PortUtils.getLocalIpAddress(), port);
	}
	
	protected ServerSocket createSocket(int port) throws IOException {
		ServerSocket socket = new ServerSocket(port);
		socket.setSoTimeout(TIMEOUT);
		return socket;
	}
	
	protected ServerClient createClient(Socket socket) {
		return new ServerClient(this, socket);
	}
	
	protected void addDataToSend(Data data, String senderIP, String uuid, String receiver) {
		synchronized(dataToSend) {
			dataToSend.add(FinalData.create(senderIP, uuid, receiver, data));
		}
	}
	
	public void start() {
		if(running) return;
		
		try {
			server 		   = createSocket(ipAddress.getPort());
			fstorage	   = FilesStorage.create(PathSystem.getFullPath("files"));
			identificator  = new Identificator(SERVER_NAME);
			running 	   = true;
			threadAccept   = new Thread(runAccept);
			threadSend	   = new Thread(runSend);
			threadReceived = new Thread(runReceived);
			threadSendFile = new Thread(runSendFile);
			threadAccept.start();
			threadSend.start();
			threadReceived.start();
			threadSendFile.start();
			eventRegistry.call(ServerEvent.STARTED);
		} catch(Exception ex) {
			eventRegistry.call(ServerEvent.CANNOT_START);
		} finally {
			if(!running) stop();
		}
	}
	
	private void closeClients() {
		synchronized(clients) {
			if(!clients.isEmpty()) {
				// Close and remove the clients properly
				Iterator<Entry<Identificator, ServerClient>> it
					= clients.entrySet().iterator();
				while(it.hasNext()) {
					ServerClient client = it.next().getValue();
					client.close(Status.SERVER_STOPPED);
					it.remove();
				}
			}
		}
	}
	
	public void stop() {
		if(!running && (server != null && server.isClosed()))
			return;
		
		try {
			closeClients();
			if(server 	!= null) server.close();
			if(fstorage != null) fstorage.remove();
			running = false;
			
			if(server != null) {
				eventRegistry.call(ServerEvent.STOPPED);
			}
		} catch(Exception ex) {
			eventRegistry.call(ServerEvent.CANNOT_STOP);
		} finally {
			if(running) stop();
		}
	}
	
	public void disconnect(String uuid) {
		disconnect(uuid, Status.DISCONNECTED_BY_SERVER);
	}
	
	protected void disconnect(String uuid, Status status) {
		synchronized(clients) {
			if(hasClient(uuid)) {
				ServerClient client = getClient(uuid);
				client.close(status);
				removeClient0(uuid);
				eventRegistry.call(
					ServerEvent.CLIENT_DISCONNECTED, client);
			}
		}
	}
	
	public void send(Data data) {
		send(data, SERVER_NAME, RECEIVE_ALL);
	}
	
	protected void send(Data data, String uuid) {
		send(data, uuid, RECEIVE_ALL);
	}
	
	protected void send(Data data, String uuid, String receiver) {
		addDataToSend(data, getClientIP(uuid), uuid, receiver);
	}
	
	public void send(Status status) {
		send(new StatusData(status));
	}
	
	public void send(String message) {
		send(new Message(message, SERVER_NAME));
	}
	
	public void sendFile(File file) {
		synchronized(filesToSend) {
			filesToSend.add(file);
		}
	}
	
	public void sendFiles(File... files) {
		for(File file : files) {
			sendFile(file);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Serializable> DataList<T> createList(DataListType type) {
		switch(type) {
			case CONNECTED_CLIENTS:
				synchronized(clients) {
					ServerClientInfo[] array
						= new ServerClientInfo[clients.size()];
					int i = 0;
					for(ServerClient client : clients.values()) {
						ServerClientInfo data
							= new ServerClientInfo(
								client.getIP(),
								client.getIdentificator()
									  .getUUID()
									  .toString(),
								client.getUsername());
						array[i++] = data;
					}
					DataList<ServerClientInfo> list
						= type.<ServerClientInfo>create(array);
					list.setItemClass(ServerClientInfo.class);
					return (DataList<T>) list;
				}
		}
		return null;
	}
	
	public void sendList(DataListType type) {
		sendList(createList(type));
	}
	
	public <T extends Serializable> void sendList(DataList<T> list) {
		if(list == null) {
			throw new IllegalArgumentException(
				"Data list cannot be null!");
		}
		synchronized(clients) {
			for(ServerClient client : clients.values()) {
				sendList(client, list);
			}
		}
	}
	
	public void sendList(ServerClient client, DataListType type) {
		sendList(client, createList(type));
	}
	
	public <T extends Serializable> void sendList(ServerClient client, DataList<T> list) {
		client.send(list.inject(identificator, client.getUUID()));
	}
	
	protected void sendServerIdentificator(ServerClient client) {
		client.send(new IdentificatorData(identificator));
	}
	
	protected void terminateFor(ServerClient client, String fileHash, boolean self) {
		synchronized(terminatedFiles) {
			terminatedFiles.append(client, fileHash);
		}
		synchronized(waitStateFiles) {
			waitStateFiles.removeValue(fileHash);
		}
		if(!self) {
			synchronized(senders) {
				for(int i = 0; i < senders.size(); i++) {
					FileSender sender = senders.get(i);
					if(sender.getHash().equals(fileHash)) {
						sender.close();
						FileSender sendercopy = sender.copyFor(client.getUUID());
						eventRegistry.call(
							ServerEvent.FILE_SEND_TERMINATED, sendercopy);
						break;
					}
				}
			}
		}
	}
	
	public void terminate(String uuid, String fileHash, TransferType type) {
		switch(type) {
			case RECEIVE:
				getClient(uuid).terminate(fileHash, type);
				break;
			case SEND:
				synchronized(senders) {
					for(int i = 0; i < senders.size(); i++) {
						FileSender sender = senders.get(i);
						if(sender.getHash().equals(fileHash)) {
							sender.close();
							senders.remove(i--);
						}
					}
				}
				send(new TerminationData(
					fileHash, TransferType.SEND));
				break;
		}
	}
	
	public ServerClient getClient(String uuid) {
		synchronized(clients) {
			Iterator<Entry<Identificator, ServerClient>> it
				= clients.entrySet().iterator();
			while(it.hasNext()) {
				Entry<Identificator, ServerClient> e
					= it.next();
				if(e.getKey().equals(uuid))
					return e.getValue();
			}
			return null;
		}
	}
	
	public String getClientIP(String uuid) {
		return getClient(uuid).getIP();
	}
	
	public Identificator getClientIdentificator(String ipAddress) {
		synchronized(clients) {
			Iterator<Identificator> it
				= clients.keySet().iterator();
			while(it.hasNext()) {
				Identificator i = it.next();
				if(((String) i.getValue()).equals(ipAddress))
					return i;
			}
			return null;
		}
	}
	
	public String getClientUsername(String uuid) {
		return getClient(uuid).getUsername();
	}
	
	public String getClientUUID(String ipAddress) {
		Identificator identificator
			= getClientIdentificator(ipAddress);
		return identificator == null ? null :
			identificator.getUUID().toString();
	}
	
	public boolean hasClient(String uuid) {
		synchronized(clients) {
			Iterator<Identificator> it
				= clients.keySet().iterator();
			while(it.hasNext()) {
				if(it.next().equals(uuid))
					return true;
			}
			return false;
		}
	}
	
	public boolean removeClient(String uuid) {
		synchronized(clients) {
			Iterator<Entry<Identificator, ServerClient>> it
				= clients.entrySet().iterator();
			while(it.hasNext()) {
				if(it.next().getKey().equals(uuid)) {
					it.remove();
					return true;
				}
			}
			return false;
		}
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public String getIP() {
		return ipAddress.getIPv6();
	}
	
	public int getPort() {
		return ipAddress.getPort();
	}
	
	public String getServerName() {
		return SERVER_NAME;
	}
	
	public List<ServerClient> getClients() {
		synchronized(clients) {
			return Collections.unmodifiableList(
				new ArrayList<>(Utils.mapToList(clients)));
		}
	}
	
	public ListMap<ServerClient, String> getAcceptedFiles() {
		return acceptedFiles;
	}
	
	public ListMap<ServerClient, String> getTerminatedFiles() {
		return terminatedFiles;
	}
	
	public Identificator getIdentificator() {
		return identificator;
	}
	
	public boolean isSecure() {
		return false;
	}
	
	public <E> void addListener(
			EventType<ServerEvent, E> type, Listener<E> listener) {
		eventRegistry.add(type, listener);
	}
	
	public <E> void removeListener(
			EventType<ServerEvent, E> type, Listener<E> listener) {
		eventRegistry.remove(type, listener);
	}
	
	void addClient(Identificator identificator, ServerClient client) {
		synchronized(clients) {
			clients.put(identificator, client);
		}
	}
	
	void removeClient0(String clientIP) {
		removeClient(clientIP);
		sendList(DataListType.CONNECTED_CLIENTS);
	}
	
	protected void accept(ServerClient client, String hash) {
		synchronized(waitStateFiles) {
			waitStateFiles.removeValue(hash);
		}
		synchronized(acceptedFiles) {
			acceptedFiles.append(client, hash);
		}
	}
	
	protected void waitState(ServerClient client, String hash) {
		synchronized(waitStateFiles) {
			waitStateFiles.append(client, hash);
		}
	}
	
	protected boolean canConnect(String uuid) {
		return !hasClient(uuid);
	}
	
	private boolean waitTillAccepted(ServerClient client, StorageFile file) {
		boolean contains = false;
		synchronized(responses) {
			contains = responses.contains(client);
		}
		if(!contains) {
			synchronized(responses) {
				responses.add(client);
			}
		} else {
			boolean has = true;
			while(running && has) {
				synchronized(responses) {
					has = responses.contains(client);
				}
				Utils.sleep(1);
			}
			return waitTillAccepted(client, file);
		}
		String hash = file.getHash();
		String name = file.getName();
		long size	= file.getTotalSize();
		client.sendWait(new FileInfoData(
			hash, name, size, SEND_WAIT_TIME));
		
		List<String> accepted 	= acceptedFiles.ensure(client);
		List<String> terminated = terminatedFiles.ensure(client);
		List<String> waitState  = waitStateFiles.ensure(client);
		
		long current = 0;
		while(running && (
				!(accepted.contains(hash)  ||
				terminated.contains(hash)) &&
				(current < SEND_WAIT_TIME  ||
				waitState.contains(hash)))) {
			++current;
			Utils.sleep(1);
		}
		
		synchronized(responses) {
			responses.remove(client);
		}
		return accepted.contains(hash);
	}
	
	private void createFileSenders(File file) {
		synchronized(clients) {
			for(ServerClient client : clients.values()) {
				createFileSender(client, file);
			}
		}
	}
	
	private void createFileSender(ServerClient client, File file) {
		new Thread(() -> createFileSender0(client, file)).start();
	}
	
	private void createFileSender0(ServerClient client, File file) {
		boolean contains = false;
		synchronized(responses) {
			contains = responses.contains(client);
		}
		if(!contains) {
			synchronized(responses) {
				responses.add(client);
			}
		} else {
			boolean has = true;
			while(running && has) {
				synchronized(responses) {
					has = responses.contains(client);
				}
				Utils.sleep(1);
			}
			createFileSender0(client, file);
			return;
		}
		FileSender fileSender = new FileSender(file);
		String clientIP = client.getIP();
		String hash 	= fileSender.getHash();
		String name 	= file.getName();
		long size		= fileSender.getTotalSize();
		client.sendWait(new FileInfoData(
				hash, name, size, SEND_WAIT_TIME)
			.inject(identificator, clientIP));
		
		List<String> accepted 	= acceptedFiles.ensure(client);
		List<String> terminated = terminatedFiles.ensure(client);
		List<String> waitState  = waitStateFiles.ensure(client);
		
		long current = 0;
		while(running && (
				!(accepted.contains(hash)  ||
				terminated.contains(hash)) &&
				(current < SEND_WAIT_TIME  ||
				waitState.contains(hash)))) {
			++current;
			Utils.sleep(1);
		}
		
		synchronized(responses) {
			responses.remove(client);
		}
		if(accepted.contains(hash)) {
			List<String> banned = terminatedFiles.ensure(client);
			String uuid			= identificator.getUUID().toString();
			Sender sender 		= new Sender() {
				
				@Override
				public void begin() {
					eventRegistry.call(
						ServerEvent.FILE_SEND_BEGIN, fileSender);
				}
				
				@Override
				public void end() {
					synchronized(senders) {
						senders.remove(fileSender);
					}
					synchronized(terminatedFiles) {
						terminatedFiles.removeValue(hash);
					}
					eventRegistry.call(
						ServerEvent.FILE_SEND_END, fileSender);
				}
				
				@Override
				public void send(Data data) {
					if(!running) {
						fileSender.close();
						return;
					}
					
					if(data instanceof FileData) {
						boolean has = false;
						synchronized(banned) {
							has = banned.contains(hash);
						}
						if(!has) client.send(
							data.inject(identificator, clientIP));
						eventRegistry.call(
							ServerEvent.FILE_DATA_SENT, fileSender);
					}
				}
				
				@Override
				public String getSender() {
					return uuid;
				}
			};
			synchronized(senders) {
				senders.add(fileSender);
			}
			fileSender.init(sender, BUFFER_SIZE);
		}
	}
}