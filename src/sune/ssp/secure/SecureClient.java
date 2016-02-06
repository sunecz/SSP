package sune.ssp.secure;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;

import javax.net.ssl.SSLSocket;

import sune.ssp.Client;
import sune.ssp.crypt.Crypt;
import sune.ssp.crypt.CryptedData;
import sune.ssp.crypt.Session;
import sune.ssp.crypt.SimpleSession;
import sune.ssp.crypt.SymmetricKey;
import sune.ssp.data.Data;
import sune.ssp.data.PublicKeyData;
import sune.ssp.event.ClientEvent;
import sune.ssp.util.Serialization;

public class SecureClient extends Client {
	
	private final Session session;
	private final SymmetricKey symmetricKey;
	private PublicKey serverPublicKey;
	private volatile boolean symmetricKeySent;
	
	protected SecureClient(String serverIP, int serverPort) {
		super(serverIP, serverPort);
		session 	 = SimpleSession.createSession();
		symmetricKey = Crypt.weakSymmetricKey();
		addListener(ClientEvent.CONNECTED, (value) -> {
			send(new PublicKeyData(session.getPublicKey()));
		});
	}
	
	public static SecureClient create(String serverIP, int serverPort) {
		return new SecureClient(serverIP, serverPort);
	}
	
	@Override
	protected Socket createSocket(String serverIP, int serverPort)
			throws UnknownHostException, IOException {
		SSLSocket socket = SecurityHelper.createClient(serverIP, serverPort);
		socket.setSoTimeout(8000);
		socket.startHandshake();
		return socket;
	}
	
	@Override
	protected void addDataToSend(Data data) {
		if(data instanceof PublicKeyData) {
			super.addDataToSend(data);
		} else {
			super.addDataToSend(
				isConnected() &&
				symmetricKeySent ?
					new CryptedData(
						symmetricKey,
						data) :
					data);
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
					Crypt.decryptAES(cdata.getData(),
						symmetricKey.getKey()));
		}
		return data;
	}
	
	public void sendSymmetricKey() {
		sendWait(new CryptedData(serverPublicKey, symmetricKey));
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}
}