package sune.ssp.secure;

import java.security.Key;
import java.security.PublicKey;

import javax.net.ssl.SSLSocket;

import sune.ssp.ServerClient;
import sune.ssp.crypt.Crypt;
import sune.ssp.crypt.CryptedData;
import sune.ssp.crypt.SymmetricKey;
import sune.ssp.data.Data;
import sune.ssp.data.PublicKeyData;
import sune.ssp.util.Serialization;

public class SecureServerClient extends ServerClient {
	
	private PublicKey publicKey;
	private SymmetricKey symmetricKey;
	private volatile boolean publicKeySent;
	
	protected SecureServerClient(SecureServer server, SSLSocket socket) {
		super(server, socket);
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
				if(object instanceof SymmetricKey) {
					symmetricKey = (SymmetricKey) object;
				} else return (Data) object;
			} else if(encryptionKey instanceof SymmetricKey) {
				return Serialization.<Data>deserializeFromString(
					Crypt.decryptAES(cdata.getData(),
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