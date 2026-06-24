package io.sclera.controller.frontend;

import io.sclera.service.BillingAdminEmailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/billingAdminEmail")
public class BillingAdminEmailController {

    private final BillingAdminEmailService billingAdminEmailService;

    @PostMapping("/user/{email}/addBillingAdminEmails")
    public ResponseEntity<String> addEmails(@PathVariable String email,
                                            @RequestParam String loggedInUser,
                                            @RequestBody List<String> billingAdminEmails,
                                            HttpServletRequest httpServletRequest)
    {
        return billingAdminEmailService.addEmails(email,loggedInUser,billingAdminEmails,httpServletRequest);
    }

    @GetMapping("/user/{email}/getBillingAdminEmails")
    public ResponseEntity<List<String>> getBillingAdminEmails(@PathVariable String email,
                                                              @RequestParam String loggedInUser,
                                                              HttpServletRequest httpServletRequest)
    {
        return billingAdminEmailService.getBillingAdminEmails(email,loggedInUser,httpServletRequest);
    }

}
