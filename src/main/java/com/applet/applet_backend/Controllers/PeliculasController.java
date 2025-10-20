package com.applet.applet_backend.Controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/peliculas")
public class PeliculasController {

    private static final Logger logger = LoggerFactory.getLogger(PeliculasController.class);

    @Value("${omdb.apikey:b9a48916}")
    private String apiKey;

    private final RestTemplate restTemplate;
    
    private static final String BASE_URL = "https://www.omdbapi.com";

    public PeliculasController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ✅ Buscar películas por título
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarPeliculas(@RequestParam String titulo) {
        logger.info("Buscando películas con título: {}", titulo);
        
        try {
            validarParametroBusqueda(titulo);

            String url = String.format("%s/?apikey=%s&s=%s&type=movie", 
                BASE_URL, apiKey, titulo.trim());

            logger.info("Consultando OMDB API...");
            
            Map<String, Object> respuesta = realizarPeticionSegura(url);
            
            if (respuesta == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(crearError("Servicio temporalmente no disponible"));
            }
            
            return procesarRespuestaBusqueda(respuesta);

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(crearError(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearError("Error interno del servidor"));
        }
    }

    // ✅ Obtener detalles de película por ID
    @GetMapping("/detalles")
    public ResponseEntity<?> obtenerDetallesPelicula(@RequestParam String id) {
        logger.info("Obteniendo detalles para ID: {}", id);
        
        try {
            validarParametroId(id);

            String url = String.format("%s/?apikey=%s&i=%s&plot=full", 
                BASE_URL, apiKey, id.trim());
            
            Map<String, Object> respuesta = realizarPeticionSegura(url);
            
            if (respuesta == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(crearError("Servicio temporalmente no disponible"));
            }
            
            return procesarRespuestaDetalles(respuesta);

        } catch (IllegalArgumentException e) {
            logger.warn("Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(crearError(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearError("Error interno del servidor"));
        }
    }

    // 🔧 Método mejorado para peticiones seguras
    private Map<String, Object> realizarPeticionSegura(String url) {
        try {
            logger.debug("Realizando petición a: {}", url.replace(apiKey, "***"));
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            
            logger.info("Petición exitosa, status: {}", response.getStatusCode());
            return response.getBody();
            
        } catch (ResourceAccessException e) {
            logger.error("Error de conexión con OMDB: {}", e.getMessage());
            return Collections.emptyMap();
        } catch (HttpClientErrorException e) {
            logger.error("Error HTTP de OMDB: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Map.of("Response", "False", "Error", "Error en la API: " + e.getStatusCode());
        } catch (Exception e) {
            logger.error("Error inesperado en petición: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    // 🔧 Métodos de validación
    private void validarParametroBusqueda(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("El título de búsqueda no puede estar vacío");
        }
        if (titulo.trim().length() < 2) {
            throw new IllegalArgumentException("El título debe tener al menos 2 caracteres");
        }
    }

    private void validarParametroId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la película no puede estar vacío");
        }
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<?> procesarRespuestaBusqueda(Map<String, Object> respuesta) {
        if (respuesta == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(crearError("No se pudo conectar con el servicio de películas"));
        }

        if (!"True".equals(respuesta.get("Response"))) {
            String error = (String) respuesta.get("Error");
            String mensajeError = error != null ? error : "No se encontraron películas";
            logger.info("Búsqueda sin resultados: {}", mensajeError);
            return ResponseEntity.ok(Map.of("peliculas", Collections.emptyList(), "mensaje", mensajeError));
        }

        List<Object> resultados = (List<Object>) respuesta.get("Search");
        if (resultados == null || resultados.isEmpty()) {
            logger.info("Búsqueda exitosa pero sin resultados");
            return ResponseEntity.ok(Map.of("peliculas", Collections.emptyList()));
        }

        List<Map<String, Object>> peliculas = new ArrayList<>();
        for (Object resultado : resultados) {
            if (resultado instanceof Map) {
                Map<String, Object> pelicula = procesarPeliculaBusqueda((Map<String, Object>) resultado);
                if (!pelicula.isEmpty()) {
                    peliculas.add(pelicula);
                }
            }
        }

        logger.info("Búsqueda exitosa, {} películas encontradas", peliculas.size());
        return ResponseEntity.ok(Map.of("peliculas", peliculas));
    }

    private Map<String, Object> procesarPeliculaBusqueda(Map<String, Object> datosPelicula) {
        Map<String, Object> pelicula = new HashMap<>();
        
        pelicula.put("id", datosPelicula.get("imdbID"));
        pelicula.put("titulo", datosPelicula.get("Title"));
        pelicula.put("año", datosPelicula.get("Year"));
        pelicula.put("tipo", datosPelicula.get("Type"));
        pelicula.put("poster", datosPelicula.get("Poster"));
        
        return pelicula;
    }

    private ResponseEntity<?> procesarRespuestaDetalles(Map<String, Object> respuesta) {
        if (respuesta == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(crearError("No se pudo conectar con el servicio de películas"));
        }

        if (!"True".equals(respuesta.get("Response"))) {
            String error = (String) respuesta.get("Error");
            String mensajeError = error != null ? error : "No se encontró la película";
            return ResponseEntity.badRequest().body(crearError(mensajeError));
        }

        Map<String, Object> detalles = procesarDetallesCompletos(respuesta);
        return ResponseEntity.ok(detalles);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> procesarDetallesCompletos(Map<String, Object> datosPelicula) {
        Map<String, Object> detalles = new HashMap<>();
        
        detalles.put("id", datosPelicula.get("imdbID"));
        detalles.put("titulo", datosPelicula.get("Title"));
        detalles.put("año", datosPelicula.get("Year"));
        detalles.put("clasificacion", datosPelicula.get("Rated"));
        detalles.put("lanzamiento", datosPelicula.get("Released"));
        detalles.put("duracion", datosPelicula.get("Runtime"));
        detalles.put("genero", datosPelicula.get("Genre"));
        detalles.put("director", datosPelicula.get("Director"));
        detalles.put("actores", datosPelicula.get("Actors"));
        detalles.put("sinopsis", datosPelicula.get("Plot"));
        detalles.put("idioma", datosPelicula.get("Language"));
        detalles.put("pais", datosPelicula.get("Country"));
        detalles.put("premios", datosPelicula.get("Awards"));
        detalles.put("poster", datosPelicula.get("Poster"));
        detalles.put("ratingImdb", datosPelicula.get("imdbRating"));
        detalles.put("votosImdb", datosPelicula.get("imdbVotes"));
        
        if (datosPelicula.get("Ratings") instanceof List) {
            detalles.put("ratings", procesarRatings((List<Object>) datosPelicula.get("Ratings")));
        }
        
        detalles.put("metascore", datosPelicula.get("Metascore"));
        detalles.put("tipo", datosPelicula.get("Type"));
        
        return detalles;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> procesarRatings(List<Object> ratings) {
        List<Map<String, String>> ratingsProcesados = new ArrayList<>();
        
        for (Object ratingObj : ratings) {
            if (ratingObj instanceof Map) {
                Map<String, Object> rating = (Map<String, Object>) ratingObj;
                Map<String, String> ratingProcesado = new HashMap<>();
                
                ratingProcesado.put("fuente", (String) rating.get("Source"));
                ratingProcesado.put("valor", (String) rating.get("Value"));
                
                ratingsProcesados.add(ratingProcesado);
            }
        }
        
        return ratingsProcesados;
    }

    private Map<String, String> crearError(String mensaje) {
        return Map.of("error", mensaje);
    }
}