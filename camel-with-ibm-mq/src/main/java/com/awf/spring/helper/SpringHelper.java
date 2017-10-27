package com.awf.spring.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class SpringHelper extends AbstractHelper {

	public Resource getResource(String filePath) {
		Resource resource = new ClassPathResource(filePath);
		return resource;
	}

	public InputStream getResourceAsInputStream(String filePath) throws IOException {
		Resource resource = this.getResource(filePath);
		return resource.getInputStream();
	}

	public String getResourceAsString(String filePath) throws IOException {
		InputStream inputStream = this.getResourceAsInputStream(filePath);
		return IOUtils.toString(inputStream);
	}

	public File getResourceAsFile(String filePath) throws IOException {
		Resource resource = this.getResource(filePath);
		return resource.getFile();
	}

}
