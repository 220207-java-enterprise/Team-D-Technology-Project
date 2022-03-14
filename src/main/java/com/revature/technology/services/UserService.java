package com.revature.technology.services;

import com.revature.technology.dtos.requests.LoginRequest;
import com.revature.technology.dtos.requests.NewUserRequest;
import com.revature.technology.dtos.responses.Principal;
import com.revature.technology.dtos.responses.ResourceCreationResponse;
import com.revature.technology.models.User;
import com.revature.technology.models.UserRole;
import com.revature.technology.repositories.UserRepository;
import com.revature.technology.util.exceptions.AuthenticationException;
import com.revature.technology.repositories.UserRoleRepository;
import com.revature.technology.util.exceptions.InvalidRequestException;
import com.revature.technology.util.exceptions.ResourceConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;
    private UserRoleRepository userRoleRepository;

    @Autowired
    public UserService(UserRepository UserRepo, UserRoleRepository userRoleRepository) {
        this.userRepository = UserRepo;
        this.userRoleRepository = userRoleRepository;
    }

    public ResourceCreationResponse register(NewUserRequest newUserRequest) throws IOException {
        System.out.println();
        User newUser = newUserRequest.extractUser();

        if (!isUserValid(newUser)) {
            throw new InvalidRequestException("Bad registration details given.");
        }

        boolean usernameAvailable = isUsernameAvailable(newUser.getUsername());
        boolean emailAvailable = isEmailAvailable(newUser.getEmail());

        if (!usernameAvailable || !emailAvailable) {
            String msg = "The values provided for the following fields are already taken by other users: ";
            if (!usernameAvailable) msg += "username ";
            if (!emailAvailable) msg += "email";
            throw new ResourceConflictException(msg);
        }

        // TODO encrypt provided password before storing in the database



        //Update UserRole
        UserRole userRole = userRoleRepository.getUserRoleByRole(newUser.getRole().getRole());
        newUser.setRole(userRole);
        userRepository.save(newUser);


        return new ResourceCreationResponse(newUser.getUserId());
    }

    // ***********************************
    //      LOGIN USER
    // ***********************************

    public User login(LoginRequest loginRequest){

        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (!isUsernameValid(username) || !isPasswordValid(password)){
            throw new InvalidRequestException("Invalid credentials provided");
        }


        User potentialUser = userRepository.getUserByUsername(username);

        if (potentialUser == null){
            throw new AuthenticationException();
        }

        if(!loginRequest.getPassword().equals(potentialUser.getPassword())) {
            throw new AuthenticationException();
        }

        User authUser = userRepository.getUserByUsername(username);

        if (authUser == null){
            throw new AuthenticationException();
        }

        return authUser;
    }

    public boolean isUserActive(String id){

        User user = userRepository.getUserById(id);
        return user.getIsActive();
    }

    protected boolean isUserValid(User appUser) {

        // First name and last name are not just empty strings or filled with whitespace
        if (appUser.getGivenName().trim().equals("") || appUser.getSurname().trim().equals("")) {
            return false;
        }

        // Usernames must be a minimum of 8 and a max of 25 characters in length, and only contain alphanumeric characters.
        if (!isUsernameValid(appUser.getUsername())) {
            return false;
        }

        // Passwords require a minimum eight characters, at least one uppercase letter, one lowercase
        // letter, one number and one special character
        if (!isPasswordValid(appUser.getPassword())) {
            return false;
        }

        if(!isRoleValid(appUser.getRole())) {
            return false;
        }

        if(!isEmailValid(appUser.getEmail())){
            return false;
        }

        return true;

    }

    public boolean isRoleValid(UserRole role){
        ArrayList<String> validRoles = new ArrayList<String>();
        validRoles.add("FINANCE MANAGER");
        validRoles.add("ADMIN");
        validRoles.add("EMPLOYEE");

        if (!validRoles.contains(role.getRole())){
            return false;
        }
        return true;
    }

    public boolean isEmailValid(String email) {
        return email.matches("^[^@\\s]+@[^@\\s.]+\\.[^@.\\s]+$");
    }

    public boolean isUsernameValid(String username) {
        if (username == null) return false;
        return username.matches("^[a-zA-Z0-9]{8,25}");
    }

    public boolean isPasswordValid(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    public boolean isUsernameAvailable(String username) {
        return userRepository.getUserByUsername(username) == null;
    }

    public boolean isEmailAvailable(String email) {
        return userRepository.getUserByEmail(email) == null;
    }

}
