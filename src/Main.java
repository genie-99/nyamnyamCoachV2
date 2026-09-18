import domain.BodyInfo;
import domain.Food;
import domain.MacroRatio;
import domain.RecommendResult;
import repository.FoodRepository;
import service.DietRecommendService;
import service.DietService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.StringJoiner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final FoodRepository foodRepository;
    private final DietService dietService = new DietService();
    private final DietRecommendService recommendService;
    private final Map<Integer, Diet> diets = new LinkedHashMap<>();
    private int nextDietId = 1;

    public Main(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
        this.recommendService = new DietRecommendService(foodRepository, dietService);
    }

    public static void main(String[] args) {
        List<Food> foods;
        try {
            foods = new FoodCsvReader().read("data/rawData.csv");
        } catch (IOException | IllegalArgumentException e) {
            System.out.println("음식 데이터를 불러오지 못했습니다: " + e.getMessage());
            return;
        }
        new Main(new FoodRepository(foods)).run();
    }

    private void run() {
        while (true) {
            System.out.println("==================================");
            System.out.println("식단 관리 프로그램");
            System.out.println("==================================");
            System.out.println("1. 식단 등록");
            System.out.println("2. 식단 조회");
            System.out.println("3. 식단 편집");
            System.out.println("4. 식단 삭제");
            System.out.println("5. 먹은 음식 분석 및 추천");
            System.out.println("6. 종료");
            System.out.print("메뉴 선택: ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    registerDiet();
                    break;
                case "2":
                    showDiets();
                    break;
                case "3":
                    editDiet();
                    break;
                case "4":
                    deleteDiet();
                    break;
                case "5":
                    analyzeAndRecommend();
                    break;
                case "6":
                    System.out.println("프로그램을 종료합니다.");
                    return;
                default:
                    System.out.println("1~6 중에서 선택해 주세요.");
            }
            System.out.println();
        }
    }

    private void registerDiet() {
        String name = readLine("식단 이름: ");
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) {
            System.out.println("식단 이름을 입력해 주세요.");
            return;
        }

        List<Food> foods = readFoods("음식 이름(쉼표로 구분): ");
        if (foods == null) return;

        int id = nextDietId++;
        diets.put(id, new Diet(id, name, foods));
        System.out.println("식단을 등록했습니다. 번호: " + id);
    }

    private void showDiets() {
        if (diets.isEmpty()) {
            System.out.println("등록된 식단이 없습니다.");
            return;
        }

        for (Diet diet : diets.values()) {
            System.out.printf("%d. %s%n", diet.id, diet.name);
            System.out.println("   음식: " + foodNames(diet.foods));
            System.out.println("   탄단지 비율: " + dietService.calculateActualRatio(diet.foods));
        }
    }

    private void editDiet() {
        Diet diet = readDiet();
        if (diet == null) return;

        System.out.println("현재 식단: " + diet.name + " / " + foodNames(diet.foods));
        String newName = readLine("새 식단 이름(Enter: 유지): ");
        if (newName == null) return;
        String foodInput = readLine("새 음식 이름(쉼표로 구분, Enter: 유지): ");
        if (foodInput == null) return;

        List<Food> newFoods = diet.foods;
        if (!foodInput.trim().isEmpty()) {
            newFoods = parseFoods(foodInput);
            if (newFoods == null) return;
        }

        if (!newName.trim().isEmpty()) diet.name = newName.trim();
        diet.foods = newFoods;
        System.out.println("식단을 편집했습니다.");
    }

    private void deleteDiet() {
        Diet diet = readDiet();
        if (diet == null) return;

        diets.remove(diet.id);
        System.out.println("식단을 삭제했습니다: " + diet.name);
    }

    private void analyzeAndRecommend() {
        Double height = readPositiveDouble("키(cm): ");
        if (height == null) return;
        Double weight = readPositiveDouble("몸무게(kg): ");
        if (weight == null) return;

        List<Food> foods = readFoods("먹은 음식 이름(쉼표로 구분): ");
        if (foods == null) return;

        double bmi = new BodyInfo(height, weight).getBmi();
        MacroRatio target = dietService.getTargetRatio(bmi);
        MacroRatio current = dietService.calculateActualRatio(foods);
        double currentCalories = dietService.calculateTotalCalories(foods);

        System.out.printf("BMI: %.1f%n", bmi);
        System.out.println("먹은 음식: " + foodNames(foods));
        System.out.println("목표 탄단지 비율: " + target);
        System.out.println("현재 탄단지 비율: " + current);
        System.out.printf("현재 총 칼로리: %.1f kcal%n", currentCalories);
        printMacroGap("탄수화물", target.getCarbo(), current.getCarbo());
        printMacroGap("단백질", target.getProtein(), current.getProtein());
        printMacroGap("지방", target.getFat(), current.getFat());

        Double maxCalories = readPositiveDouble("추천 후 최대 총 칼로리(kcal): ");
        if (maxCalories == null) return;
        if (maxCalories < currentCalories) {
            System.out.println("현재 식단이 이미 최대 칼로리를 넘었습니다.");
            return;
        }

        RecommendResult result = recommendService.recommend(foods, target, maxCalories);
        if (result.getRecommendedFoods().isEmpty()) {
            System.out.println("현재 식단보다 목표에 가까워지는 추천 조합이 없습니다.");
            return;
        }

        System.out.println("추천 음식(최대 3개): " + foodNames(result.getRecommendedFoods()));
        System.out.println("추가 후 탄단지 비율: " + result.getFinalRatio());
        System.out.printf("추가 후 총 칼로리: %.1f kcal%n", result.getTotalCalories());
        System.out.printf("목표와의 차이값: %.1f → %.1f%n",
                dietService.calculateDifference(target, current), result.getDifference());
    }

    private void printMacroGap(String name, double target, double current) {
        double gap = target - current;
        if (gap > 0.05) {
            System.out.printf("%s 부족: %.1f%%p%n", name, gap);
        } else if (gap < -0.05) {
            System.out.printf("%s 과다: %.1f%%p%n", name, -gap);
        } else {
            System.out.println(name + " 목표와 일치");
        }
    }

    private Double readPositiveDouble(String prompt) {
        String input = readLine(prompt);
        if (input == null) return null;
        try {
            double value = Double.parseDouble(input.trim());
            if (Double.isFinite(value) && value > 0) return value;
        } catch (NumberFormatException ignored) {
            // 아래에서 입력 오류를 안내한다.
        }
        System.out.println("0보다 큰 숫자를 입력해 주세요.");
        return null;
    }

    private Diet readDiet() {
        if (diets.isEmpty()) {
            System.out.println("등록된 식단이 없습니다.");
            return null;
        }

        showDiets();
        String input = readLine("식단 번호: ");
        if (input == null) return null;

        try {
            Diet diet = diets.get(Integer.parseInt(input.trim()));
            if (diet != null) return diet;
        } catch (NumberFormatException ignored) {
            // 아래에서 잘못된 번호를 안내한다.
        }
        System.out.println("해당 번호의 식단이 없습니다.");
        return null;
    }

    private List<Food> readFoods(String prompt) {
        String input = readLine(prompt);
        return input == null ? null : parseFoods(input);
    }

    private List<Food> parseFoods(String input) {
        List<String> missing = new ArrayList<>();
        for (String part : input.split(",", -1)) {
            String name = part.trim();
            Food food = foodRepository.findByName(name);
            if (food == null) {
                missing.add(name.isEmpty() ? "(빈 이름)" : name);
            }
        }

        if (!missing.isEmpty()) {
            System.out.println("음식 데이터에서 찾을 수 없습니다: " + String.join(", ", missing));
            System.out.println("CSV의 식품명을 정확히 입력해 주세요. 예: 김밥, 기장밥");
            return null;
        }
        return foodRepository.findByNames(input);
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }

    private String foodNames(List<Food> foods) {
        StringJoiner names = new StringJoiner(", ");
        for (Food food : foods) names.add(food.getFoodName());
        return names.toString();
    }

    private static class Diet {
        private final int id;
        private String name;
        private List<Food> foods;

        private Diet(int id, String name, List<Food> foods) {
            this.id = id;
            this.name = name;
            this.foods = foods;
        }
    }
}
