package com.uaemex.gesdep;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class DatabaseInitializer {

    public static void init() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // ACTIVIDAD 1
        Map<String, Object> act1 = new HashMap<>();
        act1.put("name", "Basquetbol");
        act1.put("coachName", "Juan Pérez");
        act1.put("schedule", "Lunes, 16:00 - 18:00");
        act1.put("place", "Gimnasio Municipal");
        act1.put("photoUrl", "");
        db.collection("activities").document("basquetbol")
                .set(act1, SetOptions.merge());

        // ACTIVIDAD 2
        Map<String, Object> act2 = new HashMap<>();
        act2.put("name", "Fútbol");
        act2.put("coachName", "Carlos López");
        act2.put("schedule", "Martes, 17:00 - 19:00");
        act2.put("place", "Cancha Municipal");
        act2.put("photoUrl", "");
        db.collection("activities").document("futbol")
                .set(act2, SetOptions.merge());
    }
}

