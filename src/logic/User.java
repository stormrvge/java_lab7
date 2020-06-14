package logic;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    private String password;
    private boolean login_successfully;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.login_successfully = false;
    }

    public User() {
        this.username = null;
        this.password = null;
        this.login_successfully = false;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setLoginState(boolean login_successfully) {
        this.login_successfully = login_successfully;
    }

    public boolean getLoginState() {
        return this.login_successfully;
    }
}
