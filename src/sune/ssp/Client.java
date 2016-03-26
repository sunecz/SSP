package sune.ssp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import sune.ssp.data.AcceptData;
import sune.ssp.data.ClientData;
import sune.ssp.data.ClientInfo;
import sune.ssp.data.Data;
import sune.ssp.data.FileData;
import sune.ssp.data.FileInfo;
import sune.ssp.data.FileInfoData;
import sune.ssp.data.FinalData;
import sune.ssp.data.Status;
import sune.ssp.data.StatusData;
import sune.ssp.data.TerminationData;
import sune.ssp.etc.Connection;
import sune.ssp.etc.IPAddress;
import sune.ssp.etc.Identificator;
import sune.ssp.event.ClientEvent;
import sune.ssp.event.EventRegistry;
import sune.ssp.event.EventType;
import sune.ssp.event.Listener;
import sune.ssp.file.FileReceiver;
import sune.ssp.file.FileSender;
import sune.ssp.file.Receiver;
import sune.ssp.file.Sender;
import sune.ssp.file.TransferType;
import sune.ssp.util.DataWaiter;
import sune.ssp.util.PortUtils;
import sune.ssp.util.Randomizer;
import sune.ssp.util.Utils;
import sune.ssp.util.Waiter;

/**
 * This class represents simple and not secured client that is using
 * SSP (Sune's Simple Protocol). It provides default communication
 * with a server and basic functionality, such as sending/receiving
 * data and sending/receiving files.<br>
 * 
 * <h2>Creating a new client</h2>
 * Client is created using static method {@link #create(String, int)}.
 * After calling this method you will obtain a new client object that
 * holds information about to which server it should be connected. The
 * client is not connected till method {@link #connect()} is called.
 * After calling this method client will try to connect to the specified
 * server.<br><br>
 * <ul>
 * 	<li>When the client has been successfully connected event listeners bound
 * to event {@link ClientEvent#CONNECTED CONNECTED} are called.</li>
 * 	<li>When the client could not be connected event listeners bound to event
 * {@link ClientEvent#CANNOT_CONNECT CANNOT_CONNECT} are called.</li>
 * 	<li>When the connection has timed out event listeners bound to event
 * {@link ClientEvent#CONNECTION_TIMEOUT CONNECTION_TIMEOUT} are called.</li>
 * </ul>
 * 
 * All event listeners should be bound to the specific event before calling
 * the {@link #connect()} method.
 * 
 * <pre>
 * Client client = Client.create(serverIP, serverPort);
 * client.connect();
 * </pre>
 * 
 * <i>Note: The {@link #connect()} method runs in the same thread it was called.
 * So if the connection takes much time call this method in different thread.</i>
 * 
 * <h2>Binding event listeners to events</h2>
 * Every event listener is bound to an event in the same way - using the
 * {@link #addListener(EventType, Listener)} method and can be removed using
 * the {@link #removeListener(EventType, Listener)} method.
 * 
 * <pre>
 * Client client = Client.create(serverIP, serverPort);
 * client.addListener(ClientEvent.CONNECTED, (value) -> {
 * 	System.out.println("Client has been connected!");
 * });
 * client.connect();
 * </pre>
 * 
 * <i>Note: The <b>value</b> parameter in event listener could be null and is
 * null at some events, such as {@link CONNECTED} and others.</i><br><br>
 * @author Sune*/
public class Client {
	
	private static final String UNKNOWN_FILE_NAME = "Unknown name";
	private static final String RECEIVE_ALL 	  = "";
	
	private String username;
	private Connection connection;
	
	private Identificator identificator;
	
	private Socket socket;
	private ObjectInputStream reader;
	private ObjectOutputStream writer;
	private volatile boolean running;
	
	private boolean connected;
	private Queue<FinalData> dataToSend;
	private Queue<FinalData> dataReceived;
	
	private Queue<Data> waitQueue;
	private volatile boolean sent;
	
	private Map<String, FileReceiver> receivers;
	private Queue<File> filesToSend;
	private List<FileSender> senders;
	private List<Waiter> waiters;
	
	private boolean promptReceiveFile;
	/**
	 * Sets whether the prompt to receive a file should be shown or not.
	 * @param value If true, event listeners bound to event
	 * {@link ClientEvent#PROMPT_RECEIVE_FILE PROMPT_RECEIVE_FILE} are
	 * called when a file information are sent to this client, otherwise not.*/
	public void setPromptToReceiveFile(boolean value) {
		promptReceiveFile = value;
	}
	
	private int BUFFER_SIZE = 8192;
	/**
	 * Sets size of buffer that is used when sending a file.
	 * @param size New size of the buffer. Default value is 8192.*/
	public void setBufferSize(int size) {
		BUFFER_SIZE = size;
	}
	
	private int MAX_SEND_TRANSFERS = 5;
	/**
	 * Sets the maximum amount of file-send transfers.
	 * @param max New maximum amount of file-send transfers.
	 * Default value is 5.*/
	public void setMaxSendTransfers(int max) {
		MAX_SEND_TRANSFERS = max;
	}
	
	protected int TIMEOUT = 8000;
	/**
	 * Sets the timeout when connecting to the server.
	 * @param timeout New timeout value (in milliseconds).
	 * Default value is 8000.*/
	public void setTimeout(int timeout) {
		TIMEOUT = timeout;
	}
	
	// Event registry
	protected final EventRegistry<ClientEvent> eventRegistry = new EventRegistry<>();
	
	private Thread threadSend;
	private Runnable runSend = (() -> {
		while(running) {
			synchronized(dataToSend) {
				if(!dataToSend.isEmpty()) {
					FinalData data;
					if((data = dataToSend.poll()) != null) {
						try {
							sent = false;
							writer.writeObject(data);
							writer.flush();
						} catch(Exception ex) {
						}
						sent = true;
					}
				}
			}
			Utils.sleep(1);
		}
	});
	
	private Thread threadReceive;
	private Runnable runReceive = (() -> {
		while(running) {
			try {
				Object object;
				if((object = reader.readObject()) != null &&
				   (object instanceof FinalData)) {
					synchronized(dataReceived) {
						dataReceived.add((FinalData) object);
					}
				}
			} catch(Exception ex) {
			}
			Utils.sleep(1);
		}
	});
	
	private Thread threadProcess;
	private Runnable runProcess = (() -> {
		while(running) {
			synchronized(dataReceived) {
				if(!dataReceived.isEmpty()) {
					try {
						FinalData fdata;
						if((fdata = dataReceived.poll()) != null) {
							Data data = onDataReceived(fdata.toData().cast());
							if(data instanceof StatusData) {
								Status status = ((StatusData) data).getStatus();
								switch(status) {
									case SUCCESSFULLY_CONNECTED:
										connected = true;
										eventRegistry.call(ClientEvent.CONNECTED);
										break;
									case CLIENT_ALREADY_CONNECTED:
										eventRegistry.call(ClientEvent.ALREADY_CONNECTED);
										disconnect(false);
										break;
									case DISCONNECTED_BY_SERVER:
									case DISCONNECTED_BY_SPAM:
									case SERVER_STOPPED:
										disconnect(false);
										break;
									default:
										break;
								}
							} else if(data instanceof ClientData) {
								ClientData cd = (ClientData) data;
								identificator = cd.getIdentificator();
								eventRegistry.call(
									ClientEvent.IDENTIFICATOR_RECEIVED);
								send(new ClientInfo(username));
							} else if(data instanceof FileInfoData) {
								FileInfoData fi = (FileInfoData) data;
								String hash 	= fi.getHash();
								String name 	= fi.getName();
								long size 		= fi.getSize();
								String senderIP = fi.getSenderIP();
								long time		= fi.getWaitTime();
								Waiter waiter 	= new Waiter(time) {
									
									@Override
									public void accepted() {
										sendWait(new AcceptData(hash));
										ensureFileReceiver(
											hash, name, size, senderIP);
									}
									
									@Override
									public void refused() {
										terminate(hash, TransferType.RECEIVE);
									}
									
									@Override
									public void waitingStateChanged(boolean value) {
										if(value) sendWait(new AcceptData(hash, true));
									}
								};
								
								if(promptReceiveFile) {
									waiter.start();
									eventRegistry.call(ClientEvent.PROMPT_RECEIVE_FILE,
										new DataWaiter(waiter, data));
								} else {
									waiter.accepted();
								}
							} else if(data instanceof FileData) {
								FileData fd		= (FileData) data;
								String hash 	= fd.getHash();
								long total 		= fd.getTotalSize();
								String senderIP = fd.getSenderIP();
								ensureFileReceiver(hash, null, total, senderIP);
								synchronized(receivers) {
									FileReceiver receiver = receivers.get(hash);
									receiver.receive(fd.getRawData());
								}
							} else if(data instanceof TerminationData) {
								TerminationData td = (TerminationData) data;
								TransferType type  = td.getType();
								String hash 	   = td.getHash();
								switch(type) {
									case RECEIVE:
										synchronized(senders) {
											for(int i = 0; i < senders.size(); i++) {
												FileSender sender = senders.get(i);
												if(sender.getHash().equals(hash)) {
													sender.close();
													senders.remove(i);
													eventRegistry.call(
														ClientEvent.FILE_SEND_TERMINATED, sender);
													break;
												}
											}
										}
										break;
									case SEND:
										FileReceiver receiver = null;
										synchronized(receivers) {
											receiver = receivers.get(hash);
											receivers.remove(hash);
										}
										eventRegistry.call(
											ClientEvent.FILE_RECEIVE_TERMINATED, receiver);
										break;
								}
							}
							eventRegistry.call(ClientEvent.DATA_RECEIVED, data);
						}
					} catch(Exception ex) {
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
									createFileSender(file);
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
	
	protected Client(String serverIP, int serverPort) {
		this.connection   = new Connection(
			new IPAddress(PortUtils.getLocalIpAddress(), serverPort),
			new IPAddress(serverIP, serverPort));
		this.dataToSend   = new ConcurrentLinkedQueue<>();
		this.dataReceived = new ConcurrentLinkedQueue<>();
		this.waitQueue	  = new ConcurrentLinkedQueue<>();
		this.filesToSend  = new ConcurrentLinkedQueue<>();
		this.receivers 	  = new LinkedHashMap<>();
		this.senders	  = new ArrayList<>();
		this.waiters	  = new ArrayList<>();
	}
	
	/**
	 * Creates a not connected client with information for future establishing
	 * a connection with the server specified by its IP address and its port.
	 * To connect this created client the {@link #connect()} method has to be
	 * called.
	 * @param serverIP Server IP address
	 * @param serverPort Server port
	 * @return Created not connected client object.*/
	public static Client create(String serverIP, int serverPort) {
		return new Client(serverIP, serverPort);
	}
	
	protected Socket createSocket(String serverIP, int serverPort)
			throws UnknownHostException, IOException {
		Socket socket = new Socket(serverIP, serverPort);
		socket.setSoTimeout(TIMEOUT);
		return socket;
	}
	
	protected void addDataToSend(Data data, String receiver) {
		synchronized(dataToSend) {
			dataToSend.add(FinalData.create(identificator, receiver, data));
		}
	}
	
	protected Data onDataReceived(Data data) {
		return data;
	}
	
	protected String generateUsername() {
		return "username" + Randomizer.nextLong();
	}
	
	/**
	 * Connects this client to the server. This method is run in the same
	 * thread as it was called.*/
	public void connect() {
		if(running) return;
		
		try {
			if(username == null || username.isEmpty()) {
				setUsername(generateUsername());
			}
			IPAddress addr = connection.getDestination();
			socket = createSocket(addr.getIP(),
								  addr.getPort());
			connection = new Connection(
				new IPAddress(socket.getLocalAddress()
									.getHostAddress(),
							  socket.getPort()),
				connection.getDestination());
			writer = new ObjectOutputStream(
				new BufferedOutputStream(
					socket.getOutputStream()));
			writer.flush();
			
			reader = new ObjectInputStream(
				new BufferedInputStream(
					socket.getInputStream()));
			
			running 	   = true;
			threadSend 	   = new Thread(runSend);
			threadReceive  = new Thread(runReceive);
			threadProcess  = new Thread(runProcess);
			threadSendFile = new Thread(runSendFile);
			threadSend.start();
			threadReceive.start();
			threadProcess.start();
			threadSendFile.start();
		} catch(SocketException ex) {
			eventRegistry.call(ClientEvent.CONNECTION_TIMEOUT);
			disconnect();
		} catch(Exception ex) {
			eventRegistry.call(ClientEvent.CANNOT_CONNECT);
			disconnect();
		}
	}
	
	/**
	 * Disconnects this client from the server. This method is run in the same
	 * thread as it was called.*/
	public void disconnect() {
		disconnect(true);
	}
	
	private void disconnect(boolean sendStatus) {
		if(!running && (socket != null && socket.isClosed()))
			return;
		
		try {
			stopWaiters();
			if(sendStatus)
				sendWait(Status.DISCONNECTED_BY_USER);
			if(socket != null)
				socket.close();
			running   = false;
			connected = false;
			eventRegistry.call(ClientEvent.DISCONNECTED);
		} catch(Exception ex) {
			eventRegistry.call(ClientEvent.CANNOT_DISCONNECT);
		} finally {
			if(running) disconnect(sendStatus);
		}
	}
	
	private void stopWaiters() {
		Iterator<Waiter> it = waiters.iterator();
		while(it.hasNext()) {
			Waiter waiter = it.next();
			if(waiter != null)
				waiter.stop();
			it.remove();
		}
	}
	
	/**
	 * Sends a data to the server and waits till the data are sent.
	 * @param data Data to be sent*/
	public void sendWait(Data data) {
		waitQueue.add(data);
		while(waitQueue.peek() != data)
			Utils.sleep(1);
		sent = false;
		send(data);
		while(!sent)
			Utils.sleep(1);
		waitQueue.remove();
	}
	
	/**
	 * Sends a status to the server and waits till the status is sent.
	 * @param status Status to be sent*/
	public void sendWait(Status status) {
		sendWait(new StatusData(status));
	}
	
	/**
	 * Sends a data to the server. This method does not wait till
	 * the data are sent.
	 * @param data Data to be sent*/
	public void send(Data data) {
		send(data, RECEIVE_ALL);
	}
	
	/**
	 * Sends a data to the client specified by its IP address. This method
	 * does not wait till the data are sent.
	 * @param data Data to be sent
	 * @param receiver IP address of the client to which the data should be
	 * sent.*/
	public void send(Data data, String receiver) {
		addDataToSend(data, receiver);
	}
	
	/**
	 * Sends a status to the server. This method does not wait till the status
	 * is sent.
	 * @param status Status to be sent*/
	public void send(Status status) {
		send(new StatusData(status));
	}
	
	/**
	 * Sends a file to the server. This method does not wait till the file
	 * is sent.
	 * @param file File to be sent*/
	public void sendFile(File file) {
		synchronized(filesToSend) {
			filesToSend.add(file);
		}
	}
	
	/**
	 * Sends multiple files to the server. This method does not wait till the files
	 * are sent.
	 * @param files Files to be sent*/
	public void sendFiles(File... files) {
		for(File file : files) {
			sendFile(file);
		}
	}
	
	/**
	 * Terminates a file transfer by its type and file's hash.
	 * @param fileHash Hash of file whose transfer should be terminated.
	 * @param type Type of the transfer*/
	public void terminate(String fileHash, TransferType type) {
		switch(type) {
			case RECEIVE:
				synchronized(receivers) {
					for(Entry<String, FileReceiver> entry : receivers.entrySet()) {
						FileReceiver receiver = entry.getValue();
						if(receiver.getHash().equals(fileHash)) {
							receivers.remove(entry.getKey());
						}
					}
				}
				send(new TerminationData(
					fileHash, TransferType.RECEIVE));
				break;
			case SEND:
				synchronized(senders) {
					for(int i = 0; i < senders.size(); ++i) {
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
	
	/**
	 * Sets this client's username.
	 * @param name New username of this client*/
	public void setUsername(String name) {
		username = name;
	}
	
	/**
	 * Gets this client's username.
	 * @return This client's username*/
	public String getUsername() {
		return username;
	}
	
	/**
	 * Gets whether this client is connected to the server or not.
	 * @return True, if the client is connected, otherwise false.*/
	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * Gets this client's IP address.
	 * @return This client's IP address.*/
	public String getIP() {
		return connection.getSource().getIPv6();
	}
	
	/**
	 * Gets this client's port that is used to communicate with the server.
	 * @return This client's port that is used to communicate with the
	 * server.*/
	public int getPort() {
		return connection.getSource().getPort();
	}
	
	/**
	 * Gets IP address of the server to which this client is connected.
	 * @return IP address of the server to which this client is connected.*/
	public String getServerIP() {
		return connection.getDestination().getIPv6();
	}
	
	/**
	 * Gets port of the server to which this client is connected.
	 * @return Port of the server to which this client is connected.*/
	public int getServerPort() {
		return connection.getDestination().getPort();
	}
	
	/**
	 * Gets information about if this client is secured or not.
	 * @return True, if the client is secured, otherwise not.*/
	public boolean isSecure() {
		return false;
	}
	
	/**
	 * Binds an event listener to an event.
	 * @param <E> Type of the value parameter.
	 * @param type Event to which bind the event listener.
	 * @param listener Listener that should be bound to the event.*/
	public <E> void addListener(
			EventType<ClientEvent, E> type, Listener<E> listener) {
		eventRegistry.add(type, listener);
	}
	
	/**
	 * Unbinds an event listener from an event.
	 * @param <E> Type of the value parameter.
	 * @param type Event from which unbind the event listener.
	 * @param listener Listener that should be unbound from the event.*/
	public <E> void removeListener(
			EventType<ClientEvent, E> type, Listener<E> listener) {
		eventRegistry.remove(type, listener);
	}
	
	private void ensureFileReceiver(String hash, String name, long size, String senderIP) {
		boolean contains = false;
		synchronized(receivers) {
			contains = receivers.containsKey(hash);
		}
		if(!contains) {
			FileReceiver fileReceiver = new FileReceiver(
				hash, name, size, senderIP);
			fileReceiver.init(new Receiver() {
	
				@Override
				public void begin() {
					synchronized(receivers) {
						receivers.put(hash, fileReceiver);
					}
				}
				
				@Override
				public void end() {
					FileInfo info = new FileInfo(
						hash, name == null ?
							UNKNOWN_FILE_NAME : name,
						senderIP);
					synchronized(receivers) {
						receivers.remove(hash);
					}
					eventRegistry.call(
						ClientEvent.FILE_RECEIVED, info);
				}
				
				@Override
				public void receive(byte[] data) {
					FileData fileData = new FileData(
						hash, data, size);
					eventRegistry.call(
						ClientEvent.FILE_DATA_RECEIVED, fileData);
				}
	
				@Override
				public String getSenderIP() {
					return senderIP;
				}
			});
		} else if(name != null) {
			synchronized(receivers) {
				receivers.get(hash).setName(name);
			}
		}
	}
	
	private void createFileSender(File file) {
		new Thread(() -> createFileSender0(file)).start();
	}
	
	private AtomicInteger count_st = new AtomicInteger();
	private void waitToCorrectAmountST() {
		int count = senders.size() +
					count_st.get();
		if(count >= MAX_SEND_TRANSFERS) {
			while(count >= MAX_SEND_TRANSFERS) {
				count = senders.size() +
						count_st.get();
				Utils.sleep(1);
			}
		}
		count_st.incrementAndGet();
		// Double-check to ensure the corrent amount
		// of running send transfers.
		count = senders.size() +
				count_st.get();
		if(count > MAX_SEND_TRANSFERS) {
			count_st.decrementAndGet();
			waitToCorrectAmountST();
		}
	}
	
	private void createFileSender0(File file) {
		waitToCorrectAmountST();
		FileSender fileSender = new FileSender(file);
		sendWait(new FileInfoData(
			fileSender.getHash(),
			file.getName(),
			fileSender.getTotalSize(), -1));
		
		Sender sender = new Sender() {
			
			@Override
			public void begin() {
				eventRegistry.call(
					ClientEvent.FILE_SEND_BEGIN, fileSender);
			}
			
			@Override
			public void end() {
				synchronized(senders) {
					senders.remove(fileSender);
				}
				eventRegistry.call(
					ClientEvent.FILE_SEND_END, fileSender);
			}
			
			@Override
			public void send(Data data) {
				if(!running) {
					fileSender.close();
					return;
				}
				
				if(data instanceof FileData) {
					Client.this.send(data);
					eventRegistry.call(
						ClientEvent.FILE_DATA_SENT, fileSender);
				}
			}
			
			@Override
			public String getSenderIP() {
				return getIP();
			}
		};
		count_st.decrementAndGet();
		synchronized(senders) {
			senders.add(fileSender);
		}
		fileSender.init(sender, BUFFER_SIZE);
	}
}