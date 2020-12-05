package sacip;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.midas.as.AgentServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Launcher {

	public static String instancia = "";

	public static void main(String[] args) {
		
		try {
			int localport = 7101;
			String serverport = "7100";
			String serverAddress = "127.0.0.1";
			//String serverAddress = "35.192.97.232";

			iniciandoAgentesServidor(localport+"", serverport, serverAddress);
			localport++;
			iniciandoAgentesUsuario(localport+"", serverport, serverAddress);

			// iniciandoAgentesContainerUnico(localport+"", serverport, serverAddress);

			ApplicationContext ctx = SpringApplication.run(Launcher.class, args);

			// System.out.println("Let's inspect the beans provided by Spring Boot:");

			// String[] beanNames = ctx.getBeanDefinitionNames();
			// //Arrays.sort(beanNames);
			// for (String beanName : beanNames) {
			// 	System.out.println(beanName);
			// }
			
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

	public static void iniciandoAgentesUsuario(String localport, String serverport, String serverAddress) throws IOException
	{
		//Inicializando Agentes do Usuário
		String UserAgentsStructureXML = readFile("UserAgentsStructure.xml");
		String UserAgentsServicesXML = readFile("UserAgentsServices.xml");			
		UserAgentsStructureXML = UserAgentsStructureXML.replace("$localport", localport).replace("$serverport", serverport).replace("$serverAddress", serverAddress).replace("</name>", instancia+"</name>");
		UserAgentsServicesXML = UserAgentsServicesXML.replace("</name>", instancia+"</name>").replace("</entity>", instancia+"</entity>").replace("</organization>", instancia+"</organization>");
		AgentServer.initialize(true, true, UserAgentsStructureXML, UserAgentsServicesXML);
	}

	public static void iniciandoAgentesServidor(String localport, String serverport, String serverAddress) throws IOException
	{
		//inicializando Agentes do Servidor.
		String ServerAgentsStructureXML = readFile("ServerAgentsStructure.xml");
		String ServerAgentsServicesXML = readFile("ServerAgentsServices.xml");			
		ServerAgentsStructureXML = ServerAgentsStructureXML.replace("$localport", localport).replace("$serverport", serverport).replace("$serverAddress", serverAddress).replace("</name>", instancia+"</name>");
		AgentServer.initialize(true, true, ServerAgentsStructureXML, ServerAgentsServicesXML);
	}

	public static void iniciandoAgentesContainerUnico(String localport, String serverport, String serverAddress) throws IOException
	{
		//inicializando Agentes do Servidor.
		String ServerAgentsStructureXML = readFile("structure.xml");
		String ServerAgentsServicesXML = readFile("services.xml");			
		ServerAgentsStructureXML = ServerAgentsStructureXML.replace("$localport", localport).replace("$serverport", serverport).replace("$serverAddress", serverAddress).replace("</name>", instancia+"</name>");
		AgentServer.initialize(true, true, ServerAgentsStructureXML, ServerAgentsServicesXML);
	}
}