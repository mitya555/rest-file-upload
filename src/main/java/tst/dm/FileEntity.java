package tst.dm;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hamcrest.core.IsInstanceOf;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class FileEntity {
	
	@Id
	@GeneratedValue
	public Long id;
	
	public String filename;
	public String contentType;
	
	public String author;
	@JsonIgnore
	public Timestamp created;
	@JsonIgnore
	public Timestamp uploaded;
	@JsonProperty
	public String getCreated() { return created == null ? null : created.toLocalDateTime().toString(); }
	public void setCreated(String iso) { created = Timestamp.valueOf(LocalDateTime.parse(iso)); }
	@JsonProperty
	public String getUploaded() { return uploaded == null ? null : uploaded.toLocalDateTime().toString(); }
	public void setUploaded(String iso) { uploaded = Timestamp.valueOf(LocalDateTime.parse(iso)); }

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof FileEntity) {
			FileEntity val = (FileEntity)obj;
			return objectsEqual(author, val.author) &&
					objectsEqual(created, val.created) &&
					objectsEqual(uploaded, val.uploaded) &&
					objectsEqual(filename, val.filename) &&
					objectsEqual(contentType, val.contentType) &&
					objectsEqual(id, val.id);
		}
		return false;
	}
	private boolean objectsEqual(Object o1, Object o2) { return (o1 == null && o2 == null) || (o1 != null && o1.equals(o2)); }
}
