import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GestorReportes {
    private MainApp mainApp;
    private Database db;

    public GestorReportes(MainApp mainApp, Database db) {
        this.mainApp = mainApp;
        this.db = db;
    }

    // ==================== REPORTE 1: VENTAS POR PERÍODO ====================
    public void generarReporteVentas() {
        try {
            // Pedir fechas
            JPanel panel = new JPanel(new java.awt.GridLayout(2, 2, 5, 5));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Calendar calendario = Calendar.getInstance();
            calendario.add(Calendar.MONTH, -1);

            JTextField fechaInicioField = new JTextField(dateFormat.format(calendario.getTime()));
            JTextField fechaFinField = new JTextField(dateFormat.format(new Date()));

            panel.add(new JLabel("Fecha Inicio (YYYY-MM-DD):"));
            panel.add(fechaInicioField);
            panel.add(new JLabel("Fecha Fin (YYYY-MM-DD):"));
            panel.add(fechaFinField);


            int result = JOptionPane.showConfirmDialog(mainApp, panel,
                    "Reporte de Ventas - Seleccionar Período",
                    JOptionPane.OK_CANCEL_OPTION);

            if (result != JOptionPane.OK_OPTION) return;

            String fechaInicio = fechaInicioField.getText().trim();
            String fechaFin = fechaFinField.getText().trim();

            // Crear PDF
            String fileName = "Reporte_Ventas_" + System.currentTimeMillis() + ".pdf";
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("REPORTE DE VENTAS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Período: " + fechaInicio + " al " + fechaFin));
            document.add(new Paragraph("Fecha de generación: " + dateFormat.format(new Date())));
            document.add(new Paragraph("\n"));

            // Consulta SQL
            String sql = "SELECT " +
                    "    DATE(o.order_date_time) as fecha, " +
                    "    COUNT(DISTINCT o.order_id) as total_ordenes, " +
                    "    SUM(omi.order_menu_item_quantity) as total_items, " +
                    "    SUM(omi.order_menu_item_quantity * mi.menu_item_price) as total_ventas " +
                    "FROM order_restaurant o " +
                    "JOIN order_menu_item omi ON o.order_id = omi.order_id " +
                    "JOIN menu_item mi ON omi.menu_item_id = mi.menu_item_id " +
                    "WHERE o.order_status IN ('completed', 'served') " +
                    "  AND DATE(o.order_date_time) BETWEEN ?::date AND ?::date " +
                    "GROUP BY DATE(o.order_date_time) " +
                    "ORDER BY fecha DESC";

            ResultSet rs = db.executeQuery(sql, fechaInicio, fechaFin);

            // Crear tabla
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Encabezados
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
            PdfPCell header1 = new PdfPCell(new Phrase("Fecha", headerFont));
            PdfPCell header2 = new PdfPCell(new Phrase("Órdenes completadas", headerFont));
            PdfPCell header3 = new PdfPCell(new Phrase("Items Vendidos", headerFont));
            PdfPCell header4 = new PdfPCell(new Phrase("Total ($)", headerFont));

            header1.setBackgroundColor(BaseColor.DARK_GRAY);
            header2.setBackgroundColor(BaseColor.DARK_GRAY);
            header3.setBackgroundColor(BaseColor.DARK_GRAY);
            header4.setBackgroundColor(BaseColor.DARK_GRAY);

            table.addCell(header1);
            table.addCell(header2);
            table.addCell(header3);
            table.addCell(header4);

            // Datos
            double totalGeneral = 0;
            int totalOrdenes = 0;
            int totalItems = 0;

            while (rs.next()) {
                table.addCell(rs.getString("fecha"));
                table.addCell(String.valueOf(rs.getInt("total_ordenes")));
                table.addCell(String.valueOf(rs.getInt("total_items")));
                table.addCell(String.format("$%.2f", rs.getDouble("total_ventas")));

                totalGeneral += rs.getDouble("total_ventas");
                totalOrdenes += rs.getInt("total_ordenes");
                totalItems += rs.getInt("total_items");
            }
            rs.close();

            document.add(table);

            // Resumen
            document.add(new Paragraph("\n"));
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            document.add(new Paragraph("RESUMEN DEL PERÍODO", boldFont));
            document.add(new Paragraph("Total de órdenes completadas: " + totalOrdenes));
            document.add(new Paragraph("Total de items vendidos: " + totalItems));
            document.add(new Paragraph("Total de ventas: $" + String.format("%.2f", totalGeneral), boldFont));

            document.close();

            JOptionPane.showMessageDialog(mainApp,
                    "Reporte generado exitosamente!\n\nArchivo: " + fileName);

            // Abrir el PDF
            Desktop.getDesktop().open(new File(fileName));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error generando reporte: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== REPORTE 2: PLATILLOS MÁS VENDIDOS ====================
    public void generarReportePlatillosMasVendidos() {
        try {
            String fileName = "Reporte_Platillos_" + System.currentTimeMillis() + ".pdf";
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("PLATILLOS MÁS VENDIDOS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Fecha de generación: " + dateFormat.format(new Date())));
            document.add(new Paragraph("\n"));

            // Consulta SQL
            String sql = "SELECT " +
                    "    mi.menu_item_id, " +
                    "    mi.menu_item_description, " +
                    "    mi.menu_item_price, " +
                    "    SUM(omi.order_menu_item_quantity) as cantidad_vendida, " +
                    "    SUM(omi.order_menu_item_quantity * mi.menu_item_price) as total_ingresos " +
                    "FROM menu_item mi " +
                    "JOIN order_menu_item omi ON mi.menu_item_id = omi.menu_item_id " +
                    "JOIN order_restaurant o ON omi.order_id = o.order_id " +
                    "WHERE o.order_status IN ('completed', 'served') " +
                    "GROUP BY mi.menu_item_id, mi.menu_item_description, mi.menu_item_price " +
                    "ORDER BY cantidad_vendida DESC " +
                    "LIMIT 20";

            ResultSet rs = db.query(sql);

            // Crear tabla
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setWidths(new float[]{1, 4, 2, 2, 2});

            // Encabezados
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE);
            addTableHeader(table, headerFont, "ID", "Platillo", "Precio", "Cant. Vendida", "Total ($)");

            // Datos
            int rank = 1;
            double totalIngresos = 0;

            while (rs.next()) {
                table.addCell(String.valueOf(rank++));
                table.addCell(rs.getString("menu_item_description"));
                table.addCell(String.format("$%.2f", rs.getDouble("menu_item_price")));
                table.addCell(String.valueOf(rs.getInt("cantidad_vendida")));
                table.addCell(String.format("$%.2f", rs.getDouble("total_ingresos")));

                totalIngresos += rs.getDouble("total_ingresos");
            }
            rs.close();

            document.add(table);

            // Total
            document.add(new Paragraph("\n"));
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            document.add(new Paragraph("Ingresos totales de estos platillos: $" +
                    String.format("%.2f", totalIngresos), boldFont));

            document.close();

            JOptionPane.showMessageDialog(mainApp,
                    "Reporte generado exitosamente!\n\nArchivo: " + fileName);
            Desktop.getDesktop().open(new File(fileName));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error generando reporte: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ==================== REPORTE 3: INVENTARIO DE INGREDIENTES ====================
    public void generarReporteInventario() {
        try {
            String fileName = "Reporte_Inventario_" + System.currentTimeMillis() + ".pdf";
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            // Título
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("INVENTARIO DE INGREDIENTES", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Fecha de generación: " + dateFormat.format(new Date())));
            document.add(new Paragraph("\n"));

            // Consulta SQL
            String sql = "SELECT " +
                    "    i.ingredient_id, " +
                    "    i.ingredient_name, " +
                    "    it.ingredient_type_description, " +
                    "    i.ingredient_measure_unit, " +
                    "    i.ingredient_expiration, " +
                    "    COUNT(mii.menu_item_id) as platillos_usados " +
                    "FROM ingredients i " +
                    "LEFT JOIN ingredient_type it ON i.ingredient_type_code = it.ingredient_type_code " +
                    "LEFT JOIN menu_item_ingredient mii ON i.ingredient_id = mii.ingredient_id " +
                    "GROUP BY i.ingredient_id, i.ingredient_name, it.ingredient_type_description, " +
                    "         i.ingredient_measure_unit, i.ingredient_expiration " +
                    "ORDER BY i.ingredient_name";

            ResultSet rs = db.query(sql);

            // Crear tabla
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setWidths(new float[]{1, 3, 2, 1.5f, 2, 1.5f});

            // Encabezados
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            addTableHeader(table, headerFont, "ID", "Ingrediente", "Tipo", "Unidad", "Expiración", "Platillos");

            // Datos
            int totalIngredientes = 0;
            int ingredientesProximosVencer = 0;
            Date hoy = new Date();
            long treintaDias = 30L * 24 * 60 * 60 * 1000;

            Font normalFont = new Font(Font.FontFamily.HELVETICA, 9);
            Font warningFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.RED);

            while (rs.next()) {
                totalIngredientes++;

                java.sql.Date expiration = rs.getDate("ingredient_expiration");
                boolean proximoVencer = false;

                if (expiration != null) {
                    long diff = expiration.getTime() - hoy.getTime();
                    proximoVencer = diff > 0 && diff < treintaDias;
                    if (proximoVencer) ingredientesProximosVencer++;
                }

                Font cellFont = proximoVencer ? warningFont : normalFont;

                PdfPCell cell1 = new PdfPCell(new Phrase(String.valueOf(rs.getInt("ingredient_id")), cellFont));
                PdfPCell cell2 = new PdfPCell(new Phrase(rs.getString("ingredient_name"), cellFont));
                PdfPCell cell3 = new PdfPCell(new Phrase(
                        rs.getString("ingredient_type_description") != null ?
                                rs.getString("ingredient_type_description") : "N/A", cellFont));
                PdfPCell cell4 = new PdfPCell(new Phrase(
                        rs.getString("ingredient_measure_unit") != null ?
                                rs.getString("ingredient_measure_unit") : "-", cellFont));
                PdfPCell cell5 = new PdfPCell(new Phrase(
                        expiration != null ? expiration.toString() : "N/A", cellFont));
                PdfPCell cell6 = new PdfPCell(new Phrase(String.valueOf(rs.getInt("platillos_usados")), cellFont));

                table.addCell(cell1);
                table.addCell(cell2);
                table.addCell(cell3);
                table.addCell(cell4);
                table.addCell(cell5);
                table.addCell(cell6);
            }
            rs.close();

            document.add(table);

            // Resumen
            document.add(new Paragraph("\n"));
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            document.add(new Paragraph("RESUMEN", boldFont));
            document.add(new Paragraph("Total de ingredientes: " + totalIngredientes));

            if (ingredientesProximosVencer > 0) {
                warningFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(255, 0, 0));
                Paragraph warning = new Paragraph("⚠ Ingredientes próximos a vencer (30 días): " +
                        ingredientesProximosVencer, warningFont);
                document.add(warning);
            }

            document.close();

            JOptionPane.showMessageDialog(mainApp,
                    "Reporte generado exitosamente!\n\nArchivo: " + fileName);
            Desktop.getDesktop().open(new File(fileName));

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainApp, "Error generando reporte: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Método auxiliar para agregar encabezados
    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }
}