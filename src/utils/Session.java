package utils;

import domain.User;

public class Session {

    private User loginUser;

    // 로그인
    public void login(User user) {
        this.loginUser = user;
    }

    // 로그아웃
    public void logout() {
        this.loginUser = null;
    }

    // 로그인 여부 확인
    public boolean isLogin() {
        return loginUser != null;
    }

    // 현재 로그인한 회원 조회
    public User getLoginUser() {
        return loginUser;
    }
}