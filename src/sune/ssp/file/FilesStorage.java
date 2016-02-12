package sune.ssp.file;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class FilesStorage {
	
	public static final class StorageFile {
		
		private final String hash;
		private final String name;
		private final File file;
		private final int bufferSize;
		private final long totalSize;
		
		private Map<String, FileReader> readers;
		private FileWriter writer;
		
		public StorageFile(String hash, String name, File file, int bufferSize, long totalSize) {
			if(hash == null || hash.isEmpty()) {
				throw new IllegalArgumentException(
					"File hash cannot be null or empty!");
			}
			if(name == null || name.isEmpty()) {
				throw new IllegalArgumentException(
					"File name cannot be null or empty!");
			}
			if(file == null) {
				throw new IllegalArgumentException(
					"File object cannot be null!");
			}
			this.hash		= hash;
			this.name 		= name;
			this.file 		= file;
			this.bufferSize = bufferSize;
			this.totalSize  = totalSize;
			this.file.deleteOnExit();
		}
		
		public void close() {
			if(writer  != null) writer.close();
			if(readers != null) {
				for(FileReader fr : readers.values()) {
					fr.close();
				}
			}
		}
		
		public boolean delete() {
			close();
			return file.delete();
		}
		
		public FileReader getReader(String name) {
			if(readers == null)
				readers = new LinkedHashMap<>();
			FileReader reader = readers.get(name);
			if(reader == null)
				readers.put(
					name,
					reader = new FileReader(
								file,
								bufferSize,
								totalSize));
			return reader;
		}
		
		public FileWriter getWriter() {
			return writer == null ?
				writer = new FileWriter(file, totalSize) :
				writer;
		}
		
		public long getTotalSize() {
			return totalSize;
		}
		
		public String getHash() {
			return hash;
		}
		
		public String getName() {
			return name;
		}
		
		public File getFile() {
			return file;
		}
	}
	
	private static final int BUFFER_SIZE = 8192;
	
	private final String path;
	private final int bufferSize;
	private final Map<String, StorageFile> files;
	
	protected FilesStorage(String path, int bufferSize) {
		this.path 		= fixAndEsurePath(path);
		this.bufferSize = bufferSize;
		this.files		= new LinkedHashMap<>();
	}
	
	private static final String fixAndEsurePath(String path) {
		path 	  = path.replace("\\", "/");
		path 	  = path + (!path.endsWith("/") ? "/" : "");
		File file = new File(path);
		if(!file.exists()) file.mkdirs();
		return path;
	}
	
	public static final FilesStorage create(String path) {
		return new FilesStorage(path, BUFFER_SIZE);
	}
	
	public StorageFile createFile(String hash, String name, long totalSize) {
		if(hash == null || hash.isEmpty()) {
			throw new IllegalArgumentException(
				"File hash cannot be null or empty!");
		}
		if(name == null || name.isEmpty()) {
			throw new IllegalArgumentException(
				"File name cannot be null or empty!");
		}
		File file = new File(path + hash + "_" + name);
		try {
			if(file.exists()) file.delete();
			if(file.createNewFile()) {
				StorageFile sfile = new StorageFile(
					hash, name, file, bufferSize, totalSize);
				file.deleteOnExit();
				synchronized(files) {
					files.put(hash, sfile);
				}
				return sfile;
			}
		} catch(Exception ex) {
		}
		return null;
	}
	
	public StorageFile getFile(String hash) {
		synchronized(files) {
			return files.get(hash);
		}
	}
	
	public boolean removeFile(String hash) {
		try {
			StorageFile file;
			synchronized(files) {
				file = files.remove(hash);
			}
			return file != null && file.delete();
		} catch(Exception ex) {
		}
		return false;
	}
	
	void removedir(File file) {
		if(!file.exists() || !file.isDirectory())
			return;
		try {
			for(File f : file.listFiles()) {
				try {
					if(f.isDirectory()) removedir(f);
					else 				f.delete();
				} catch(Exception ex) {
				}
			}
			file.delete();
		} catch(Exception ex) {
		}
	}
	
	public void remove() {
		removedir(new File(path));
	}
}