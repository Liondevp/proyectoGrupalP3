package Model;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.FindIterable;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Modelo para gestionar registros de amigos.
 */
public class modelDiary extends Usuario implements Interface {

    private MongoConnection conexion;
    private String genero;
    private String nombreApodo;
    private String fecha;
    private String numero;
    private String comida;
    private int edad;
    private String color;
    private String musica;

    public modelDiary() {
        super("", "", "", "", ""); // Constructor de la clase base `Usuario`
         this.conexion = MongoConnection.getInstance(); // Conexión a MongoDB
        this.genero = "";
        this.nombreApodo = "";
        this.fecha = "";
        this.numero = "";
        this.comida = "";
        this.edad = 0;
        this.color = "";
        this.musica = "";
    }

    // Getters y Setters
    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getNombreApodo() {
        return nombreApodo;
    }

    public void setNombreApodo(String nombreApodo) {
        this.nombreApodo = nombreApodo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComida() {
        return comida;
    }

    public void setComida(String comida) {
        this.comida = comida;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMusica() {
        return musica;
    }

    public void setMusica(String musica) {
        this.musica = musica;
    }

    
    // Métodos CRUD para la colección "Amigos"
    @Override
    public void obtenerDatos() {
        List<Document> amigos = obtenerAmigos();

        if (amigos.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay amigos registrados.", "Información", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder datos = new StringBuilder("Amigos registrados:\n");
            for (Document amigo : amigos) {
                datos.append("Nombre/Apodo: ").append(amigo.getString("nombreApodo")).append("\n")
                        .append("Género: ").append(amigo.getString("genero")).append("\n")
                        .append("Fecha: ").append(amigo.getString("fecha")).append("\n")
                        .append("Número: ").append(amigo.getString("numero")).append("\n")
                        .append("Comida favorita: ").append(amigo.getString("comida")).append("\n")
                        .append("Edad: ").append(amigo.getInteger("edad")).append("\n")
                        .append("Color favorito: ").append(amigo.getString("color")).append("\n")
                        .append("Música favorita: ").append(amigo.getString("musica")).append("\n\n");
            }
           // JOptionPane.showMessageDialog(null, datos.toString(), "Información de Amigos", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void agregarAmigo(String genero, String nombreApodo, String fecha, String numero, String comida, int edad, String color, String musica, String importante) {
        Document amigo = new Document("genero", genero)
                .append("nombreApodo", nombreApodo)
                .append("fecha", fecha)
                .append("numero", numero)
                .append("comida", comida)
                .append("edad", edad)
                .append("color", color)
                .append("musica", musica)
                .append("importante", importante);
        conexion.getCollectionAmigos().insertOne(amigo);
    }

    public void eliminarAmigo(String nombreApodo) {
        Document filtro = new Document("nombreApodo", nombreApodo);
        conexion.getCollectionAmigos().deleteOne(filtro);
        JOptionPane.showMessageDialog(null, "Amigo eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public void actualizarAmigo(String nombreApodo, Document nuevosDatos) {
        Document filtro = new Document("nombreApodo", nombreApodo);
        Document actualizacion = new Document("$set", nuevosDatos);

        try {
            conexion.getCollectionAmigos().updateOne(filtro, actualizacion);
            JOptionPane.showMessageDialog(null, "Amigo actualizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al actualizar el amigo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public List<Document> obtenerAmigos() {
        MongoCollection<Document> collection = conexion.getCollectionAmigos();
        FindIterable<Document> iterable = collection.find();
        List<Document> amigos = new ArrayList<>();
        for (Document doc : iterable) {
            amigos.add(doc);
        }
        return amigos;
    }

  public List<Document> buscarAmigo(Document filtro) {
    MongoCollection<Document> collection = conexion.getCollectionAmigos();
    FindIterable<Document> iterable = collection.find(filtro);
    List<Document> amigos = new ArrayList<>();
    for (Document doc : iterable) {
        amigos.add(doc);
    }
    return amigos;
}
}
