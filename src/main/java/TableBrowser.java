import java.awt.Dimension;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

public class TableBrowser extends javax.swing.JInternalFrame {

    public TableBrowser(String title, TableModel model) {
        super(title, true, true, true, true);
        initComponents(model);
    }

    private void initComponents(TableModel model) {

        JTable table = new JTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(800, 400));
        table.setFillsViewportHeight(true);


        JScrollPane scrollPane = new JScrollPane(table);

        this.getContentPane().add(scrollPane);
        this.pack();
    }
}
