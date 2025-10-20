package com.applet.applet_backend.Controllers;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/colores")
@CrossOrigin(origins = "http://localhost:3000")
public class ColoresController {

private static final Random RANDOM = new Random();

@GetMapping("/paleta")
public PaletaGenerada generarPaleta() {
    List<Color> colores = new ArrayList<>();

    for (int i = 0; i < 5; i++) {
        String hex = String.format("#%06X", RANDOM.nextInt(0xFFFFFF + 1));
        colores.add(new Color(hex, rgbDeHex(hex)));
    }

    return new PaletaGenerada(colores, "Paleta generada aleatoriamente");
}

    @PostMapping("/mezclar")
    public Color mezclar(@RequestBody ColoresMezcla entrada) {
        try {
            int[] rgb1 = hexAEnteros(entrada.color1());
            int[] rgb2 = hexAEnteros(entrada.color2());

            int r = (rgb1[0] + rgb2[0]) / 2;
            int g = (rgb1[1] + rgb2[1]) / 2;
            int b = (rgb1[2] + rgb2[2]) / 2;

            String resultadoHex = String.format("#%02X%02X%02X", r, g, b);
            return new Color(resultadoHex, String.format("rgb(%d, %d, %d)", r, g, b));
        } catch (Exception e) {
            return new Color("#000000", "Error: " + e.getMessage());
        }
    }

    @PostMapping("/hsl")
    public Color convertirHSL(@RequestBody HSLRequest req) {
        float h = req.h();
        float s = req.s() / 100f;
        float l = req.l() / 100f;

        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = l - c / 2;

        float r = 0;
        float g = 0;
        float b = 0;

        if (h < 60) { r = c; g = x; }
        else if (h < 120) { r = x; g = c; }
        else if (h < 180) { g = c; b = x; }
        else if (h < 240) { g = x; b = c; }
        else if (h < 300) { r = x; b = c; }
        else { r = c; b = x; }

        int rFinal = Math.round((r + m) * 255);
                    int gFinal = Math.round((g + m) * 255);
                    int bFinal = Math.round((b + m) * 255);

        String hex = String.format("#%02X%02X%02X", rFinal, gFinal, bFinal);
        return new Color(hex, String.format("rgb(%d, %d, %d)", rFinal, gFinal, bFinal));
    }

    private int[] hexAEnteros(String hex) {
        hex = hex.replace("#", "");
        return new int[]{
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    private String rgbDeHex(String hex) {
        int[] rgb = hexAEnteros(hex);
        return String.format("rgb(%d, %d, %d)", rgb[0], rgb[1], rgb[2]);
    }

    public record Color(String hex, String rgb) {}
    public record PaletaGenerada(List<Color> colores, String descripcion) {}
    public record ColoresMezcla(String color1, String color2) {}
    public record HSLRequest(float h, float s, float l) {}

    @GetMapping("/aleatorio")
public Color generarColorAleatorio() {
    String hex = String.format("#%06X", RANDOM.nextInt(0xFFFFFF + 1));
    return new Color(hex, rgbDeHex(hex));
}

@PostMapping("/generar-paleta")
public Map<String, Object> generarPaletaPersonalizada(@RequestBody PaletaRequest request) {
    List<Color> colores = new ArrayList<>();
    
    // Lógica para generar paleta basada en colorBase, tipoPaleta y cantidad
    for (int i = 0; i < request.cantidad(); i++) {
        String hex = String.format("#%06X", RANDOM.nextInt(0xFFFFFF + 1));
        colores.add(new Color(hex, rgbDeHex(hex)));
    }
    
    // Generar CSS y SCSS
    StringBuilder css = new StringBuilder(":root {\n");
    StringBuilder scss = new StringBuilder();
    
    for (int i = 0; i < colores.size(); i++) {
        css.append("  --color-").append(i + 1).append(": ").append(colores.get(i).hex()).append(";\n");
        scss.append("$color-").append(i + 1).append(": ").append(colores.get(i).hex()).append(";\n");
    }
    css.append("}");
    
    Map<String, Object> response = new HashMap<>();
    response.put("colores", colores);
    response.put("css", css.toString());
    response.put("scss", scss.toString());
    
    return response;
}

public record PaletaRequest(String colorBase, String tipoPaleta, int cantidad) {}
}
