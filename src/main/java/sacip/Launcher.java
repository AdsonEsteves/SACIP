package sacip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.midas.as.AgentServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Launcher {

	public static int instancia = 1;

	public static void main(String[] args) {
		
		try {
			String localport = "7101";
			String serverport = "7100";
			String serverAddress = "127.0.0.1";
			//String serverAddress = "35.192.97.232";

			String structureXMLtemplate = readFile("structure.xml");			
			String servicesXMLtemplate = readFile("services.xml");
			String structureXML;
			String servicesXML;

			structureXML = structureXMLtemplate.replace("$localport", localport).replace("$serverport", serverport).replace("$serverAddress", serverAddress).replace("</name>", instancia+"</name>");
			servicesXML = servicesXMLtemplate.replace("</name>", instancia+"</name>").replace("</entity>", instancia+"</entity>").replace("</organization>", instancia+"</organization>");
			AgentServer.initialize(true, true, structureXML, servicesXML);

			// instancia++;

			// structureXML = structureXMLtemplate.replace("$localport", "7102").replace("$serverport", serverport).replace("$serverAddress", serverAddress).replace("</name>", instancia+"</name>");
			// servicesXML = servicesXMLtemplate.replace("</name>", instancia+"</name>").replace("</entity>", instancia+"</entity>").replace("</organization>", instancia+"</organization>");
			// AgentServer.initialize(true, true, structureXML, servicesXML);

			// SpringApplication.run(Launcher.class, args);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String readFile(String file) throws IOException
	{
		Path fileName = Path.of(file);         
        String actual = Files.readString(fileName);
		return actual;
	}
}