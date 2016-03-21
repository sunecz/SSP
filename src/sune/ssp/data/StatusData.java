package sune.ssp.data;

public class StatusData extends Data {
	
	private static final long serialVersionUID = -4962929510828676083L;

	public StatusData(Status status) {
		super("status", status);
	}
	
	public Status getStatus() {
		return (Status) getData("status");
	}
}