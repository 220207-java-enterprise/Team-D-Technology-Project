package com.revature.technology.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.revature.technology.dtos.prism.requests.AddEmployeeToOrgRequest;
import com.revature.technology.dtos.prism.requests.AuthOrganizationRequest;
import com.revature.technology.dtos.prism.requests.NewOrgRequest;
import com.revature.technology.dtos.prism.responses.AuthOrganizationPrincipal;
import com.revature.technology.dtos.prism.responses.OrgRegistrationResponse;
import com.revature.technology.dtos.prism.responses.PrismResourceCreationResponse;
import com.revature.technology.models.User;
import com.revature.technology.util.exceptions.ForbiddenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

// TODO make this a bean and use IoC container to auto-wire dependencies
@Component
public class PrismClient {

    public static RestTemplate prismClient = new RestTemplate();

    public PrismClient() {
    }

    public static OrgRegistrationResponse registerNewOrganizationUsingPrism() throws JsonProcessingException {
        // Set content type for the request to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // prepare the request payload using NewOrgRequest
        NewOrgRequest newOrgRequest = new NewOrgRequest("Team-D-Organization", "some-secret-key");
        HttpEntity<NewOrgRequest> request = new HttpEntity<>(newOrgRequest, headers);

        // make the request by attaching a payload and parsing a response
        OrgRegistrationResponse response = prismClient.postForObject("http://localhost:5000/prism/organizations",
                request, OrgRegistrationResponse.class);
        System.out.println(response);

        return response;
    }

    public static ResponseEntity<AuthOrganizationPrincipal> authenticateOrganizationUsingPrism(OrgRegistrationResponse orgRegistrationResponse){

        // Set content type for the request to application/json
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        AuthOrganizationRequest authRequest = new AuthOrganizationRequest(orgRegistrationResponse);

        HttpEntity<AuthOrganizationRequest> authOrgRequest = new HttpEntity<>(authRequest, headers);
        System.out.println("AuthRequest --> "+authOrgRequest);

        // make the request by attaching a payload and parsing a response
        ResponseEntity<AuthOrganizationPrincipal> authOrgResponse = prismClient.postForEntity(
                "http://localhost:5000/prism/auth", authOrgRequest, AuthOrganizationPrincipal.class);

        System.out.println("AuthResponse --> "+authOrgResponse);

        return authOrgResponse;
    }

    public void registerNewEmployeeUsingPrism(User newUser) throws JsonProcessingException {

        OrgRegistrationResponse orgRegistrationResponse = registerNewOrganizationUsingPrism();
        ResponseEntity<AuthOrganizationPrincipal> authOrganizationPrincipal =
                authenticateOrganizationUsingPrism(orgRegistrationResponse);

        HttpHeaders headers = authOrganizationPrincipal.getHeaders();
        System.out.println(headers);
        System.out.println(authOrganizationPrincipal);

        AddEmployeeToOrgRequest addEmployeeRequestObject = new AddEmployeeToOrgRequest(newUser.getGivenName(), newUser.getSurname(),
                newUser.getEmail(), new AddEmployeeToOrgRequest.AccountInfo(
                "Test Bank", "111222333", "123456789"));

        // create a request to prism, inject newUser into the request (with Dummy AccountInfo data
        HttpEntity<AddEmployeeToOrgRequest> addEmployeeToOrgRequestHttpEntity = new HttpEntity<>(addEmployeeRequestObject,
                headers);

        ResponseEntity<PrismResourceCreationResponse> addEmployeeResponse = prismClient.postForEntity(
                "http://localhost:5000/prism/employees", addEmployeeToOrgRequestHttpEntity,
                PrismResourceCreationResponse.class);
    }

    public void postPaymentUsingPrism(){
        // the payeeId and paymentAmount will be posted to Payment (prism)
        // TODO call this method in resolveReimbursement() method

    }
}
