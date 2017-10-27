package com.worldline.asp.tcp;

import org.apache.camel.spring.Main;

public class CamelReloadMain {

	    private CamelReloadMain() {
	    }

	    public static void main(String[] args) throws Exception {
	        // Main makes it easier to run a Spring application
	        Main main = new Main();

	        // configure the location of the Spring XML file
	        main.setApplicationContextUri("spring/camel-context.xml");	  
	        System.setProperty("ibm.Charset.Name", "cp037");
	
	        // turn on reload when the XML file is updated in the source code
	       main.setFileWatchDirectory("src/main/resources/spring");
	        // run and block until Camel is stopped (or JVM terminated)
	        main.run();
	    }

}
