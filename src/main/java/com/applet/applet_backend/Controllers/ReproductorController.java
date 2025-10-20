package com.applet.applet_backend.Controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/musica")
@CrossOrigin(origins = "http://localhost:3000")
public class ReproductorController {

    @Value("${youtube.api.key}")
    private String youtubeApiKey;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    // ✅ PLAYLIST CON TUS ARCHIVOS REALES - URLs ABSOLUTAS
    private final List<Cancion> playlistReal = Arrays.asList(
            new Cancion(1, "Estranged", "Guns N' Roses", 563,
                    "http://localhost:3000/audio/Guns%20N%27%20Roses%20-%20Estranged.mp3",
                    "http://localhost:3000/images/Estranged.jpg", "estranged"),
            new Cancion(2, "Patience", "Guns N' Roses", 354,
                    "http://localhost:3000/audio/Guns%20N%27%20Roses%20-%20Patience.mp3",
                    "http://localhost:3000/images/Patience.jpg", "patience"),
            new Cancion(3, "Master of Puppets", "Metallica", 516,
                    "http://localhost:3000/audio/Master%20of%20Puppets%20(Remastered).mp3",
                    "http://localhost:3000/images/Master.jpg", "master"),
            new Cancion(4, "Nothing Else Matters", "Metallica", 388,
                    "http://localhost:3000/audio/Nothing%20Else%20Matters.mp3",
                    "http://localhost:3000/images/Nothing.jpg", "nothing"),

 new Cancion(5, "Monkey Business", "Skid Row", 332,
            "http://localhost:3000/audio/Monkey%20Business.mp3",
            "http://localhost:3000/images/Monkey.jpg", "monkey"),
    new Cancion(6, "18 and Life", "Skid Row", 312,
            "http://localhost:3000/audio/18%20and%20Life.mp3",
            "http://localhost:3000/images/18.jpg", "18andlife"),
    new Cancion(7, "Hysteria", "Def Leppard", 537,
            "http://localhost:3000/audio/Hysteria.mp3",
            "http://localhost:3000/images/Hysteria.jpg", "hysteria"),
    new Cancion(8, "Bringin' On The Heartbreak", "Def Leppard", 264,
            "http://localhost:3000/audio/Bringin%20On%20The%20Heartbreak.mp3",
            "http://localhost:3000/images/Bringin%20On%20The%20Heartbreak.jpg", "bringinon")
            
            
    );

    // 🔹 OBTENER PLAYLIST REAL
    @GetMapping("/playlist")
    public List<Cancion> obtenerPlaylist() {
        System.out.println("🎵 === ENVIANDO PLAYLIST REAL ===");
        System.out.println("📊 Total de canciones: " + playlistReal.size());

        for (int i = 0; i < playlistReal.size(); i++) {
            Cancion cancion = playlistReal.get(i);
            System.out.println((i + 1) + ". " + cancion.titulo() + " - " + cancion.artista());
            System.out.println("   🎵 URL: " + cancion.url());
            System.out.println("   🖼️  Portada: " + cancion.portada());
        }

        return playlistReal;
    }

    // 🔹 SERVIR ARCHIVOS DE AUDIO DIRECTAMENTE
    @GetMapping("/audio/{filename:.+}")
    public ResponseEntity<Resource> servirAudio(@PathVariable String filename) {
        try {
            // Ruta a tu carpeta de audio en el frontend
            Path audioPath = Paths.get("../frontend/public/audio/").resolve(filename).normalize();
            Resource resource = new UrlResource(audioPath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("audio/mpeg"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🔹 BUSCAR EN YOUTUBE (mantener igual)
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarEnYouTube(@RequestParam String query) {
        try {
            if (youtubeApiKey == null || youtubeApiKey.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "YouTube API Key no configurada"));
            }

            List<CancionYouTube> resultados = buscarVideosYouTube(query);
            return ResponseEntity.ok(resultados);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(
                    Map.of("error", "Error al buscar en YouTube: " + e.getMessage()));
        }
    }

    private List<CancionYouTube> buscarVideosYouTube(String query) throws Exception {
        YouTube youtube = new YouTube.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                null).setApplicationName("applet-reproductor").build();

        YouTube.Search.List search = youtube.search().list(Arrays.asList("id", "snippet"));
        search.setKey(youtubeApiKey);
        search.setQ(query + " música official audio");
        search.setType(Collections.singletonList("video"));
        search.setMaxResults(15L);
        search.setFields("items(id/videoId,snippet/title,snippet/channelTitle,snippet/thumbnails/medium/url)");

        SearchListResponse searchResponse = search.execute();
        List<SearchResult> searchResults = searchResponse.getItems();

        if (searchResults == null || searchResults.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> videoIds = searchResults.stream()
                .map(item -> item.getId().getVideoId())
                .toList();

        return obtenerDetallesVideos(videoIds);
    }

    private List<CancionYouTube> obtenerDetallesVideos(List<String> videoIds) throws Exception {
        YouTube youtube = new YouTube.Builder(
                new NetHttpTransport(),
                JSON_FACTORY,
                null).setApplicationName("applet-reproductor").build();

        YouTube.Videos.List videoRequest = youtube.videos().list(Arrays.asList("snippet", "contentDetails"));
        videoRequest.setKey(youtubeApiKey);
        videoRequest.setId(videoIds);
        videoRequest.setFields(
                "items(id,snippet/title,snippet/channelTitle,snippet/thumbnails/medium/url,contentDetails/duration)");

        VideoListResponse videoResponse = videoRequest.execute();
        List<Video> videos = videoResponse.getItems();

        return videos.stream()
                .map(this::mapVideoToCancion)
                .filter(Objects::nonNull)
                .toList();
    }

    private CancionYouTube mapVideoToCancion(Video video) {
        try {
            String titulo = video.getSnippet().getTitle();
            String artista = video.getSnippet().getChannelTitle();
            String thumbnail = video.getSnippet().getThumbnails().getMedium().getUrl();
            String duracionISO = video.getContentDetails().getDuration();

            int duracionSegundos = parseDuracionISO(duracionISO);

            return new CancionYouTube(
                    video.getId(),
                    titulo,
                    artista,
                    duracionSegundos,
                    "https://www.youtube.com/watch?v=" + video.getId(),
                    thumbnail,
                    duracionISO);
        } catch (Exception e) {
            return null;
        }
    }

    private int parseDuracionISO(String duracionISO) {
        try {
            return (int) java.time.Duration.parse(duracionISO).getSeconds();
        } catch (Exception e) {
            return 0;
        }
    }

    // Records
    public record Cancion(
            int id,
            String titulo,
            String artista,
            int duracion,
            String url,
            String portada,
            String videoId) {
    }

    public record CancionYouTube(
            String videoId,
            String titulo,
            String artista,
            int duracion,
            String url,
            String portada,
            String duracionISO) {
    }
}