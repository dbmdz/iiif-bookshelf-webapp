package com.datazuul.iiif.bookshelf.model;

import com.datazuul.iiif.presentation.api.model.Version;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author ralf
 */
@Document(collection = "iiif-manifest-summaries")
public class IiifManifestSummary {

    @Id
    private UUID uuid = UUID.randomUUID();

    private Version version;
    private String manifestUri;
    private String previewImageIiifImageServiceUri;
    private HashMap<Locale, String> labels = new HashMap<>();
    private HashMap<Locale, String> descriptions = new HashMap<>();
    private HashMap<Locale, String> attributions = new HashMap<>();

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getManifestUri() {
        return manifestUri;
    }

    public void setManifestUri(String manifestUri) {
        this.manifestUri = manifestUri;
    }

    public String getPreviewImageIiifImageServiceUri() {
        return previewImageIiifImageServiceUri;
    }

    public void setPreviewImageIiifImageServiceUri(String previewImageIiifImageServiceUri) {
        this.previewImageIiifImageServiceUri = previewImageIiifImageServiceUri;
    }

    public void addLabel(Locale locale, String label) {
        labels.put(locale, label);
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

    public void addDescription(Locale locale, String description) {
        descriptions.put(locale, description);
    }

    public String getDescription(Locale locale) {
        return descriptions.get(locale);
    }

    public HashMap<Locale, String> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(HashMap<Locale, String> descriptions) {
        this.descriptions = descriptions;
    }

    public void addAttribution(Locale locale, String attribution) {
        attributions.put(locale, attribution);
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

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}
