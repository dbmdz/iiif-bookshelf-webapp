package de.digitalcollections.iiif.bookshelf.backend.repository.impl;

import de.digitalcollections.iiif.bookshelf.backend.repository.IiifManifestSummaryRepository;
import de.digitalcollections.iiif.bookshelf.backend.repository.IiifManifestSummarySearchRepository;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class IiifManifestSummarySearchRepositoryImplSolrj implements IiifManifestSummarySearchRepository<UUID> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IiifManifestSummarySearchRepositoryImplSolrj.class);

  @Autowired
  private IiifManifestSummaryRepository iiifManifestSummaryRepository;

  @Autowired
  private SolrClient solr;

  public void deleteById(String id) {
    try {
      solr.deleteById(id);
      solr.commit();
    } catch (SolrServerException | IOException exception) {
      LOGGER.error("Could not delete " + id, exception);
    }
  }

  @Override
  public List<UUID> findBy(String text) {
    SolrQuery query = new SolrQuery();
    query.setQuery(escapeUnwantedSpecialChars(text));
    query.setFields("id", "labelDE_txt", "attributionDE_txt", "descriptionDE_txt");
    query.setStart(0);
    QueryResponse response;
    try {
      response = solr.query(query);
    } catch (SolrServerException | IOException ex) {
      LOGGER.error(null, ex);
      return new ArrayList<>();
    }
    SolrDocumentList rs = response.getResults();
    long numFound = rs.getNumFound();
    // this.numFound = numFound;
    ArrayList<UUID> ids = new ArrayList<>((int) numFound);
    int current = 0;
    while (current < numFound) {
      current += rs.size();
      query.setStart(current);
      try {
        response = solr.query(query);
      } catch (SolrServerException | IOException ex) {
        LOGGER.error(null, ex);
      }
      rs = response.getResults();
      numFound = rs.getNumFound();

    }
    LOGGER.info("--------------------------------------------------Results: " + rs.size());
    return getUUIDs(rs);
  }

  @Override
  public Page<IiifManifestSummary> findBy(String text, Pageable pageable) {
    SolrQuery query = new SolrQuery();
    query.setQuery(escapeUnwantedSpecialChars(text));
    query.setFields("id", "labelDE_txt", "attributionDE_txt", "descriptionDE_txt");
    query.setStart(pageable.getOffset());
    query.setRows(pageable.getPageSize());

    QueryResponse response;
    try {
      LOGGER.info("query = " +query);
      response = solr.query(query);
    } catch (SolrServerException | IOException ex) {
      LOGGER.error(null, ex);
      return new PageImpl<>(new ArrayList<>());
    }

    SolrDocumentList result = response.getResults();
    List<UUID> uuids = getUUIDs(result);
    List<IiifManifestSummary> manifests = iiifManifestSummaryRepository.findByUuidIn(uuids);
    LOGGER.info("Found " + uuids.size() + " UUIDs and " + manifests.size() + " manifests.");
    return new PageImpl<>(manifests, pageable, result.getNumFound());
  }

  @Override
  public void save(IiifManifestSummary manifestSummary) {
    // first delete doc with this uuid (if exists) otherwise we get duplicates
    try {
      solr.deleteByQuery("id:" + manifestSummary.getUuid().toString());
      solr.commit();
    } catch (RemoteSolrException | SolrServerException | IOException exception) {
      LOGGER.error("Could not delete existing " + manifestSummary, exception);
    }

    SolrInputDocument doc = new SolrInputDocument();
    doc.addField("id", manifestSummary.getUuid());
    doc.addField("manifesturi_txt", manifestSummary.getManifestUri());
    for (Entry<Locale, String> e : manifestSummary.getLabels().entrySet()) {
      String key = e.getKey().getLanguage();
      String value = e.getValue();
      doc.addField("label" + key.toUpperCase() + "_txt", value);
     }
    for (Entry<Locale, String> e : manifestSummary.getAttributions().entrySet()) {
      String key = e.getKey().getLanguage();
      String value = e.getValue();
      doc.addField("attribution" + key.toUpperCase() + "_txt", value);
    }
    for (Entry<Locale, String> e : manifestSummary.getDescriptions().entrySet()) {
      String key = e.getKey().getLanguage();
      String value = e.getValue();
      doc.addField("description" + key.toUpperCase() + "_txt", value);
    }
    try {
      solr.add(doc);
      solr.commit();
    } catch (SolrServerException | IOException ex) {
      LOGGER.error("Could not save ", ex);
    }
  }

  @Override
  public List<UUID> findBy(String text, int start, int rows) {
    SolrQuery query = new SolrQuery();
    query.setQuery(escapeUnwantedSpecialChars(text)); // test also with "Foobar part +500"
    query.setFields("id", "labelDE_txt", "attributionDE_txt", "descriptionDE_txt", "manifesturi_txt");
    query.setStart(start);
    query.setRows(rows);
    QueryResponse response;
    try {
      response = solr.query(query);
    } catch (SolrServerException | IOException ex) {
      LOGGER.error(null, ex);
      return new ArrayList<>();
    }
    SolrDocumentList results = response.getResults();
    LOGGER.info("--------------------------------------------------Results: " + results.size());
    return getUUIDs(results);
  }

  private List<UUID> getUUIDs(List<SolrDocument> docs) {
    ArrayList<UUID> ids = new ArrayList<>(docs.size());
    for (SolrDocument doc : docs) {
      UUID uuid = createUUID(doc.getFirstValue("id"));
      ids.add(uuid);
      LOGGER.info("Solr id: " + uuid);
    }
    return ids;
  }

  private UUID createUUID(Object value) {
    return UUID.fromString(value.toString());
  }

  protected String escapeUnwantedSpecialChars(String text) {
    // We don't want to escape whitespaces, * and "
    // But we want to escape all the ohter special characters
    return ClientUtils.escapeQueryChars(text).replaceAll("\\\\\\*", "*").replaceAll("\\\\\\s", " ").replaceAll("\\\\\\\"", "\"");
  }
}
