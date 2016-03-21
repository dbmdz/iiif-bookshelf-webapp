package com.datazuul.iiif.catalog.portal.model;

/**
 *
 * @author ralf
 */
public class IiifManifestSummary {
    private String manifestUri;
    private String previewImageIiifImageServiceUri;
    private String label;
    private String description;
    private String attribution;

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }
    
    
}
