package com.nortal.assignment;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Joosep Lall.
 */
@Component
public class ScheduledTasks {
    private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);

    private HttpClient client = HttpClientBuilder.create().build();
    private HttpGet mazePieceRequest = new HttpGet("http://localhost:8080/maze");
    private File mazeMap;
    private XSSFWorkbook workbook;
    private XSSFSheet spreadsheet;

    public ScheduledTasks() {
        this.mazeMap = new File("maze-map.xlsx");
        this.workbook = new XSSFWorkbook();
        this.spreadsheet = workbook.createSheet();
    }

    /**
     * This method should be executed periodically with fixed delay of at least 1 second
     * Spring Scheduler should be used to achieve this
     */
    @Scheduled(fixedRate = 1200)
    public void getCoordinatesCombination() throws IOException {
        HttpResponse response = client.execute(mazePieceRequest);
        InputStream content = response.getEntity().getContent();
        mapCoordinatesToExcel(content, mazeMap);
        content.close();
    }

    /**
     * Parse REST response body and mark cells in provided mazeMap Excel file
     * The algorithm to mark the cells could be following
     * <ol>
     *     <li>Load workbook</li>
     *     <li>For each cell reference in responseBody find cell in sheet (create if missing)</li>
     *     <li>Set cell style obtained from createCellStyle()</li>
     * </ol>
     *
     * @param responseBody - comma separated Excel cell references (A1, B3, V5, etc.)
     *                     References are within A1:Z100
     * @param mazeMap      - xlsx file reference where map should be created
     */
    private void mapCoordinatesToExcel(InputStream responseBody, File mazeMap) throws IOException {
        String responseString = IOUtils.toString(responseBody);
        List<String> items = Arrays.asList(responseString.split(","));
        XSSFCellStyle style = createCellStyle(workbook);

        FileOutputStream out = new FileOutputStream(mazeMap);
        Row row;
        Cell cell;
        for (String loc: items) {
            int rowNum = Integer.parseInt(loc.substring(1))-1;
            if (spreadsheet.getRow(rowNum) == null) {
                row = spreadsheet.createRow(rowNum);
            } else {
                row = spreadsheet.getRow(rowNum);
            }

            if (row.getCell(getCol(loc)) == null) {
                cell = row.createCell(getCol(loc));
            } else {
                cell = row.getCell(getCol(loc));
            }
            setRowHeight(row);
            setColumnWidth(spreadsheet, (short) getCol(loc));
            cell.setCellStyle(style);
        }
        workbook.write(out);
        out.close();

        LOG.info("Writing map piece {} to file {}", responseString, mazeMap.getAbsolutePath());
    }

    /**
     * Cells should be marked with the following style
     */
    private XSSFCellStyle createCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Sets Column to proper width so that maze would look nice. Call whenever seems appropriate.
     */
    private void setColumnWidth(XSSFSheet sheet, short columnIndex) {
        sheet.setColumnWidth(columnIndex, 3 * 256);
    }

    /**
     * Sets Row to proper height so that maze would look nice. Call whenever seems appropriate.
     */
    private void setRowHeight(Row row) {
        row.setHeightInPoints(15);
    }

    private int getCol(String item) {
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char[] alphabet  = str.toCharArray();
        for (int i = 0; i < alphabet.length; i++) {
            if (alphabet[i] == item.charAt(0)) {
                return i;
            }
        }
        return -1;
    }
}
