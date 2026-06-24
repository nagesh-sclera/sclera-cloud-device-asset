package io.sclera.service;

import io.sclera.exception.ClientException;
import io.sclera.repository.BillingAdminEmailRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingAdminEmailService {

    private final BillingAdminEmailRepository billingAdminEmailRepository;

    public ResponseEntity<String> addEmails(String email, String loggedInUser, List<String> billingAdminEmails, HttpServletRequest httpServletRequest)
    {
        if (email == null || loggedInUser == null)
        {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

        // Fetch the current billing admin emails from the database
        List<String> currentBillingAdminEmails = billingAdminEmailRepository.findAllBillingEmails();

        // Convert both current and new emails into sets for easy comparison
        Set<String> currentEmailsSet = new HashSet<>(currentBillingAdminEmails);
        Set<String> newEmailsSet = new HashSet<>(billingAdminEmails);

        // Identify which emails need to be removed (those in current but not in new list)
        Set<String> emailsToRemove = new HashSet<>(currentEmailsSet);
        emailsToRemove.removeAll(newEmailsSet);

        // Identify which emails need to be added (those in new list but not in current)
        Set<String> emailsToAdd = new HashSet<>(newEmailsSet);
        emailsToAdd.removeAll(currentEmailsSet);

        // Remove the unselected emails from the database
        if (!emailsToRemove.isEmpty())
        {
            billingAdminEmailRepository.deleteBillingAdminEmail(new ArrayList<>(emailsToRemove));
        }

        // Add the newly selected emails to the database
        if (!emailsToAdd.isEmpty())
        {
            for (String newEmail : emailsToAdd)
            {
                billingAdminEmailRepository.addEmail(newEmail);
            }
        }
        return ResponseEntity.ok("Emails added/removed successfully");

    }


    public ResponseEntity<List<String>> getBillingAdminEmails(String email, String loggedInUser, HttpServletRequest httpServletRequest)
    {
        if(email == null || loggedInUser == null)
        {
            log.error("Invalid Client Parameters.EndPoint:{}", httpServletRequest.getRequestURI());
            throw new ClientException("Invalid client params", 700, httpServletRequest.getRequestURI());
        }

        List<String> billingAdminEmails=billingAdminEmailRepository.findAllBillingEmails();
        return ResponseEntity.ok(billingAdminEmails);
    }


    public List<String> getBillingAdminEmails(HttpServletRequest httpServletRequest) {
        return billingAdminEmailRepository.findAllBillingEmails();

    }

}
