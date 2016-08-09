package de.digitalcollections.iiif.bookshelf.model;

import java.math.BigInteger;
import org.springframework.data.annotation.Id;

/**
 *
 * @author ralf
 */
public class AbstractDocument {

    @Id
    private BigInteger documentId;

    public void setId(BigInteger id) {
        this.documentId = id;
    }

    public BigInteger getId() {
        return documentId;
    }
}
