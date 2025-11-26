package com.uaemex.gesdep.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de tipos de eventos con sus capacidades y requisitos
 * Define los parámetros estándar para cada categoría de evento
 */
public class EventConfig {

    /**
     * Clase interna para definir las características de cada tipo de evento
     */
    public static class EventTypeConfig {
        public String name;
        public String type; // "deportivo" o "cultural"
        public String registrationType; // "individual" o "team"
        public int minParticipants;
        public int maxParticipants;
        public int minTeamMembers; // Para eventos de equipo
        public int maxTeamMembers; // Para eventos de equipo
        public int estimatedDurationMinutes;
        public String description;

        public EventTypeConfig(String name, String type, String registrationType,
                               int minParticipants, int maxParticipants,
                               int minTeamMembers, int maxTeamMembers,
                               int estimatedDurationMinutes, String description) {
            this.name = name;
            this.type = type;
            this.registrationType = registrationType;
            this.minParticipants = minParticipants;
            this.maxParticipants = maxParticipants;
            this.minTeamMembers = minTeamMembers;
            this.maxTeamMembers = maxTeamMembers;
            this.estimatedDurationMinutes = estimatedDurationMinutes;
            this.description = description;
        }
    }

    // Mapa de configuraciones por categoría
    private static final Map<String, EventTypeConfig> EVENT_CONFIGS = new HashMap<>();

    static {
        // ========== EVENTOS DEPORTIVOS - EQUIPOS ==========

        // Fútbol
        EVENT_CONFIGS.put("futbol", new EventTypeConfig(
                "Fútbol",
                "deportivo",
                "team",
                2,  // Mínimo 2 equipos
                16, // Máximo 16 equipos (torneo)
                5,  // Mínimo 5 jugadores por equipo (fútbol 5)
                11, // Máximo 11 jugadores (fútbol 11)
                90, // 90 minutos
                "Partido o torneo de fútbol. Requiere mínimo 2 equipos confirmados."
        ));

        // Basquetbol
        EVENT_CONFIGS.put("basquetbol", new EventTypeConfig(
                "Basquetbol",
                "deportivo",
                "team",
                2,  // Mínimo 2 equipos
                8,  // Máximo 8 equipos
                5,  // Mínimo 5 jugadores
                12, // Máximo 12 jugadores
                60, // 60 minutos
                "Partido o torneo de basquetbol. Requiere mínimo 2 equipos confirmados."
        ));

        // Voleibol
        EVENT_CONFIGS.put("voleibol", new EventTypeConfig(
                "Voleibol",
                "deportivo",
                "team",
                2,  // Mínimo 2 equipos
                8,  // Máximo 8 equipos
                6,  // Mínimo 6 jugadores
                12, // Máximo 12 jugadores
                60, // 60 minutos
                "Partido o torneo de voleibol. Requiere mínimo 2 equipos confirmados."
        ));

        // ========== EVENTOS DEPORTIVOS - INDIVIDUALES ==========

        // Atletismo (carreras)
        EVENT_CONFIGS.put("atletismo", new EventTypeConfig(
                "Atletismo",
                "deportivo",
                "individual",
                2,  // Mínimo 2 participantes
                20, // Máximo 20 por carrera
                0,  // No aplica para individual
                0,
                30, // 30 minutos
                "Carreras de velocidad, medio fondo o fondo. Categorías: 100m, 200m, 400m, 800m, 1500m, 5000m."
        ));

        // Salto de longitud
        EVENT_CONFIGS.put("salto_longitud", new EventTypeConfig(
                "Salto de Longitud",
                "deportivo",
                "individual",
                2,  // Mínimo 2 participantes
                15, // Máximo 15 participantes
                0,
                0,
                45, // 45 minutos
                "Competencia de salto de longitud. Cada participante tiene 3 intentos."
        ));

        // Ajedrez
        EVENT_CONFIGS.put("ajedrez", new EventTypeConfig(
                "Ajedrez",
                "deportivo",
                "individual",
                2,  // Mínimo 2 participantes (1v1)
                32, // Máximo 32 participantes (torneo)
                0,
                0,
                90, // 90 minutos por partida
                "Torneo de ajedrez. Partidas individuales o eliminación directa."
        ));

        // Natación
        EVENT_CONFIGS.put("natacion", new EventTypeConfig(
                "Natación",
                "deportivo",
                "individual",
                2,  // Mínimo 2 participantes
                8,  // Máximo 8 por carril
                0,
                0,
                30, // 30 minutos
                "Competencia de natación. Categorías: 50m, 100m, 200m libre/pecho/mariposa/dorso."
        ));

        // Ciclismo
        EVENT_CONFIGS.put("ciclismo", new EventTypeConfig(
                "Ciclismo",
                "deportivo",
                "individual",
                2,  // Mínimo 2 participantes
                50, // Máximo 50 ciclistas
                0,
                0,
                120, // 2 horas
                "Carrera de ciclismo en ruta o pista. Distancias variadas."
        ));

        // ========== EVENTOS CULTURALES - INDIVIDUALES ==========

        // Danza (individual/solista)
        EVENT_CONFIGS.put("danza_individual", new EventTypeConfig(
                "Danza Individual",
                "cultural",
                "individual",
                2,  // Mínimo 2 participantes
                30, // Máximo 30 participantes
                0,
                0,
                60, // 60 minutos
                "Competencia de danza individual. Categorías: ballet, jazz, contemporáneo, folklórico."
        ));

        // Teatro (actuación individual)
        EVENT_CONFIGS.put("teatro_individual", new EventTypeConfig(
                "Teatro Individual",
                "cultural",
                "individual",
                2,  // Mínimo 2 participantes
                20, // Máximo 20 participantes
                0,
                0,
                90, // 90 minutos
                "Monólogos y actuaciones individuales. Máximo 5 minutos por participante."
        ));

        // Música (solista)
        EVENT_CONFIGS.put("musica_solista", new EventTypeConfig(
                "Música Solista",
                "cultural",
                "individual",
                2,  // Mínimo 2 participantes
                25, // Máximo 25 participantes
                0,
                0,
                90, // 90 minutos
                "Competencia de música solista. Instrumentos o voz."
        ));

        // Pintura/Arte
        EVENT_CONFIGS.put("arte", new EventTypeConfig(
                "Arte y Pintura",
                "cultural",
                "individual",
                2,  // Mínimo 2 participantes
                40, // Máximo 40 participantes
                0,
                0,
                120, // 2 horas
                "Competencia de pintura, dibujo o arte visual. Tema libre o específico."
        ));

        // ========== EVENTOS CULTURALES - GRUPOS ==========

        // Danza (grupos/equipos)
        EVENT_CONFIGS.put("danza_grupo", new EventTypeConfig(
                "Danza Grupal",
                "cultural",
                "team",
                2,  // Mínimo 2 grupos
                15, // Máximo 15 grupos
                4,  // Mínimo 4 bailarines
                20, // Máximo 20 bailarines
                90, // 90 minutos
                "Competencia de danza grupal/conjuntos. Categorías: folklórico, moderno, urbano."
        ));

        // Teatro (obras grupales)
        EVENT_CONFIGS.put("teatro_grupo", new EventTypeConfig(
                "Teatro Grupal",
                "cultural",
                "team",
                2,  // Mínimo 2 grupos
                10, // Máximo 10 grupos
                3,  // Mínimo 3 actores
                15, // Máximo 15 actores
                120, // 2 horas
                "Obras de teatro grupales. Máximo 15 minutos por presentación."
        ));

        // Música (bandas/grupos)
        EVENT_CONFIGS.put("musica_banda", new EventTypeConfig(
                "Música - Bandas",
                "cultural",
                "team",
                2,  // Mínimo 2 bandas
                12, // Máximo 12 bandas
                3,  // Mínimo 3 integrantes
                10, // Máximo 10 integrantes
                120, // 2 horas
                "Competencia de bandas musicales. Máximo 10 minutos por banda."
        ));

        // ========== EVENTOS MIXTOS/ESPECIALES ==========

        // Semana deportiva (multi-evento)
        EVENT_CONFIGS.put("semana_deportiva", new EventTypeConfig(
                "Semana Deportiva",
                "deportivo",
                "team",
                4,  // Mínimo 4 equipos
                20, // Máximo 20 equipos
                10, // Mínimo 10 miembros por delegación
                30, // Máximo 30 miembros
                480, // 8 horas al día
                "Semana deportiva con múltiples disciplinas. Cada equipo participa en varias categorías."
        ));

        // Festival cultural
        EVENT_CONFIGS.put("festival_cultural", new EventTypeConfig(
                "Festival Cultural",
                "cultural",
                "team",
                5,  // Mínimo 5 grupos
                30, // Máximo 30 grupos
                3,  // Mínimo 3 integrantes
                25, // Máximo 25 integrantes
                360, // 6 horas
                "Festival cultural con múltiples presentaciones artísticas."
        ));
    }

    /**
     * Obtiene la configuración de un tipo de evento
     */
    public static EventTypeConfig getConfig(String category) {
        return EVENT_CONFIGS.get(category);
    }

    /**
     * Obtiene todas las categorías disponibles
     */
    public static Map<String, EventTypeConfig> getAllConfigs() {
        return new HashMap<>(EVENT_CONFIGS);
    }

    /**
     * Verifica si una categoría existe
     */
    public static boolean categoryExists(String category) {
        return EVENT_CONFIGS.containsKey(category);
    }

    /**
     * Obtiene todas las categorías deportivas
     */
    public static Map<String, EventTypeConfig> getSportsConfigs() {
        Map<String, EventTypeConfig> sports = new HashMap<>();
        for (Map.Entry<String, EventTypeConfig> entry : EVENT_CONFIGS.entrySet()) {
            if ("deportivo".equals(entry.getValue().type)) {
                sports.put(entry.getKey(), entry.getValue());
            }
        }
        return sports;
    }

    /**
     * Obtiene todas las categorías culturales
     */
    public static Map<String, EventTypeConfig> getCulturalConfigs() {
        Map<String, EventTypeConfig> cultural = new HashMap<>();
        for (Map.Entry<String, EventTypeConfig> entry : EVENT_CONFIGS.entrySet()) {
            if ("cultural".equals(entry.getValue().type)) {
                cultural.put(entry.getKey(), entry.getValue());
            }
        }
        return cultural;
    }

    /**
     * Obtiene las categorías de eventos individuales
     */
    public static Map<String, EventTypeConfig> getIndividualConfigs() {
        Map<String, EventTypeConfig> individual = new HashMap<>();
        for (Map.Entry<String, EventTypeConfig> entry : EVENT_CONFIGS.entrySet()) {
            if ("individual".equals(entry.getValue().registrationType)) {
                individual.put(entry.getKey(), entry.getValue());
            }
        }
        return individual;
    }

    /**
     * Obtiene las categorías de eventos de equipo
     */
    public static Map<String, EventTypeConfig> getTeamConfigs() {
        Map<String, EventTypeConfig> teams = new HashMap<>();
        for (Map.Entry<String, EventTypeConfig> entry : EVENT_CONFIGS.entrySet()) {
            if ("team".equals(entry.getValue().registrationType)) {
                teams.put(entry.getKey(), entry.getValue());
            }
        }
        return teams;
    }
}
