package tst.dm;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

	private Path dir;
	
	@Autowired
	public FileService(FileConfigProps conf) throws IOException {
		dir = Paths.get(conf.dir);
		if (!Files.exists(dir))
			Files.createDirectory(dir);
	}
	
	public void save(MultipartFile file, Long id) throws IOException {
		Files.copy(file.getInputStream(), dir.resolve(id.toString()), StandardCopyOption.REPLACE_EXISTING);
	}
	
	public Resource getResource(Long id) throws MalformedURLException {
		return new UrlResource(exists(id).toUri());
	}

	public Path exists(Long id) {
		Path path = dir.resolve(id.toString());
		if (!Files.exists(path))
			throw new FileNotFoundException("File '" + path + "' not found");
		return path;
	}
}
