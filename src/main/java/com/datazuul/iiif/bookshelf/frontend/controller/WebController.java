package com.datazuul.iiif.bookshelf.frontend.controller;

import com.datazuul.iiif.bookshelf.business.service.IiifManifestService;
import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
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
public class WebController {

    @Autowired
    private IiifManifestService iiifManifestService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String list(Model model) {
        model.addAttribute("manifests", iiifManifestService.getAll());
//    model.addAttribute("infoUrl", "/iiif/image/" + identifier + "/info.json");
        return "index";
    }
    
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add(Model model) {
        model.addAttribute("manifest", new IiifManifestSummary());
        return "add";
    }
    
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String add(IiifManifestSummary manifest, Model model) {
        return "index";
    }
}
