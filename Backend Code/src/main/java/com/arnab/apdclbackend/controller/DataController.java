package com.arnab.apdclbackend.controller;
import com.arnab.apdclbackend.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/arnab_backend_api")
public class DataController {

    @Autowired
    private DatabaseService db;

    @GetMapping("/energy")
    public double getEnergy(@RequestParam String appliance, @RequestParam String rating) throws SQLException {
        return db.fetchEnergyConsumption(appliance, rating);
    }

    @GetMapping("/charges")
    public double getCharges(@RequestParam String category) throws SQLException {
        return db.fetchEnergyCharges(category);
    }

    @GetMapping("/appliances")
    public List<String> getAppliances() throws SQLException {
        return db.fetchAllAppliances();
    }

    @GetMapping("/category")
    public List<String> getcategory() throws SQLException{
        return db.fetchcategory();
    }

}

