package sune.ssp.etc;

import java.io.Serializable;

public enum ListType {
	
	CONNECTED_CLIENTS;
	
	@SuppressWarnings("unchecked")
	public <T extends Serializable> DataList<T> create(T... data) {
		return DataList.create(this, data);
	}
}