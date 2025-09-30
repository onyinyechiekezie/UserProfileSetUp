package services;

import com.userprofilesetup.data.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class UserServiceTest {

    private UserService userService;

    @Autowired
    private UserRepository userRepository;


}
