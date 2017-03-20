package tst.dm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("files")
@Component
public class FileConfig {

	public String dir = "files";
}
