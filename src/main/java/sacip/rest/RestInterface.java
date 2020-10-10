package sacip.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestInterface {
    
    private String test = "HELLO WORLD";

    @GetMapping("/test")
	public String test(String name) {
		return test;
	}
}
