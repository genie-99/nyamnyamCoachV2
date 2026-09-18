package service;

import domain.Food;
import domain.MacroRatio;
import domain.RecommendResult;
import repository.FoodRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

public class DietRecommendService {
    private static final double EPSILON = 1e-9;
    private static final Comparator<RankedFood> BEST_CANDIDATE_FIRST =
            Comparator.comparingDouble((RankedFood ranked) -> ranked.difference)
                    .thenComparingDouble(ranked -> ranked.food.getKcal())
                    .thenComparing(ranked -> ranked.food.getFoodName());

    private final FoodRepository foodRepository;
    private final DietService dietService;
    private final int candidateLimit;
    private final int maxRecommendedFoods;

    public DietRecommendService(FoodRepository foodRepository, DietService dietService) {
        this(foodRepository, dietService, 15, 3);
    }

    public DietRecommendService(FoodRepository foodRepository, DietService dietService,
                                int candidateLimit, int maxRecommendedFoods) {
        if (candidateLimit <= 0 || maxRecommendedFoods <= 0) {
            throw new IllegalArgumentException("후보 수와 추천 음식 수는 1 이상이어야 합니다.");
        }
        this.foodRepository = foodRepository;
        this.dietService = dietService;
        this.candidateLimit = candidateLimit;
        this.maxRecommendedFoods = maxRecommendedFoods;
    }

    public RecommendResult recommend(List<Food> currentFoods, MacroRatio target, double maxCalories) {
        if (!Double.isFinite(maxCalories) || maxCalories <= 0) {
            throw new IllegalArgumentException("최대 칼로리는 0보다 큰 숫자여야 합니다.");
        }

        double currentCalories = dietService.calculateTotalCalories(currentFoods);
        if (currentCalories > maxCalories) {
            throw new IllegalArgumentException("현재 식단이 이미 최대 칼로리를 넘었습니다.");
        }

        double carbo = 0;
        double protein = 0;
        double fat = 0;
        for (Food food : currentFoods) {
            carbo += food.getCarbo();
            protein += food.getProtein();
            fat += food.getFat();
        }

        MacroRatio currentRatio = dietService.calculateRatioFromGrams(carbo, protein, fat);
        List<Food> candidates = selectCandidates(target, currentRatio, carbo, protein, fat,
                currentCalories, maxCalories);
        SearchState best = new SearchState(currentRatio, currentCalories,
                dietService.calculateDifference(target, currentRatio));

        dfs(0, candidates, new ArrayList<Food>(), carbo, protein, fat,
                currentCalories, target, maxCalories, best);

        return new RecommendResult(best.foods, best.ratio, best.calories, best.difference);
    }

    private List<Food> selectCandidates(MacroRatio target, MacroRatio currentRatio,
                                        double carbo, double protein, double fat,
                                        double currentCalories, double maxCalories) {
        // 가장 나쁜 후보가 맨 위에 있어, 더 나은 후보가 오면 즉시 교체한다.
        PriorityQueue<RankedFood> worstFirst =
                new PriorityQueue<>(candidateLimit, BEST_CANDIDATE_FIRST.reversed());
        Set<String> seenNames = new HashSet<>();

        for (Food food : foodRepository.findAll()) {
            if (!isUsable(food) || currentCalories + food.getKcal() > maxCalories) {
                continue;
            }

            MacroRatio foodRatio = dietService.calculateRatioFromGrams(
                    food.getCarbo(), food.getProtein(), food.getFat());
            if (!helpsDeficit(target, currentRatio, foodRatio)) {
                continue;
            }
            if (!seenNames.add(food.getFoodName())) {
                continue;
            }

            MacroRatio afterAdding = dietService.calculateRatioFromGrams(
                    carbo + food.getCarbo(), protein + food.getProtein(), fat + food.getFat());
            RankedFood ranked = new RankedFood(food,
                    dietService.calculateDifference(target, afterAdding));
            if (worstFirst.size() < candidateLimit) {
                worstFirst.add(ranked);
            } else if (BEST_CANDIDATE_FIRST.compare(ranked, worstFirst.peek()) < 0) {
                worstFirst.poll();
                worstFirst.add(ranked);
            }
        }

        List<RankedFood> rankedFoods = new ArrayList<>(worstFirst);
        rankedFoods.sort(BEST_CANDIDATE_FIRST);
        List<Food> candidates = new ArrayList<>();
        for (RankedFood ranked : rankedFoods) {
            candidates.add(ranked.food);
        }
        return candidates;
    }

    private boolean isUsable(Food food) {
        return food.getFoodName() != null && !food.getFoodName().isEmpty()
                && Double.isFinite(food.getKcal()) && food.getKcal() > 0
                && Double.isFinite(food.getCarbo()) && food.getCarbo() >= 0
                && Double.isFinite(food.getProtein()) && food.getProtein() >= 0
                && Double.isFinite(food.getFat()) && food.getFat() >= 0
                && food.getCarbo() + food.getProtein() + food.getFat() > 0;
    }

    private boolean helpsDeficit(MacroRatio target, MacroRatio current, MacroRatio food) {
        return (target.getCarbo() > current.getCarbo() + EPSILON
                    && food.getCarbo() > current.getCarbo() + EPSILON)
                || (target.getProtein() > current.getProtein() + EPSILON
                    && food.getProtein() > current.getProtein() + EPSILON)
                || (target.getFat() > current.getFat() + EPSILON
                    && food.getFat() > current.getFat() + EPSILON);
    }

    private void dfs(int depth, List<Food> candidates, List<Food> selected,
                     double carbo, double protein, double fat, double totalCalories,
                     MacroRatio target, double maxCalories, SearchState best) {
        if (depth == candidates.size() || selected.size() == maxRecommendedFoods) {
            MacroRatio actual = dietService.calculateRatioFromGrams(carbo, protein, fat);
            double difference = dietService.calculateDifference(target, actual);
            if (isBetter(difference, totalCalories, selected.size(), best)) {
                best.foods = new ArrayList<>(selected);
                best.ratio = actual;
                best.calories = totalCalories;
                best.difference = difference;
            }
            return;
        }

        Food food = candidates.get(depth);

        // 1. 현재 음식을 선택한다. 칼로리를 넘는 가지는 더 내려가지 않는다.
        if (totalCalories + food.getKcal() <= maxCalories) {
            selected.add(food);
            dfs(depth + 1, candidates, selected,
                    carbo + food.getCarbo(), protein + food.getProtein(), fat + food.getFat(),
                    totalCalories + food.getKcal(), target, maxCalories, best);
            selected.remove(selected.size() - 1); // 원상복구: 다음 가지에는 이 음식이 없다.
        }

        // 2. 현재 음식을 선택하지 않는다.
        dfs(depth + 1, candidates, selected, carbo, protein, fat,
                totalCalories, target, maxCalories, best);
    }

    private boolean isBetter(double difference, double calories, int foodCount, SearchState best) {
        if (difference < best.difference - EPSILON) return true;
        if (Math.abs(difference - best.difference) > EPSILON) return false;
        if (calories < best.calories - EPSILON) return true;
        return Math.abs(calories - best.calories) <= EPSILON && foodCount < best.foods.size();
    }

    private static class RankedFood {
        private final Food food;
        private final double difference;

        private RankedFood(Food food, double difference) {
            this.food = food;
            this.difference = difference;
        }
    }

    private static class SearchState {
        private List<Food> foods = new ArrayList<>();
        private MacroRatio ratio;
        private double calories;
        private double difference;

        private SearchState(MacroRatio ratio, double calories, double difference) {
            this.ratio = ratio;
            this.calories = calories;
            this.difference = difference;
        }
    }
}
