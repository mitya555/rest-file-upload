package tst.dm;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hamcrest.core.IsInstanceOf;

@Entity
public class FileEntity {
	
	@Id
	@GeneratedValue
	public Long id;
	
	public String filename;
	public String contentType;
	
	public String author;
	public Timestamp created;
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof FileEntity) {
			FileEntity val = (FileEntity)obj;
			return objectsEqual(author, val.author) &&
					objectsEqual(created, val.created) &&
					objectsEqual(filename, val.filename) &&
					objectsEqual(contentType, val.contentType) &&
					objectsEqual(id, val.id);
		}
		return false;
	}
	private boolean objectsEqual(Object o1, Object o2) { return (o1 == null && o2 == null) || (o1 != null && o1.equals(o2)); }
}
