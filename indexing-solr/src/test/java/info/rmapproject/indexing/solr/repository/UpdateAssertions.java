package info.rmapproject.indexing.solr.repository;

import info.rmapproject.core.model.RMapStatus;
import info.rmapproject.indexing.solr.model.DiscoSolrDocument;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.UpdateField;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static info.rmapproject.indexing.IndexUtils.iae;
import static info.rmapproject.indexing.solr.model.DiscoSolrDocument.DISCO_STATUS;
import static info.rmapproject.indexing.solr.model.DiscoSolrDocument.DOC_LAST_UPDATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Composes a {@link CompositeAnswer} containing assertions that may be used to validate the contents of the
 * {@code Set<PartialUpdate>} that are {@link DiscosIndexer#postIndex(DiscoSolrDocument) saved} post-index.  Use the
 * {@link UpdateAssertions.Builder} to create instances.
 * <h3>Example usage:</h3>
 * To assert that the set of partial updates contains at least two documents.  One document with the id
 * {@code tobetombstoned_1} and a status of {@code TOMBSTONED}, and another document with the id
 * {@code tobetombstoned_2} and a status of {@code TOMBSTONED}:
 * <pre>
 * when(mockTemplate.saveBeans(eq(CORE_NAME), anySet())).thenAnswer(inv -> {
 *     UpdateAssertions.Builder builder = new UpdateAssertions.Builder();
 *     return builder
 *             .updateMustContainHavingStatus((partialUpdate) -> docIdMatches(partialUpdate, "tobetombstoned_1"), TOMBSTONED)
 *             .updateMustContainHavingStatus((partialUpdate) -> docIdMatches(partialUpdate, "tobetombstoned_2"), TOMBSTONED)
 *             .build()
 *             .answer(inv);
 * });
 * </pre>
 * <h3>Example usage:</h3>
 * To assert that an update inactivates a document already in the index, and <em>does not</em> inactivate the newly
 * indexed document:
 * <pre>
 * when(mockTemplate.saveBeans(eq(CORE_NAME), anySet())).thenAnswer(inv -> {
 *     UpdateAssertions.Builder builder = new UpdateAssertions.Builder();
 *     return builder
 *             .updateMustContainHavingStatus(partialUpdate -> docIdMatches(partialUpdate, "tobeinactivated"), INACTIVE)
 *             .updateMustNotContain((partialUpdate) -> docIdMatches(partialUpdate, "justindexed"))
 *             .build()
 *             .answer(inv);
 * });
 * </pre>
 *
 * @author Elliot Metsger (emetsger@jhu.edu)
 */
class UpdateAssertions {

    private UpdateAssertions() {
        // must use builder
    }

    /**
     * Sets an expectation on the provided {@code SolrTemplate} by creating a custom
     * {@link org.mockito.stubbing.Answer}.  When {@link SolrTemplate#saveBeans(String, Collection)} is called, the
     * {@code Answer} verifies that one of the documents being updated pertains to {@code discoIri}, and that the status
     * field equals the {@code expectedStatus}.
     *
     * @param discoIri match the SolrDocument being updated.  There is expected to be exactly one match.
     * @param expectedStatus the {@link DiscoSolrDocument#DISCO_STATUS status} the matching Solr document must have
     * @param compositeAnswer the {@code CompositeAnswer} being built
     */
    @SuppressWarnings("unchecked")
    private static void assertUpdateMustContainHavingStatus(String discoIri, RMapStatus expectedStatus,
                                                            CompositeAnswer<DiscoPartialUpdate> compositeAnswer) {
        assertUpdateMustContainHavingStatus((partialUpdate) ->
                partialUpdate.getDiscoIri().equals(discoIri), expectedStatus, compositeAnswer);
    }

    /**
     * Sets an expectation on the provided {@code SolrTemplate} by creating a custom
     * {@link org.mockito.stubbing.Answer}.  When {@link SolrTemplate#saveBeans(String, Collection)} is called, the
     * {@code Answer} verifies that one of the documents being updated pertains to {@code discoIri}, and that the status
     * field equals the {@code expectedStatus}.
     *
     * @param mustContain matches the Solr Documents being updated.  Must not be {@code null}
     * @param expectedStatus the {@link DiscoSolrDocument#DISCO_STATUS status} the matching Solr documents must have
     * @param compositeAnswer the {@code CompositeAnswer} being built
     */
    @SuppressWarnings("unchecked")
    private static void assertUpdateMustContainHavingStatus(Predicate<DiscoPartialUpdate> mustContain,
                                                            RMapStatus expectedStatus,
                                                            CompositeAnswer<DiscoPartialUpdate> compositeAnswer) {
        compositeAnswer.andAnswer((inv) -> {
                    // Insures that the PartialUpdate going to the index contains the correct value for the
                    // disco_status field, and that some value is present for doc_last_updated
                    Set<DiscoPartialUpdate> updates = inv.getArgument(1);

                    // The predicate must match something, or we fail
                    assertTrue("No DiscoPartialUpdates matched.", updates.stream().anyMatch(mustContain));

                    updates.stream()
                            .filter(mustContain)
                            .forEach(partialUpdate -> {
                                assertValueForUpdateField(
                                        partialUpdate,
                                        DISCO_STATUS,
                                        expectedStatus.toString());

                                assertValuePresenceForUpdateField(
                                        partialUpdate,
                                        DOC_LAST_UPDATED);
                            });

                    return null;
                });
    }

    private static void assertDeleteMustContainId(String discoDocumentId, CompositeAnswer<?> compositeAnswer) {
        compositeAnswer.andAnswer((inv) -> {
            Collection<String> idsBeingDeleted = inv.getArgument(1);

            assertTrue("No DiscoIRIs matched.", idsBeingDeleted.stream().anyMatch(discoDocumentId::equals));

            return null;
        });
    }

    private static void assertUpdateMustContain(Predicate<DiscoPartialUpdate> mustContain,
                                                CompositeAnswer<DiscoPartialUpdate> compositeAnswer) {
        compositeAnswer.andAnswer((inv) -> {
                    Set<DiscoPartialUpdate> updates = inv.getArgument(1);
            assertTrue("Missing expected DiscoPartialUpdate when saving updates to the index.",
                    updates.stream().anyMatch(mustContain));
                    return null;
                });
    }

    private static void assertUpdateMustNotContain(Predicate<DiscoPartialUpdate> mustNotContain,
                                                   CompositeAnswer<DiscoPartialUpdate> compositeAnswer) {
        compositeAnswer.andAnswer((inv) -> {
                    Set<DiscoPartialUpdate> updates = inv.getArgument(1);
                    assertFalse("Encountered unexpected DiscoPartialUpdate when saving updates to the index.",
                            updates.stream().anyMatch(mustNotContain));
                    return null;
                });
    }

    private static void assertValueForUpdateField(DiscoPartialUpdate update, String fieldName, String expectedValue) {
        Stream<UpdateField> updateFields = update.getUpdates().stream();
        String actualValue = (String)updateFields.filter(updateField -> updateField.getName().equals(fieldName))
                .findAny()
                .orElseThrow(iae("Did not find an update field for " + fieldName))
                .getValue();
        assertEquals(expectedValue, actualValue);
    }

    private static void assertValuePresenceForUpdateField(DiscoPartialUpdate update, String fieldName) {
        Stream<UpdateField> updateFields = update.getUpdates().stream();
        Object actualValue = updateFields.filter(updateField -> updateField.getName().equals(fieldName))
                .findAny()
                .orElseThrow(iae("Did not find an update field for " + fieldName))
                .getValue();
        assertNotNull("Null value for update field " + fieldName, actualValue);
        if (actualValue instanceof String)
            assertTrue("Empty string for update field " + fieldName, ((String)actualValue).trim().length() > 0);
        if (actualValue instanceof Long)
            assertTrue("Uninitialized long for update field " + fieldName, ((Long) actualValue) > 0);
    }

    /**
     * Builds instances of {@link UpdateAssertions}.
     */
    static class Builder {

        private CompositeAnswer<DiscoPartialUpdate> compositeAnswer;

        private boolean built = false;

        Builder() {
            this.compositeAnswer = new CompositeAnswer<>();
        }

        Builder updateMustNotContain(Predicate<DiscoPartialUpdate> mustNotContain) {
            checkConstraints();
            assertUpdateMustNotContain(mustNotContain, compositeAnswer);
            return this;
        }

        Builder updateMustContain(Predicate<DiscoPartialUpdate> mustContain) {
            checkConstraints();
            assertUpdateMustContain(mustContain, compositeAnswer);
            return this;
        }

        Builder updateMustContainHavingStatus(Predicate<DiscoPartialUpdate> mustContain, RMapStatus status) {
            checkConstraints();
            assertUpdateMustContainHavingStatus(mustContain, status, compositeAnswer);
            return this;
        }

        Builder deleteMustContain(String discoDocumentId) {
            checkConstraints();
            assertDeleteMustContainId(discoDocumentId, compositeAnswer);
            return this;
        }

        CompositeAnswer build() {
            checkConstraints();
            return this.compositeAnswer;
        }

        private void checkConstraints() {
            if (built) {
                throw new IllegalStateException("Already built.  Instantiate a new builder.");
            }
        }
    }

}
