package by.kaminsky.util;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.stereotype.Component;

@Slf4j
@NoArgsConstructor
@Component
public class CellStylesHelper {

    public CellStyle setStyleTotalEstimateSum(CellStyle cs, Font f) {
        setStyleTotalCell(cs, f, HorizontalAlignment.RIGHT);
        f.setFontHeightInPoints((short) 16);
        return cs;
    }

    public CellStyle setStyleEndTotalTable(CellStyle cs) {
        cs.setBorderTop(BorderStyle.MEDIUM);
        return cs;
    }

    public CellStyle setStyleTableCell(CellStyle cs, HorizontalAlignment Alignment) {
        makeBorders(cs);
        cs.setAlignment(Alignment);
        return cs;
    }

    public CellStyle setStyleHeaderCell(CellStyle cs, Font f) {
        makeBorders(cs);
        cs.setAlignment(HorizontalAlignment.CENTER);
        f.setBold(true);
        cs.setFont(f);
        return cs;
    }

    public CellStyle setStyleSectionTittle(CellStyle cs, Font f) {
        makeBorders(cs);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        byte[] rgb = new byte[]{(byte) 240, (byte) 200, (byte) 160};
        XSSFColor color = new XSSFColor(rgb);
        cs.setFillForegroundColor(color);
        f.setBold(true);
        f.setFontHeightInPoints((short) 12);
        cs.setFont(f);
        return cs;
    }

    public CellStyle setStylePrepaymentBlock(CellStyle cs, Font f) {
        cs.setAlignment(HorizontalAlignment.RIGHT);
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        byte[] rgb = new byte[]{(byte) 190, (byte) 190, (byte) 190};
        XSSFColor color = new XSSFColor(rgb);
        cs.setFillForegroundColor(color);
        f.setBold(true);
        f.setFontHeightInPoints((short) 12);
        cs.setFont(f);
        return cs;
    }

    public CellStyle setStylePrepaymentPercent(CellStyle cs, Font f) {
        setStylePrepaymentBlock(cs, f);
        cs.setAlignment(HorizontalAlignment.LEFT);
        return cs;
    }


    public CellStyle setStyleTotalSection(CellStyle cs, Font f) {
        cs.setBorderBottom(BorderStyle.MEDIUM);
        cs.setBorderLeft(BorderStyle.MEDIUM);
        cs.setBorderRight(BorderStyle.MEDIUM);
        cs.setBorderTop(BorderStyle.MEDIUM);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        byte[] rgb = new byte[]{(byte) 150, (byte) 200, (byte) 250};
        XSSFColor color = new XSSFColor(rgb);
        cs.setFillForegroundColor(color);
        f.setBold(true);
        f.setItalic(true);
        f.setFontHeightInPoints((short) 14);
        cs.setFont(f);
        return cs;
    }

    public CellStyle setStyleTotalCell(CellStyle cs, Font f, HorizontalAlignment Alignment) {
        cs.setBorderBottom(BorderStyle.MEDIUM);
        cs.setBorderLeft(BorderStyle.MEDIUM);
        cs.setBorderRight(BorderStyle.MEDIUM);
        cs.setBorderTop(BorderStyle.MEDIUM);
        cs.setAlignment(Alignment);
        f.setBold(true);
        cs.setFont(f);
        return cs;
    }


    private void makeBorders(CellStyle cs) {
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);
    }
}
