package repository;

import domain.User;

import java.util.List;

public interface UserRepository {

    void save(User user); //회원 저장

    User findById(Long id); // 회원 한 명 조회

    User findByLoginId(String loginId); //로그인할 때 loginId로 회원 조회

    List<User> findAll(); //전체 회원 조회
}