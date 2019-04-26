package carrot.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AccountController {
    @RequestMapping(value = "/hello")
    public String hello(HttpServletRequest request,@RequestParam String name){
        return "hello "+name;
    }
}
