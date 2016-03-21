/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.datazuul.iiif.catalog.portal.frontend.controller;

import com.datazuul.iiif.catalog.portal.business.service.IiifManifestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author Ralf Eichinger (ralf.eichinger at bsb-muenchen.de)
 */
@Controller
public class SearchController {

    @Autowired
    private IiifManifestService iiifManifestService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("manifests", iiifManifestService.getAllManifests());
//    model.addAttribute("infoUrl", "/iiif/image/" + identifier + "/info.json");
        return "index";
    }
}
