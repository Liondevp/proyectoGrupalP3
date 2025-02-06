package Model;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javax.swing.JOptionPane;
import org.bson.Document;

public class MongoConnection {

    private static MongoConnection instance; // Singleton instance
    private MongoClient mongo;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private MongoCollection<Document> collectionAmigos;
    private MongoCollection<Document> collectionSalidas;
    private final String server = "localhost";
    private final int puerto = 27017;
    private final String namebd = "P2ProyectoGrupal";
    private final String nameCollection = "Usuarios";
    private final String nameCollectionAmigos = "Amigos";
    private final String nameCollectionSalidas = "Salidas";

    // Constructor privado para evitar instanciación directa
    private MongoConnection() {
        crearConexion();
    }

    // Método estático para obtener la instancia Singleton
    public static synchronized MongoConnection getInstance() {
        if (instance == null) {
            instance = new MongoConnection();
        }
        return instance;
    }

    // Método para crear la conexión a MongoDB
    private void crearConexion() {
        try {
            mongo = new MongoClient(server, puerto);
            database = mongo.getDatabase(namebd);
            collection = database.getCollection(nameCollection);
            collectionAmigos = database.getCollection(nameCollectionAmigos);
            collectionSalidas = database.getCollection(nameCollectionSalidas);
        } catch (MongoException e) {
            JOptionPane.showMessageDialog(null, "Error al conectar a MongoDB: " + e.getMessage());
        }
    }

    // Método para obtener la colección de usuarios
    public MongoCollection<Document> getCollection() {
        return collection;
    }

    // Método para obtener la colección de amigos
    public MongoCollection<Document> getCollectionAmigos() {
        return collectionAmigos;
    }

    // Método para obtener la colección de salidas
    public MongoCollection<Document> getCollectionSalidas() {
        return collectionSalidas;
    }

    // Método para obtener la base de datos
    public MongoDatabase getDatabase() {
        return database;
    }

    // Método para cerrar la conexión a MongoDB
    public void cerrarConexion() {
        if (mongo != null) {
            try {
                mongo.close();
            } catch (MongoException e) {
                JOptionPane.showMessageDialog(null, "Error al cerrar la conexión a MongoDB: " + e.getMessage());
            }
        }
    }
}