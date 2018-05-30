package de.digitalcollections.iiif.bookshelf.backend.api.repository;

import de.digitalcollections.iiif.bookshelf.model.IiifManifestSummary;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository for IiifManifestSummaries identified by the manifest URI.
 */
public interface IiifManifestSummaryRepository extends MongoRepository<IiifManifestSummary, UUID>, IiifManifestSummaryRepositoryCustom {

  public List<IiifManifestSummary> findAllByOrderByLastModifiedDesc();

  public Page<IiifManifestSummary> findAllByOrderByLastModifiedDesc(Pageable pageRequest);

  public Page<IiifManifestSummary> findBy(TextCriteria criteria, Pageable page); // do not expose mongo TextCriteria to service layer!

  public IiifManifestSummary findByManifestUri(String manifestUri);

  public List<IiifManifestSummary> findByUuidIn(List<UUID> uuids);

  public Page<IiifManifestSummary> findByUuidIn(List<UUID> uuids, Pageable page);

  public List<IiifManifestSummary> findByViewId(String viewId);

  public List<IiifManifestSummary> findByViewIdOrUuid(String viewId, UUID uuid);

  public default IiifManifestSummary create() {
    return new IiifManifestSummary();
  }

  public default IiifManifestSummary findOne(UUID uuid) {
    IiifManifestSummary iiifManifestSummary = create();
    iiifManifestSummary.setUuid(uuid);
    Optional<IiifManifestSummary> result = findOne(Example.of(iiifManifestSummary));
    if (!result.isPresent()) {
      return null;
    }
    return result.get();
  }
}
