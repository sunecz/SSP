package sune.ssp.etc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sune.ssp.data.Data;
import sune.ssp.util.Utils;

public final class DataList<T extends Serializable> extends Data {
	
	private static final long serialVersionUID = -3879756773662309537L;
	
	static Serializable[] retype(Object[] objects) {
		int length 			 = objects.length;
		Serializable[] array = new Serializable[length];
		for(int i = 0; i < length; ++i)
			array[i] = (Serializable) objects[i];
		return array;
	}
	
	// Used for casting from Data object and should never be used!
	@SuppressWarnings({ "unchecked", "unused" })
	private DataList(ListType type, ArrayList<Serializable> list, Class<T> clazz) {
		this(type, (T[]) retype(list.toArray()));
		setData("itemClass", clazz);
	}
	
	@SafeVarargs
	protected DataList(ListType type, T... data) {
		super("type", 	   type,
			  "array", 	   Utils.toList(data),
			  "itemClass", Serializable.class);
	}
	
	@SafeVarargs
	public static <T extends Serializable> DataList<T> create(ListType type, T... data) {
		if(type == null || data == null) {
			throw new IllegalArgumentException(
				"List type and/or data cannot be null!");
		}
		return new DataList<>(type, data);
	}
	
	public void setItemClass(Class<T> clazz) {
		setData("itemClass", clazz);
	}
	
	public ListType getType() {
		return (ListType) getData("type");
	}
	
	@SuppressWarnings("unchecked")
	private final T[] getDataArray() {
		return Utils.copy((T[]) retype(((List<T>) getData("array")).toArray()));
	}
	
	public T[] getData() {
		return getDataArray();
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getItemClass() {
		return (Class<T>) getData("itemClass");
	}
}