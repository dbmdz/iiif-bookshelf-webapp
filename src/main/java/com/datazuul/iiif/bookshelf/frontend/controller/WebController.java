package com.datazuul.iiif.bookshelf.frontend.controller;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.datazuul.iiif.bookshelf.business.service.IiifManifestSummaryService;
import com.datazuul.iiif.bookshelf.frontend.model.PageWrapper;
import com.datazuul.iiif.bookshelf.model.SearchRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 * @author Ralf Eichinger (ralf.eichinger at bsb-muenchen.de)
 */
@Controller
public class WebController {

  @Autowired
  private IiifManifestSummaryService iiifManifestSummaryService;

  @RequestMapping(value = "/", method = RequestMethod.GET)
  public String list(Model model, Pageable pageRequest) {
    final Page<IiifManifestSummary> page = iiifManifestSummaryService.getAll(pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/"));
    model.addAttribute("searchRequest", new SearchRequest());
    
//    model.addAttribute("manifests", iiifManifestSummaryService.getAll());
//    model.addAttribute("count", iiifManifestSummaryService.countAll());
//    model.addAttribute("infoUrl", "/iiif/image/" + identifier + "/info.json");
    return "index";
  }

  @RequestMapping(value = "/add", method = RequestMethod.GET)
  public String add(Model model) {
    model.addAttribute("manifest", new IiifManifestSummary());
    return "add";
  }

  @RequestMapping(value = "/add", method = RequestMethod.POST)
  public String add(IiifManifestSummary manifestSummary, Model model) {
    iiifManifestSummaryService.enrichAndSave(manifestSummary);
    return "redirect:/";
  }

  @RequestMapping(value = "/find", method = RequestMethod.POST)
  public String find(SearchRequest searchRequest, Model model, Pageable pageRequest) {
    final Page<IiifManifestSummary> page = iiifManifestSummaryService.findAll(searchRequest.getTerm(), pageRequest);
    model.addAttribute("page", new PageWrapper(page, "/"));
    model.addAttribute("searchRequest", searchRequest);
    return "index";
  }
  
  @CrossOrigin(origins = "*")
  @RequestMapping(value = {"/view/{uuid}"}, method = RequestMethod.GET)
  public String viewBook(@PathVariable UUID uuid, Model model) {
    IiifManifestSummary iiifManifestSummary = iiifManifestSummaryService.get(uuid);
    model.addAttribute("manifestId", iiifManifestSummary.getManifestUri());
//        model.addAttribute("canvasId", iiifPresentationEndpoint + identifier + "/canvas/p1");
//        return "bookreader/view-book";
    return "mirador/view";
  }
}
