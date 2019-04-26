package carrot.demo.remote;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name= "account-service")
public interface AccountService {
    @RequestMapping(value = "/hello")
    public String hello(@RequestParam(value = "name") String name);
}
