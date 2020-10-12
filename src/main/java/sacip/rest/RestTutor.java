package sacip.rest;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tutor")
public class RestTutor {
    
    private String test = "HELLO WORLD";

	@GetMapping("/requisitaConteudos")
	public String getConteudosRecomendados(){
		return "";		
	}

    @GetMapping("/trilha/{user}")
	public String getTrilhaUsuario(String name) {
		return test;
	}

    @GetMapping("/trilha")
	public String getTrilhas(String name) {
		return test;
	}

	@GetMapping("/dicas")
	public String getDicas(String name) {
		return test;
	}

}
	
