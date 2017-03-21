package tst.dm;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class RestFileUploadApplicationTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FileService files;

    @MockBean
    private MetaService meta;

    private static class TestFileEntity extends FileEntity {
    	public String testCreated;
    	public String testContent;
    	public static TestFileEntity get(Long _id) {
    		String _created = "2016-12-31T23:59:59";
    		return new TestFileEntity() {{
        		author = "John Doe";
        		contentType = "text/plain";
        		created = Timestamp.valueOf(LocalDateTime.parse(_created));
        		filename = "test.txt";
        		id = _id;
        		testCreated = _created;
        		testContent = "Test Upload File Service";
        	}};
    	}
    }
    
    @Test
    public void saveUploadedFile() throws Exception {
        TestFileEntity ent = TestFileEntity.get(null);

        given(meta.save(ent))
        		.willReturn(TestFileEntity.get(1L));
    	
        MockMultipartFile multipartFile = new MockMultipartFile("file", ent.filename, ent.contentType, ent.testContent.getBytes()),
        		mfAuthor = new MockMultipartFile("author", "", "", ent.author.getBytes()),
        		mfCreated = new MockMultipartFile("created", "", "", ent.testCreated.getBytes());
        
        mvc.perform(fileUpload("/api/").file(multipartFile).file(mfAuthor).file(mfCreated))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/1"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.uri", is("/api/1")));
        
        then(meta).should().save(ent);
        
        then(files).should().save(multipartFile, 1L);
    }
    
    @Test
    public void getStuff() throws Exception {
        TestFileEntity ent = TestFileEntity.get(1L);

    	given(meta.exists(1L)).willReturn(ent);
        
    	given(files.getResource(1L)).willReturn(new ByteArrayResource(ent.testContent.getBytes()));
    	
        mvc.perform(get("/api/1/stream"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.TEXT_PLAIN))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + ent.filename + "\""))
                .andExpect(content().string(ent.testContent));
    	
        mvc.perform(get("/api/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.author", is(ent.author)))
                .andExpect(jsonPath("$.contentType", is(ent.contentType)))
                .andExpect(jsonPath("$.created", is(ent.created.getTime())))
                .andExpect(jsonPath("$.filename", is(ent.filename)))
                .andExpect(jsonPath("$.id", is(ent.id.intValue())));
    }

    @Test
    public void notFound() throws Exception {
        given(files.getResource(1L))
                .willThrow(FileNotFoundException.class);

        given(meta.exists(1L))
        		.willThrow(FileNotFoundException.class);

        mvc.perform(get("/api/1/stream"))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/1"))
                .andExpect(status().isNotFound());
    }
}
