package tst.dm;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.util.TimeZone;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    
    @TestConfiguration
    static class ClockConfig {

    	@Bean
    	@Primary
    	public Clock clock() {
    	    return Clock.fixed(TestFileEntity.get(null).uploaded.toInstant(), TimeZone.getDefault().toZoneId());
    	}
    }

    @Autowired
    private MockMvc mvc;

    @MockBean
    private FileService files;

    @MockBean
    private MetaService meta;

    private static class TestFileEntity extends FileEntity {
    	public String testCreated;
    	public String testUploaded;
    	public String testContent;
    	public static TestFileEntity get(Long _id) {
    		String _created = "2016-12-31T23:59:59", _uploaded = "1970-01-01T00:00";
    		return new TestFileEntity() {{
        		author = "John Doe";
        		contentType = "text/plain";
        		setCreated(_created);
        		setUploaded(_uploaded);
        		filename = "test.txt";
        		id = _id;
        		testCreated = _created;
        		testUploaded = _uploaded;
        		testContent = "Test Upload File Service";
        	}};
    	}
    }
    
    @Captor
    ArgumentCaptor<FileEntity> entCaptor;
    
    @Test
    public void saveUploadedFile() throws Exception {
        TestFileEntity ent = TestFileEntity.get(null);

        given(meta.save(any(FileEntity.class)))
        		.willReturn(TestFileEntity.get(1L));
    	
        MockMultipartFile multipartFile = new MockMultipartFile("file", ent.filename, ent.contentType, ent.testContent.getBytes()),
        		mfAuthor = new MockMultipartFile("author", "", "", ent.author.getBytes()),
        		mfCreated = new MockMultipartFile("created", "", "", ent.testCreated.getBytes());
        
        mvc.perform(fileUpload("/api/").file(multipartFile).file(mfAuthor).file(mfCreated))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/1"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.uri", is("/api/1")));
        
        InOrder inOrder = inOrder(meta, files);
        
        then(meta).should(inOrder).save(entCaptor.capture());
        assertEquals(ent, entCaptor.getValue());

        then(files).should(inOrder).save(multipartFile, 1L);
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
                .andExpect(jsonPath("$.created", is(ent.testCreated)))
                .andExpect(jsonPath("$.uploaded", is(ent.testUploaded)))
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
