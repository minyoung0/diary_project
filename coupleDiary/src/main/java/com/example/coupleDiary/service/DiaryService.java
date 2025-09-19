package com.example.coupleDiary.service;

import com.example.coupleDiary.CoupleDiaryApplication;
import com.example.coupleDiary.domain.DateWeather;
import com.example.coupleDiary.repository.DateWeatherRepository;
import lombok.AllArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class DiaryService {

    private final DateWeatherRepository dateWeatherRepository;

    @Value("${openweathermap.key}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private String apiKey;

    private static final Logger logger= LoggerFactory.getLogger(CoupleDiaryApplication.class);


    @Transactional
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시 0분 0초 마다
    public void saveWeatherDate(){
        logger.info("데이터 잘 가져옴");
        dateWeatherRepository.save(getWeatherFromApi());
    }
    private DateWeather getWeatherFromApi(){
        //open weather map에서 날씨 데이터 가져오기
        String weatherData = getWeatherString();
        System.out.println(getWeatherString());

        //받아온 날씨 json 파싱하기
        Map<String,Object> parsedWeather = parseWeather(weatherData);

        DateWeather dateWeather = new DateWeather();
        dateWeather.setDate(LocalDate.now());
        dateWeather.setWeather(parsedWeather.get("main").toString());
        Object t = parsedWeather.get("temp");
        double temp = (t instanceof Number) ? ((Number) t).doubleValue()
                : Double.parseDouble(String.valueOf(t));
        dateWeather.setTemperature(temp);
        return dateWeather;
    }

    private String getWeatherString() {
        try {
            String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=Seoul&appid=" + apiKey;
            URL url = new URL(apiUrl);

            //apiURL을 HTTP형식으로 호출하겠다
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            //응답코드 (ex: 202,401,...)
            int code = connection.getResponseCode();

            BufferedReader br;
            if (code == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            String inputLine;
            StringBuilder response = new StringBuilder();
            while (((inputLine = br.readLine()) != null)) {
                response.append(inputLine);
            }
            br.close();

            return response.toString();
        } catch (Exception e) {
            return "failed to get response";
        }

    }

    private Map<String, Object> parseWeather(String jsonString) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject;

        try {
            jsonObject = (JSONObject) jsonParser.parse(jsonString);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> resultMap = new HashMap<>();

        // main.temp
        JSONObject mainData = (JSONObject) jsonObject.get("main");
        resultMap.put("temp", mainData.get("temp"));

        // weather[0].main / weather[0].icon
        JSONArray weatherArray = (JSONArray) jsonObject.get("weather"); // ← JSONArray로 받아야 함
        if (weatherArray != null && !weatherArray.isEmpty()) {
            JSONObject weatherData = (JSONObject) weatherArray.get(0);
            resultMap.put("main", weatherData.get("main"));
            resultMap.put("icon", weatherData.get("icon"));
        }

        return resultMap;

    }


}
