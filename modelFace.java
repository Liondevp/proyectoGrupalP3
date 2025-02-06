package Model;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.swing.JOptionPane;

public class modelFace {

    private MongoConnection mongoConnection;

    public modelFace() {
        mongoConnection = MongoConnection.getInstance(); // Obtener la instancia única de MongoConnection
    }
    // Método para registrar un nuevo usuario
 public boolean registrarUsuario(String nombre, String apellido, String correo, String usuario, String clave) {
    MongoCollection<Document> collection = mongoConnection.getCollection();

    // Validar si el usuario o correo ya existen
    Document filtro = new Document("$or", List.of(
        new Document("usuario", usuario),
        new Document("correo", correo)
    ));

    Document usuarioExistente = collection.find(filtro).first();
    if (usuarioExistente != null) {
        String mensaje = "El registro no puede completarse porque:\n";
        if (usuarioExistente.getString("usuario").equals(usuario)) {
            mensaje += "- Ya existe un usuario con el mismo nombre de usuario.\n";
        }
        if (usuarioExistente.getString("correo").equals(correo)) {
            mensaje += "- Ya existe un usuario con el mismo correo electrónico.";
        }
        JOptionPane.showMessageDialog(null, mensaje, "Error de Registro", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    // Registro de usuario si no hay duplicados
    Document nuevoUsuario = new Document("nombre", nombre)
            .append("apellido", apellido)
            .append("correo", correo)
            .append("usuario", usuario)
            .append("clave", clave);

    try {
        collection.insertOne(nuevoUsuario);
        return true;
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error al registrar el usuario: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }
}

    // Método para verificar si un usuario y contraseña son correctos
    public boolean verificarUsuario(String usuario, String clave) {
        MongoCollection<Document> collection = mongoConnection.getCollection();
        Document usuarioDoc = collection.find(new Document("usuario", usuario)).first();
        if (usuarioDoc != null) {
            String claveGuardada = usuarioDoc.getString("clave");
            return claveGuardada.equals(clave); // Usuario y contraseña correctos
        }
        return false; // Usuario no encontrado o contraseña incorrecta
    }

    // Método para recordar el usuario (guardado en un archivo)
    public void recordarUsuario(String usuario) {
        try (FileWriter writer = new FileWriter("usuario_recordado.txt")) {
            writer.write(usuario);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para recuperar la contraseña (simula el envío de un correo)
    public boolean recuperarContraseña(String usuario) {
        MongoCollection<Document> collection = mongoConnection.getCollection();
        Document usuarioDoc = collection.find(new Document("usuario", usuario)).first();
        if (usuarioDoc != null) {
            String correo = usuarioDoc.getString("correo");
            enviarCorreoRecuperacion(correo);
            return true;
        }
        return false; // Usuario no encontrado
    }

    // Método para enviar un correo electrónico de recuperación
    private void enviarCorreoRecuperacion(String correo) {
        // Configuración del servidor de correo
        String host = "smtp.example.com"; // Cambia esto a la configuración de tu servidor de correo
        String from = "your-email@example.com"; // Cambia esto a tu dirección de correo electrónico
        String pass = "your-email-password"; // Cambia esto a tu contraseña de correo electrónico

        Properties properties = System.getProperties();
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.user", from);
        properties.put("mail.smtp.password", pass);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(properties);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(correo));
            message.setSubject("Recuperación de Contraseña");
            message.setText("Haz clic en el siguiente enlace para recuperar tu contraseña: [enlace de recuperación]");

            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
