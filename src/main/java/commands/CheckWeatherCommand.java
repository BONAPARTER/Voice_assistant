package commands;

import utils.CityCoordinatesLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static java.lang.System.err;
import static java.lang.System.out;

public class CheckWeatherCommand implements CommandAction {
    @Override
    public void execute(Map<String, String> parameters) {
        String cityParam = parameters.get("city");
        if (cityParam == null || cityParam.trim().isEmpty()) {
            cityParam = "brest";
        }

        String cleanCity = CityCoordinatesLoader.cleanCityName(cityParam);
        out.println("Запрашиваем погоду для города: " + cleanCity.toUpperCase());

        CityCoordinatesLoader.CityCoordinates coordinates = CityCoordinatesLoader.getCoordinates(cleanCity);

        double latitude = coordinates.latitude;
        double longitude = coordinates.longitude;

        out.println("Координаты: широта=" + latitude + ", долгота=" + longitude);

        // Формируем корректный URL для Open-Meteo
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true",
                latitude, longitude
        );

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();
                parseWeatherData(responseBody, cleanCity);
            } else {
                out.println("Не удалось получить данные о погоде. Код ошибки: " + response.statusCode());
                out.println("URL запроса: " + url);
            }
        } catch (Exception e) {
            err.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    private void parseWeatherData(String jsonResponse, String city) {
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(jsonResponse);

            org.json.JSONObject currentWeather = jsonObject.getJSONObject("current_weather");
            double temperature = currentWeather.getDouble("temperature");
            double windspeed = currentWeather.getDouble("windspeed");
            String weatherCode = currentWeather.optString("weathercode", "N/A");

            out.println("\nПогода в городе: " + city.substring(0, 1).toUpperCase() + city.substring(1));
            out.println("Температура: " + temperature + "°C");
            out.println("Скорость ветра: " + windspeed + " км/ч");
            out.println("Код погоды: " + weatherCode + "\n");

            out.println(getWeatherDescription(weatherCode));
        } catch (Exception e) {
            err.println("Ошибка при парсинге JSON: " + e.getMessage());
            err.println("Ответ сервера: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())) + "...");
        }
    }

    private String getWeatherDescription(String weatherCode) {
        if (weatherCode.equals("N/A")) return "";

        int code = Integer.parseInt(weatherCode);
        return switch (code) {
            case 0 -> "Ясное небо";
            case 1, 2, 3 -> "Переменная облачность";
            case 45, 48 -> "Туман";
            case 51, 53, 55 -> "Морось";
            case 56, 57 -> "Морось с обледенением";
            case 61, 63, 65 -> "Дождь";
            case 66, 67 -> "Дождь с обледенением";
            case 71, 73, 75, 77 -> "Снег";
            case 80, 81, 82 -> "Ливневый дождь";
            case 85, 86 -> "Ливневый снег";
            case 95 -> "Гроза";
            case 96, 99 -> "Гроза с градом";
            default -> "Погодные условия неизвестны";
        };
    }
}