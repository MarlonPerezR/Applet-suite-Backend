package com.applet.applet_backend.Controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calculadora")

public class CalculadoraController {

    @PostMapping("/calcular")
    public Resultado calcular(@RequestBody Operacion operacion) {
        double resultado = 0;
        String error = null;
        String operacionRealizada = null;

        try {
            switch (operacion.operador()) {
                case "+" -> {
                    resultado = operacion.x() + operacion.y();
                    operacionRealizada = operacion.x() + " + " + operacion.y();
                }
                case "-" -> {
                    resultado = operacion.x() - operacion.y();
                    operacionRealizada = operacion.x() + " - " + operacion.y();
                }
                case "*" -> {
                    resultado = operacion.x() * operacion.y();
                    operacionRealizada = operacion.x() + " × " + operacion.y();
                }
                case "/" -> {
                    if (operacion.y() == 0)
                        error = "Error: División por cero";
                    else {
                        resultado = operacion.x() / operacion.y();
                        operacionRealizada = operacion.x() + " ÷ " + operacion.y();
                    }
                }
                case "√" -> {
                    if (operacion.x() < 0)
                        error = "Error: Raíz cuadrada de número negativo";
                    else {
                        resultado = Math.sqrt(operacion.x());
                        operacionRealizada = "√" + operacion.x();
                    }
                }
                case "%" -> {
                    resultado = (operacion.x() * operacion.y()) / 100;
                    operacionRealizada = operacion.x() + " % de " + operacion.y();
                }
                case "^" -> {
                    resultado = Math.pow(operacion.x(), operacion.y());
                    operacionRealizada = operacion.x() + "^" + operacion.y();
                }
                default -> error = "Operador no válido: " + operacion.operador();
            }
        } catch (Exception e) {
            error = "Error en el cálculo: " + e.getMessage();
        }

        return new Resultado(resultado, error, operacionRealizada);
    }
    

    @PostMapping("/calcular/unario")
    public Resultado calcularUnario(@RequestBody OperacionUnaria operacion) {
        double resultado = 0;
        String error = null;
        String operacionRealizada = null;

        try {
            switch (operacion.operador()) {
                case "√" -> {
                    if (operacion.x() < 0)
                        error = "Error: Raíz cuadrada de número negativo";
                    else {
                        resultado = Math.sqrt(operacion.x());
                        operacionRealizada = "√" + operacion.x();
                    }
                }
                case "±" -> {
                    resultado = -operacion.x();
                    operacionRealizada = "Negativo de " + operacion.x();
                }
                case "1/x" -> {
                    if (operacion.x() == 0)
                        error = "Error: División por cero";
                    else {
                        resultado = 1 / operacion.x();
                        operacionRealizada = "1/" + operacion.x();
                    }
                }
                default -> error = "Operador unario no válido: " + operacion.operador();
            }
        } catch (Exception e) {
            error = "Error en el cálculo: " + e.getMessage();
        }

        

        return new Resultado(resultado, error, operacionRealizada);
    }

    public record Operacion(double x, double y, String operador) {}
    public record OperacionUnaria(double x, String operador) {}
    public record Resultado(double valor, String error, String operacionRealizada) {}

    
}
