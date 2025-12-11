package io.github.xivqn.exporters;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelData implements AutoCloseable {

    private final String filePath;

    private final String sheetName;

    private Workbook workbook;

    private FileInputStream fileInputStream;

    public ExcelData(String filePath, String sheetName) throws IOException {
        this.filePath = filePath;
        this.sheetName = sheetName;
        initializeWorksheet();
    }

    private void initializeWorksheet() throws IOException {
        this.fileInputStream = new FileInputStream(filePath);
        this.workbook = new XSSFWorkbook(fileInputStream);
        workbook.setForceFormulaRecalculation(true);
    }

    public void appendRow(Object[] row) throws IOException {
        Sheet sheet = getOrCreateSheet(this.sheetName);

        int lastRow = sheet.getLastRowNum();
        Row newRow = sheet.createRow(lastRow + 1);
        for (int i = 0; i < row.length; i++) {
            Cell cell = newRow.createCell(i);
            if (row[i] instanceof String) {
                cell.setCellValue((String) row[i]);
            } else if (row[i] instanceof Double) {
                cell.setCellValue((Double) row[i]);
            }
            // More types should not be needed for now
        }

        write();
    }

    private Sheet getOrCreateSheet(String sheetName) {
        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
            sheet = workbook.createSheet(sheetName);
        }
        return sheet;
    }

    private void write() throws IOException {
        fileInputStream.close();
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        initializeWorksheet();
    }

    @Override
    public void close() throws Exception {
        workbook.close();
        fileInputStream.close();
    }
}
