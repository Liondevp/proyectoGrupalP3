package Controller;

import Model.modelSchedule;
import Vista.scheduleOutings;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.bson.Document;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

public class controlSchedule {

    private scheduleOutings vista;
    private modelSchedule modelo;

    public controlSchedule(scheduleOutings vista, modelSchedule modelo) {
        this.vista = vista;
        this.modelo = modelo;
        this.vista.btnAgendarSalida.addActionListener(e -> agendarSalida());
        this.vista.btnBuscarSalida.addActionListener(e -> buscarSalida());
        this.vista.btnRegresar.addActionListener(e -> regresar());
        this.vista.btnBorrar.addActionListener(e -> eliminarSalida());

        cargarAmigosEnTabla();
        cargarSalidasEnTabla();
    }

    private Object[][] datosAmigos;

    public void setDatosAmigos(Object[][] datos) {
        this.datosAmigos = datos;
        cargarAmigosEnTabla();
    }

    // Método para cargar los amigos en la tabla al iniciar la vista
    private void cargarAmigosEnTabla() {
        DefaultTableModel modeloTabla = (DefaultTableModel) vista.tblDatosSalidas.getModel();
        modeloTabla.setRowCount(0); // Limpiar la tabla antes de cargar los datos

        if (datosAmigos != null && datosAmigos.length > 0) {
            for (Object[] amigo : datosAmigos) {
                modeloTabla.addRow(new Object[]{
                    amigo[0], // género
                    amigo[1], // nombreApodo
                    amigo[2], // fecha
                    amigo[3], // número
                    amigo[4], // comida
                    amigo[5], // edad
                    amigo[6], // color
                    amigo[7], // música
                    amigo[8] // importante
                });
            }
        } else {
            List<String> amigos = modelo.obtenerAmigos();
            if (amigos.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "No hay amigos para mostrar.");
            } else {
                for (String amigo : amigos) {
                    modeloTabla.addRow(new Object[]{amigo});
                }
            }
        }
    }

    // Método para cargar las salidas en la tabla
    private void cargarSalidasEnTabla() {
        DefaultTableModel modeloTablaSalidas = (DefaultTableModel) vista.tblSalidas.getModel();
        modeloTablaSalidas.setRowCount(0); // Limpiar la tabla antes de cargar los datos

        List<Document> salidas = modelo.obtenerSalidas();
        for (Document salida : salidas) {
            modeloTablaSalidas.addRow(new Object[]{
                salida.getString("amigo"),
                salida.getString("nombreSalida"),
                salida.getString("fecha"),
                salida.getString("lugar")
            });
        }
    }

    public void agendarSalida() {
        // Limpiar los errores previos
        limpiarErrores();

        // Validación de campos
        String nombreSalida = vista.txtNombreSalida.getText().trim();
        Date fechaSalidaDate = vista.jDFechaSalida.getDate();
        String lugarSalida = vista.txtLugar.getText().trim();
        int filaSeleccionada = vista.tblDatosSalidas.getSelectedRow();

        boolean hayErrores = false;

        if (nombreSalida.isEmpty()) {
            vista.lblErrorNombreSalida.setText("El nombre de la salida es obligatorio.");
            hayErrores = true;
        }
       
    if (fechaSalidaDate == null) {
        vista.lblErrorFechaSalida.setText("La fecha de la salida es obligatoria.");
        hayErrores = true;
    } else {
        // Obtener la fecha actual sin la hora
        LocalDate today = LocalDate.now();
        LocalDate fechaSalida = fechaSalidaDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        // Validar que la fecha de salida no sea anterior a hoy
        if (fechaSalida.isBefore(today)) {
            vista.lblErrorFechaSalida.setText("La fecha de la salida no puede ser en el pasado.");
            hayErrores = true;
        } else {
            vista.lblErrorFechaSalida.setText("");
        }
    }

        

        if (lugarSalida.isEmpty()) {
            vista.lblErrorLugarSalida.setText("El lugar de la salida es obligatorio.");
            hayErrores = true;
        }

        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(vista, "Por favor, selecciona con quién vas a salir.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            hayErrores = true;
        }

        if (!hayErrores) {
            // Validar si el nombre de la salida ya existe
            List<Document> salidasExistentes = modelo.buscarSalidaPorNombre(nombreSalida);

            if (!salidasExistentes.isEmpty()) {
                // Si ya existe una salida con el mismo nombre, mostrar un mensaje de error
                JOptionPane.showMessageDialog(vista, "Ya existe una salida con el mismo nombre. "
                        + "Por favor elige un nombre diferente.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Obtener el nombre del amigo seleccionado
            String amigoSeleccionado = (String) vista.tblDatosSalidas.getValueAt(filaSeleccionada, 1);

            // Formatear la fecha a String
            SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
            String fechaSalida = formatoFecha.format(fechaSalidaDate);

            // Agendar la salida en la base de datos
            boolean agendado = modelo.agendarSalida(amigoSeleccionado, nombreSalida, fechaSalida, lugarSalida);
            if (agendado) {
                // Mostrar un mensaje de confirmación con la información
                String mensaje = "Has agendado una salida con: " + amigoSeleccionado + "\n"
                        + "Nombre de la salida: " + nombreSalida + "\n"
                        + "Fecha: " + fechaSalida + "\n"
                        + "Lugar: " + lugarSalida;

                JOptionPane.showMessageDialog(vista, mensaje, "Resumen de la salida",
                        JOptionPane.INFORMATION_MESSAGE);

                // Actualizar la tabla de salidas
                cargarSalidasEnTabla();
            } else {
                JOptionPane.showMessageDialog(vista, "Error al agendar la salida.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Método para buscar una salida por nombre
    private void buscarSalida() {
        String nombreBusqueda = vista.txtBuscarAmigoSalida.getText();

        if (nombreBusqueda.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "Por favor ingrese el nombre de la salida.");
            return;
        }

        // Buscar la salida usando el modelo
        List<Document> resultados = modelo.buscarSalidaPorNombre(nombreBusqueda);

        if (resultados.isEmpty()) {
            JOptionPane.showMessageDialog(vista, "No se encontraron salidas con ese nombre.");
        } else {
            // Mostrar los resultados en un JOptionPane
            StringBuilder mensaje = new StringBuilder("Resultados encontrados:\n");
            for (Document salida : resultados) {
                mensaje.append("Amigo: ").append(salida.getString("amigo")).append("\n")
                        .append("Salida: ").append(salida.getString("nombreSalida")).append("\n")
                        .append("Fecha: ").append(salida.getString("fecha")).append("\n")
                        .append("Lugar: ").append(salida.getString("lugar")).append("\n\n");
            }
            JOptionPane.showMessageDialog(vista, mensaje.toString());
        }
    }

    private void eliminarSalida() {
        int filaSeleccionada = vista.tblSalidas.getSelectedRow();

        if (filaSeleccionada == -1) {
            // Mostrar mensaje si no se seleccionó ninguna fila
            JOptionPane.showMessageDialog(vista, "Por favor, selecciona una salida para eliminar.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(vista,
                "¿Estás seguro de que deseas eliminar esta salida?",
                "Confirmación", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            DefaultTableModel modeloTabla = (DefaultTableModel) vista.tblSalidas.getModel();
            String amigo = (String) modeloTabla.getValueAt(filaSeleccionada, 0); 
            String nombreSalida = (String) modeloTabla.getValueAt(filaSeleccionada, 1); 
            String fecha = (String) modeloTabla.getValueAt(filaSeleccionada, 2); 
            String lugar = (String) modeloTabla.getValueAt(filaSeleccionada, 3); 

            boolean eliminado = modelo.eliminarSalida(amigo, nombreSalida, fecha, lugar);

            if (eliminado) {
                JOptionPane.showMessageDialog(vista, "Salida eliminada con éxito.",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);

                cargarSalidasEnTabla();
            } else {
                JOptionPane.showMessageDialog(vista, "No se pudo eliminar la salida. Intenta nuevamente.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Método para limpiar los errores anteriores
    private void limpiarErrores() {
        vista.lblErrorNombreSalida.setText("");
        vista.lblErrorFechaSalida.setText("");
        vista.lblErrorLugarSalida.setText("");
    }

    private void regresar() {
        vista.dispose();
    }
}
