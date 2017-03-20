package tst.dm;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class FileEntity {
	
	@Id
	@GeneratedValue
	public Long id;
	
	public String filename;
	public String contentType;
	
	public String author;
	public Timestamp created;
}
