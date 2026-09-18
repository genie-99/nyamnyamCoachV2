import domain.User;
import service.UserService;
import utils.Session;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        UserService userService = new UserService();
        Session session = new Session();


        while (true) {

            System.out.println();
            System.out.println("===== 회원 관리 시스템 =====");

            if (session.isLogin()) {
                System.out.println("현재 로그인 사용자: "
                        + session.getLoginUser().getName());

                System.out.println("1. 회원정보 조회");
                System.out.println("2. 회원정보 수정");
                System.out.println("3. 로그아웃");
                System.out.println("4. 회원 탈퇴");
                System.out.println("0. 종료");

            } else {
                System.out.println("1. 회원가입");
                System.out.println("2. 로그인");
                System.out.println("0. 종료");
            }

            System.out.print("메뉴를 선택하세요: ");
            int menu = sc.nextInt();

            // 로그인 상태가 아닌 경우
            if (!session.isLogin()) {

                switch (menu) {

                    case 1:
                        // 회원가입
                        System.out.print("아이디: ");
                        String loginId = sc.next();

                        System.out.print("비밀번호: ");
                        String password = sc.next();

                        System.out.print("이름: ");
                        String name = sc.next();

                        System.out.print("키: ");
                        double height = sc.nextDouble();

                        System.out.print("몸무게: ");
                        double weight = sc.nextDouble();

                        User user = new User(
                                null,
                                loginId,
                                password,
                                name,
                                height,
                                weight
                        );

                        userService.register(user);
                        break;

                    case 2:
                        // 로그인
                        System.out.print("아이디: ");
                        String loginIdForLogin = sc.next();

                        System.out.print("비밀번호: ");
                        String passwordForLogin = sc.next();

                        User loginUser = userService.login(
                                loginIdForLogin,
                                passwordForLogin
                        );

                        if (loginUser != null) {
                            session.login(loginUser);
                        }

                        break;

                    case 0:
                        System.out.println("프로그램을 종료합니다.");
                        return;

                    default:
                        System.out.println("잘못된 메뉴입니다.");
                }


            } else {

                // 로그인 상태인 경우
                switch (menu) {

                    case 1:
                        // 회원정보 조회
                        User loginUser = session.getLoginUser();

                        System.out.println();
                        System.out.println("===== 내 회원정보 =====");
                        System.out.println("ID: " + loginUser.getId());
                        System.out.println("Login ID: " + loginUser.getLoginId());
                        System.out.println("이름: " + loginUser.getName());
                        System.out.println("키: " + loginUser.getHeight());
                        System.out.println("몸무게: " + loginUser.getWeight());
                        break;

                    case 2: {
                        // 회원정보 수정
                        User currentUser = session.getLoginUser();

                        System.out.print("새 비밀번호: ");
                        String newPassword = sc.next();

                        System.out.print("새 이름: ");
                        String newName = sc.next();

                        System.out.print("새 키: ");
                        double newHeight = sc.nextDouble();

                        System.out.print("새 몸무게: ");
                        double newWeight = sc.nextDouble();

                        userService.updateUser(
                                currentUser,
                                newPassword,
                                newName,
                                newHeight,
                                newWeight
                        );

                        break;
                    }

                    case 3: {
                        // 로그아웃
                        session.logout();

                        System.out.println("로그아웃되었습니다.");

                        break;
                    }

                    case 4: {
                        // 회원 탈퇴
                        User currentUser = session.getLoginUser();

                        userService.withdraw(currentUser);

                        // 탈퇴 후 로그아웃
                        session.logout();

                        break;
                    }

                    case 0:
                        System.out.println("프로그램을 종료합니다.");
                        return;

                    default:
                        System.out.println("잘못된 메뉴입니다.");
                }
            }
        }
    }
}