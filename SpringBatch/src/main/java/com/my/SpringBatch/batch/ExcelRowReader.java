package com.my.SpringBatch.batch;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.batch.item.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class ExcelRowReader implements ItemStreamReader<Row> {
    private final String filePath;
    private FileInputStream fileInputStream;
    private Workbook workbook;
    private Iterator<Row> rowCursor;
    private int currentRowNumber;
    private final String CURRENT_ROW_KEY = "current.row.number";
    public ExcelRowReader(String filePath) throws IOException {
        this.filePath = filePath;
        this.currentRowNumber = 0;
    }
    @Override
    public Row read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if(rowCursor != null && rowCursor.hasNext()){
            currentRowNumber++;
            return rowCursor.next();
        }else{
            return null;
        }
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try{
            fileInputStream = new FileInputStream(filePath);
            workbook = WorkbookFactory.create(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            this.rowCursor = sheet.iterator();

            if(executionContext.containsKey(CURRENT_ROW_KEY)){
                currentRowNumber = executionContext.getInt(CURRENT_ROW_KEY);
            }

            for(int i=0;i<currentRowNumber && rowCursor.hasNext();i++){
                rowCursor.next();
            }
        } catch(IOException e){
            throw new ItemStreamException(e);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(CURRENT_ROW_KEY, currentRowNumber);
    }

    @Override
    public void close() throws ItemStreamException {
        try{
            if(workbook != null){
                workbook.close();
            }
            if(fileInputStream != null){
                fileInputStream.close();
            }
        } catch(IOException e){
            throw new ItemStreamException(e);
        }
    }
}
