package by.kaminsky.service;

import by.kaminsky.dto.EstimateDataDto;
import by.kaminsky.dto.MaterialDto;
import by.kaminsky.dto.WorkDto;
import by.kaminsky.enums.SourceCompanies;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EstimateCreationServiceImpl implements EstimateCreationService {

    public static final Path NEW_ESTIMATE_PATH = Paths.get("dispatcher" + File.separator + "src" + File.separator +
            "main" + File.separator + "resources").toAbsolutePath();
    public static final String SAMPLE = Paths.get("dispatcher" + File.separator + "src" + File.separator +
            "main" + File.separator + "resources" + File.separator + "basis.xlsx").toAbsolutePath().toString();
    private static final int START_WORK_TABLE = 9;

    @Override
    public String createPrometheusEstimate(EstimateDataDto estimateData) {
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
            sheet.createRow(START_WORK_TABLE + estimateData.getWorks().size() + 7).setHeightInPoints(4);
            createSectionTittle(sheet, styleMap.get("styleSectionTittle"),
                    START_WORK_TABLE + estimateData.getWorks().size() + 8, "МАТЕРИАЛЫ");
            val startMaterialsBlock = START_WORK_TABLE + estimateData.getWorks().size() + 9;
            generateMaterialsBlock(sheet, estimateData, startMaterialsBlock, styleMap);
            sheet.createRow(startMaterialsBlock + estimateData.getMaterials().size() + 7).setHeightInPoints(4);
            book.write(outputEstimate);
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
            try {
                throw new RuntimeException(e);
            } catch (RuntimeException exception) {
                return null;
            }
        } catch (RuntimeException e) {
            log.error("Undefended Error: " + e.getMessage());
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
        log.info("Start to design works block");
        val works = estimateData.getWorks().entrySet().stream().toList();
        val worksTableSize = estimateData.getWorks().size() + 6;
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
                if (i >= worksTableSize - 5) {
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
        total.getCell(5).setCellFormula("SUM(F" + (START_WORK_TABLE + 1) + ":F" + (START_WORK_TABLE + worksTableSize) + ")");
    }

    private void generateMaterialsBlock(Sheet sheet, EstimateDataDto estimateData, int startMaterialsBlock,
                                        Map<String, CellStyle> styleMap) {
        log.info("Start to design materials block");
        val materials = estimateData.getMaterials().entrySet().stream().toList();
        val materialsTableSize = estimateData.getMaterials().size() + 6;
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
                if (i >= materialsTableSize - 5) {
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
        total.getCell(5).setCellFormula("SUM(F" + (startMaterialsBlock + 1) + ":F" + (startMaterialsBlock + materialsTableSize) + ")");
    }

    private Map<String, CellStyle> createStyleMap(Workbook book) {
        Map<String, CellStyle> styleMap = new HashMap<>();
        styleMap.put("styleDescriptionCell", createStyleTableCell(book.createCellStyle(), HorizontalAlignment.LEFT));
        styleMap.put("styleNumberCell", createStyleTableCell(book.createCellStyle(), HorizontalAlignment.CENTER));
        styleMap.put("stylePriceCell", createStyleTableCell(book.createCellStyle(), HorizontalAlignment.RIGHT));
        styleMap.put("styleTotalCell", createStyleTotalCell(book.createCellStyle(), book.createFont(),
                HorizontalAlignment.CENTER));
        styleMap.put("styleTotalPriceCell", createStyleTotalCell(book.createCellStyle(), book.createFont(),
                HorizontalAlignment.RIGHT));
        styleMap.put("styleHeaderCell", createStyleHeaderCell(book.createCellStyle(), book.createFont()));
        styleMap.put("styleSectionTittle", createStyleSectionTittle(book.createCellStyle(), book.createFont()));
        return styleMap;
    }

    private void makeBorders(CellStyle cs) {
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);
    }

    private CellStyle createStyleTableCell(CellStyle cs, HorizontalAlignment Alignment) {
        makeBorders(cs);
        cs.setAlignment(Alignment);
        return cs;
    }

    private CellStyle createStyleHeaderCell(CellStyle cs, Font f) {
        makeBorders(cs);
        cs.setAlignment(HorizontalAlignment.CENTER);
        f.setBold(true);
        cs.setFont(f);
        return cs;
    }

    private CellStyle createStyleSectionTittle(CellStyle cs, Font f) {
        makeBorders(cs);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setFillBackgroundColor(IndexedColors.RED.getIndex());
        f.setBold(true);
        f.setFontHeightInPoints((short) 12);
        cs.setFont(f);
        return cs;
    }

    private CellStyle createStyleTotalCell(CellStyle cs, Font f, HorizontalAlignment Alignment) {
        cs.setBorderBottom(BorderStyle.MEDIUM);
        cs.setBorderLeft(BorderStyle.MEDIUM);
        cs.setBorderRight(BorderStyle.MEDIUM);
        cs.setBorderTop(BorderStyle.MEDIUM);
        cs.setAlignment(Alignment);
        f.setBold(true);
        cs.setFont(f);
        return cs;
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
        return new EstimateDataDto("Установка дымоходной системы \"Прометей\"", workList, materialList);


    }
}
