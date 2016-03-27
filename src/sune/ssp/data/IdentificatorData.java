package sune.ssp.data;

import sune.ssp.etc.Identificator;

public class IdentificatorData extends Data {
	
	private static final long serialVersionUID = -4152690400578289108L;
	
	public IdentificatorData(Identificator identificator) {
		super("identificator", identificator);
	}
	
	public Identificator getIdentificator() {
		return (Identificator) getData("identificator");
	}
}