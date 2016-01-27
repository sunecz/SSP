package sune.ssp.event;

import sune.ssp.data.Data;
import sune.ssp.data.FileData;
import sune.ssp.data.FileInfo;
import sune.ssp.file.FileReceiver;
import sune.ssp.file.FileSender;
import sune.ssp.util.DataWaiter;

public final class ClientEvent implements IEventType {
	
	public static final EventType<ClientEvent, Data> 		 DATA_RECEIVED 			 = new EventType<>();
	public static final EventType<ClientEvent, FileSender> 	 FILE_SEND_BEGIN 		 = new EventType<>();
	public static final EventType<ClientEvent, FileSender> 	 FILE_SEND_END 			 = new EventType<>();
	public static final EventType<ClientEvent, FileSender> 	 FILE_DATA_SENT 		 = new EventType<>();
	public static final EventType<ClientEvent, FileInfo>	 FILE_RECEIVED 			 = new EventType<>();
	public static final EventType<ClientEvent, FileData> 	 FILE_DATA_RECEIVED 	 = new EventType<>();
	public static final EventType<ClientEvent, FileReceiver> FILE_RECEIVE_TERMINATED = new EventType<>();
	public static final EventType<ClientEvent, FileSender> 	 FILE_SEND_TERMINATED 	 = new EventType<>();
	public static final EventType<ClientEvent, DataWaiter> 	 PROMPT_RECEIVE_FILE 	 = new EventType<>();
	public static final EventType<ClientEvent, Object> 		 CONNECTED 				 = new EventType<>();
	public static final EventType<ClientEvent, Object> 		 ALREADY_CONNECTED 		 = new EventType<>();
	public static final EventType<ClientEvent, Object> 		 CONNECTION_TIMEOUT 	 = new EventType<>();
	public static final EventType<ClientEvent, Object> 		 CANNOT_CONNECT 		 = new EventType<>();
	public static final EventType<ClientEvent, Object> 		 DISCONNECTED 			 = new EventType<>();
	public static final EventType<ClientEvent, Object> 		 CANNOT_DISCONNECT 		 = new EventType<>();
}