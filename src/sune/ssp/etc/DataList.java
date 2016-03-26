package sune.ssp.etc;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

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
	private DataList(DataListType type, ArrayList<Serializable> list, Class<T> clazz) {
		this(type, (T[]) retype(list.toArray()));
		setData("itemClass", clazz);
	}
	
	@SafeVarargs
	protected DataList(DataListType type, T... data) {
		super("type", 	   type,
			  "array", 	   data,
			  "itemClass", Serializable.class);
	}
	
	@SafeVarargs
	public static <T extends Serializable> DataList<T> create(DataListType type, T... data) {
		if(type == null || data == null) {
			throw new IllegalArgumentException(
				"List type and/or data cannot be null!");
		}
		return new DataList<>(type, data);
	}
	
	public void setItemClass(Class<T> clazz) {
		setData("itemClass", clazz);
	}
	
	public DataListType getType() {
		return (DataListType) getData("type");
	}
	
	@SuppressWarnings("unchecked")
	public T[] getData() {
		return Utils.copy((T[]) getData("array"));
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getItemClass() {
		return (Class<T>) getData("itemClass");
	}
	
	@SuppressWarnings("unchecked")
	public final T[] toTypeArray(Class<?> clazz) {
		if(isTypeOf(clazz)) return null;
		Serializable[] array = getData();
		int length = array.length;
		T[] narray = (T[]) Array.newInstance(clazz, length);
		for(int i = 0; i < length; ++i)
			narray[i] = (T) array[i];
		return narray;
	}
	
	public boolean isTypeOf(Class<?> clazz) {
		return getItemClass().isAssignableFrom(clazz);
	}
}