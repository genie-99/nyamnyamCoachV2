import domain.Food;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodCsvReader {

    public List<Food> read(String filePath) throws IOException {

        List<Food> foods = new ArrayList<>();
        Path path = Paths.get(filePath);

        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = br.readLine();
            if (headerLine == null) return foods;

            // 헤더에서 필요한 영양 성분의 열 번호를 찾는다.
            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> headerMap = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                headerMap.put(headers.get(i).trim(), i);
            }

            int foodNameIdx = getIndex(headerMap, "식품명");
            int caloriesIdx = getIndex(headerMap, "에너지(kcal)");
            int proteinIdx = getIndex(headerMap, "단백질(g)");
            int fatIdx = getIndex(headerMap, "지방(g)");
            int carboIdx = getIndex(headerMap, "탄수화물(g)");
            int sugarIdx = getIndex(headerMap, "당류(g)");

            String line;
            long id = 1;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                List<String> values = parseCsvLine(line);
                String foodName = getValue(values, foodNameIdx);
                Double kcal = parseDouble(getValue(values, caloriesIdx));
                Double protein = parseDouble(getValue(values, proteinIdx));
                Double fat = parseDouble(getValue(values, fatIdx));
                Double carbo = parseDouble(getValue(values, carboIdx));
                Double sugar = parseDouble(getValue(values, sugarIdx));

                if (foodName.isEmpty() || kcal == null || protein == null
                        || fat == null || carbo == null || sugar == null) {
                    continue;
                }

                foods.add(new Food(id++, foodName, carbo, kcal, protein, fat, sugar));
            }

            return foods;
        }
    }

    private int getIndex(
            Map<String, Integer> headerMap,
            String headerName
    ) {

        Integer index = headerMap.get(headerName);

        if (index == null) {
            throw new IllegalArgumentException(
                    headerName + " 열을 찾을 수 없습니다."
            );
        }

        return index;
    }


    private String getValue(
            List<String> values,
            int index
    ) {

        if (index >= values.size()) {
            return "";
        }

        return values.get(index).trim();
    }


    private Double parseDouble(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Double.parseDouble(
                    value.replace(",", "")
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private List<String> parseCsvLine(String line) {

        List<String> result = new ArrayList<>();

        StringBuilder sb = new StringBuilder();

        boolean insideQuotes = false;

        for(int i =0;i< line.length();i++){
            char ch = line.charAt(i);

            if(ch == '"') {
                if(insideQuotes && i+1 < line.length() && line.charAt(i+1) == '"'){
                    sb.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if( ch == ',' && !insideQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(ch);
            }
        }
        result.add(sb.toString());

        return result;
    }


}
