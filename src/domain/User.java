package domain;

public class User {

    private Long id;
    private String loginId;
    private String password;
    private String name;
    private double height;
    private double weight;
    private UserStatus status;

    public User(Long id, String loginId, String password, String name,
                double height, double weight) {

        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.height = height;
        this.weight = weight;
        this.status = UserStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public double getHeight() {
        return height;
    }

    public double getWeight() {
        return weight;
    }

    public UserStatus getStatus() {
        return status;
    }
    
    public void setId(Long id) {
        this.id = id;
    }


    // 회원정보 수정
    public void updateInfo(String password, String name,
                           double height, double weight) {

        this.password = password;
        this.name = name;
        this.height = height;
        this.weight = weight;
    }

    // 회원 탈퇴
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
}