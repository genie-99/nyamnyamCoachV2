package service;

import domain.User;
import domain.UserStatus;
import repository.UserFileRepository;
import repository.UserRepository;

import java.util.List;

public class UserService {

    private final UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserFileRepository();
    }

    // 회원가입
    public void register(User user) {

        User existingUser =
                userRepository.findByLoginId(user.getLoginId());

        if (existingUser != null) {
            System.out.println("이미 존재하는 아이디입니다.");
            return;
        }

        userRepository.save(user);

        System.out.println("회원가입이 완료되었습니다.");
    }

    // ID로 회원 조회
    public User findById(Long id) {
        return userRepository.findById(id);
    }

    // Login ID로 회원 조회
    public User findByLoginId(String loginId) {
        return userRepository.findByLoginId(loginId);
    }

    // 전체 회원 조회
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // 로그인
    public User login(String loginId, String password) {

        User user = userRepository.findByLoginId(loginId);

        if (user == null) {
            System.out.println("존재하지 않는 아이디입니다.");
            return null;
        }

        if (user.getStatus() == UserStatus.INACTIVE) {
            System.out.println("탈퇴한 회원입니다.");
            return null;
        }

        if (!user.getPassword().equals(password)) {
            System.out.println("비밀번호가 일치하지 않습니다.");
            return null;
        }

        System.out.println("로그인 성공!");
        return user;
    }

    // 회원정보 수정
    public void updateUser(User user,
                           String password,
                           String name,
                           double height,
                           double weight) {

        user.updateInfo(
                password,
                name,
                height,
                weight
        );

        System.out.println("회원정보가 수정되었습니다.");
    }

    // 회원 탈퇴
    public void withdraw(User user) {

        user.deactivate();

        System.out.println("회원 탈퇴가 완료되었습니다.");
    }
}