import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.table.DefaultTableModel;

public class JDBCTableAdapter extends DefaultTableModel {

    public JDBCTableAdapter(ResultSet rs) {
        super();

        try {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();

            // Nombres de columnas
            Object[] columnNames = new Object[cols];
            for (int i = 1; i <= cols; i++) {
                columnNames[i-1] = meta.getColumnLabel(i);
            }

            // Posicionar al final para saber cuántas filas hay
            rs.last();
            int rows = rs.getRow();
            Object[][] data = new Object[rows][cols];

            // Regresar al principio
            rs.beforeFirst();

            int r = 0;
            while (rs.next()) {
                for (int c = 1; c <= cols; c++) {
                    data[r][c-1] = rs.getString(c);
                }
                r++;
            }

            // Cargar al modelo
            this.setDataVector(data, columnNames);

        } catch (SQLException ex) {
            throw new RuntimeException("Error al cargar datos en la tabla: " + ex.getMessage());
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // Todo se muestra solo para consulta → no editable
        return false;
    }
}
