package service;

import dao.UsersDao;
import entity.Users;
import java.util.logging.Logger;

public class LoginService {

    private final UsersDao usersDao;
    private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName());

    public LoginService(UsersDao usersDao) {
        this.usersDao = usersDao;
    }

    public LoginResult login(String username, String password) {

        // 1. Validate input
        if (isNullOrEmpty(username) || isNullOrEmpty(password)) {
            return new LoginResult(LoginResult.Status.FAILED_CREDENTIALS, null);
        }

        // 2. DAO call
        Users user;
        try {
            user = usersDao.login(username, password);
        } catch (Exception e) {
            LOGGER.severe("LoginService: DAO error -> " + e.getMessage());
            return new LoginResult(LoginResult.Status.FAILED_SYSTEM_ERROR, null);
        }

        // 3. Invalid credentials
        if (user == null) {
            return new LoginResult(LoginResult.Status.FAILED_CREDENTIALS, null);
        }

        // 4. Role check
        String role = (user.getRole() == null) ? "" : user.getRole().trim().toLowerCase();

        switch (role) {
            case "admin":
                return new LoginResult(LoginResult.Status.SUCCESS_ADMIN, user);

            case "user":
                return new LoginResult(LoginResult.Status.SUCCESS_USER, user);

            default:
                return new LoginResult(LoginResult.Status.FAILED_INVALID_ROLE, null);
        }
    }

    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
