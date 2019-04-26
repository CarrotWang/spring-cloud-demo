package carrot.demo.controller;

import carrot.demo.remote.AccountService;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class DealController {

    @Autowired
    private AccountService accountService;

    @RequestMapping(name = "/hello")
    public String hello(HttpServletRequest request,@RequestParam String name){
        return accountService.hello(name);
    }
}
