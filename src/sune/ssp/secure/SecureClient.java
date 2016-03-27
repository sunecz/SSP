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
	
	private CryptMethod cryptMethod;
	private boolean useStrongKey;
	
	protected SecureClient(String serverIP, int serverPort, Session session,
			CryptMethod cryptMethod, boolean strongKey) {
		super(serverIP, serverPort);
		this.cryptMethod  = cryptMethod;
		this.useStrongKey = strongKey;
		this.session 	  = session;
		this.symmetricKey = generateKey();
		addListener(ClientEvent.IDENTIFICATOR_RECEIVED, (value) -> {
			send(new PublicKeyData(this.session.getPublicKey()));
		});
	}
	
	public static SecureClient create(String serverIP, int serverPort) {
		return create(serverIP, serverPort, SimpleSession.createSession());
	}
	
	public static SecureClient create(String serverIP, int serverPort,
			Session session) {
		return create(serverIP, serverPort, session, Crypt.getAES(), true);
	}
	
	public static SecureClient create(String serverIP, int serverPort,
			Session session, CryptMethod cryptMethod, boolean strongKey) {
		return new SecureClient(serverIP, serverPort, session,
				cryptMethod, strongKey);
	}
	
	protected SymmetricKey generateKey() {
		if(!Crypt.unlimitedKeySize()) {
			Crypt.unlimitedKeySize(true);
		}
		return new SymmetricKey(
			useStrongKey ?
				cryptMethod.strongKey() :
				cryptMethod.weakKey());
	}
	
	public void setCryptMethod(CryptMethod method) {
		cryptMethod = method;
		generateKey();
	}
	
	public void setStrongKey(boolean strongKey) {
		useStrongKey = strongKey;
		generateKey();
	}
	
	@Override
	protected Socket createSocket(String serverIP, int serverPort)
			throws UnknownHostException, IOException {
		SSLSocket socket = SecurityManager.createClient(serverIP, serverPort);
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
				// Send the symmetric key to the server and
				// wait till the key is sent.
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
	
	void sendSymmetricKey() {
		sendWait(new CryptedData(serverPublicKey, symmetricKey.getKey()));
	}
	
	public Session getSession() {
		return session;
	}
	
	public String getKeyAlgorithm() {
		return String.format(
			"%s %d-bit",
			cryptMethod.getName(),
			useStrongKey ? 
				cryptMethod.strongKeyBits() :
				cryptMethod.weakKeyBits());
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}