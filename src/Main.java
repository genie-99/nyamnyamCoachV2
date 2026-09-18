import domain.BodyInfo;
import domain.Food;
import domain.MacroRatio;
import domain.RecommendResult;
import domain.User;

import repository.FoodRepository;

import service.DietRecommendService;
import service.DietService;
import service.UserService;

import utils.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;

public class Main {

    private final Scanner scanner = new Scanner(System.in);

    // 음식 관련
    private final FoodRepository foodRepository;
    private final DietService dietService = new DietService();
    private final DietRecommendService recommendService;

    // 회원 관련
    private final UserService userService = new UserService();
    private final Session session = new Session();

    // 사용자별 식단 저장
    private final Map<String, Map<Integer, Diet>> dietsByUser
            = new LinkedHashMap<>();

    private int nextDietId = 1;


    public Main(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;

        this.recommendService =
                new DietRecommendService(
                        foodRepository,
                        dietService
                );
    }


    public static void main(String[] args) {

        List<Food> foods;

        try {

            foods =
                    new FoodCsvReader()
                            .read("data/rawData.csv");

        } catch (IOException | IllegalArgumentException e) {

            System.out.println(
                    "음식 데이터를 불러오지 못했습니다: "
                            + e.getMessage()
            );

            return;
        }

        FoodRepository foodRepository =
                new FoodRepository(foods);

        Main main =
                new Main(foodRepository);

        main.run();
    }


    // =========================
    // 프로그램 실행
    // =========================

    private void run() {

        while (true) {

            System.out.println();
            System.out.println("==============================");
            System.out.println("       식단 관리 프로그램");
            System.out.println("==============================");


            // 로그인 안 한 상태
            if (!session.isLogin()) {

                printLogoutMenu();

                String menu =
                        readLine("메뉴 선택: ");

                if (menu == null) {
                    return;
                }

                switch (menu.trim()) {

                    case "1":
                        registerUser();
                        break;

                    case "2":
                        login();
                        break;

                    case "0":
                        System.out.println("프로그램을 종료합니다.");
                        return;

                    default:
                        System.out.println("잘못된 메뉴입니다.");
                }

            }

            // 로그인 상태
            else {

                printLoginMenu();

                String menu =
                        readLine("메뉴 선택: ");

                if (menu == null) {
                    return;
                }

                switch (menu.trim()) {

                    case "1":
                        showUserInfo();
                        break;

                    case "2":
                        updateUser();
                        break;

                    case "3":
                        registerDiet();
                        break;

                    case "4":
                        showDiets();
                        break;

                    case "5":
                        editDiet();
                        break;

                    case "6":
                        deleteDiet();
                        break;

                    case "7":
                        analyzeAndRecommend();
                        break;

                    case "8":
                        logout();
                        break;

                    case "9":
                        withdraw();
                        break;

                    case "0":
                        System.out.println("프로그램을 종료합니다.");
                        return;

                    default:
                        System.out.println("잘못된 메뉴입니다.");
                }
            }
        }
    }


    // =========================
    // Menu
    // =========================

    private void printLogoutMenu() {

        System.out.println("1. 회원가입");
        System.out.println("2. 로그인");
        System.out.println("0. 종료");
    }


    private void printLoginMenu() {

        User user =
                session.getLoginUser();

        System.out.println(
                "현재 로그인 사용자: "
                        + user.getName()
        );

        System.out.println();

        System.out.println("1. 회원정보 조회");
        System.out.println("2. 회원정보 수정");

        System.out.println("3. 식단 등록");
        System.out.println("4. 식단 조회");
        System.out.println("5. 식단 편집");
        System.out.println("6. 식단 삭제");

        System.out.println("7. 먹은 음식 분석 및 추천");

        System.out.println("8. 로그아웃");
        System.out.println("9. 회원 탈퇴");

        System.out.println("0. 종료");
    }


    // =========================
    // 회원가입
    // =========================

    private void registerUser() {

        System.out.println();
        System.out.println("===== 회원가입 =====");

        String loginId =
                readLine("아이디: ");

        String password =
                readLine("비밀번호: ");

        String name =
                readLine("이름: ");


        Double height =
                readPositiveDouble("키(cm): ");

        if (height == null) {
            return;
        }


        Double weight =
                readPositiveDouble("몸무게(kg): ");

        if (weight == null) {
            return;
        }


        User user = new User(
                null,
                loginId,
                password,
                name,
                height,
                weight
        );

        userService.register(user);
    }


    // =========================
    // 로그인
    // =========================

    private void login() {

        System.out.println();
        System.out.println("===== 로그인 =====");

        String loginId =
                readLine("아이디: ");

        String password =
                readLine("비밀번호: ");


        User loginUser =
                userService.login(
                        loginId,
                        password
                );


        if (loginUser != null) {

            session.login(loginUser);

            System.out.println(
                    loginUser.getName()
                            + "님 로그인되었습니다."
            );
        }
    }


    // =========================
    // 회원정보 조회
    // =========================

    private void showUserInfo() {

        User user =
                session.getLoginUser();


        System.out.println();
        System.out.println("===== 내 회원정보 =====");

        System.out.println(
                "ID: " + user.getId()
        );

        System.out.println(
                "Login ID: " + user.getLoginId()
        );

        System.out.println(
                "이름: " + user.getName()
        );

        System.out.println(
                "키: " + user.getHeight()
        );

        System.out.println(
                "몸무게: " + user.getWeight()
        );


        double bmi =
                new BodyInfo(
                        user.getHeight(),
                        user.getWeight()
                ).getBmi();

        System.out.printf(
                "BMI: %.1f%n",
                bmi
        );
    }


    // =========================
    // 회원정보 수정
    // =========================

    private void updateUser() {

        User currentUser =
                session.getLoginUser();


        String newPassword =
                readLine("새 비밀번호: ");

        String newName =
                readLine("새 이름: ");


        Double newHeight =
                readPositiveDouble(
                        "새 키(cm): "
                );

        if (newHeight == null) {
            return;
        }


        Double newWeight =
                readPositiveDouble(
                        "새 몸무게(kg): "
                );

        if (newWeight == null) {
            return;
        }


        userService.updateUser(
                currentUser,
                newPassword,
                newName,
                newHeight,
                newWeight
        );
    }


    // =========================
    // 로그아웃
    // =========================

    private void logout() {

        session.logout();

        System.out.println(
                "로그아웃되었습니다."
        );
    }


    // =========================
    // 회원 탈퇴
    // =========================

    private void withdraw() {

        User currentUser =
                session.getLoginUser();


        userService.withdraw(
                currentUser
        );


        // 해당 회원 식단도 제거
        dietsByUser.remove(
                currentUser.getLoginId()
        );


        session.logout();

        System.out.println(
                "회원 탈퇴되었습니다."
        );
    }


    // =========================
    // 사용자별 Diet Map
    // =========================

    private Map<Integer, Diet> getCurrentUserDiets() {

        String loginId =
                session
                        .getLoginUser()
                        .getLoginId();


        return dietsByUser.computeIfAbsent(
                loginId,
                key -> new LinkedHashMap<>()
        );
    }


    // =========================
    // 식단 등록
    // =========================

    private void registerDiet() {

        String name =
                readLine("식단 이름: ");

        if (name == null) {
            return;
        }

        name = name.trim();


        if (name.isEmpty()) {

            System.out.println(
                    "식단 이름을 입력해 주세요."
            );

            return;
        }


        List<Food> foods =
                readFoods(
                        "음식 이름(쉼표로 구분): "
                );


        if (foods == null) {
            return;
        }


        int id =
                nextDietId++;


        Diet diet =
                new Diet(
                        id,
                        name,
                        foods
                );


        getCurrentUserDiets()
                .put(
                        id,
                        diet
                );


        System.out.println(
                "식단을 등록했습니다. 번호: "
                        + id
        );
    }


    // =========================
    // 식단 조회
    // =========================

    private void showDiets() {

        Map<Integer, Diet> diets =
                getCurrentUserDiets();


        if (diets.isEmpty()) {

            System.out.println(
                    "등록된 식단이 없습니다."
            );

            return;
        }


        for (Diet diet : diets.values()) {

            System.out.printf(
                    "%d. %s%n",
                    diet.id,
                    diet.name
            );


            System.out.println(
                    "   음식: "
                            + foodNames(diet.foods)
            );


            MacroRatio ratio =
                    dietService
                            .calculateActualRatio(
                                    diet.foods
                            );


            System.out.println(
                    "   탄단지 비율: "
                            + ratio
            );
        }
    }


    // =========================
    // 식단 수정
    // =========================

    private void editDiet() {

        Diet diet =
                readDiet();


        if (diet == null) {
            return;
        }


        System.out.println(
                "현재 식단: "
                        + diet.name
                        + " / "
                        + foodNames(diet.foods)
        );


        String newName =
                readLine(
                        "새 식단 이름(Enter: 유지): "
                );


        if (newName == null) {
            return;
        }


        String foodInput =
                readLine(
                        "새 음식 이름(쉼표로 구분, Enter: 유지): "
                );


        if (foodInput == null) {
            return;
        }


        List<Food> newFoods =
                diet.foods;


        if (!foodInput.trim().isEmpty()) {

            newFoods =
                    parseFoods(foodInput);

            if (newFoods == null) {
                return;
            }
        }


        if (!newName.trim().isEmpty()) {

            diet.name =
                    newName.trim();
        }


        diet.foods =
                newFoods;


        System.out.println(
                "식단을 편집했습니다."
        );
    }


    // =========================
    // 식단 삭제
    // =========================

    private void deleteDiet() {

        Diet diet =
                readDiet();


        if (diet == null) {
            return;
        }


        getCurrentUserDiets()
                .remove(diet.id);


        System.out.println(
                "식단을 삭제했습니다: "
                        + diet.name
        );
    }


    // =========================
    // 음식 분석 + 알고리즘 추천
    // =========================

    private void analyzeAndRecommend() {

        // ★ 핵심
        // 키/몸무게를 다시 Console에서 받지 않는다.
        // 회원가입 때 저장한 User 정보를 사용한다.

        User user =
                session.getLoginUser();


        double height =
                user.getHeight();

        double weight =
                user.getWeight();


        List<Food> foods =
                readFoods(
                        "먹은 음식 이름(쉼표로 구분): "
                );


        if (foods == null) {
            return;
        }


        // BMI 계산
        double bmi =
                new BodyInfo(
                        height,
                        weight
                ).getBmi();


        // BMI 기준 목표 탄단지
        MacroRatio target =
                dietService
                        .getTargetRatio(bmi);


        // 실제 먹은 탄단지
        MacroRatio current =
                dietService
                        .calculateActualRatio(
                                foods
                        );


        // 실제 총 칼로리
        double currentCalories =
                dietService
                        .calculateTotalCalories(
                                foods
                        );


        System.out.println();
        System.out.println("===== 식단 분석 =====");


        System.out.printf(
                "BMI: %.1f%n",
                bmi
        );


        System.out.println(
                "먹은 음식: "
                        + foodNames(foods)
        );


        System.out.println(
                "목표 탄단지 비율: "
                        + target
        );


        System.out.println(
                "현재 탄단지 비율: "
                        + current
        );


        System.out.printf(
                "현재 총 칼로리: %.1f kcal%n",
                currentCalories
        );


        printMacroGap(
                "탄수화물",
                target.getCarbo(),
                current.getCarbo()
        );


        printMacroGap(
                "단백질",
                target.getProtein(),
                current.getProtein()
        );


        printMacroGap(
                "지방",
                target.getFat(),
                current.getFat()
        );


        Double maxCalories =
                readPositiveDouble(
                        "추천 후 최대 총 칼로리(kcal): "
                );


        if (maxCalories == null) {
            return;
        }


        if (maxCalories < currentCalories) {

            System.out.println(
                    "현재 식단이 이미 최대 칼로리를 넘었습니다."
            );

            return;
        }


        // DFS + Backtracking 기반 추천
        RecommendResult result =
                recommendService.recommend(
                        foods,
                        target,
                        maxCalories
                );


        if (result
                .getRecommendedFoods()
                .isEmpty()) {

            System.out.println(
                    "현재 식단보다 목표에 가까워지는 "
                            + "추천 조합이 없습니다."
            );

            return;
        }


        System.out.println();
        System.out.println("===== 추천 결과 =====");


        System.out.println(
                "추천 음식(최대 3개): "
                        + foodNames(
                        result.getRecommendedFoods()
                )
        );


        System.out.println(
                "추가 후 탄단지 비율: "
                        + result.getFinalRatio()
        );


        System.out.printf(
                "추가 후 총 칼로리: %.1f kcal%n",
                result.getTotalCalories()
        );


        System.out.printf(
                "목표와의 차이값: %.1f → %.1f%n",
                dietService.calculateDifference(
                        target,
                        current
                ),
                result.getDifference()
        );
    }


    // =========================
    // 탄단지 부족/과다
    // =========================

    private void printMacroGap(
            String name,
            double target,
            double current
    ) {

        double gap =
                target - current;


        if (gap > 0.05) {

            System.out.printf(
                    "%s 부족: %.1f%%p%n",
                    name,
                    gap
            );

        } else if (gap < -0.05) {

            System.out.printf(
                    "%s 과다: %.1f%%p%n",
                    name,
                    -gap
            );

        } else {

            System.out.println(
                    name + " 목표와 일치"
            );
        }
    }


    // =========================
    // 숫자 입력
    // =========================

    private Double readPositiveDouble(
            String prompt
    ) {

        String input =
                readLine(prompt);


        if (input == null) {
            return null;
        }


        try {

            double value =
                    Double.parseDouble(
                            input.trim()
                    );


            if (
                    Double.isFinite(value)
                            && value > 0
            ) {

                return value;
            }

        } catch (NumberFormatException ignored) {

        }


        System.out.println(
                "0보다 큰 숫자를 입력해 주세요."
        );

        return null;
    }


    // =========================
    // Diet 번호 검색
    // =========================

    private Diet readDiet() {

        Map<Integer, Diet> diets =
                getCurrentUserDiets();


        if (diets.isEmpty()) {

            System.out.println(
                    "등록된 식단이 없습니다."
            );

            return null;
        }


        showDiets();


        String input =
                readLine("식단 번호: ");


        if (input == null) {
            return null;
        }


        try {

            int id =
                    Integer.parseInt(
                            input.trim()
                    );


            Diet diet =
                    diets.get(id);


            if (diet != null) {
                return diet;
            }


        } catch (NumberFormatException ignored) {

        }


        System.out.println(
                "해당 번호의 식단이 없습니다."
        );

        return null;
    }


    // =========================
    // Food 입력
    // =========================

    private List<Food> readFoods(
            String prompt
    ) {

        String input =
                readLine(prompt);


        if (input == null) {
            return null;
        }


        return parseFoods(input);
    }


    // =========================
    // Food 이름 → Food 객체
    // =========================

    private List<Food> parseFoods(
            String input
    ) {

        List<String> missing =
                new ArrayList<>();


        for (
                String part :
                input.split(",", -1)
        ) {

            String name =
                    part.trim();


            Food food =
                    foodRepository
                            .findByName(name);


            if (food == null) {

                missing.add(
                        name.isEmpty()
                                ? "(빈 이름)"
                                : name
                );
            }
        }


        if (!missing.isEmpty()) {

            System.out.println(
                    "음식 데이터에서 찾을 수 없습니다: "
                            + String.join(
                            ", ",
                            missing
                    )
            );


            System.out.println(
                    "CSV의 식품명을 정확히 입력해 주세요."
            );


            return null;
        }


        return foodRepository
                .findByNames(input);
    }


    // =========================
    // 문자열 입력
    // =========================

    private String readLine(
            String prompt
    ) {

        System.out.print(prompt);


        return scanner.hasNextLine()
                ? scanner.nextLine()
                : null;
    }


    // =========================
    // Food 이름 출력
    // =========================

    private String foodNames(
            List<Food> foods
    ) {

        StringJoiner names =
                new StringJoiner(", ");


        for (Food food : foods) {

            names.add(
                    food.getFoodName()
            );
        }


        return names.toString();
    }


    // =========================
    // Diet
    // =========================

    private static class Diet {

        private final int id;

        private String name;

        private List<Food> foods;


        private Diet(
                int id,
                String name,
                List<Food> foods
        ) {

            this.id = id;
            this.name = name;
            this.foods = foods;
        }
    }
}