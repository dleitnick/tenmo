package com.techelevator.dao;

import com.techelevator.tenmo.dao.JdbcUserDao;
import com.techelevator.tenmo.exception.DaoException;
import com.techelevator.tenmo.model.RegisterUserDto;
import com.techelevator.tenmo.model.User;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@DisplayName("JdbcUserDao Tests")
class JdbcUserDaoTests extends BaseDaoTests {
    protected static final User USER_1 = new User(1001, "user1", "user1", "USER");
    protected static final User USER_2 = new User(1002, "user2", "user2", "USER");
    protected static final User USER_3 = new User(1003, "user3", "user3", "USER");

    private JdbcUserDao sut;

    @BeforeEach
    void setup() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        sut = new JdbcUserDao(jdbcTemplate);
    }

    @Test
    @DisplayName("01. Get user by null username throws exception")
    void getUserByUsername_given_null_throws_exception() {
        assertThrows(IllegalArgumentException.class, () -> {
            sut.getUserByUsername(null);
        });
    }

    @Test
    @DisplayName("02. Getting user by invalid username returns null")
    void getUserByUsername_given_invalid_username_returns_null() {
        assertNull(sut.getUserByUsername("invalid"));
    }

    @Test
    @DisplayName("03. Get user by valid username returns user")
    void getUserByUsername_given_valid_user_returns_user() {
        User actualUser = sut.getUserByUsername(USER_1.getUsername());

        assertEquals(USER_1, actualUser);
    }

    @Test
    @DisplayName("04. Get user by invalid id returns null")
    void getUserById_given_invalid_user_id_returns_null() {
        User actualUser = sut.getUserById(-1);

        assertNull(actualUser);
    }

    @Test
    @DisplayName("05. Get user by valid id returns user")
    void getUserById_given_valid_user_id_returns_user() {
        User actualUser = sut.getUserById(USER_1.getId());

        assertEquals(USER_1, actualUser);
    }

    @Test
    @DisplayName("06. Get users returns all users")
    void getUsers_returns_all_users() {
        List<User> users = sut.getUsers("None");

        assertNotNull(users);
        assertEquals(3, users.size());
        assertEquals(USER_1, users.get(0));
        assertEquals(USER_2, users.get(1));
        assertEquals(USER_3, users.get(2));
    }

    @Test
    @DisplayName("07. Create user with null username throws DaoException")
    void createUser_with_null_username() {
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setUsername(null);
        registerUserDto.setPassword(USER_1.getPassword());
        assertThrows(DaoException.class, () -> {
            sut.createUser(registerUserDto);
        });
    }

    @Test
    @DisplayName("08. Create user with existing username throws DaoException")
    void createUser_with_existing_username() {
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setUsername(USER_1.getUsername());
        registerUserDto.setPassword(USER_3.getPassword());
        assertThrows(DaoException.class, () -> {
            sut.createUser(registerUserDto);
        });
    }

    @Test
    @DisplayName("09. Create user with null password throws IllegalArgumentException")
    void createUser_with_null_password() {
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setUsername(USER_3.getUsername());
        registerUserDto.setPassword(null);
        assertThrows(IllegalArgumentException.class, () -> {
            sut.createUser(registerUserDto);
        });
    }

    @Test
    @DisplayName("10. Creating a new user returns a new user")
    void createUser_creates_a_user() {
        RegisterUserDto user = new RegisterUserDto();
        user.setUsername("new");
        user.setPassword("USER");

        User createdUser = sut.createUser(user);

        assertNotNull(createdUser);

        User retrievedUser = sut.getUserByUsername(createdUser.getUsername());
        assertEquals(retrievedUser, createdUser);
    }
}
