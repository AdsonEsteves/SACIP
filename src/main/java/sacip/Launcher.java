package sacip;

import org.midas.as.AgentServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Launcher {
	public static void main(String[] args)
	{		
		// Inicializando Arquitetura via C�digo
		// (não mostra a tela de gerenciamento)
		System.out.println("testes");
		AgentServer.initialize(true, true);
		SpringApplication.run(Launcher.class, args);
	}
}