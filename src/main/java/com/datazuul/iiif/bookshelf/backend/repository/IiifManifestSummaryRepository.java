package com.datazuul.iiif.bookshelf.backend.repository;

import com.datazuul.iiif.bookshelf.model.IiifManifestSummary;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for IiifManifestSummaries identified by the manifest URI.
 *
 * @author ralf
 */
public interface IiifManifestSummaryRepository extends MongoRepository<IiifManifestSummary, UUID> {
    public IiifManifestSummary findByManifestUri(String manifestUri);
}
