package stroom.annotations.service;

import stroom.annotations.hibernate.AnnotationIndex;
import stroom.util.shared.QueryApiException;

import java.util.List;

public interface IndexService {
    /**
     * Retrieve all of the index entities currently registered
     * @return The list of all known index entities
     * @throws QueryApiException
     */
    List<AnnotationIndex> getAll() throws QueryApiException;

    /**
     * Retrieve the full config for the given DocRef
     * @param uuid              The UUID of the docRef to return
     * @return                  The full implementation specific config for this docRef.
     * @throws QueryApiException  If something goes wrong
     */
    AnnotationIndex get(String uuid) throws QueryApiException;

    /**
     * A new document has been created in Stroom
     *
     * @param uuid              The UUID of the document as created by stroom
     * @param name              The name of the document to be created.
     * @return The new index entity
     * @throws QueryApiException  If something goes wrong
     */
    AnnotationIndex create(String uuid, String name) throws QueryApiException;

    /**
     * A notification from Stroom that a document is being copied. The external system should
     * copy it's configuration for the original into a new entity.
     *
     * @param originalUuid      The uuid of the document being copied
     * @param copyUuid          The uuid of the copy
     * @return The new index entity
     * @throws QueryApiException  If something goes wrong
     */
    AnnotationIndex copyDocument(String originalUuid, String copyUuid) throws QueryApiException;

    /**
     * A Notification from Stroom that the document has been 'moved'. In most cases the external system
     * will not care about this.
     *
     * @param uuid             The uuid of the document that was moved
     * @return The updated index entity
     * @throws QueryApiException  If something goes wrong
     */
    AnnotationIndex documentMoved(String uuid) throws QueryApiException;

    /**
     * A notifiation from Stroom that the name of a document has been changed. Whilst the name belongs to stroom
     * it may be helpful for the external system to know what the name is, but the name should not be used for referencing
     * the DocRef between systems as it could easily be out of sync.
     *
     * @param uuid The uuid of the document you want to rename.
     * @param name The new name of the document.
     * @return The updated index entity
     * @throws QueryApiException  If something goes wrong
     */
    AnnotationIndex documentRenamed(String uuid, String name) throws QueryApiException;

    /**
     * The document with this UUID is being deleted in Stroom.
     *
     * @param uuid The uuid of the document you want to delete.
     * @throws QueryApiException  If something goes wrong
     */
    void deleteDocument(String uuid) throws QueryApiException;
}
