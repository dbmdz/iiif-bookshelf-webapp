package com.datazuul.iiif.bookshelf.model;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 *
 * @author ralf
 */
@Document(collection="iiif-manifest-summaries")
public class IiifManifestSummary {
    @Id
    private URI manifestUri;
    private String previewImageIiifImageServiceUri;
    private String label;
    private String description;
    private String attribution;

    public URI getManifestUri() {
        return manifestUri;
    }

    public void setManifestUri(String manifestUri) throws URISyntaxException {
        this.manifestUri = new URI(manifestUri);
    }
    
    public void setManifestUri(URI manifestUri) {
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
