package de.digitalcollections.iiif.bookshelf.backend.repository.impl;

import de.digitalcollections.iiif.bookshelf.backend.repository.IiifManifestSummaryRepository;
import de.digitalcollections.iiif.bookshelf.backend.repository.IiifManifestSummarySearchRepository;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.UUID;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
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
    query.setQuery(text);
    query.setFields("uuid", "label.de", "attribution.de", "description.de");
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

      ListIterator<SolrDocument> iter = rs.listIterator();
      while (iter.hasNext()) {
        SolrDocument doc = iter.next();
        ids.add(UUID.fromString(doc.getFieldValues("uuid").toArray()[0].toString()));
        LOGGER.info("Solr uuid: " + ids.get(current));
        current++;
        LOGGER.info("************************************************************** " + current + "   " + numFound);
      }
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

    for (int i = 0; i < rs.size(); ++i) {
      ids.add(UUID.fromString(rs.get(i).getFieldValues("uuid").toArray()[0].toString()));
      LOGGER.info("Solr uuid: " + ids.get(i));
    }
    return ids;

  }

  @Override
  public Page<IiifManifestSummary> findBy(String text, Pageable pageable) {
    SolrQuery query = new SolrQuery();
    query.setQuery(text);
    query.setFields("uuid", "label.de", "attribution.de", "description.de");
    query.setStart(pageable.getOffset());
    query.setRows(pageable.getPageSize());
    // query.set("defType", "edismax");
    QueryResponse response;
    try {
      response = solr.query(query);
    } catch (SolrServerException | IOException ex) {
      LOGGER.error(null, ex);
      return new PageImpl<>(new ArrayList<>());
    }
    SolrDocumentList rs = response.getResults();
    long numFound = rs.getNumFound();
    // this.numFound = numFound;

    ArrayList<UUID> ids = new ArrayList<>((int) query.getRows());
    LOGGER.info("--------------------------------------------------Results: " + rs.size());
    for (int i = 0; i < rs.size(); ++i) {
      if (rs.get(i).size() > 0) {
        ids.add(UUID.fromString(rs.get(i).getFieldValues("uuid").toArray()[0].toString()));
      }
      // LOGGER.info("Solr uuid: " + ids.get(i));
    }

    List<IiifManifestSummary> manifests = iiifManifestSummaryRepository.findByUuidIn(ids);
    LOGGER.info("Solr ids: " + ids.size() + " -----------------Mongo manifest list: " + manifests.size());
    // create Page for result
    PageImpl<IiifManifestSummary> page = new PageImpl<>(manifests, pageable, numFound);
    return page;

  }

  @Override
  public void save(IiifManifestSummary manifestSummary) {
    // FIXME first delete doc with this uuid (if exists)
    try {
      UpdateResponse resp = solr.deleteByQuery("uuid:" + manifestSummary.getUuid().toString());
      solr.commit();
    } catch (SolrServerException | IOException exception) {
      LOGGER.error("Could not save " + manifestSummary, exception);
    }

    SolrInputDocument doc = new SolrInputDocument();
    String key;
    String value;
    doc.addField("uuid", manifestSummary.getUuid());
    for (Entry<Locale, String> e : manifestSummary.getLabels().entrySet()) {
      key = e.getKey().getLanguage();
      value = e.getValue();
      doc.addField("label." + key, value);
    }
    for (Entry<Locale, String> e : manifestSummary.getAttributions().entrySet()) {
      key = e.getKey().getLanguage();
      value = e.getValue();
      doc.addField("attribution." + key, value);
    }
    for (Entry<Locale, String> e : manifestSummary.getDescriptions().entrySet()) {
      key = e.getKey().getLanguage();
      value = e.getValue();
      doc.addField("description." + key, value);
    }
    try {
      solr.add(doc);
      solr.commit();
    } catch (SolrServerException | IOException ex) {
      LOGGER.error(null, ex);
    }
  }

  @Override
  public List<UUID> findBy(String text, int start, int rows) {
    SolrQuery query = new SolrQuery();
    query.setQuery(text); // test also with "Foobar part +500"
    query.setFields("uuid", "label.de", "attribution.de", "description.de");
    query.setStart(start);
    query.setRows(rows);
    // query.set("defType", "edismax");
    QueryResponse response;
    try {
      response = solr.query(query);
    } catch (SolrServerException | IOException ex) {
      LOGGER.error(null, ex);
      return new ArrayList<>();
    }
    SolrDocumentList rs = response.getResults();
    ArrayList<UUID> ids = new ArrayList<>((int) rows);
    LOGGER.info("--------------------------------------------------Results: " + rs.size());
    for (int i = 0; i < rs.size(); ++i) {
      ids.add(UUID.fromString(rs.get(i).getFieldValues("uuid").toArray()[0].toString()));
      LOGGER.info("Solr uuid: " + ids.get(i));
    }
    return ids;

  }
}
