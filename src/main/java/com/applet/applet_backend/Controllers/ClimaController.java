package com.applet.applet_backend.Controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.*;

@RestController
@RequestMapping("/api/clima")
public class ClimaController {

    @Value("${weatherapi.key}") // Cambiado a weatherapi.key
    private String apiKey;

    private final RestTemplate restTemplate;

    private static final String BASE_URL = "http://api.weatherapi.com/v1";

    public ClimaController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ✅ Obtener clima por ciudad - WEATHERAPI
    @GetMapping("/ciudad")
    public ResponseEntity<?> obtenerClimaPorCiudad(@RequestParam String ciudad) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "API Key no configurada. Contacta al administrador."));
            }

            String url = String.format("%s/current.json?key=%s&q=%s&lang=es", BASE_URL, apiKey, ciudad);

            Map<String, Object> jsonResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }).getBody();

            if (jsonResponse == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Respuesta vacía del servidor de clima."));
            }

            Map<String, Object> clima = extraerDatosClimaWeatherAPI(jsonResponse);
            return ResponseEntity.ok(clima);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al obtener el clima: " + e.getMessage()));
        }
    }

    // ✅ Obtener clima por coordenadas - WEATHERAPI
    @GetMapping("/coordenadas")
    public ResponseEntity<?> obtenerClimaPorCoordenadas(@RequestParam double lat, @RequestParam double lon) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "API Key no configurada. Contacta al administrador."));
            }

            String url = String.format("%s/current.json?key=%s&q=%s,%s&lang=es", BASE_URL, apiKey, lat, lon);

            Map<String, Object> jsonResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }).getBody();

            if (jsonResponse == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No se pudo obtener el clima para las coordenadas especificadas."));
            }

            Map<String, Object> clima = extraerDatosClimaWeatherAPI(jsonResponse);
            return ResponseEntity.ok(clima);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al obtener el clima por coordenadas: " + e.getMessage()));
        }
    }

    // ✅ Obtener pronóstico de 3 días - WEATHERAPI
    @GetMapping("/pronostico")
    public ResponseEntity<?> obtenerPronostico(@RequestParam String ciudad) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "API Key no configurada. Contacta al administrador."));
            }

            String url = String.format("%s/forecast.json?key=%s&q=%s&days=3&lang=es", BASE_URL, apiKey, ciudad);

            Map<String, Object> jsonResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }).getBody();

            if (jsonResponse == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No se pudo obtener el pronóstico."));
            }

            List<Map<String, Object>> pronostico = procesarPronosticoWeatherAPI(jsonResponse);
            return ResponseEntity.ok(Map.of("pronostico", pronostico));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al obtener el pronóstico: " + e.getMessage()));
        }
    }

    // ✅ Método auxiliar para extraer datos del clima de WeatherAPI
    @SuppressWarnings("unchecked")
    private Map<String, Object> extraerDatosClimaWeatherAPI(Map<String, Object> jsonResponse) {
        Map<String, Object> clima = new HashMap<>();

        if (jsonResponse.get("location") instanceof Map) {
            Map<String, Object> location = (Map<String, Object>) jsonResponse.get("location");
            clima.put("ciudad", location.get("name"));
            clima.put("pais", location.get("country"));
            clima.put("lat", location.get("lat"));
            clima.put("lon", location.get("lon"));
        }

        if (jsonResponse.get("current") instanceof Map) {
            Map<String, Object> current = (Map<String, Object>) jsonResponse.get("current");
            clima.put("temperatura", current.get("temp_c")); // WeatherAPI usa temp_c para Celsius
            clima.put("humedad", current.get("humidity"));
            clima.put("presion", current.get("pressure_mb"));
            clima.put("viento", current.get("wind_kph")); // WeatherAPI usa km/h

            if (current.get("condition") instanceof Map) {
                Map<String, Object> condition = (Map<String, Object>) current.get("condition");
                clima.put("descripcion", condition.get("text"));
                clima.put("icono", condition.get("icon"));
            }
        }

        return clima;
    }

    // ✅ Método auxiliar para procesar pronóstico de WeatherAPI
    private List<Map<String, Object>> procesarPronosticoWeatherAPI(Map<String, Object> jsonResponse) {
        List<Map<String, Object>> pronostico = new ArrayList<>();

        if (!tienePronosticoValido(jsonResponse)) {
            return pronostico;
        }

        List<Object> forecastDays = obtenerDiasPronostico(jsonResponse);
        for (Object dayObj : forecastDays) {
            procesarDiaPronostico(dayObj, pronostico);
        }

        return pronostico;
    }

    private boolean tienePronosticoValido(Map<String, Object> jsonResponse) {
        return jsonResponse.get("forecast") instanceof Map;
    }

    @SuppressWarnings("unchecked")
    private List<Object> obtenerDiasPronostico(Map<String, Object> jsonResponse) {
        Map<String, Object> forecast = (Map<String, Object>) jsonResponse.get("forecast");
        if (forecast.get("forecastday") instanceof List) {
            return (List<Object>) forecast.get("forecastday");
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private void procesarDiaPronostico(Object dayObj, List<Map<String, Object>> pronostico) {
        if (!(dayObj instanceof Map)) {
            return;
        }

        Map<String, Object> dayData = (Map<String, Object>) dayObj;
        Map<String, Object> diaPronostico = crearDiaPronostico(dayData);

        if (!diaPronostico.isEmpty()) {
            pronostico.add(diaPronostico);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> crearDiaPronostico(Map<String, Object> dayData) {
        Map<String, Object> diaPronostico = new HashMap<>();
        diaPronostico.put("dia", dayData.get("date"));

        if (dayData.get("day") instanceof Map) {
            Map<String, Object> dayInfo = (Map<String, Object>) dayData.get("day");
            agregarDatosTemperatura(diaPronostico, dayInfo);
            agregarDatosClimaticos(diaPronostico, dayInfo);
        }

        return diaPronostico;
    }

    private void agregarDatosTemperatura(Map<String, Object> diaPronostico, Map<String, Object> dayInfo) {
        diaPronostico.put("max", dayInfo.get("maxtemp_c"));
        diaPronostico.put("min", dayInfo.get("mintemp_c"));
    }

    @SuppressWarnings("unchecked")
    private void agregarDatosClimaticos(Map<String, Object> diaPronostico, Map<String, Object> dayInfo) {
        if (dayInfo.get("condition") instanceof Map) {
            Map<String, Object> condition = (Map<String, Object>) dayInfo.get("condition");
            diaPronostico.put("descripcion", condition.get("text"));
            diaPronostico.put("icono", condition.get("icon"));
        }
    }

    // ✅ Obtener icono del clima - WeatherAPI usa URLs completas
    @GetMapping("/icono/{icono}")
    public ResponseEntity<?> obtenerIcono(@PathVariable String icono) {
        // WeatherAPI ya proporciona URLs completas para los iconos
        // Si el icono ya es una URL completa, devolverla tal cual
        if (icono.startsWith("http")) {
            return ResponseEntity.ok(Map.of("url", icono));
        }
        // Si no, construir la URL (para compatibilidad con datos antiguos)
        String iconUrl = String.format("https:%s", icono);
        return ResponseEntity.ok(Map.of("url", iconUrl));
    }

    // ✅ Método auxiliar para obtener valores double de forma segura
    @SuppressWarnings("unused")
    private double obtenerDoubleSeguro(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return 0.0;
    }
}