import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Database {

    private static final Logger LOGGER = Logger.getLogger(Database.class.getSimpleName());

    private static final String DB_URL = "jdbc:postgresql://localhost:5432/restaurantDB";
    private static final String DRIVER = "org.postgresql.Driver";

    private static Database instance = null;
    private Connection connection;

    // Constructor privado (Singleton)
    private Database(String user, String password) {
        try {
            Class.forName(DRIVER);
            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", password);

            connection = DriverManager.getConnection(DB_URL, props);
            connection.setAutoCommit(true);
            LOGGER.info("Conexion exitosa a la base de datos restaurantDB");

        } catch (ClassNotFoundException ex) {
            LOGGER.severe("No se pudo cargar el driver JDBC: " + ex.getMessage());
        } catch (SQLException ex) {
            LOGGER.severe("Error al conectar a la base de datos: " + ex.getMessage());
        }
    }

    // Método para obtener la instancia del Singleton
    public static Database getDatabase(String user, String password) {
        if (instance == null) {
            instance = new Database(user, password);
        }
        return instance;
    }


    // SELECT
    public ResultSet query(String sql) throws SQLException {
        Statement stmt = connection.createStatement(
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_READ_ONLY
        );
        return stmt.executeQuery(sql);
    }

    // INSERT, UPDATE, DELETE
    public int update(String sql) throws SQLException {
        Statement stmt = connection.createStatement();
        return stmt.executeUpdate(sql);
    }

    // ========== MÉTODOS DE TRANSACCIONES ==========

    // Inicia una transacción desactivando auto-commit

    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
        LOGGER.info("Transacción iniciada");
    }

    // Confirma la transacción y reactiva auto-commit

    public void commit() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
        LOGGER.info("Transacción confirmada (COMMIT)");
    }

    // Revierte la transacción y reactiva auto-commit

    public void rollback() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
            LOGGER.warning("Transacción revertida (ROLLBACK)");
        } catch (SQLException e) {
            LOGGER.severe("Error al hacer rollback: " + e.getMessage());
        }
    }

    /**
     * Ejecuta un bloque de código dentro de una transacción
     * Si sale bien, hace commit. Si hay error, hace rollback automáticamente.
     */
    public void executeTransaction(TransactionBlock block) throws SQLException {
        try {
            beginTransaction();
            block.execute();
            commit();
        } catch (Exception e) {
            rollback();
            throw new SQLException("Error en transacción: " + e.getMessage(), e);
        }
    }

    // Interfaz funcional para bloques de transacción

    @FunctionalInterface
    public interface TransactionBlock {
        void execute() throws SQLException;
    }

    // ========== MÉTODOS CON PREPARED STATEMENTS ==========

    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            setParameters(pstmt, params);
            int result = pstmt.executeUpdate();
            LOGGER.fine("UPDATE ejecutado: " + result + " fila(s) afectada(s)");
            return result;
        }
    }

    public ResultSet executeInsert(String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        setParameters(pstmt, params);
        pstmt.executeUpdate();
        ResultSet keys = pstmt.getGeneratedKeys();
        LOGGER.fine("INSERT ejecutado con claves generadas");
        return keys;
    }

    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        PreparedStatement pstmt = connection.prepareStatement(
                sql,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_READ_ONLY
        );
        setParameters(pstmt, params);
        ResultSet rs = pstmt.executeQuery();
        LOGGER.fine("Query ejecutado");
        return rs;
    }

    private void setParameters(PreparedStatement pstmt, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];
            int index = i + 1;

            if (param == null) {
                pstmt.setNull(index, Types.NULL);
            } else if (param instanceof String) {
                pstmt.setString(index, (String) param);
            } else if (param instanceof Integer) {
                pstmt.setInt(index, (Integer) param);
            } else if (param instanceof Double) {
                pstmt.setDouble(index, (Double) param);
            } else if (param instanceof Long) {
                pstmt.setLong(index, (Long) param);
            } else if (param instanceof Boolean) {
                pstmt.setBoolean(index, (Boolean) param);
            } else if (param instanceof Date) {
                pstmt.setDate(index, (Date) param);
            } else if (param instanceof Timestamp) {
                pstmt.setTimestamp(index, (Timestamp) param);
            } else if (param instanceof Float) {
                pstmt.setFloat(index, (Float) param);
            } else {
                pstmt.setObject(index, param);
            }
        }
    }

    // ========== UTILIDADES ==========

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Conexión a la base de datos cerrada");
            }
        } catch (SQLException e) {
            LOGGER.severe("Error al cerrar la conexión: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}