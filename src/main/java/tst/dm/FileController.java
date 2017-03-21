package tst.dm;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api")
public class FileController {
	
	@Autowired
	FileService files;

	@Autowired
	MetaService meta;
	
	@PostMapping(value={ "/", "/{id}" }, consumes="multipart/form-data")
	ResponseEntity<?> upload(@PathVariable Optional<Long> id,
			@RequestPart(name="file") MultipartFile file,
			@RequestPart(name="author") Optional<String> author,
			@RequestPart(name="created") Optional<String> created) throws URISyntaxException, IOException {
		if (file == null || file.isEmpty())
			throw new FileIsEmptyException("File not uploaded");
		FileEntity ent = id.map(val -> {
			files.exists(val);
			return meta.exists(val);
		}).orElse(new FileEntity());
		ent.author = author.orElse(null);
		ent.created = created.isPresent() ? Timestamp.valueOf(LocalDateTime.parse(created.get())) : null;
		ent.contentType = file.getContentType();
		ent.filename = file.getOriginalFilename();
		FileEntity _ent = meta.save(ent);
		Long _id = _ent.id;
		files.save(file, _id);
		return ResponseEntity.status(id.isPresent() ? /*Found*/302 : /*Created*/201)
				.header(HttpHeaders.LOCATION, "/api/" + _id)
				.body(new Object() {
					public URI uri = new URI("/api/" + _id); // ServletUriComponentsBuilder.fromCurrentRequest().path("/api/{id}").buildAndExpand(_id).toUri();
				});
	}
	
	@GetMapping("/{id}")
	FileEntity metaData(@PathVariable Long id) {
		return meta.exists(id);
	}
	
	@GetMapping("/{id}/stream")
	public ResponseEntity<Resource> stream(@PathVariable Long id) throws MalformedURLException {
		FileEntity metaData = meta.exists(id);
        return ResponseEntity.ok()
        		.header(HttpHeaders.CONTENT_TYPE, metaData.contentType)
        		.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metaData.filename + "\"")
        		.body(files.getResource(id));
    }
	
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> notFound(FileNotFoundException ex) {
        return ResponseEntity.status(/*Not Found*/404).body(new Object() { public String cause = ex.getMessage(); });
    }
	
    @ExceptionHandler(FileIsEmptyException.class)
    public ResponseEntity<?> noContent(FileIsEmptyException ex) {
        return ResponseEntity.status(/*No Content*/204).body(new Object() { public String cause = ex.getMessage(); });
    }
}
