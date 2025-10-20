package com.applet.applet_backend.Controllers;

import org.springframework.web.bind.annotation.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/password")
@CrossOrigin(origins = "http://localhost:3000")
public class PasswordController {

    private static final SecureRandom random = new SecureRandom();

    @PostMapping("/generar")
    public ResultadoPassword generar(@RequestBody RequisitosPassword req) {
        try {
            String password = generarPassword(req);
            String fortaleza = evaluarFortaleza(password);
            return new ResultadoPassword(password, fortaleza, null);
        } catch (Exception e) {
            return new ResultadoPassword(null, null, "Error generando contraseña: " + e.getMessage());
        }
    }

    @PostMapping("/evaluar")
    public ResultadoPassword evaluar(@RequestBody EvaluarPassword req) {
        try {
            String fortaleza = evaluarFortaleza(req.password());
            return new ResultadoPassword(req.password(), fortaleza, null);
        } catch (Exception e) {
            return new ResultadoPassword(null, null, "Error evaluando contraseña: " + e.getMessage());
        }
    }

    @GetMapping("/generar-pin")
    public Map<String, Object> generarPin() {
        int pin = 100000 + random.nextInt(900000); // 6 dígitos
        return Map.of("pin", String.valueOf(pin));
    }

    private String generarPassword(RequisitosPassword req) {
        String mayus = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String minus = "abcdefghijklmnopqrstuvwxyz";
        String nums = "0123456789";
        String simbolos = "!@#$%^&*()-_=+[]{}|;:',.<>?/";

        StringBuilder pool = new StringBuilder();
        if (req.incluirMayusculas()) pool.append(mayus);
        if (req.incluirMinusculas()) pool.append(minus);
        if (req.incluirNumeros()) pool.append(nums);
        if (req.incluirSimbolos()) pool.append(simbolos);

        if (pool.isEmpty()) throw new IllegalArgumentException("Debe seleccionar al menos un tipo de carácter");

        return random.ints(req.longitud(), 0, pool.length())
                .mapToObj(pool::charAt)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    private String evaluarFortaleza(String password) {
        int score = 0;

        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:',.<>?/].*")) score++;

        return switch (score) {
            case 0, 1, 2 -> "Débil";
            case 3, 4 -> "Media";
            case 5, 6 -> "Fuerte";
            default -> "Desconocida";
        };
    }

    public record RequisitosPassword(
            int longitud,
            boolean incluirMayusculas,
            boolean incluirMinusculas,
            boolean incluirNumeros,
            boolean incluirSimbolos) {}

    public record EvaluarPassword(String password) {}

    public record ResultadoPassword(String password, String fortaleza, String error) {}
}
