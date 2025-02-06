package Model;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;

public class modelSchedule {

    private MongoConnection mongoConnection;
    private MongoCollection<Document> collectionAmigos;
    private MongoCollection<Document> collectionSalidas;

    public modelSchedule() {
        mongoConnection = MongoConnection.getInstance();
        collectionAmigos = mongoConnection.getCollectionAmigos();
        collectionSalidas = mongoConnection.getCollectionSalidas();
    }

    // Método para recuperar los amigos desde la base de datos
    public List<String> obtenerAmigos() {
        List<String> amigos = new ArrayList<>();
        try {
            for (Document doc : collectionAmigos.find()) {
                amigos.add(doc.getString("nombre"));
            }
        } catch (Exception e) {
            System.out.println("Error al obtener amigos: " + e.getMessage());
        }
        return amigos;
    }

    // Método para agendar la salida con validación de duplicados
    public boolean agendarSalida(String amigo, String nombreSalida, String fecha, String lugar) {
        try {
            // Verificar si ya existe una salida con los mismos datos
            Document filtro = new Document("amigo", amigo)
                    .append("nombreSalida", nombreSalida)
                    .append("fecha", fecha)
                    .append("lugar", lugar);
            if (collectionSalidas.find(filtro).first() != null) {
                return false; // Ya existe una salida con los mismos datos
            }

            // Insertar la nueva salida
            Document nuevaSalida = new Document("amigo", amigo)
                    .append("nombreSalida", nombreSalida)
                    .append("fecha", fecha)
                    .append("lugar", lugar);
            collectionSalidas.insertOne(nuevaSalida);
            return true;
        } catch (Exception e) {
            System.out.println("Error al agendar salida: " + e.getMessage());
            return false;
        }
    }

    public List<Document> buscarSalidaPorNombre(String nombre) {
        List<Document> resultados = new ArrayList<>();
        try {
            for (Document doc : collectionSalidas.find(new Document("nombreSalida", nombre))) {
                resultados.add(doc);
            }
        } catch (Exception e) {
            System.out.println("Error al buscar salida: " + e.getMessage());
        }
        return resultados;
    }

    public List<Document> obtenerSalidas() {
        List<Document> salidas = new ArrayList<>();
        try {
            for (Document doc : collectionSalidas.find()) {
                salidas.add(doc);
            }
        } catch (Exception e) {
            System.out.println("Error al obtener salidas: " + e.getMessage());
        }
        return salidas;
    }

    public void cerrarConexion() {
        mongoConnection.cerrarConexion();
    }
    
    public boolean eliminarSalida(String amigo, String nombreSalida, String fecha, String lugar) {
    try {
        // Crear filtro para encontrar el documento a eliminar
        Document filtro = new Document("amigo", amigo)
                .append("nombreSalida", nombreSalida)
                .append("fecha", fecha)
                .append("lugar", lugar);

        // Eliminar el documento
        long eliminados = collectionSalidas.deleteOne(filtro).getDeletedCount();

        // Verificar si se eliminó algún documento
        return eliminados > 0;
    } catch (Exception e) {
        System.out.println("Error al eliminar salida: " + e.getMessage());
        return false;
    }
}
}
