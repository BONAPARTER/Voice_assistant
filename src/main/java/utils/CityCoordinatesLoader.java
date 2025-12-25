package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

public class CityCoordinatesLoader {

    private static final Map<String, CityCoordinates> CITY_COORDINATES = new HashMap<>();
    private static boolean isLoaded = false;

    public static void loadCities() {
        if (isLoaded) return;

        try (InputStream is = CityCoordinatesLoader.class.getResourceAsStream("/cities.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String country = parts[0].trim();
                    String city = parts[1].trim();
                    double latitude = Double.parseDouble(parts[2].trim());
                    double longitude = Double.parseDouble(parts[3].trim());

                    addCity(city.toLowerCase(), latitude, longitude);

                    if (parts.length >= 5 && !parts[4].trim().isEmpty()) {
                        for (String alias : parts[4].split("\\|")) {
                            addCity(alias.trim().toLowerCase(), latitude, longitude);
                        }
                    }
                }
            }

            isLoaded = true;
            out.println("Загружено " + CITY_COORDINATES.size() + " городов");

        } catch (Exception e) {
            err.println("Ошибка при загрузке координат городов: " + e.getMessage());

            addCity("brest", 52.0976, 23.7341);
            addCity("брест", 52.0976, 23.7341);
            addCity("minsk", 53.9045, 27.5615);
            addCity("минск", 53.9045, 27.5615);
        }
    }

    private static void addCity(String cityName, double latitude, double longitude) {
        CITY_COORDINATES.put(cityName, new CityCoordinates(latitude, longitude));
    }

    public static CityCoordinates getCoordinates(String cityName) {
        if (!isLoaded) {
            loadCities();
        }

        CityCoordinates coordinates = CITY_COORDINATES.get(cityName.toLowerCase());
        if (coordinates != null) {
            return coordinates;
        }

        String cleanCity = cleanCityName(cityName);
        return CITY_COORDINATES.getOrDefault(cleanCity,
                new CityCoordinates(52.0976, 23.7341));
    }

    public static String cleanCityName(String city) {
        String[] endings = {"е", "у", "а", "ом", "и", "ой", "е", "ь"};
        String cleaned = city.toLowerCase();

        for (String ending : endings) {
            if (cleaned.endsWith(ending)) {
                cleaned = cleaned.substring(0, cleaned.length() - ending.length());
                break;
            }
        }

        // Специальные случаи
        Map<String, String> specialCases = new HashMap<>();
        specialCases.put("минс", "минск");
        specialCases.put("брест", "брест");
        specialCases.put("москв", "москва");
        specialCases.put("люцк", "луцк");

        return specialCases.getOrDefault(cleaned, cleaned);
    }

    public static class CityCoordinates {
        public final double latitude;
        public final double longitude;

        public CityCoordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}