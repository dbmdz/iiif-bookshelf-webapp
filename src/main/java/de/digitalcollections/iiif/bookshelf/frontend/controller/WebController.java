package de.digitalcollections.iiif.bookshelf.frontend.controller;

import com.google.common.collect.Maps;
import de.digitalcollections.commons.springmvc.controller.AbstractController;
import de.digitalcollections.iiif.bookshelf.business.api.service.IiifCollectionService;
import de.digitalcollections.iiif.bookshelf.business.api.service.IiifManifestSummaryService;
import de.digitalcollections.iiif.bookshelf.frontend.model.PageWrapper;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import de.digitalcollections.iiif.bookshelf.model.SearchRequest;
import de.digitalcollections.iiif.bookshelf.model.exceptions.NotFoundException;
import de.digitalcollections.iiif.bookshelf.model.exceptions.SearchSyntaxException;
import de.digitalcollections.iiif.model.jackson.IiifObjectMapper;
import de.digitalcollections.iiif.model.sharedcanvas.Manifest;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(WebController.class);

  @Autowired
  private MessageSource messageSource;

  @Autowired
  @Value("#{iiifVersions}")
  private Map<String, Object> iiifVersions;

  @Value("${custom.app.security.enabled}")
  private boolean authentication;

  @Autowired
  private IiifCollectionService iiifCollectionService;

  @Autowired
  private IiifManifestSummaryService iiifManifestSummaryService;

  @Autowired
  private IiifObjectMapper iiifObjectMapper;

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
    } catch (IOException e) {
      LOGGER.warn("Could not load manifest from {} because of malformed JSON", manifestSummary.getManifestUri(), e);
      model.addAttribute("manifest", manifestSummary);
      model.addAttribute("errorMessage", "Manifest at URL contains malformed JSON.");
      return "add";
    } catch (NotFoundException e) {
      LOGGER.warn("Could not find manifest at {}", manifestSummary.getManifestUri(), e);
      model.addAttribute("manifest", manifestSummary);
      model.addAttribute("errorMessage", "No Manifest was found at URL.");
      return "add";
    } catch (Exception e) {
      LOGGER.warn("Could not add manifest from {}", manifestSummary.getManifestUri(), e);
    }
    return "redirect:/";
  }

  @RequestMapping(value = "/addCollection", method = RequestMethod.POST)
  public String addCollection(IiifManifestSummary manifestSummary, Model model) {
    try {
      iiifCollectionService.importAllObjects(manifestSummary);
    } catch (Exception e) {
      LOGGER.warn("Could not add collection manifest from {}", manifestSummary.getManifestUri(), e);
      model.addAttribute("manifest", manifestSummary);
      model.addAttribute("errorMessage", "Could not add collection manifest from " + manifestSummary.getManifestUri());
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
    } catch (IOException e) {
      LOGGER.warn("IOException for manifest at {}: ", manifestUri, e);
      throw new ApiException("Invalid manifest at URL '" + manifestUri + "'", HttpStatus.BAD_REQUEST);
    } catch (NotFoundException | URISyntaxException e) {
      LOGGER.warn("Exception for manifest at {}: ", manifestUri, e);
      throw new ApiException("No manifest at URL '" + manifestUri + "'", HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      LOGGER.warn("Exception for manifest at {}: ", manifestUri, e);
      throw new ApiException("Exception for manifest at: '" + manifestUri + "'", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @ResponseBody
  @RequestMapping(value = "/api/addCollection", method = RequestMethod.POST, produces = "application/json")
  public boolean apiAddCollection(@RequestParam("uri") String manifestUri) throws ApiException {
    IiifManifestSummary summary = new IiifManifestSummary();
    summary.setManifestUri(manifestUri);
    try {
      iiifCollectionService.importAllObjects(summary);
      return true;
    } catch (IOException e) {
      LOGGER.warn("IOException for collection at {}: ", manifestUri, e);
      throw new ApiException("Invalid collection manifest at URL '" + manifestUri + "'", HttpStatus.BAD_REQUEST);
    } catch (URISyntaxException e) {
      throw new ApiException("No collection manifest at URL '" + manifestUri + "'", HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      LOGGER.warn("Exception for collection at {}: ", manifestUri, e);
      throw new ApiException("Exception for collection at: '" + manifestUri + "'", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

//  @CrossOrigin(origins = "*")
//  @RequestMapping(value = {"/view/{uuid}"}, method = RequestMethod.GET)
//  public String viewBook(@PathVariable UUID uuid, Model model) {
//    IiifManifestSummary iiifManifestSummary = iiifManifestSummaryService.get(uuid);
//    model.addAttribute("manifestId", iiifManifestSummary.getManifestUri());
//    String title = iiifManifestSummaryService.getLabel(iiifManifestSummary, LocaleContextHolder.getLocale());
//    model.addAttribute("title", title);
//    return "mirador/view";
//  }
  @CrossOrigin(origins = "*")
  @RequestMapping(value = {"/view/{id}"}, method = RequestMethod.GET)
  public String viewObject(@PathVariable String id, Model model) {
    IiifManifestSummary iiifManifestSummary;

    try {
      // if old bookmark with uuid, send redirect to new viewId (if exists)
      UUID uuid = UUID.fromString(id);
      iiifManifestSummary = iiifManifestSummaryService.get(uuid);
      if (iiifManifestSummary != null) {
        String viewId = iiifManifestSummary.getViewId();
        if (viewId != null && !uuid.toString().equals(viewId)) {
          return "redirect:/view/" + iiifManifestSummary.getViewId();
        }
      }
    } catch (IllegalArgumentException e) {
      // no uuid, so it is a viewId
    }

    iiifManifestSummary = iiifManifestSummaryService.get(id);
    if (iiifManifestSummary == null) {
      throw new NotFoundException();
    }
    model.addAttribute("iiifVersions", iiifVersions);
    model.addAttribute("manifestId", iiifManifestSummary.getManifestUri());
    String title = iiifManifestSummaryService.getLabel(iiifManifestSummary, LocaleContextHolder.getLocale());
    model.addAttribute("title", title);
    return "mirador/view";
  }

  @CrossOrigin(origins = "*")
  @RequestMapping(value = {"/uv/{id}"}, method = RequestMethod.GET)
  public String viewObjectInUniversalViewer(@PathVariable String id, Model model) {
    IiifManifestSummary iiifManifestSummary;

    try {
      // if old bookmark with uuid, send redirect to new viewId (if exists)
      UUID uuid = UUID.fromString(id);
      iiifManifestSummary = iiifManifestSummaryService.get(uuid);
      if (iiifManifestSummary != null) {
        String viewId = iiifManifestSummary.getViewId();
        if (viewId != null && !uuid.toString().equals(viewId)) {
          return "redirect:/uv/" + iiifManifestSummary.getViewId();
        }
      }
    } catch (IllegalArgumentException e) {
      // no uuid, so it is a viewId
    }

    iiifManifestSummary = iiifManifestSummaryService.get(id);
    if (iiifManifestSummary == null) {
      throw new NotFoundException();
    }
    model.addAttribute("iiifVersions", iiifVersions);
    model.addAttribute("manifestId", iiifManifestSummary.getManifestUri());
    String title = iiifManifestSummaryService.getLabel(iiifManifestSummary, LocaleContextHolder.getLocale());
    model.addAttribute("title", title);
    return "uv/view";
  }

  @CrossOrigin(origins = "*")
  @RequestMapping(value = {"/info/{id}"}, method = RequestMethod.GET)
  public String objectInfo(@PathVariable String id, Model model, Locale locale) throws IOException {
    IiifManifestSummary iiifManifestSummary;

    try {
      // if old bookmark with uuid, send redirect to new viewId (if exists)
      UUID uuid = UUID.fromString(id);
      iiifManifestSummary = iiifManifestSummaryService.get(uuid);
      if (iiifManifestSummary != null) {
        String viewId = iiifManifestSummary.getViewId();
        if (viewId != null && !uuid.toString().equals(viewId)) {
          return "redirect:/info/" + iiifManifestSummary.getViewId();
        }
      }
    } catch (IllegalArgumentException e) {
      // no uuid, so it is a viewId
    }

    iiifManifestSummary = iiifManifestSummaryService.get(id);
    if (iiifManifestSummary == null) {
      throw new NotFoundException();
    }
    final String manifestUri = iiifManifestSummary.getManifestUri();
    model.addAttribute("manifestId", manifestUri);
    String title = iiifManifestSummaryService.getLabel(iiifManifestSummary, LocaleContextHolder.getLocale());
    model.addAttribute("title", title);
    model.addAttribute("manifestSummary", iiifManifestSummary);

    try {
      Manifest manifest = iiifObjectMapper.readValue(new URL(manifestUri), Manifest.class);
      model.addAttribute("manifest", manifest);
    } catch (Exception e) {
      model.addAttribute("error_message", messageSource.getMessage("manifest_error", new Object[]{}, locale));
    }
    return "info";
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApiException(ApiException e) {
    Map<String, Object> rv = Maps.newHashMap();
    rv.put("error", e.message);
    return ResponseEntity.status(e.statusCode).body(rv);
  }

  @RequestMapping(value = {"/search"}, method = RequestMethod.GET)
  public String search(SearchRequest searchRequest, Model model, Pageable pageRequest, @RequestParam(required = false,
          defaultValue = "grid") String style, BindingResult results) {
    verifyBinding(results);

    model.addAttribute("authentication", authentication);

    if (searchRequest == null) {
      searchRequest = new SearchRequest();
    }

    Page<IiifManifestSummary> page = null;
    if (!StringUtils.isEmpty(searchRequest.getQuery())) {
      final String term = searchRequest.getQuery().replace(":", "\\:");
      if (!StringUtils.isEmpty(term)) {
        try {
          page = iiifManifestSummaryService.findAll(term, pageRequest);
        } catch (SearchSyntaxException ex) {
          page = new PageImpl(new ArrayList<>());
          results.reject("error.search_syntax");
        }
      }
    } else {
      page = iiifManifestSummaryService.getAll(pageRequest);
    }

    model.addAttribute("menu", "search");
    model.addAttribute("page", new PageWrapper(page, "/search"));
    model.addAttribute("searchRequest", searchRequest);
    model.addAttribute("style", style);

    // model.addAttribute("manifests", iiifManifestSummaryService.getAll());
    // model.addAttribute("count", iiifManifestSummaryService.countAll());
    // model.addAttribute("infoUrl", "/iiif/image/" + identifier + "/info.json");
    return "search-advanced";
  }

  @RequestMapping(value = {"/login"}, method = RequestMethod.GET)
  public String login() {
    return "login";
  }
}
