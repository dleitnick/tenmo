package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.RegisterUserDto;
import com.techelevator.tenmo.model.User;

import java.util.List;

public interface UserDao {

    /**
     * Get all users except for the specified user
     * @param username User to exclude
     * @return List of Users
     */
    List<User> getUsers(String username);

    /**
     * Get User by ID
     * @param id User ID
     * @return The User
     */
    User getUserById(int id);

    /**
     * Get User by username
     * @param username Username of User
     * @return The User
     */
    User getUserByUsername(String username);

    /**
     * Create a new User
     * @param user A RegisterUserDto that includes the username and password of a User
     * @return The new User
     */
    User createUser(RegisterUserDto user);
}
