package com.joel.lectorcodigo.conexion;

import java.io.File;
import java.sql.*;

public class Conexion {

    private static Connection connection;

    public static void inicializarBaseDatos() {
        try (Statement stmt = getConnection().createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS codigo_barras (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "codigo VARCHAR(100) NOT NULL UNIQUE, " +
                    "producto VARCHAR(255) NOT NULL, " +
                    "tipo VARCHAR(50), " +
                    "fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP)";
            stmt.execute(sql);
            System.out.println("Tabla codigo_barras inicializada correctamente");
        } catch (SQLException e) {
            System.err.println("Error al inicializar la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        if (connection == null) {
            synchronized (Conexion.class) {
                if (connection == null) {
                    try {
                        // 1. Apuntar a la carpeta del usuario (C:\Users\TuUsuario\LectorCodigoData)
                        String userHome = System.getProperty("user.home");
                        File appFolder = new File(userHome, "LectorCodigoData");

                        // 2. Crear la carpeta si no existe
                        if (!appFolder.exists()) {
                            appFolder.mkdirs();
                        }

                        // 3. Crear subcarpeta 'file' si no existe
                        File subfolder = new File(appFolder, "file");
                        if (!subfolder.exists()) {
                            subfolder.mkdirs();
                        }

                        // 4. Ruta absoluta y segura para el archivo SQLite
                        File dbFile = new File(subfolder, "data.db");
                        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

                        // 5. Conectar
                        connection = DriverManager.getConnection(url);

                    } catch (SQLException e) {
                        throw new RuntimeException("Error al conectar a la DB", e);
                    }
                }
            }
        }
        return connection;
    }

    public void listarPersonas() {
        String sql = "SELECT * FROM person";
        // Usamos getConnection() para asegurarnos de usar la conexion activa
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.println("id = " + rs.getInt("id"));
                System.out.println("name = " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}