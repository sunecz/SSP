package sune.ssp.event;

import sune.ssp.ServerClient;
import sune.ssp.data.ClientInfo;
import sune.ssp.data.Data;
import sune.ssp.data.FileData;
import sune.ssp.data.FileInfo;
import sune.ssp.file.FileReceiver;
import sune.ssp.file.FileSender;

public final class ServerEvent implements IEventType {
	
	public static final EventType<ServerEvent, Data> 		 DATA_RECEIVED 			  = new EventType<>();
	public static final EventType<ServerEvent, FileData> 	 FILE_DATA_RECEIVED 	  = new EventType<>();
	public static final EventType<ServerEvent, FileInfo>	 FILE_RECEIVED 			  = new EventType<>();
	public static final EventType<ServerEvent, ServerClient> CLIENT_DISCONNECTED 	  = new EventType<>();
	public static final EventType<ServerEvent, FileSender> 	 FILE_SEND_BEGIN 		  = new EventType<>();
	public static final EventType<ServerEvent, FileSender> 	 FILE_SEND_END 			  = new EventType<>();
	public static final EventType<ServerEvent, FileSender> 	 FILE_DATA_SENT 		  = new EventType<>();
	public static final EventType<ServerEvent, FileReceiver> FILE_RECEIVE_TERMINATED  = new EventType<>();
	public static final EventType<ServerEvent, FileSender> 	 FILE_SEND_TERMINATED 	  = new EventType<>();
	public static final EventType<ServerEvent, ServerClient> CLIENT_CONNECTED 		  = new EventType<>();
	public static final EventType<ServerEvent, ServerClient> CLIENT_ALREADY_CONNECTED = new EventType<>();
	public static final EventType<ServerEvent, Object> 		 STARTED 	 			  = new EventType<>();
	public static final EventType<ServerEvent, Object> 		 CANNOT_START  			  = new EventType<>();
	public static final EventType<ServerEvent, Object> 		 STOPPED 				  = new EventType<>();
	public static final EventType<ServerEvent, Object> 		 CANNOT_STOP 			  = new EventType<>();
	
	public static final EventType<ServerEvent, ClientInfo> CLIENT_INFO_RECEIVED	= new EventType<>();
}