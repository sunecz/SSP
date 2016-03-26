package sune.ssp.data;

import sune.ssp.etc.Identificator;

public class ClientData extends Data {
	
	private static final long serialVersionUID = -8803944432429004713L;
	
	public ClientData(Identificator identificator) {
		super("identificator", identificator);
	}
	
	public Identificator getIdentificator() {
		return (Identificator) getData("identificator");
	}
}