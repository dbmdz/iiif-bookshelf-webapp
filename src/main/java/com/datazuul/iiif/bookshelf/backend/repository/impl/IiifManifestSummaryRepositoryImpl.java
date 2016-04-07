package com.datazuul.iiif.bookshelf.backend.repository.impl;

import com.datazuul.iiif.bookshelf.backend.repository.IiifManifestSummaryRepository;
import com.datazuul.iiif.bookshelf.backend.repository.IiifManifestSummaryRepositoryCustom;
import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Repository;

/**
 *
 * @author ralf
 */
@Repository
public class IiifManifestSummaryRepositoryImpl implements IiifManifestSummaryRepositoryCustom {

  @Autowired
  IiifManifestSummaryRepository repo;

  @Override
  public Page<IiifManifestSummary> findBy(String text, Pageable page) {
    TextCriteria textCriteria = TextCriteria.forDefaultLanguage().matchingPhrase(text);
    return repo.findBy(textCriteria, page);
  }

}
