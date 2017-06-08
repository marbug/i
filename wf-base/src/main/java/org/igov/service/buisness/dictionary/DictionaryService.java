package org.igov.service.buisness.dictionary;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.igov.util.ToolFS;
import static org.igov.io.fs.FileSystemData.getBufferedReader_PatternDictonary;
import static org.igov.io.fs.FileSystemDictonary.VALUES;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Kovilin
 */
@Component("DictionaryService")
@Service
public class DictionaryService {
    
    private static final Logger LOG = LoggerFactory.getLogger(DictionaryService.class);
    
    public Map<String, String> processDictionary(String sPath, String sID_Field, String sValue){
        Map<String, String> values = new HashMap<>();
        
        try{
            BufferedReader oBufferedReader = 
                    new BufferedReader(new InputStreamReader(
                            ToolFS.getInputStream("patterns/dictionary/", sPath + ".csv"), "UTF-8"));
            

            String sLine;
            String sLineColums = oBufferedReader.readLine();
            LOG.info("sLineColums is {}", sLineColums);
            String[] columns = sLineColums.split("\\;");
            int columnNumber = -1;
            
            for(int i = 0; i < columns.length; i++){
                if(columns[i].equals(sID_Field)){
                    columnNumber = i;
                    break;
                }
            }
            
            if(columnNumber == -1){
                throw new RuntimeException("There is no column with name " + sID_Field + " in " + sPath + ".csv");
            }
            
            while ((sLine = oBufferedReader.readLine()) != null) {
                String key = StringUtils.substringBefore(sLine, ";");
                
                if(key.equals(sValue)){
                    String [] columnsValues = sLine.split("\\;");
                    for(int i = 0; i < columnsValues.length; i++){
                        if(i == columnNumber){
                            values.put(key, columnsValues[i]);
                            break;
                        }
                    }
                }
            }

            oBufferedReader.close();
        } catch (Exception e) {
            LOG.warn("Error during loading csv file: {}", e.getMessage());
            LOG.trace("FAIL:", e);
            throw new ExceptionInInitializerError(e);
        }
        
        return values;
    }
    
}
