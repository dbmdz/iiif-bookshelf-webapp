package de.digitalcollections.iiif.bookshelf.frontend.controller;

import de.digitalcollections.iiif.bookshelf.business.service.IiifManifestSummaryService;
import de.digitalcollections.iiif.bookshelf.frontend.model.PageWrapper;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.SearchRequest;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class WebController {

  @Value("${authentication}")
  private boolean authentication;

  @Autowired
  private IiifManifestSummaryService iiifManifestSummaryService;

  @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
  public String list(Model model, Pageable pageRequest) {
    final Page<IiifManifestSummary> page = iiifManifestSummaryService.getAll(pageRequest);
    model.addAttribute("authentication", authentication);
    model.addAttribute("page", new PageWrapper(page, "/"));
    model.addAttribute("searchRequest", new SearchRequest());

    // model.addAttribute("manifests", iiifManifestSummaryService.getAll());
    // model.addAttribute("count", iiifManifestSummaryService.countAll());
    // model.addAttribute("infoUrl", "/iiif/image/" + identifier + "/info.json");
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
    final String term = searchRequest.getTerm();
    if (!StringUtils.isEmpty(term)) {
      final Page<IiifManifestSummary> page = iiifManifestSummaryService.findAll(term, pageRequest);
      model.addAttribute("authentication", authentication);
      model.addAttribute("page", new PageWrapper(page, "/"));
      model.addAttribute("searchRequest", searchRequest);
      return "index";
    }
    return "redirect:/";
  }

  @CrossOrigin(origins = "*")
  @RequestMapping(value = {"/view/{uuid}"}, method = RequestMethod.GET)
  public String viewBook(@PathVariable UUID uuid, Model model) {
    IiifManifestSummary iiifManifestSummary = iiifManifestSummaryService.get(uuid);
    model.addAttribute("manifestId", iiifManifestSummary.getManifestUri());
    // model.addAttribute("canvasId", iiifPresentationEndpoint + identifier + "/canvas/p1");
    // return "bookreader/view-book";
    return "mirador/view";
  }
}
