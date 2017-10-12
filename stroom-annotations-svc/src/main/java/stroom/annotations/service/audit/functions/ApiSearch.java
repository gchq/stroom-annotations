package stroom.annotations.service.audit.functions;

import event.logging.*;

import javax.ws.rs.core.Response;

public class ApiSearch extends ApiCall {
    private String q;
    private String seekId;
    private Long seekLastUpdated;

    public ApiSearch(final Response response,
                     final Exception exception,
                     final String q,
                     final String seekId,
                     final Long seekLastUpdated) {
        super(response, exception);
        this.q = q;
        this.seekId = seekId;
        this.seekLastUpdated = seekLastUpdated;
    }

    @Override
    public void enrichEventDetail(Event.EventDetail eventDetail) {
        eventDetail.setTypeId("SEARCH");
        eventDetail.setDescription("Freetext search through Annotations");

        final Search search = new Search();
        eventDetail.setSearch(search);

        final Query query = new Query();
        search.setQuery(query);

        final Query.Advanced queryTerms = new Query.Advanced();
        query.setAdvanced(queryTerms);

        final Term qTerm = new Term();
        queryTerms.getAdvancedQueryItems().add(qTerm);
        qTerm.setName("q");
        qTerm.setValue(q);
        qTerm.setCondition(TermCondition.CONTAINS);

        if (null != seekId) {
            final Term seekIdTerm = new Term();
            queryTerms.getAdvancedQueryItems().add(seekIdTerm);
            seekIdTerm.setName("seekId");
            seekIdTerm.setValue(seekId);
            seekIdTerm.setCondition(TermCondition.GREATER_THAN);
        }

        if (null != seekLastUpdated) {
            final Term seekLastUpdatedTerm = new Term();
            queryTerms.getAdvancedQueryItems().add(seekLastUpdatedTerm);
            seekLastUpdatedTerm.setName("seekLastUpdated");
            seekLastUpdatedTerm.setValue(Long.toString(seekLastUpdated));
            seekLastUpdatedTerm.setCondition(TermCondition.GREATER_THAN);
        }
    }
}
