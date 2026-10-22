package com.joel.lectorcodigo.datos.excel;

import com.joel.lectorcodigo.datos.CodigoBarra;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class ExcelData {
    private FileChooser fileChooser;

    public ExcelData() {
        fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccione su archivo Excel");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivos Excel (*.xlsx)", "*.xlsx")
        );
    }

    public boolean importarData(ObservableList<CodigoBarra> barraObservableList) {
       /* Workbook libro = new XSSFWorkbook(); // .xlsx >= 2007 de exel (actual) poi-ooxml
        //Workbook libro1 = new HSSFWorkbook(); // .xls     >= 1997 hasta 2003  poi
        Sheet hoja = libro.createSheet("nombreHoja excel");
        Row fila = hoja.createRow(0);
        Cell celda = fila.createCell(0);
        celda.setCellValue("infor");
        try {
            OutputStream outputStream = new FileOutputStream("archivoExcelCreado.xlsx");
            libro.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }*/

        File fileExcel = fileChooser.showOpenDialog(null);
        if (fileExcel != null && fileExcel.exists()) {
            try (InputStream inputStream = new FileInputStream(fileExcel);
                 XSSFWorkbook libro = new XSSFWorkbook(inputStream)
            ) {
                XSSFSheet hoja = libro.getSheetAt(0);

                Iterator<Row> filas = hoja.rowIterator();

                if (filas.hasNext()) {
                    filas.next(); // Saltar encabezado
                }

                while (filas.hasNext()) {
                    Row fila = filas.next();

                    CodigoBarra codigoBarra = new CodigoBarra();

                    Cell celdaCodigo = fila.getCell(0);
                    Cell celdaProducto = fila.getCell(1);

                    if (celdaCodigo != null) {
                        if (celdaCodigo.getCellType() == CellType.STRING) {
                            codigoBarra.setCodigo(celdaCodigo.getStringCellValue());
                        } else if (celdaCodigo.getCellType() == CellType.NUMERIC) {
                            codigoBarra.setCodigo(String.valueOf((int) celdaCodigo.getNumericCellValue()));
                        }
                    }

                    if (celdaProducto != null && celdaProducto.getCellType() == CellType.STRING) {
                        codigoBarra.setProducto(celdaProducto.getStringCellValue());
                    }

                    barraObservableList.add(codigoBarra);
                }
                return true;

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

        }

        return false;
    }

}
