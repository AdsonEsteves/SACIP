package sacip.launch;

import org.midas.as.AgentServer;

public class Launcher {
	public static void main(String[] args)
	{		
		// Inicializando Arquitetura via C�digo
		// (não mostra a tela de gerenciamento)
		System.out.println("teste");
		AgentServer.initialize(true, true);
	}
}