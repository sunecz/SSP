package sune.ssp.secure;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;

import javax.net.ssl.SSLSocket;

import sune.ssp.Client;
import sune.ssp.data.Data;
import sune.ssp.data.PublicKeyData;
import sune.ssp.event.ClientEvent;
import sune.ssp.util.Serialization;
import sune.util.crypt.Crypt;
import sune.util.crypt.CryptMethod;

public class SecureClient extends Client {
	
	private final Session session;
	private final SymmetricKey symmetricKey;
	private PublicKey serverPublicKey;
	private volatile boolean symmetricKeySent;
	
	private CryptMethod cryptMethod = Crypt.getAES();
	
	protected SecureClient(String serverIP, int serverPort, Session session) {
		super(serverIP, serverPort);
		this.session 	  = session;
		this.symmetricKey = generateStrongKey();
		addListener(ClientEvent.IDENTIFICATOR_RECEIVED, (value) -> {
			send(new PublicKeyData(this.session.getPublicKey()));
		});
	}
	
	public static SecureClient create(String serverIP, int serverPort) {
		return create(serverIP, serverPort, SimpleSession.createSession());
	}
	
	public static SecureClient create(String serverIP, int serverPort,
			Session session) {
		return new SecureClient(serverIP, serverPort, session);
	}
	
	protected SymmetricKey generateStrongKey() {
		if(!Crypt.unlimitedKeySize()) {
			Crypt.unlimitedKeySize(true);
		}
		return new SymmetricKey(cryptMethod.strongKey());
	}
	
	public void setCryptMethod(CryptMethod method) {
		cryptMethod = method;
		generateStrongKey();
	}
	
	@Override
	protected Socket createSocket(String serverIP, int serverPort)
			throws UnknownHostException, IOException {
		SSLSocket socket = SecurityHelper.createClient(serverIP, serverPort);
		socket.setSoTimeout(TIMEOUT);
		socket.startHandshake();
		return socket;
	}
	
	@Override
	protected void addDataToSend(Data data, String receiver) {
		if(data instanceof PublicKeyData) {
			super.addDataToSend(data, receiver);
		} else {
			super.addDataToSend(
				isConnected() &&
				symmetricKeySent ?
					new CryptedData(
						symmetricKey,
						data,
						cryptMethod) :
					data,
				receiver);
		}
	}
	
	@Override
	protected Data onDataReceived(Data data) {
		if(data instanceof PublicKeyData) {
			PublicKeyData pkdata = (PublicKeyData) data;
			serverPublicKey 	 = pkdata.getPublicKey();
			new Thread(() -> {
				// Send the symmetric key to the server
				sendSymmetricKey();
				symmetricKeySent = true;
			}).start();
		} else if(data instanceof CryptedData) {
			CryptedData cdata = (CryptedData) data;
			return Serialization.<Data>
				deserializeFromString(
					cryptMethod.decrypt(
						cdata.getData(),
						symmetricKey.getKey()))
				.cast();
		}
		return data;
	}
	
	public void sendSymmetricKey() {
		sendWait(new CryptedData(serverPublicKey, symmetricKey.getKey()));
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}