package com.applet.applet_backend.Controllers;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/conversor")
@CrossOrigin(origins = "http://localhost:3000")
public class ConversorController {

    @PostMapping("/convertir")
    public ResultadoConversion convertirUnidad(@RequestBody ConversionRequest request) {
        try {
            double resultado = realizarConversion(
                    request.valor(), request.unidadOrigen(), request.unidadDestino(), request.tipoConversion());

            String descripcion = String.format("%.2f %s = %.4f %s",
                    request.valor(), request.unidadOrigen(), resultado, request.unidadDestino());

            return new ResultadoConversion(resultado, null, descripcion);
        } catch (IllegalArgumentException e) {
            return new ResultadoConversion(0, e.getMessage(), null);
        } catch (Exception e) {
            return new ResultadoConversion(0, "Error en la conversión: " + e.getMessage(), null);
        }
    }

    @GetMapping("/tasas-cambio")
    public TasasCambio obtenerTasasCambio() {
        Map<String, Double> tasas = Map.of(
                "USD", 1.0, "EUR", 0.85, "MXN", 20.0,
                "GBP", 0.73, "JPY", 110.0, "COP", 3900.0,
                "ARS", 350.0, "BRL", 5.2);
        return new TasasCambio(tasas, "Última actualización: " + java.time.LocalDateTime.now());
    }

    @GetMapping("/unidades/{tipo}")
    public List<String> obtenerUnidades(@PathVariable String tipo) {
        return switch (tipo.toLowerCase()) {
            case "longitud" -> List.of("metros", "kilometros", "centimetros", "milimetros", "pies", "pulgadas", "millas", "yardas");
            case "peso" -> List.of("kilogramos", "gramos", "libras", "onzas", "toneladas", "stone");
            case "temperatura" -> List.of("celsius", "fahrenheit", "kelvin");
            case "moneda" -> List.of("USD", "EUR", "MXN", "GBP", "JPY", "COP", "ARS", "BRL");
            case "volumen" -> List.of("litros", "mililitros", "galones", "onzas_liq", "pulgadas_cubicas");
            case "tiempo" -> List.of("segundos", "minutos", "horas", "dias", "semanas", "meses", "años");
            default -> throw new IllegalArgumentException("Tipo de conversión no soportado: " + tipo);
        };
    }

    private double realizarConversion(double valor, String origen, String destino, String tipo) {
        tipo = tipo.toLowerCase();

        return switch (tipo) {
            case "longitud" -> convertirLongitud(valor, origen, destino);
            case "peso" -> convertirPeso(valor, origen, destino);
            case "temperatura" -> convertirTemperatura(valor, origen, destino);
            case "moneda" -> convertirMoneda(valor, origen, destino);
            case "volumen" -> convertirVolumen(valor, origen, destino);
            case "tiempo" -> convertirTiempo(valor, origen, destino);
            default -> throw new IllegalArgumentException("Tipo no válido");
        };
    }

    // Métodos auxiliares para conversiones
    private double convertirLongitud(double v, String o, String d) {
        Map<String, Double> f = Map.of(
                "metros", 1.0, "kilometros", 1000.0, "centimetros", 0.01,
                "milimetros", 0.001, "pies", 0.3048, "pulgadas", 0.0254,
                "millas", 1609.34, "yardas", 0.9144);
        return v * f.get(o) / f.get(d);
    }

    private double convertirPeso(double v, String o, String d) {
        Map<String, Double> f = Map.of(
                "kilogramos", 1.0, "gramos", 0.001, "libras", 0.453592,
                "onzas", 0.0283495, "toneladas", 1000.0, "stone", 6.35029);
        return v * f.get(o) / f.get(d);
    }

    private double convertirTemperatura(double v, String o, String d) {
        if (o.equals("celsius") && d.equals("fahrenheit")) return (v * 9 / 5) + 32;
        if (o.equals("fahrenheit") && d.equals("celsius")) return (v - 32) * 5 / 9;
        if (o.equals("celsius") && d.equals("kelvin")) return v + 273.15;
        if (o.equals("kelvin") && d.equals("celsius")) return v - 273.15;
        if (o.equals("fahrenheit") && d.equals("kelvin")) return (v - 32) * 5 / 9 + 273.15;
        if (o.equals("kelvin") && d.equals("fahrenheit")) return (v - 273.15) * 9 / 5 + 32;
        return v;
    }

    private double convertirMoneda(double v, String o, String d) {
        Map<String, Double> tasas = Map.of(
                "USD", 1.0, "EUR", 0.85, "MXN", 20.0,
                "GBP", 0.73, "JPY", 110.0, "COP", 3900.0,
                "ARS", 350.0, "BRL", 5.2);
        return v / tasas.get(o) * tasas.get(d);
    }

    private double convertirVolumen(double v, String o, String d) {
        Map<String, Double> f = Map.of(
                "litros", 1.0, "mililitros", 0.001,
                "galones", 3.78541, "onzas_liq", 0.0295735, "pulgadas_cubicas", 0.0163871);
        return v * f.get(o) / f.get(d);
    }

    private double convertirTiempo(double v, String o, String d) {
        Map<String, Double> f = Map.of(
                "segundos", 1.0, "minutos", 60.0, "horas", 3600.0,
                "dias", 86400.0, "semanas", 604800.0, "meses", 2.628e6, "años", 3.154e7);
        return v * f.get(o) / f.get(d);
    }

    public record ConversionRequest(double valor, String unidadOrigen, String unidadDestino, String tipoConversion) {}
    public record ResultadoConversion(double valor, String error, String descripcion) {}
    public record TasasCambio(Map<String, Double> tasas, String ultimaActualizacion) {}
}
