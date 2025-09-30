package com.userprofilesetup.services;

import com.userprofilesetup.data.models.User;

public interface UserService {

//    User createUser(User user);
    String signup(String email, String password);

}
