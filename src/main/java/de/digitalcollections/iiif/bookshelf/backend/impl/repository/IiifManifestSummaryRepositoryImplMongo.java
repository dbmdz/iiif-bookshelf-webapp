package de.digitalcollections.iiif.bookshelf.backend.impl.repository;

import de.digitalcollections.iiif.bookshelf.backend.api.repository.IiifManifestSummaryRepository;
import de.digitalcollections.iiif.bookshelf.backend.api.repository.IiifManifestSummaryRepositoryCustom;
import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Repository;

@Repository
public class IiifManifestSummaryRepositoryImplMongo implements IiifManifestSummaryRepositoryCustom {

  @Autowired
  IiifManifestSummaryRepository repo;

  @Autowired
  private MongoOperations mongoOperations;

  @Autowired
  private MongoTemplate mongoTemplate;

  @PostConstruct
  public void ensureTextIndex() {
    // make sure the index is set up properly (not yet possible via Spring Data Annotations)
    // mongoOperations.getCollection("iiif-manifest-summaries").ensureIndex(new BasicDBObject("description", "text"));
  }

  @Override
  public Page<IiifManifestSummary> findBy(String text, Pageable page) {
    TextCriteria textCriteria = new TextCriteria();
    textCriteria.matchingAny(text);
    // TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matchingAny(text);
    // Query query = TextQuery.queryText(textCriteria).sortByScore();
    // DBCursor find = mongoOperations.getCollection("iiif-manifest-summaries").find(query.getQueryObject());
    return repo.findBy(textCriteria, page);

    // List<IiifManifestSummary> find = mongoTemplate.find(query, IiifManifestSummary.class);
    // return null;
  }
  // public List<IiifManifestSummary> findByUuidsIn(List<UUID> uuids)//{}
}
