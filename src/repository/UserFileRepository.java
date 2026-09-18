package repository;
import domain.User;

import java.util.ArrayList;
import java.util.List;

public class UserFileRepository implements UserRepository {

    private List<User> userList = new ArrayList<>();
    private long sequence = 1L;

    @Override
    public void save(User user) {

        user.setId(sequence++);

        userList.add(user);
    }

    @Override
    public User findById(Long id) {
        for (User user : userList) {
            if (user.getId().equals(id)) {
                return user;
            }
        }

        return null;
    }

    @Override
    public User findByLoginId(String loginId) {
        for (User user : userList) {
            if (user.getLoginId().equals(loginId)) {
                return user;
            }
        }

        return null;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(userList);
    }
}
