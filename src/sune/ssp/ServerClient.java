package sune.ssp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import sune.ssp.data.Status;
import sune.ssp.data.StatusData;
import sune.ssp.data.TerminationData;
import sune.ssp.etc.Connection;
import sune.ssp.etc.DataListType;
import sune.ssp.etc.IPAddress;
import sune.ssp.etc.Identificator;
import sune.ssp.event.ServerEvent;
import sune.ssp.file.FileReceiver;
import sune.ssp.file.Receiver;
import sune.ssp.file.TransferType;
import sune.ssp.util.AntiSpamProtection;
import sune.ssp.util.Utils;

public class ServerClient {
	
	private static final String UNKNOWN_FILE_NAME = "Unknown name";
	private static final String RECEIVE_ALL 	  = "";
	
	protected Server server;
	private String username;
	private Connection connection;
	
	private Identificator identificator;
	
	private Socket socket;
	private ObjectInputStream reader;
	private ObjectOutputStream writer;
	
	private volatile boolean running;
	private Queue<FinalData> dataToSend;
	private Queue<FinalData> dataProcessed;
	private Queue<FinalData> dataReceived;
	
	private Queue<Data> waitQueue;
	private volatile boolean sent;
	
	private Map<String, FileReceiver> receivers;
	
	// ~ Anti-spam protection
	protected AntiSpamProtection asp;
	protected void setAntiSpamProtection(AntiSpamProtection asp) {
		this.asp = asp;
	}
	
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
							Data data = fdata.toData().cast();
							
							// Pre-process the data.
							// Used for various ServerClient implementations.
							Data prevData = data;
							if((data = preProcessData(data)) != prevData) {
								fdata = FinalData.create(fdata, data);
							}
							
							// Anti-spam protection
							if(asp != null && asp.check(data)) {
								server.disconnect(
									getUUID(), Status.DISCONNECTED_BY_SPAM);
								break;
							}
							
							if(data instanceof ClientInfo) {
								ClientInfo info = (ClientInfo) data;
								username		= info.getUsername();
								server.eventRegistry.call(
									ServerEvent.CLIENT_INFO_RECEIVED, info);
								server.sendServerIdentificator(this);
								server.sendList(DataListType.CONNECTED_CLIENTS);
							} else if(data instanceof FileInfoData) {
								FileInfoData fi = (FileInfoData) data;
								String hash 	= fi.getHash();
								String name 	= fi.getName();
								long size 		= fi.getSize();
								String sender 	= fi.getUUID();
								ensureFileReceiver(hash, name, size, sender);
							} else if(data instanceof FileData) {
								FileData fd	  = (FileData) data;
								String hash   = fd.getHash();
								long total    = fd.getTotalSize();
								String sender = fd.getUUID();
								FileReceiver receiver = ensureFileReceiver(
									hash, null, total, sender);
								receiver.receive(fd.getRawData());
							} else if(data instanceof TerminationData) {
								TerminationData td = (TerminationData) data;
								TransferType type  = td.getType();
								String hash 	   = td.getHash();
								switch(type) {
									case RECEIVE:
										server.terminateFor(this, hash, false);
										break;
									case SEND:
										FileReceiver receiver;
										synchronized(receivers) {
											receiver = receivers.get(hash);
											receivers.remove(hash);
										}
										server.eventRegistry.call(
											ServerEvent.FILE_RECEIVE_TERMINATED, receiver);
										break;
								}
							} else if(data instanceof AcceptData) {
								AcceptData ad = (AcceptData) data;
								if(ad.isWaitState()) server.waitState(this, ad.getHash());
								else 				 server.accept(this, ad.getHash());
							}
							
							if(onDataReceived(data)) {
								synchronized(dataProcessed) {
									dataProcessed.add(fdata);
								}
							}
						}
					} catch(Exception ex) {
					}
				}
			}
			Utils.sleep(1);
		}
	});
	
	protected ServerClient(Server server, Socket socket) {
		this.server		   = server;
		this.socket 	   = socket;
		this.connection	   = new Connection(
			new IPAddress(server.getIP(), server.getPort()),
			new IPAddress(socket.getInetAddress()
								.getHostAddress(),
						  socket.getPort()));
		this.dataToSend    = new ConcurrentLinkedQueue<>();
		this.dataProcessed = new ConcurrentLinkedQueue<>();
		this.dataReceived  = new ConcurrentLinkedQueue<>();
		this.waitQueue	   = new ConcurrentLinkedQueue<>();
		this.receivers	   = new LinkedHashMap<>();
		this.identificator = new Identificator(getIP());
		this.prepareStreams();
	}
	
	protected final void prepareStreams() {
		try {
			writer = new ObjectOutputStream(
				new BufferedOutputStream(
					socket.getOutputStream()));
			writer.flush();
			
			reader = new ObjectInputStream(
				new BufferedInputStream(
					socket.getInputStream()));
			running = true;
			
			threadReceive = new Thread(runReceive);
			threadSend 	  = new Thread(runSend);
			threadProcess = new Thread(runProcess);
			threadReceive.start();
			threadSend.start();
			threadProcess.start();
		} catch(Exception ex) {
		}
	}
	
	protected boolean onDataReceived(Data data) {
		return true;
	}
	
	protected Data preProcessData(Data data) {
		return data;
	}
	
	public void close() {
		close(null);
	}
	
	public void close(Status status) {
		if(!running && (socket != null && socket.isClosed()))
			return;
		
		try {
			if(status != null) {
				sendWait(status);
			}
			
			if(socket != null)
				socket.close();
			running = false;
		} catch(Exception ex) {
		} finally {
			if(running) close(null);
		}
	}
	
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
	
	public void sendWait(Status status) {
		sendWait(new StatusData(status));
	}
	
	public void send(Data data) {
		send(data, RECEIVE_ALL);
	}
	
	public void send(Data data, String receiver) {
		addDataToSend(data, server.getIdentificator(), receiver);
	}
	
	protected void send(FinalData data) {
		addDataToSend(data.toData(), server.getIdentificator(), data.getReceiver());
	}
	
	protected void addDataToSend(Data data, Identificator identificator, String receiver) {
		synchronized(dataToSend) {
			dataToSend.add(FinalData.create(identificator, receiver, data));
		}
	}
	
	public void send(Status status) {
		send(new StatusData(status));
	}
	
	public void terminate(String fileHash, TransferType type) {
		switch(type) {
			case RECEIVE:
				synchronized(receivers) {
					Iterator<Entry<String, FileReceiver>> it
						= receivers.entrySet().iterator();
					while(it.hasNext()) {
						FileReceiver receiver = it.next().getValue();
						if(receiver.getHash().equals(fileHash)) {
							it.remove();
						}
					}
				}
				send(new TerminationData(
					fileHash, TransferType.RECEIVE));
				break;
			default:
				break;
		}
	}
	
	public FinalData nextData() {
		synchronized(dataProcessed) {
			return dataProcessed.poll();
		}
	}
	
	public String getIP() {
		return connection.getDestination().getIPv6();
	}
	
	public String getUsername() {
		return username;
	}
	
	public Identificator getIdentificator() {
		return identificator;
	}
	
	public String getUUID() {
		return identificator != null ?
			identificator.getUUID().toString() : null;
	}
	
	public boolean isSecure() {
		return false;
	}
	
	private FileReceiver ensureFileReceiver(String hash, String name, long size, String sender) {
		FileReceiver fileReceiver = null;
		boolean contains 		  = false;
		synchronized(receivers) {
			contains = receivers.containsKey(hash);
		}
		if(!contains) {
			fileReceiver = new FileReceiver(
				hash, name, size, sender);
			// Used in another Thread as an effectively final variable
			FileReceiver fr = fileReceiver;
			fileReceiver.init(new Receiver() {
	
				@Override
				public void begin() {
					synchronized(receivers) {
						receivers.put(hash, fr);
					}
				}
				
				@Override
				public void end() {
					FileInfo info = new FileInfo(
						hash, name == null ?
							UNKNOWN_FILE_NAME : name,
						sender);
					synchronized(receivers) {
						receivers.remove(hash);
					}
					server.eventRegistry.call(
						ServerEvent.FILE_RECEIVED, info);
				}
				
				@Override
				public void receive(byte[] data) {
					FileData fileData = new FileData(
						hash, data, size);
					server.eventRegistry.call(
						ServerEvent.FILE_DATA_RECEIVED, fileData);
				}
	
				@Override
				public String getSender() {
					return sender;
				}
			});
		} else if(name != null) {
			synchronized(receivers) {
				fileReceiver = receivers.get(hash);
				fileReceiver.setName(name);
			}
		}
		// If the list contains the file receiver and it has
		// all the data correctly set, get the file receiver
		// from the list. This will be called if the file
		// receiver has been created and its name is set.
		if(fileReceiver == null) {
			synchronized(receivers) {
				fileReceiver = receivers.get(hash);
			}
		}
		return fileReceiver;
	}
}