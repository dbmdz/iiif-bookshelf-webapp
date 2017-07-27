package de.digitalcollections.iiif.bookshelf.frontend.controller;

import com.google.common.collect.Maps;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.iiif.bookshelf.business.api.service.IiifManifestSummaryService;
import de.digitalcollections.iiif.bookshelf.frontend.model.PageWrapper;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.SearchRequest;
import de.digitalcollections.iiif.bookshelf.model.exceptions.SearchSyntaxException;
import de.digitalcollections.iiif.presentation.model.api.exceptions.NotFoundException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebController extends AbstractController {

  private final Logger LOGGER = LoggerFactory.getLogger(WebController.class);

  @Value("${authentication}")
  private boolean authentication;

  @Autowired
  private IiifManifestSummaryService iiifManifestSummaryService;

  /**
   * List with or without search query.
   *
   * @param searchRequest contains search term if any
   * @param model view model
   * @param pageRequest paging params
   * @param style switching style between list and grid listing
   * @param results validation results and errors
   * @return view of list of objects
   */
  @RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
  public String list(SearchRequest searchRequest, Model model, Pageable pageRequest, @RequestParam(required = false,
          defaultValue = "grid") String style, BindingResult results) {
    verifyBinding(results);

    model.addAttribute("authentication", authentication);
    if (searchRequest != null && !StringUtils.isEmpty(searchRequest.getQuery())) {
      final String term = searchRequest.getQuery().replace(":", "\\:");
      if (!StringUtils.isEmpty(term)) {
        Page<IiifManifestSummary> page;
        try {
          page = iiifManifestSummaryService.findAll(term, pageRequest);
        } catch (SearchSyntaxException ex) {
          page = new PageImpl(new ArrayList<>());
          results.reject("error.search_syntax");
        }
        model.addAttribute("menu", "home");
        model.addAttribute("page", new PageWrapper(page, "/"));
        model.addAttribute("searchRequest", searchRequest);
        model.addAttribute("style", style);
        return "index";
      }
    }
    final Page<IiifManifestSummary> page = iiifManifestSummaryService.getAll(pageRequest);
    model.addAttribute("menu", "home");
    model.addAttribute("page", new PageWrapper(page, "/"));
    model.addAttribute("searchRequest", new SearchRequest());
    model.addAttribute("style", style);

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
    try {
      iiifManifestSummaryService.enrichAndSave(manifestSummary);
    } catch (ParseException e) {
      LOGGER.error("Could not load manifest from {} because of malformed JSON", manifestSummary.getManifestUri(), e);
      model.addAttribute("manifest", manifestSummary);
      model.addAttribute("errorMessage", "Manifest at URL contains malformed JSON.");
      return "add";
    } catch (NotFoundException e) {
      LOGGER.error("Could not find manifest at {}", manifestSummary.getManifestUri(), e);
      model.addAttribute("manifest", manifestSummary);
      model.addAttribute("errorMessage", "No Manifest was found at URL.");
      return "add";
    }
    return "redirect:/";
  }

  @ResponseBody
  @RequestMapping(value = "/api/add", method = RequestMethod.POST, produces = "application/json")
  public IiifManifestSummary apiAdd(@RequestParam("uri") String manifestUri) throws ApiException {
    IiifManifestSummary summary = new IiifManifestSummary();
    summary.setManifestUri(manifestUri);
    try {
      iiifManifestSummaryService.enrichAndSave(summary);
      return summary;
    } catch (ParseException e) {
      throw new ApiException("Invalid JSON at URL '" + manifestUri + "'", HttpStatus.BAD_REQUEST);
    } catch (NotFoundException e) {
      throw new ApiException("No manifest at URL '" + manifestUri + "'", HttpStatus.BAD_REQUEST);
    }
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

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApiException(ApiException e) {
    Map<String, Object> rv = Maps.newHashMap();
    rv.put("error", e.message);
    return ResponseEntity.status(e.statusCode).body(rv);
  }
}
