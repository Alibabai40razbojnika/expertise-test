package com.expertise.filip.controller;

import com.expertise.filip.dto.TractorFormatedDto;
import com.expertise.filip.wrapper.BigQueryWrapper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/query")
public class RestDataController {

    private static final Logger LOGGER = Logger.getLogger(RestDataController.class.getName());

    @Autowired
    private BigQueryWrapper bigQueryWrapper;

    /**
     * Uri with two parameters (regex limited) to get back information about tractor with serialNumber for single date
     * 
     * @param serialNumber
     * @param dateString
     * @return Tractor data for concrete date
     */
    @GetMapping("/select/serialNumber/{serialNumber:^A[0-9]+$}/date/{date:^\\d{2}\\.\\d{2}\\.\\d{4}$}")
    public ResponseEntity getSelectQueryResult(@PathVariable("serialNumber") String serialNumber, @PathVariable("date") String dateString) {
        LOGGER.log(Level.INFO, "Values passed: Serial number: {0} data: {1}", new Object[]{serialNumber, dateString});
        Date date;
        SimpleDateFormat dateFormatInput = new SimpleDateFormat("dd.mm.yyyy");
        SimpleDateFormat dateFormatOutput = new SimpleDateFormat("yyyy-mm-dd");
        try {
            date = dateFormatInput.parse(dateString);
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Date is not correct.", ex);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Date is not in a right format.");
        }

        List<TractorFormatedDto> list = bigQueryWrapper.runSelectQuery(serialNumber, dateFormatOutput.format(date));
        if (list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(list.get(0));
        }
    }
    
}
