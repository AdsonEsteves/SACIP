package sacip.rest;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rastreador")
public class RestTracking {
    
    private String test = "HELLO WORLD";

	@PostMapping("/cliques")
	public String armazenaCliques(){
		return "";		
	}

    @PostMapping("/tempoLogin")
	public String armazenaTempoLogin(String name) {
		return test;
	}

    @PostMapping("/tempoConteudo")
	public String armazenaTempoConteudo(String name) {
		return test;
	}
	
	@PostMapping("/exercicioResolvido")
	public String armazenaExercicioResolvido()
	{
		return "";
	}

	@PostMapping("/novoConteudoTrilha")
	public String armazenaNovoConteudoTrilha()
	{
		return "";
	}
}
	
