package tst.dm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MetaService {

	@Autowired
	private FileRepo repo;

	public FileEntity exists(Long id) {
		FileEntity ent = repo.findOne(id);
		if (ent == null)
			throw new FileNotFoundException("Meta info for id='" + id + "' not found");
		return ent;
	}
	
	public FileEntity save(FileEntity ent) {
		return repo.save(ent);
	}
}
