package sune.ssp.secure;

import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSocket;

import sune.ssp.ServerClient;
import sune.ssp.data.Data;
import sune.ssp.data.PublicKeyData;
import sune.ssp.etc.Identificator;
import sune.ssp.event.ServerEvent;
import sune.ssp.util.Serialization;
import sune.util.crypt.CryptMethod;

public class SecureServerClient extends ServerClient {
	
	private PublicKey publicKey;
	private SymmetricKey symmetricKey;
	private volatile boolean publicKeySent;
	private volatile boolean useCrypt;
	
	private List<DataHolder> cannotCrypt;
	private static class DataHolder {
		public final Data data;
		public final Identificator identificator;
		public final String receiver;
		
		public DataHolder(Data data,
				Identificator identificator, String receiver) {
			this.data 		   = data;
			this.identificator = identificator;
			this.receiver 	   = receiver;
		}
	}
	
	private CryptMethod cryptMethod;
	
	protected SecureServerClient(SecureServer server, SSLSocket socket) {
		super(server, socket);
		cannotCrypt = new ArrayList<>();
		cryptMethod = server.getCryptMethod();
		server.addListener(ServerEvent.CLIENT_INFO_RECEIVED, (info) -> {
			if(getIdentificator().equals(info.getUUID())) {
				useCrypt = true;
			}
		});
	}
	
	@Override
	protected Data preProcessData(Data data) {
		if(data instanceof CryptedData) {
			CryptedData cdata = (CryptedData) data;
			Key encryptionKey = cdata.getKey();
			if(encryptionKey instanceof PublicKey) {
				SecureServer sserver = (SecureServer) server;
				String decrypted = sserver.getSession().decrypt(cdata.getData());
				Object object 	 = Serialization.deserializeFromString(decrypted);
				symmetricKey 	 = new SymmetricKey((String) object);
				// Add the symmetric key to the known server keys
				((SecureServer) server).addSymmetricKey(
					getIP(), symmetricKey);
				// Send all the data that could not have been encryted.
				for(DataHolder holder : cannotCrypt) {
					addDataToSend(holder.data,
								  holder.identificator,
								  holder.receiver);
				}
				cannotCrypt.clear();
				cannotCrypt = null;
			} else if(encryptionKey instanceof SymmetricKey) {
				return Serialization.<Data>deserializeFromString(
					cryptMethod.decrypt(cdata.getData(),
						symmetricKey.getKey()));
			}
		}
		return super.preProcessData(data);
	}
	
	@Override
	protected boolean onDataReceived(Data data) {
		if(data instanceof PublicKeyData) {
			SecureServer sserver = (SecureServer) server;
			PublicKeyData pkdata = (PublicKeyData) data;
			publicKey 			 = pkdata.getPublicKey();
			sserver.addPublicKey(getIP(), publicKey);
			new Thread(() -> {
				// Send the public key of the server to the client
				sendWait(new PublicKeyData(sserver.getPublicKey()));
				publicKeySent = true;
			}).start();
			return false;
		}
		return super.onDataReceived(data);
	}
	
	@Override
	protected void addDataToSend(Data data, Identificator identificator, String receiver) {
		if(useCrypt) {
			// We have to use the encryption but we do not have the key
			// that is used to encrypt the data, so we have to wait till
			// the key is delivered.
			if(symmetricKey == null && cannotCrypt != null) {
				cannotCrypt.add(new DataHolder(
					data, identificator, receiver));
				return;
			}
			// Encrypt the data using client's symmetric key
			data = new CryptedData(symmetricKey, data, cryptMethod);
		}
		super.addDataToSend(data, identificator, receiver);
	}
	
	public SymmetricKey getSymmetricKey() {
		return symmetricKey;
	}
	
	public PublicKey getPublicKey() {
		return publicKey;
	}
	
	public boolean isPublicKeySent() {
		return publicKeySent;
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}