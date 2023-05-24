package by.kaminsky.service;

import by.kaminsky.dto.EstimateDataDto;
import by.kaminsky.dto.MaterialDto;
import by.kaminsky.dto.WorkDto;
import by.kaminsky.enums.SourceCompanies;
import by.kaminsky.exchangeRate.BynExchangeRate;
import by.kaminsky.util.CellStylesHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
@Service
@Slf4j
@RequiredArgsConstructor
public class EstimateCreationServiceImpl implements EstimateCreationService {

    private final CellStylesHelper styles;
    public static final Path NEW_ESTIMATE_PATH = Paths.get("dispatcher" + File.separator + "src" + File.separator +
            "main" + File.separator + "resources").toAbsolutePath();
    public static final String SAMPLE = Paths.get("dispatcher" + File.separator + "src" + File.separator +
            "main" + File.separator + "resources" + File.separator + "basis.xlsx").toAbsolutePath().toString();
    private static final int START_WORK_TABLE = 9;
    private static final int ADD_EMPTY_ROWS_WORKS = 5;
    private static final int ADD_EMPTY_ROWS_MATERIALS = 5;
    private static final int ADD_EMPTY_ROWS_TRANSPORT = 2;

    @Override
    public String createEstimate(EstimateDataDto estimateData) {
        String OutputPath = NEW_ESTIMATE_PATH + File.separator + "Смета Дымоход Прометей от "
                + LocalDateTime.now().toLocalDate() + ".xlsx";
        log.info("Start to design an estimate : {}", OutputPath);
        try (var inputExample = new FileInputStream(SAMPLE);
             var outputEstimate = new FileOutputStream(OutputPath)) {
            Workbook book = new XSSFWorkbook(inputExample);
            Sheet sheet = book.getSheet("Смета");
            sheet.getRow(0).getCell(0).setCellValue("Приложение №1 " +
                    "\"Ведомость объемов и стоимости работ\" к договору подряда №_____  от \"     \"__________ " +
                    LocalDateTime.now().getYear() + " г.");
            sheet.getRow(7).getCell(0).setCellValue("Установка дымоходной системы \"Прометей\"");
            val styleMap = createStyleMap(book);

            createSectionTittle(sheet, styleMap.get("styleSectionTittle"), 8, "ПЕРЕЧЕНЬ ОСНОВНЫХ РАБОТ");
            generateWorkBlock(sheet, estimateData, styleMap);
            sheet.createRow(START_WORK_TABLE + estimateData.getWorks().size() +
                    ADD_EMPTY_ROWS_WORKS + 2).setHeightInPoints(4);
            createSectionTittle(sheet, styleMap.get("styleSectionTittle"),
                    START_WORK_TABLE + estimateData.getWorks().size() + ADD_EMPTY_ROWS_WORKS + 3, "МАТЕРИАЛЫ");
            val startMaterialsBlock = START_WORK_TABLE + estimateData.getWorks().size() + ADD_EMPTY_ROWS_WORKS + 4;
            generateMaterialsBlock(sheet, estimateData, startMaterialsBlock, styleMap);
            sheet.createRow(startMaterialsBlock + estimateData.getMaterials().size() +
                    ADD_EMPTY_ROWS_MATERIALS + 2).setHeightInPoints(4);
            createSectionTittle(sheet, styleMap.get("styleSectionTittle"),
                    startMaterialsBlock + estimateData.getMaterials().size() + ADD_EMPTY_ROWS_MATERIALS + 3,
                    "ОБОРУДОВАНИЕ И ТРАНСПОРТ");
            val startTransportBlock = startMaterialsBlock + estimateData.getMaterials().size() +
                    ADD_EMPTY_ROWS_MATERIALS + 4;
            generateTransportBlock(sheet, startTransportBlock, styleMap);
            sheet.createRow(startTransportBlock + ADD_EMPTY_ROWS_TRANSPORT + 5).setHeightInPoints(4);
            createSectionTittle(sheet, styleMap.get("styleTotalSection"), startTransportBlock +
                    ADD_EMPTY_ROWS_TRANSPORT + 6, "ИТОГИ (Руб)");
            generateTotalBlock(sheet, estimateData, startTransportBlock + ADD_EMPTY_ROWS_TRANSPORT + 7, styleMap);
            book.write(outputEstimate);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            try {
                throw new RuntimeException(e);
            } catch (RuntimeException exception) {
                emergencyFileDeletion(OutputPath);
                return null;
            }
        } catch (RuntimeException e) {
            log.error("Undefended Error: " + e.getMessage());
            emergencyFileDeletion(OutputPath);
            return null;
        }
        return OutputPath;
    }

    private void createSectionTittle(Sheet sheet, CellStyle style, int index, String text) {
        val row = sheet.createRow(index);
        for (var i = 0; i < 6; i++) {
            row.createCell(i).setCellStyle(style);
        }
        sheet.addMergedRegion(new CellRangeAddress(index, index, 0, 5));
        row.getCell(0).setCellValue(text);
    }

    private void generateWorkBlock(Sheet sheet, EstimateDataDto estimateData, Map<String, CellStyle> styleMap) {
        log.debug("Start to design works block");
        val works = estimateData.getWorks().entrySet().stream().toList();
        val worksTableSize = estimateData.getWorks().size() + ADD_EMPTY_ROWS_WORKS + 1;
        for (var i = 0; i < worksTableSize; i++) {
            val row = sheet.createRow(i + START_WORK_TABLE);
            row.setHeightInPoints(13);
            val cellNumber = row.createCell(0);
            val cellDescription = row.createCell(1);
            val cellCost = row.createCell(5);
            if (i == 0) {
                cellNumber.setCellValue("№");
                cellDescription.setCellValue("Описание");
                cellCost.setCellValue("Сумма, руб");
                cellDescription.setCellStyle(styleMap.get("styleHeaderCell"));
                cellNumber.setCellStyle(styleMap.get("styleHeaderCell"));
                cellCost.setCellStyle(styleMap.get("styleHeaderCell"));
                row.createCell(2).setCellStyle(styleMap.get("styleHeaderCell"));
                row.createCell(3).setCellStyle(styleMap.get("styleHeaderCell"));
                row.createCell(4).setCellStyle(styleMap.get("styleHeaderCell"));
            } else {
                if (i >= worksTableSize - ADD_EMPTY_ROWS_WORKS) {
                    cellNumber.setCellValue("");
                    cellDescription.setCellValue("");
                    cellCost.setCellValue("");
                } else {
                    cellNumber.setCellValue(i);
                    val work = works.get(i - 1);
                    cellDescription.setCellValue(work.getKey().getPackaging().equals("") ? work.getKey().getName() :
                            work.getKey().getName() + " - " + work.getValue() + " " + work.getKey().getPackaging());
                    cellDescription.setCellStyle(styleMap.get("styleDescriptionCell"));
                    cellCost.setCellValue(work.getKey().getCost().doubleValue() * work.getValue());
                }
                cellDescription.setCellStyle(styleMap.get("styleDescriptionCell"));
                cellNumber.setCellStyle(styleMap.get("styleNumberCell"));
                cellCost.setCellStyle(styleMap.get("stylePriceCell"));
                row.createCell(2).setCellStyle(styleMap.get("styleDescriptionCell"));
                row.createCell(3).setCellStyle(styleMap.get("styleDescriptionCell"));
                row.createCell(4).setCellStyle(styleMap.get("styleDescriptionCell"));
            }
            sheet.addMergedRegion(new CellRangeAddress(i + START_WORK_TABLE, i + START_WORK_TABLE, 1, 4));
        }
        val total = sheet.createRow(worksTableSize + START_WORK_TABLE);
        for (var i = 0; i < 6; i++) {
            total.createCell(i).setCellStyle(i == 5 ? styleMap.get("styleTotalPriceCell") : styleMap.get("styleTotalCell"));
        }
        sheet.addMergedRegion(new CellRangeAddress(START_WORK_TABLE + worksTableSize,
                START_WORK_TABLE + worksTableSize, 0, 4));
        total.getCell(0).setCellValue("ИТОГО по работам");
        total.getCell(5).setCellFormula("SUM(F" + (START_WORK_TABLE + 1) + ":F" +
                (START_WORK_TABLE + worksTableSize) + ")");
    }

    private void generateMaterialsBlock(Sheet sheet, EstimateDataDto estimateData, int startMaterialsBlock,
                                        Map<String, CellStyle> styleMap) {
        log.debug("Start to design materials block");
        val materials = estimateData.getMaterials().entrySet().stream().toList();
        val materialsTableSize = estimateData.getMaterials().size() + ADD_EMPTY_ROWS_MATERIALS + 1;
        for (var i = 0; i < materialsTableSize; i++) {
            val row = sheet.createRow(i + startMaterialsBlock);
            row.setHeightInPoints(13);
            val cellNumber = row.createCell(0);
            val cellDescription = row.createCell(1);
            val cellUnit = row.createCell(2);
            val cellQuantity = row.createCell(3);
            val cellCost = row.createCell(4);
            val cellAmount = row.createCell(5);
            if (i == 0) {
                cellNumber.setCellValue("№");
                cellDescription.setCellValue("Описание");
                cellUnit.setCellValue("ед. изм");
                cellQuantity.setCellValue("кол-во");
                cellCost.setCellValue("Цена");
                cellAmount.setCellValue("Сумма, руб");
                cellNumber.setCellStyle(styleMap.get("styleHeaderCell"));
                cellDescription.setCellStyle(styleMap.get("styleHeaderCell"));
                cellUnit.setCellStyle(styleMap.get("styleHeaderCell"));
                cellQuantity.setCellStyle(styleMap.get("styleHeaderCell"));
                cellCost.setCellStyle(styleMap.get("styleHeaderCell"));
                cellAmount.setCellStyle(styleMap.get("styleHeaderCell"));
            } else {
                if (i >= materialsTableSize - ADD_EMPTY_ROWS_MATERIALS) {
                    cellNumber.setCellValue("");
                    cellDescription.setCellValue("");
                    cellUnit.setCellValue("");
                    cellQuantity.setCellValue(0);
                    cellCost.setCellValue(0);
                } else {
                    cellNumber.setCellValue(i);
                    val material = materials.get(i - 1);
                    cellDescription.setCellValue(material.getKey().getSpecific());
                    cellUnit.setCellValue(material.getKey().getPackaging());
                    cellQuantity.setCellValue(material.getValue());
                    cellCost.setCellValue(material.getKey().getCost().doubleValue());
                }
                cellAmount.setCellFormula("E" + (startMaterialsBlock + i + 1) + "*D" + (startMaterialsBlock + i + 1));
                cellNumber.setCellStyle(styleMap.get("styleNumberCell"));
                cellDescription.setCellStyle(styleMap.get("styleDescriptionCell"));
                cellUnit.setCellStyle(styleMap.get("styleDescriptionCell"));
                cellQuantity.setCellStyle(styleMap.get("stylePriceCell"));
                cellCost.setCellStyle(styleMap.get("stylePriceCell"));
                cellAmount.setCellStyle(styleMap.get("stylePriceCell"));
            }
        }
        val total = sheet.createRow(materialsTableSize + startMaterialsBlock);
        for (var i = 0; i < 6; i++) {
            total.createCell(i).setCellStyle(i == 5 ? styleMap.get("styleTotalPriceCell") : styleMap.get("styleTotalCell"));
        }
        sheet.addMergedRegion(new CellRangeAddress(startMaterialsBlock + materialsTableSize,
                startMaterialsBlock + materialsTableSize, 0, 4));
        total.getCell(0).setCellValue("ИТОГО по материалам");
        total.getCell(5).setCellFormula("SUM(F" + (startMaterialsBlock + 1) + ":F"
                + (startMaterialsBlock + materialsTableSize) + ")");
    }

    private void generateTransportBlock(Sheet sheet, int startTransportBlock, Map<String, CellStyle> styleMap) {
        log.debug("Start to design transport block");
        String[] positions = {"Доставка стройматериалов", "Строительные леса", "Автовышка"};
        val tableSize = ADD_EMPTY_ROWS_TRANSPORT + 4;
        for (var i = 0; i < tableSize; i++) {
            val row = sheet.createRow(i + startTransportBlock);
            row.setHeightInPoints(13);
            val cellNumber = row.createCell(0);
            val cellDescription = row.createCell(1);
            val cellUnit = row.createCell(2);
            val cellQuantity = row.createCell(3);
            val cellCost = row.createCell(4);
            val cellAmount = row.createCell(5);
            if (i == 0) {
                cellNumber.setCellValue("№");
                cellDescription.setCellValue("Описание");
                cellUnit.setCellValue("ед. изм");
                cellQuantity.setCellValue("кол-во");
                cellCost.setCellValue("Цена");
                cellAmount.setCellValue("Сумма, руб");
                cellNumber.setCellStyle(styleMap.get("styleHeaderCell"));
                cellDescription.setCellStyle(styleMap.get("styleHeaderCell"));
                cellUnit.setCellStyle(styleMap.get("styleHeaderCell"));
                cellQuantity.setCellStyle(styleMap.get("styleHeaderCell"));
                cellCost.setCellStyle(styleMap.get("styleHeaderCell"));
                cellAmount.setCellStyle(styleMap.get("styleHeaderCell"));
            } else {
                if (i >= tableSize - ADD_EMPTY_ROWS_TRANSPORT) {
                    cellNumber.setCellValue("");
                    cellDescription.setCellValue("");
                    cellUnit.setCellValue("");
                    cellQuantity.setCellValue(0);
                } else {
                    cellNumber.setCellValue(i);
                    cellDescription.setCellValue(positions[i - 1]);
                    cellUnit.setCellValue("раз.");
                    cellQuantity.setCellValue(1);
                }
                cellCost.setCellValue(0);
                cellAmount.setCellFormula("E" + (startTransportBlock + i + 1) + "*D" + (startTransportBlock + i + 1));
                cellNumber.setCellStyle(styleMap.get("styleNumberCell"));
                cellDescription.setCellStyle(styleMap.get("styleDescriptionCell"));
                cellUnit.setCellStyle(styleMap.get("styleDescriptionCell"));
                cellQuantity.setCellStyle(styleMap.get("stylePriceCell"));
                cellCost.setCellStyle(styleMap.get("stylePriceCell"));
                cellAmount.setCellStyle(styleMap.get("stylePriceCell"));
            }
        }
        val total = sheet.createRow(tableSize + startTransportBlock);
        for (var i = 0; i < 6; i++) {
            total.createCell(i)
                    .setCellStyle(i == 5 ? styleMap.get("styleTotalPriceCell") : styleMap.get("styleTotalCell"));
        }
        sheet.addMergedRegion(new CellRangeAddress(startTransportBlock + tableSize,
                startTransportBlock + tableSize, 0, 4));
        total.getCell(0).setCellValue("ИТОГО по оборудованию и транспорту");
        total.getCell(5)
                .setCellFormula("SUM(F" + (startTransportBlock + 1) + ":F" + (startTransportBlock + tableSize) + ")");
    }

    private void generateTotalBlock(Sheet sheet, EstimateDataDto estimateData, int startTotalBlock,
                                    Map<String, CellStyle> styleMap) {
        log.debug("Start to design total block");
        var allBlockRows = new ArrayList<Row>();
        for (var i = 0; i < 8; i++) {
            allBlockRows.add(sheet.createRow(startTotalBlock + i));
        }
        for (var i = 0; i < 8; i++) {
            for (var j = 0; j < 5; j++) {
                if (i == 7) {
                    allBlockRows.get(i).createCell(j).setCellStyle(styleMap.get("styleEndTotalCell"));
                } else if (i < 3) {
                    allBlockRows.get(i).createCell(j).setCellStyle(styleMap.get("styleTotalBlockCell"));
                } else {
                    allBlockRows.get(i).createCell(j).
                            setCellStyle(styleMap.get(j == 4 ? "stylePrepaymentPercent" : "stylePrepaymentBlock"));
                }
            }
            if (i != 7) {
                allBlockRows.get(i).createCell(5)
                        .setCellStyle(styleMap.get(i == 2 ? "styleTotalEstimateSum" : "styleTotalPriceCell"));
                sheet.addMergedRegion(new CellRangeAddress(startTotalBlock + i, startTotalBlock + i, 0, i < 3 ? 3 : 2));
            }
        }
        for (var i = 0; i < 3; i++) {
            allBlockRows.get(i).getCell(4).setCellValue("руб.");
        }
        final int worksCostCellIdx = START_WORK_TABLE + estimateData.getWorks().size() + ADD_EMPTY_ROWS_WORKS + 2;
        final int materialsCostCellIdx = worksCostCellIdx + estimateData.getMaterials().size() +
                ADD_EMPTY_ROWS_MATERIALS + 4;
        final int transportCostCellIdx = materialsCostCellIdx + ADD_EMPTY_ROWS_WORKS + 4;
        allBlockRows.get(0).getCell(0).setCellValue("Общая стоимость оборудования, материалов и транспорта");
        val materialsAndTransportTotal = allBlockRows.get(0).getCell(5);
        materialsAndTransportTotal.setCellFormula("F" + materialsCostCellIdx + "+F" + transportCostCellIdx);
        allBlockRows.get(1).getCell(0).setCellValue("Общая стоимость всех работ");
        val worksTotal = allBlockRows.get(1).getCell(5);
        worksTotal.setCellFormula("F" + worksCostCellIdx);
        allBlockRows.get(2).getCell(0).setCellValue("Итого по смете:");
        val allTotal = allBlockRows.get(2).getCell(5);
        allTotal.setCellFormula("F" + (materialsAndTransportTotal.getRowIndex() + 1) + "+F" + (worksTotal.getRowIndex() + 1));
        allBlockRows.get(3).getCell(0).setCellValue("Аванс на работу:");
        allBlockRows.get(3).getCell(3).setCellValue(20);
        allBlockRows.get(3).getCell(4).setCellValue("%");
        val worksPrepayment = allBlockRows.get(3).getCell(5);
        worksPrepayment.setCellFormula("F" + (worksTotal.getRowIndex() + 1) + "/100*D" + (worksPrepayment.getRowIndex() + 1));
        allBlockRows.get(4).getCell(0).setCellValue("Аванс на материалы:");
        allBlockRows.get(4).getCell(3).setCellValue(100);
        allBlockRows.get(4).getCell(4).setCellValue("%");
        val materialsPrepayment = allBlockRows.get(4).getCell(5);
        materialsPrepayment.setCellFormula("F" + (materialsAndTransportTotal.getRowIndex() + 1) + "/100*D"
                + (materialsPrepayment.getRowIndex() + 1));
        allBlockRows.get(5).getCell(0).setCellValue("Аванс Итого:");
        val totalPrepayment = allBlockRows.get(5).getCell(5);
        totalPrepayment.setCellFormula("F" + (worksPrepayment.getRowIndex() + 1) + "+F"
                + (materialsPrepayment.getRowIndex() + 1));
        allBlockRows.get(6).getCell(0).setCellValue("Остаток на окончательный расчет:");
        allBlockRows.get(6).getCell(5).setCellFormula("F" + (allTotal.getRowIndex() + 1)
                + "-F" + (totalPrepayment.getRowIndex() + 1));
    }

    private Map<String, CellStyle> createStyleMap(Workbook book) {
        Map<String, CellStyle> styleMap = new HashMap<>();
        styleMap.put("styleDescriptionCell", styles.setStyleTableCell(book.createCellStyle(), HorizontalAlignment.LEFT));
        styleMap.put("styleNumberCell", styles.setStyleTableCell(book.createCellStyle(), HorizontalAlignment.CENTER));
        styleMap.put("stylePriceCell", styles.setStyleTableCell(book.createCellStyle(), HorizontalAlignment.RIGHT));
        styleMap.put("styleTotalCell", styles.setStyleTotalCell(book.createCellStyle(), book.createFont(),
                HorizontalAlignment.CENTER));
        styleMap.put("styleTotalPriceCell", styles.setStyleTotalCell(book.createCellStyle(), book.createFont(),
                HorizontalAlignment.RIGHT));
        styleMap.put("styleTotalBlockCell", styles.setStyleTotalCell(book.createCellStyle(), book.createFont(),
                HorizontalAlignment.LEFT));
        styleMap.put("styleHeaderCell", styles.setStyleHeaderCell(book.createCellStyle(), book.createFont()));
        styleMap.put("styleSectionTittle", styles.setStyleSectionTittle(book.createCellStyle(), book.createFont()));
        styleMap.put("styleTotalSection", styles.setStyleTotalSection(book.createCellStyle(), book.createFont()));
        styleMap.put("stylePrepaymentBlock", styles.setStylePrepaymentBlock(book.createCellStyle(), book.createFont()));
        styleMap.put("styleEndTotalCell", styles.setStyleEndTotalTable(book.createCellStyle()));
        styleMap.put("styleTotalEstimateSum", styles.setStyleTotalEstimateSum(book.createCellStyle(), book.createFont()));
        styleMap.put("stylePrepaymentPercent", styles.setStylePrepaymentPercent(book.createCellStyle(), book.createFont()));
        return styleMap;
    }

    private void emergencyFileDeletion(String path) {
        File file = new File(path);
        if (file.delete()) {
            log.warn("File {} emergency deleted successfully", file.getName());
        } else log.warn("File {} could not be deleted", file.getName());
    }

    public EstimateDataDto createTest() {
        var m1 = MaterialDto.builder()
                .name("мат1")
                .specific("Материал1")
                .cost(new BigDecimal("40.6"))
                .packaging("шт.")
                .source(SourceCompanies.KAMINSKY.toString())
                .build();
        var m2 = MaterialDto.builder()
                .name("мат2")
                .specific("Материал2")
                .cost(new BigDecimal("84.54"))
                .packaging("лист")
                .source(SourceCompanies.KAMINSKY.toString())
                .build();
        var m3 = MaterialDto.builder()
                .name("мат3")
                .specific("Материал3")
                .cost(new BigDecimal("1.6"))
                .packaging("м.п")
                .source(SourceCompanies.KAMINSKY.toString())
                .build();
        var w1 = WorkDto.builder()
                .name("работа1")
                .packaging("м.")
                .cost(new BigDecimal("33.3"))
                .build();
        var w2 = WorkDto.builder()
                .name("работа2")
                .packaging("")
                .cost(new BigDecimal("120.5"))
                .build();
        var workList = new HashMap<WorkDto, Integer>();
        workList.put(w1, 8);
        workList.put(w2, 1);
        var materialList = new HashMap<MaterialDto, Integer>();
        materialList.put(m1, 1);
        materialList.put(m2, 4);
        materialList.put(m3, 33);
        var ex = new BynExchangeRate(LocalDate.now(), "USD", 2.91);
        return new EstimateDataDto("Установка дымоходной системы \"Прометей\"", ex, workList, materialList);


    }
}