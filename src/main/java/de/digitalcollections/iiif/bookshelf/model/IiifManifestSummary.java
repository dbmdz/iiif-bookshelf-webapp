package de.digitalcollections.iiif.bookshelf.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

@Document(collection = "iiif-manifest-summaries")
// @CompoundIndex(name = "labels_descriptions_attributions_idx", def = "{'descriptions.values' : 1, 'labels.values' : 2,
// 'attributions.values' : 1}")
public class IiifManifestSummary {

  @TextIndexed
  private HashMap<Locale, String> attributions = new HashMap<>();

  @TextIndexed
  private HashMap<Locale, String> descriptions = new HashMap<>();

  @TextIndexed(weight = 2)
  private HashMap<Locale, String> labels = new HashMap<>();

// DOES NOT WORK BECAUSE OF CUSTOM @Id (not being mongo object id):
  // @Temporal(TemporalType.TIMESTAMP)
  // @CreatedDate
  // private Date created;
  @Temporal(TemporalType.TIMESTAMP)
  @LastModifiedDate
  @Indexed(direction = IndexDirection.DESCENDING)
  private Date lastModified;

  private String logoUrl;

  @TextIndexed
  private String manifestUri;

  @TextScore
  private Float score;

  private Thumbnail thumbnail;

  @Id
  private UUID uuid = UUID.randomUUID();

  private String viewId;

  public void addAttribution(Locale locale, String attribution) {
    attributions.put(locale, attribution);
  }

  public void addDescription(Locale locale, String description) {
    descriptions.put(locale, description);
  }

  public void addLabel(Locale locale, String label) {
    labels.put(locale, label);
  }

  public String getAttribution(String language) {
    return attributions.get(new Locale(language));
  }

  public String getAttribution(Locale locale) {
    return attributions.get(locale);
  }

  public HashMap<Locale, String> getAttributions() {
    return attributions;
  }

  public void setAttributions(HashMap<Locale, String> attributions) {
    this.attributions = attributions;
  }

  public String getDescription(Locale locale) {
    return descriptions.get(locale);
  }

  public String getDescription(String language) {
    return descriptions.get(new Locale(language));
  }

  public HashMap<Locale, String> getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(HashMap<Locale, String> descriptions) {
    this.descriptions = descriptions;
  }

  public String getLabel(String language) {
    return labels.get(new Locale(language));
  }

  public String getLabel(Locale locale) {
    return labels.get(locale);
  }

  public HashMap<Locale, String> getLabels() {
    return labels;
  }

  public void setLabels(HashMap<Locale, String> labels) {
    this.labels = labels;
  }

  // public Date getCreated() {
  // return created;
  // }
  //
  // public void setCreated(Date created) {
  // this.created = created;
  // }
  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public String getLogoUrl() {
    return logoUrl;
  }

  public void setLogoUrl(String logoUrl) {
    this.logoUrl = logoUrl;
  }

  public String getManifestUri() {
    return manifestUri;
  }

  public void setManifestUri(String manifestUri) {
    this.manifestUri = manifestUri;
  }

  public Float getScore() {
    return score;
  }

  public void setScore(Float score) {
    this.score = score;
  }

  public Thumbnail getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(Thumbnail thumbnail) {
    this.thumbnail = thumbnail;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public String getViewId() {
    return viewId;
  }

  public void setViewId(String viewId) {
    this.viewId = viewId;
  }
}
