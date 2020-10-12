package sacip.rest;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interface")
public class RestInterface {
    
    private String test = "HELLO WORLD";

	@GetMapping("/contas")
	public String fazLogin()
	{
		return "";		
	}

    @PostMapping("/contas")
	public String criaNovaConta(String name) {
		return test;
	}

    @PutMapping("/contas")
	public String editarConta(String name) {
		return test;
	}
	
	@GetMapping("/pergunta")
	public String buscaPergunta()
	{
		return "";
	}

	@PostMapping("/pergunta")
	public String registraPergunta()
	{
		return "";
	}
}
	
