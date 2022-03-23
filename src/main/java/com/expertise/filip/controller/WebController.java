package com.expertise.filip.controller;

import com.expertise.filip.dto.TractorFormatedDto;
import com.expertise.filip.util.CustomProperties;
import com.expertise.filip.util.TractorForm;
import com.expertise.filip.wrapper.BigQueryWrapper;
import com.expertise.filip.wrapper.TractorWrapper;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebController {

    private static final Logger LOGGER = Logger.getLogger(WebController.class.getName());
    private static final SimpleDateFormat DATE_FORMAT_INPUT = new SimpleDateFormat("dd.mm.yyyy");
    private static final SimpleDateFormat DATE_FORMAT_OUTPUT = new SimpleDateFormat("yyyy-mm-dd");

    @Autowired
    private CustomProperties customProperties;
    
    @Autowired
    private BigQueryWrapper bigQueryWrapper;

    @GetMapping("/tractor")
    public String tractorForm(Model model) {
        model.addAttribute("tractorForm", new TractorForm());
        return "tractorForm";
    }

    /**
     * Form for getting information about tractor with serialNumber for single date
     * 
     * @param tractorForm
     * @param model
     * @return information about tractor details
     */
    @PostMapping("/tractor")
    public String tractorData(@ModelAttribute TractorForm tractorForm,
            Model model) {
        LOGGER.info("Values passed: Serial number: " + tractorForm.getSerialNumber() + " data: " + tractorForm.getDate());
        Date date = null;
        TractorWrapper tractorWrapper;
        try {
            date = DATE_FORMAT_INPUT.parse(tractorForm.getDate());
            List<TractorFormatedDto> list = bigQueryWrapper.runSelectQuery(tractorForm.getSerialNumber(), DATE_FORMAT_OUTPUT.format(date));
            if (list.isEmpty()) {
                tractorWrapper = new TractorWrapper("List is empty", null, HttpStatus.NOT_FOUND.value());
            } else {
                tractorWrapper = new TractorWrapper("Success", list.get(0), HttpStatus.OK.value());
            }
        } catch (ParseException | NullPointerException ex) {
            LOGGER.log(Level.WARNING, "Date is not correct.", ex);
            tractorWrapper = new TractorWrapper("Date is not correct. Date format should be dd.mm.yyyy", null, HttpStatus.BAD_REQUEST.value());
        }
        model.addAttribute("tractor", tractorWrapper);
        model.addAttribute("apiUrl", "https://maps.googleapis.com/maps/api/js?key=" + customProperties.getMapsApiKey());
        return "tractorData";
    }

}
