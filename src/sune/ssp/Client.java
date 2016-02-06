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

import sune.ssp.data.AcceptData;
import sune.ssp.data.ClientInfo;
import sune.ssp.data.Data;
import sune.ssp.data.FileData;
import sune.ssp.data.FileInfo;
import sune.ssp.data.FileInfoData;
import sune.ssp.data.FinalData;
import sune.ssp.data.Message;
import sune.ssp.data.Status;
import sune.ssp.data.StatusData;
import sune.ssp.data.TerminationData;
import sune.ssp.etc.Connection;
import sune.ssp.etc.IPAddress;
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
 * This class represents simple and not secured client that
 * is using SSP (Sune's Simple Protocol). It provides default
 * communication with a server and basic functionality, such
 * as sending/receiving data and sending/receiving files.
 * @author Sune
 * @since alpha v0.1-pre1*/
public class Client {
	
	private static final String UNKNOWN_FILE_NAME = "Unknown name";
	
	private String username;
	private Connection connection;
	
	private Socket socket;
	private ObjectInputStream reader;
	private ObjectOutputStream writer;
	private volatile boolean running;
	
	private boolean connected;
	private Queue<FinalData> dataToSend;
	private Queue<FinalData> dataReceived;
	
	private Queue<Data> waitQueue;
	private boolean sent;
	
	private Map<String, FileReceiver> receivers;
	private Queue<File> filesToSend;
	private List<FileSender> senders;
	private List<Waiter> waiters;
	
	private boolean promptReceiveFile;
	/**
	 * Sets whether the prompt to receive a file should be shown or not.
	 * @param value If true, listeners of {@link #registryPromptReceiveFile()}
	 * are called when a file is sent to this client, otherwise not.*/
	public void setPromptToReceiveFile(boolean value) {
		promptReceiveFile = value;
	}
	
	private int BUFFER_SIZE = 8192;
	/**
	 * Sets size of buffer that is used when sending a file.
	 * @param size New size of the buffer. Default is 8192.*/
	public void setBufferSize(int size) {
		BUFFER_SIZE = size;
	}
	
	private int MAX_SEND_TRANSFERS = 5;
	/**
	 * Sets the maximum amount of file-send transfers.
	 * @param max New maximum amount of file-send transfers. Default is 5.*/
	public void setMaxSendTransfers(int max) {
		MAX_SEND_TRANSFERS = max;
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
							}
							
							if(data instanceof FileInfoData) {
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
								FileReceiver receiver = receivers.get(hash);
								receiver.receive(fd.getRawData());
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
										FileReceiver receiver = receivers.get(hash);
										synchronized(receivers) {
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
			if(senders.size() < MAX_SEND_TRANSFERS) {
				synchronized(filesToSend) {
					try {
						File file;
						if((file = filesToSend.poll()) != null) {
							createFileSender(file);
						}
					} catch(Exception ex) {
					}
				}
			}
			
			if(senders.size() > 0) {
				synchronized(senders) {
					for(int i = 0; i < senders.size(); ++i) {
						senders.get(i).sendNext();
						Utils.sleep(1);
					}
				}
			}
			
			Utils.sleep(1);
		}
	});
	
	protected Client(String serverIP, int serverPort) {
		this.connection   = new Connection(
			new IPAddress(PortUtils.getIpAddress(), serverPort),
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
	 * Creates a client with information for future establishing a connection
	 * with the server specified by its IP address and its port.
	 * @param serverIP The server IP address
	 * @param serverPort The server port
	 * @return Created client object. Call {@link #connect()} to connect the
	 * client to the specified server.*/
	public static Client create(String serverIP, int serverPort) {
		return new Client(serverIP, serverPort);
	}
	
	protected Socket createSocket(String serverIP, int serverPort)
			throws UnknownHostException, IOException {
		Socket socket = new Socket(serverIP, serverPort);
		socket.setSoTimeout(8000);
		return socket;
	}
	
	protected void addDataToSend(Data data) {
		synchronized(dataToSend) {
			dataToSend.add(FinalData.create(getIP(), data));
		}
	}
	
	protected Data onDataReceived(Data data) {
		return data;
	}
	
	/**
	 * Connects this client to the server.*/
	public void connect() {
		if(running) return;
		
		try {
			if(username == null) {
				setUsername("username" + Randomizer.nextLong());
			}
			IPAddress addr = connection.getDestination();
			socket = createSocket(addr.getIP(),
								  addr.getPort());
			connection = new Connection(
				new IPAddress(socket.getInetAddress()
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
			send(new ClientInfo(username));
		} catch(SocketException ex) {
			eventRegistry.call(ClientEvent.CONNECTION_TIMEOUT);
			disconnect();
		} catch(Exception ex) {
			eventRegistry.call(ClientEvent.CANNOT_CONNECT);
			disconnect();
		}
	}
	
	/**
	 * Disconnects this client from the server.*/
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
	 * Sends a data to the server and waits until
	 * the data are sent.
	 * @param data The data to be sent*/
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
	 * Sends a status to the server and waits until
	 * the status is sent.
	 * @param status The status to be sent*/
	public void sendWait(Status status) {
		sendWait(new StatusData(status));
	}
	
	/**
	 * Sends a data to the server.
	 * @param data The data to be sent*/
	public void send(Data data) {
		addDataToSend(data);
	}
	
	/**
	 * Sends a status to the server.
	 * @param status The status to be sent*/
	public void send(Status status) {
		send(new StatusData(status));
	}
	
	public void send(String message) {
		send(new Message(message, username));
	}
	
	/**
	 * Sends a file to the server.
	 * @param file The file to be sent*/
	public void sendFile(File file) {
		synchronized(filesToSend) {
			filesToSend.add(file);
		}
	}
	
	/**
	 * Sends multiple files to the server.
	 * @param files The files to be sent*/
	public void sendFiles(File... files) {
		for(File file : files) {
			sendFile(file);
		}
	}
	
	/**
	 * Terminates a file transfer by its type and file's hash.
	 * @param fileHash The hash of file whose transfer should be terminated
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
					send(new TerminationData(
						fileHash, TransferType.RECEIVE));
				}
				break;
			case SEND:
				synchronized(senders) {
					for(int i = 0; i < senders.size(); ++i) {
						FileSender sender = senders.get(i);
						if(sender.getHash().equals(fileHash)) {
							sender.close();
							senders.remove(i);
						}
					}
					send(new TerminationData(
						fileHash, TransferType.SEND));
				}
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
	
	public int getPort() {
		return connection.getSource().getPort();
	}
	
	public String getServerIP() {
		return connection.getDestination().getIPv6();
	}
	
	public int getServerPort() {
		return connection.getDestination().getPort();
	}
	
	public boolean isSecure() {
		return false;
	}
	
	public <E> void addListener(
			EventType<ClientEvent, E> type, Listener<E> listener) {
		eventRegistry.add(type, listener);
	}
	
	public <E> void removeListener(
			EventType<ClientEvent, E> type, Listener<E> listener) {
		eventRegistry.remove(type, listener);
	}
	
	private void ensureFileReceiver(String hash, String name, long size, String senderIP) {
		if(!receivers.containsKey(hash)) {
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
					receivers.remove(hash);
					eventRegistry.call(
						ClientEvent.FILE_RECEIVED, info);
				}
				
				@Override
				public void receive(byte[] data) {
					eventRegistry.call(ClientEvent.FILE_DATA_RECEIVED,
						new FileData(hash, data, size));
				}
	
				@Override
				public String getSenderIP() {
					return senderIP;
				}
			});
		} else if(name != null) {
			receivers.get(hash).setName(name);
		}
	}
	
	private void createFileSender(File file) {
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
				senders.remove(fileSender);
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
		
		synchronized(senders) {
			senders.add(fileSender);
		}
		fileSender.init(sender, BUFFER_SIZE);
	}
}