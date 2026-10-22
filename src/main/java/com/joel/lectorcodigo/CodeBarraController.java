package com.joel.lectorcodigo;

import com.joel.lectorcodigo.conexion.Conexion;
import com.joel.lectorcodigo.datos.CodigoBarra;
import com.joel.lectorcodigo.datos.excel.ExcelData;
import com.joel.lectorcodigo.utils.ETipoCode;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CodeBarraController implements Initializable {
    @FXML
    private TextField txtBuscar, txtCodigo, txtProducto;
    @FXML
    private TableView<CodigoBarra> tblCodigoBarras;
    @FXML
    private RadioButton rdbEAN13;
    @FXML
    private RadioButton rdbCodabar;
    @FXML
    private RadioButton rdbCode128;
    @FXML
    private RadioButton rdbUtc;
    @FXML
    private RadioButton rdb80mm;
    @FXML
    private RadioButton rdb50mm;
    @FXML
    private RadioButton rdbPrevisualizarSi;
    private final FilteredList<CodigoBarra> codigoBarraFilteredList;
    private ObservableList<CodigoBarra> barraObservableList;
    private ToggleGroup groupTipoCodi;
    private ToggleGroup groupTamano;
    private ToggleGroup groupPrevisualizar;

    public CodeBarraController() {
        barraObservableList = FXCollections.observableArrayList();
        codigoBarraFilteredList = new FilteredList<>(barraObservableList, (a) -> true);
        groupTipoCodi = new ToggleGroup();
        groupTamano = new ToggleGroup();
        groupPrevisualizar = new ToggleGroup();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Inicializar la base de datos
        Conexion.inicializarBaseDatos();

        rdbEAN13.setToggleGroup(groupTipoCodi);
        rdbCodabar.setToggleGroup(groupTipoCodi);
        rdbCode128.setToggleGroup(groupTipoCodi);
        rdbUtc.setToggleGroup(groupTipoCodi);

        rdb50mm.setToggleGroup(groupTamano);
        rdb80mm.setToggleGroup(groupTamano);

        rdbPrevisualizarSi.setToggleGroup(groupPrevisualizar);

        rdbPrevisualizarSi.setSelected(true);
        rdbCode128.setSelected(true);
        rdb50mm.setSelected(true);

        iniciarTabla();
        agregarContextMenu();

    }

    @FXML
    private void onImportarExcel(ActionEvent actionEvent) {
        ExcelData excelData = new ExcelData();
        if (!excelData.importarData(barraObservableList)) {
            System.out.println("acurrio un error al importar los datos");
        }
    }

    @FXML
    private void btnAgregarCodigo(ActionEvent actionEvent) {
        if (validarCampos()) {
            if (!validarFormato()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Código inválido");
                // TODO: poner las reglas de codigo su formato
                alert.setContentText("El código ingresado no cumple con el formato del tipo seleccionado.");
                alert.show();
                return;
            }
            String sql = "INSERT INTO codigo_barras(codigo, tipo, producto) VALUES(?, ?, ?)";

            String tipo = getFormatoSeleccionado();
            try (Connection conn = Conexion.getConnection();
                 PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pst.setString(1, txtCodigo.getText());
                pst.setString(2, tipo);
                pst.setString(3, txtProducto.getText());

                int filas = pst.executeUpdate();

                if (filas > 0) {
                    try (ResultSet rs = pst.getGeneratedKeys()) {
                        if (rs.next()) {
                            CodigoBarra cb = new CodigoBarra(txtCodigo.getText(), txtProducto.getText());
                            cb.setId(rs.getInt(1));
                            cb.setTipo(tipo);
                            barraObservableList.add(cb);
                        }
                    }
                }

            } catch (SQLException e) {
                System.out.printf("eroro lol "+e.getMessage());
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Los campos son requeridos (codigo, producto)");
            alert.setContentText(null);
            alert.show();
        }
        limpiar();
    }

    @FXML
    private void onFiltrarCodeBar(KeyEvent event) {

        if (!txtBuscar.getText().isEmpty()) {
            codigoBarraFilteredList.setPredicate(codigoBarra -> (codigoBarra.getCodigo().toLowerCase().contains(txtBuscar.getText().toLowerCase()) || codigoBarra.getProducto().toLowerCase().contains(txtBuscar.getText().toLowerCase())));
            tblCodigoBarras.setItems(codigoBarraFilteredList);
        } else {
            codigoBarraFilteredList.setPredicate((e) -> true);
        }

    }

    private void iniciarTabla() {

        tblCodigoBarras.setItems(barraObservableList);
        tblCodigoBarras.setPlaceholder(new Label("NO EXISTE DATA"));

        tblCodigoBarras.getColumns().forEach(codigoBarraTableColumn -> {
            String columnaNombre = codigoBarraTableColumn.getText();

            switch (columnaNombre) {
                case "CODIGO":
                    codigoBarraTableColumn.setCellValueFactory(new PropertyValueFactory<>("codigo"));
                    break;
                case "PRODUCTO":
                    codigoBarraTableColumn.setCellValueFactory(new PropertyValueFactory<>("producto"));
                    break;
                case "TIPO":
                    codigoBarraTableColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));
                    break;
                case "N#":
                    TableColumn<CodigoBarra, Number> numCol = (TableColumn<CodigoBarra, Number>) codigoBarraTableColumn;
                    numCol.setCellValueFactory(cellData ->
                            new ReadOnlyObjectWrapper<>(tblCodigoBarras.getItems().indexOf(cellData.getValue()) + 1)
                    );
                    numCol.setSortable(false);
                    break;
                default:
                    break;
            }
        });

        String sql = "SELECT id, codigo, tipo, producto FROM codigo_barras";

        try (Statement stmt = Conexion.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Integer id = rs.getInt("id");
                String codigo = rs.getString("codigo");
                String producto = rs.getString("producto");
                String tipo = rs.getString("tipo");

                CodigoBarra cb = new CodigoBarra(codigo, producto);
                cb.setId(id);
                cb.setTipo(tipo);
                barraObservableList.add(cb);
            }
        } catch (SQLException e) {
            System.out.println("guardando data");
            e.printStackTrace();
        }

    }

    private void agregarContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemMostrarReporte = new MenuItem("Imprimir fila");
        MenuItem itemEliminar = new MenuItem("Eliminar fila");

        itemMostrarReporte.setOnAction(event -> {
            CodigoBarra seleccionado = tblCodigoBarras.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                System.out.println("Mostrando reporte de: " + seleccionado.getProducto());
                //crearCodigoBarras(seleccionado.getCodigo(), "codigo_" + seleccionado.getCodigo(), getFormatoSeleccionado());
                String size = rdb80mm.isSelected() ? "80mm" : "58mm";
                crearCodigosBarras(txtCodigo.getText().trim(), txtProducto.getText(), getFormatoSeleccionado(), size);
            }
        });

        itemEliminar.setOnAction(event -> {
            ObservableList<CodigoBarra> seleccionados = tblCodigoBarras.getSelectionModel().getSelectedItems();

            if (!seleccionados.isEmpty()) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmar eliminación");
                confirm.setHeaderText("¿Deseas eliminar el/los elementos seleccionados?");
                confirm.setContentText("Esta acción no se puede deshacer.");

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try (Connection cn = Conexion.getConnection()) {
                            String sql = "DELETE FROM codigo_barras WHERE id = ?";
                            try (PreparedStatement pst = cn.prepareStatement(sql)) {
                                for (CodigoBarra cb : seleccionados) {
                                    pst.setInt(1, cb.getId());
                                    pst.addBatch();
                                }
                                pst.executeBatch();
                            }
                            // Quitar del observable list (UI)
                            tblCodigoBarras.getItems().removeAll(seleccionados);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            Alert error = new Alert(Alert.AlertType.ERROR);
                            error.setTitle("Error");
                            error.setHeaderText("No se pudo eliminar");
                            error.setContentText(e.getMessage());
                            error.show();
                        }
                    }
                });
            } else {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle("Advertencia");
                alerta.setHeaderText("No hay selección");
                alerta.setContentText("Selecciona al menos un elemento para eliminar.");
                alerta.show();
            }
        });

        contextMenu.getItems().addAll(itemMostrarReporte, itemEliminar);

        tblCodigoBarras.setContextMenu(contextMenu);
    }

    private int[] getTamanoSeleccionado() {
        RadioButton seleccionado = (RadioButton) groupTamano.getSelectedToggle();
        if (seleccionado == rdb80mm) return new int[]{80, 50}; // ancho x alto en mm
        else return new int[]{50, 30};
    }

    private boolean validarFormato() {
        String barcodeFormato = getFormatoSeleccionado();
        String codigo = txtCodigo.getText().trim();

        return switch (barcodeFormato) {
            case "codabar" -> codigo.length() >= 4 && codigo.length() <= 20 &&
                    codigo.matches("[0-9\\-\\:\\$/\\.\\+ABCD]+");
            case "EAN13" -> codigo.length() == 13 && codigo.matches("\\d+"); // solo num

            case "code128" -> !codigo.isEmpty() && codigo.length() <= 80; // cualquier caracter

            case "utca" -> codigo.length() == 12 && codigo.matches("\\d+"); // 12 digi

            default -> false;
        };
    }

    private String getFormatoSeleccionado() {
        RadioButton seleccionado = (RadioButton) groupTipoCodi.getSelectedToggle();
        if (seleccionado == rdbCodabar) return ETipoCode.CODABAR.name().toLowerCase();
        else if (seleccionado == rdbEAN13) return ETipoCode.EAN13.name().toLowerCase();
        else if (seleccionado == rdbUtc) return ETipoCode.UTCA.name().toLowerCase();
        else return ETipoCode.CODE128.name().toLowerCase();
    }

    //region Zxing
    /*
    public void crearCodigoBarra(String codigoTexto, String nombreArchivo, BarcodeFormat tipoFormato) throws WriterException, IOException {
        int dpi = 300;
        int[] dimension = getTamanoSeleccionado();
        int ancho = (int) ((dimension[0] / 25.4) * dpi);
        int alto = (int) ((dimension[1] / 25.4) * dpi);

        //MatrixToImageConfig config = new MatrixToImageConfig(colorCodigo, colorFondo);
        BitMatrix bitMatrix = new MultiFormatWriter().encode(codigoTexto, tipoFormato, ancho, alto);
        BitMatrix bitMatrix1 = new MultiFormatWriter().encode(codigoTexto, BarcodeFormat.CODE_128, ancho, alto);

        Path ruta = Paths.get("codigos_generados", nombreArchivo + ".png");
        File directorio = ruta.getParent().toFile();
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", ruta);

        System.out.println("✅ Código guardado en: " + ruta.toAbsolutePath());
        limpiar();
    }*/
    //endregion

    //region Zxing
    /*
    public void crearCodigoBarras(String codigoTexto, String nombreArchivo, BarcodeFormat tipoFormato) throws WriterException, IOException {
        int dpi = 300;
        int[] dimension = getTamanoSeleccionado();
        int ancho = (int) ((dimension[0] / 25.4) * dpi);
        int alto = (int) ((dimension[1] / 25.4) * dpi);


        BitMatrix bitMatrix = new MultiFormatWriter().encode(codigoTexto, tipoFormato, ancho, alto);
        BufferedImage barcodeImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

        int textoAltura = 30;
        BufferedImage combined = new BufferedImage(barcodeImage.getWidth(), barcodeImage.getHeight() + textoAltura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = combined.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, combined.getWidth(), combined.getHeight());

        g.drawImage(barcodeImage, 0, 0, null);

        g.setColor(Color.BLACK);
        int fontSize = alto / 10;
        g.setFont(new Font("Arial", Font.PLAIN, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int x = (combined.getWidth() - fm.stringWidth(codigoTexto)) / 2;
        int y = barcodeImage.getHeight() + ((textoAltura + fm.getAscent()) / 2) - 4;
        g.drawString(codigoTexto, x, y);

        g.dispose();

        Path ruta = Paths.get("codigos_generados", nombreArchivo + ".png");
        File directorio = ruta.getParent().toFile();
        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        ImageIO.write(combined, "PNG", ruta.toFile());

        System.out.println("✅ Código con texto guardado en: " + ruta.toAbsolutePath());
        limpiar();
    }
*/
//endregion

    private void crearCodigosBarras(String codigo, String producto, String tipoFormato, String size) {
        Connection cn = null;
        try {
            String path = "/reportes/" + size + tipoFormato + ".jasper";
            System.out.println("Ruta del reporte: " + path);
            URL url = getClass().getResource(path);
            if (url == null) {
                throw new RuntimeException("No se encontró el archivo del reporte: " + path);
            }

            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(url);
            cn = Conexion.getConnection();

            Map<String, Object> parametro = new HashMap<>();
            parametro.put("codigo", codigo);
            parametro.put("producto", producto);

            if (rdbPrevisualizarSi.isSelected()) {
                JasperPrint imprimir = JasperFillManager.fillReport(jasperReport, parametro, cn);
                JasperViewer vista = new JasperViewer(imprimir, false);
                vista.setTitle("Codigo Barra Visualizador");
                vista.setVisible(true);
                vista.toFront();
            } else {
                JasperPrint imprimir = JasperFillManager.fillReport(jasperReport, parametro, cn);

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setHeaderText("¿Deseas imprimir directamente el código de barras?");
                confirm.setContentText("Se enviará a la impresora predeterminada sin mostrar diálogo.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            // Imprime directamente sin mostrar diálogo de impresión
                            JasperPrintManager.printReport(imprimir, false);
                        } catch (JRException e) {
                            e.printStackTrace();
                            Alert error = new Alert(Alert.AlertType.ERROR);
                            error.setHeaderText("Error al imprimir");
                            error.setContentText(e.getMessage());
                            error.show();
                        }
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void limpiar() {
        txtProducto.clear();
        txtCodigo.clear();
        tblCodigoBarras.getSelectionModel().clearSelection();
    }

    private boolean validarCampos() {
        return !txtCodigo.getText().isBlank() && !txtProducto.getText().isBlank();
    }

}