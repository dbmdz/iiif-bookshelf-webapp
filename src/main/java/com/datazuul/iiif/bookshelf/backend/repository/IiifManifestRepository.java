package com.datazuul.iiif.bookshelf.backend.repository;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import java.net.URI;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for IiifManifestSummaries identified by the manifest URI.
 *
 * @author ralf
 */
public interface IiifManifestRepository extends MongoRepository<IiifManifestSummary, URI> {
}
