package stroom.annotations.service.resources;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.annotations.service.hibernate.CriteriaStore;
import stroom.annotations.service.hibernate.IsDataSourceField;
import stroom.dashboard.expression.v1.FieldIndexMap;
import stroom.datasource.api.v2.DataSource;
import stroom.datasource.api.v2.DataSourceField;
import stroom.query.api.v2.*;
import stroom.query.common.v2.*;
import stroom.util.shared.HasTerminate;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class QueryResourceImpl<T> implements QueryResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryResourceImpl.class);

    private final SessionFactory database;

    private final Class<T> persistedClass;

    private final List<DataSourceField> fields;

    static final int QUERY_LIMIT = 10;

    public QueryResourceImpl(final Class<T> persistedClass, final SessionFactory database) {
        this.database = database;
        this.persistedClass = persistedClass;

        this.fields = Arrays.stream(persistedClass.getMethods()).map(method -> method.getAnnotation(IsDataSourceField.class))
                .filter(Objects::nonNull)
                .map(IsDataSourceField::fieldSupplier)
                .map(aClass -> {
                    try {
                        return aClass.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        LOGGER.warn("Could not create instance of DataSourceField supplier with " + aClass.getName());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .map(Supplier::get)
                .collect(Collectors.toList());
    }

    @Override
    public Response getDataSource(final DocRef docRef) {
        return Response
                .accepted(new DataSource(this.fields))
                .build();
    }

    @Override
    public Response search(final SearchRequest request) {

        final Session session = database.getCurrentSession();
        final CriteriaBuilder cb = database.getCurrentSession().getCriteriaBuilder();

        final CriteriaQuery<Tuple> cq = cb.createTupleQuery();
        final Root<T> root = cq.from(this.persistedClass);

        cq.multiselect(this.fields.stream()
                .map(f -> root.get(f.getName()))
                .collect(Collectors.toList()));

        cq.where(getPredicate(cb, root, request.getQuery().getExpression()));
        final List<Tuple> tuples = session.createQuery(cq).getResultList();
        final SearchResponse searchResponse = projectResults(request, tuples);

        return Response
                .accepted(searchResponse)
                .build();
    }

    private SearchResponse projectResults(final SearchRequest searchRequest,
                                        final List<Tuple> tuples) {
        tuples.forEach(a -> {
            LOGGER.info("Result Found " + a);
            for (int x=0; x<this.fields.size(); x++) {
                final Object value = a.get(x);
                LOGGER.info(String.format("%s: %s", this.fields.get(x).getName(), value));

            }

        });

        // TODO: possibly the mapping from the componentId to the coprocessorsettings map is a bit odd.
        final CoprocessorSettingsMap coprocessorSettingsMap = CoprocessorSettingsMap.create(searchRequest);

        Map<CoprocessorSettingsMap.CoprocessorKey, Coprocessor> coprocessorMap = new HashMap<>();
        // TODO: Mapping to this is complicated! it'd be nice not to have to do this.
        final FieldIndexMap fieldIndexMap = new FieldIndexMap(true);

        // Compile all of the result component options to optimise pattern matching etc.
        if (coprocessorSettingsMap.getMap() != null) {
            for (final Map.Entry<CoprocessorSettingsMap.CoprocessorKey, CoprocessorSettings> entry : coprocessorSettingsMap.getMap().entrySet()) {
                final CoprocessorSettingsMap.CoprocessorKey coprocessorId = entry.getKey();
                final CoprocessorSettings coprocessorSettings = entry.getValue();

                // Create a parameter map.
                final Map<String, String> paramMap = Collections.emptyMap();
                if (searchRequest.getQuery().getParams() != null) {
                    for (final Param param : searchRequest.getQuery().getParams()) {
                        paramMap.put(param.getKey(), param.getValue());
                    }
                }

                final Coprocessor coprocessor = createCoprocessor(
                        coprocessorSettings, fieldIndexMap, paramMap, new HasTerminate() {
                            //TODO do something about this
                            @Override
                            public void terminate() {
                                System.out.println("terminating");
                            }

                            @Override
                            public boolean isTerminated() {
                                return false;
                            }
                        });

                if (coprocessor != null) {
                    coprocessorMap.put(coprocessorId, coprocessor);
                }
            }
        }

        //TODO TableCoprocessor is doing a lot of work to pre-process and aggregate the datas

        for (Tuple criteriaDataPoint : tuples) {
            String[] dataArray = new String[fieldIndexMap.size()];

            //TODO should probably drive this off a new fieldIndexMap.getEntries() method or similar
            //then we only loop round fields we car about
            for (int x=0; x<this.fields.size(); x++) {
                final Object value = criteriaDataPoint.get(x);
                final String fieldName = this.fields.get(x).getName();

                int posInDataArray = fieldIndexMap.get(fieldName);
                //if the fieldIndexMap returns -1 the field has not been requested
                if (posInDataArray != -1) {
                    dataArray[posInDataArray] = value.toString();
                }
            }

            LOGGER.info("Data Array" + Arrays.stream(dataArray)
                    .map(s -> ", " + s)
                    .reduce("", (s, s2) -> s + s2, (s, s2) -> s + s2));

            coprocessorMap.entrySet().forEach(coprocessor -> {
                coprocessor.getValue().receive(dataArray);
            });
        }

        // TODO putting things into a payload and taking them out again is a waste of time in this case. We could use a queue instead and that'd be fine.
        //TODO: 'Payload' is a cluster specific name - what lucene ships back from a node.
        // Produce payloads for each coprocessor.
        Map<CoprocessorSettingsMap.CoprocessorKey, Payload> payloadMap = null;
        if (coprocessorMap != null && coprocessorMap.size() > 0) {
            for (final Map.Entry<CoprocessorSettingsMap.CoprocessorKey, Coprocessor> entry : coprocessorMap.entrySet()) {
                final Payload payload = entry.getValue().createPayload();
                if (payload != null) {
                    if (payloadMap == null) {
                        payloadMap = new HashMap<>();
                    }
                    payloadMap.put(entry.getKey(), payload);
                }
            }
        }


        // Construct the store
        CriteriaStore store = new CriteriaStore(Collections.singletonList(10), new StoreSize(Collections.singletonList(10)));
        store.process(coprocessorSettingsMap);
        store.coprocessorMap(coprocessorMap);
        store.payloadMap(payloadMap);

        // defaultMaxResultsSizes could be obtained from the StatisticsStore but at this point that object is ephemeral.
        // It seems a little pointless to put it into the StatisticsStore only to get it out again so for now
        // we'll just get it straight from the config.

        final SearchResponseCreator searchResponseCreator = new SearchResponseCreator(store);

        LOGGER.info("Creating Results");

        final SearchResponse searchResponse = searchResponseCreator.create(searchRequest);

        for (final Result result : searchResponse.getResults()) {
            LOGGER.info("Result " + result.toString() + " " + result.getClass().getName());

            if (result instanceof TableResult) {
                final TableResult tableResult = (TableResult) result;
                tableResult.getRows().forEach(row ->
                    LOGGER.info("Row: " + row)
                );
            } else if (result instanceof FlatResult) {
                final FlatResult flatResult = (FlatResult) result;
                flatResult.getValues().forEach(values -> {
                    LOGGER.info("Values");
                    values.forEach(o -> LOGGER.info("\t" + o));
                });
            }
        }

        if (null != searchResponse.getErrors()) {
            LOGGER.info("Showing Errors");
            searchResponse.getErrors().forEach(LOGGER::warn);
        }

        LOGGER.info("And done");

        return searchResponse;
    }

    //TODO This lives in stroom and should have a copy here
    public static Coprocessor createCoprocessor(final CoprocessorSettings settings,
                                                final FieldIndexMap fieldIndexMap, final Map<String, String> paramMap, final HasTerminate taskMonitor) {
        if (settings instanceof TableCoprocessorSettings) {
            final TableCoprocessorSettings tableCoprocessorSettings = (TableCoprocessorSettings) settings;
            final TableCoprocessor tableCoprocessor = new TableCoprocessor(
                    tableCoprocessorSettings, fieldIndexMap, taskMonitor, paramMap);
            return tableCoprocessor;
        }
        return null;
    }

    private Predicate getPredicate(final CriteriaBuilder cb,
                                   final Root<T> root,
                                   final ExpressionItem item) {
        if (item instanceof ExpressionTerm) {
            final ExpressionTerm term = (ExpressionTerm) item;

            switch (term.getCondition()) {
                case EQUALS: {
                    return cb.equal(root.get(term.getField()), term.getValue());
                }
                case CONTAINS: {
                    return cb.like(root.get(term.getField()), "%" + term.getValue() + "%");
                }
                case BETWEEN: {
                    final String[] parts = term.getValue().split(",");
                    if (parts.length == 2) {
                        return cb.between(root.get(term.getField()), parts[0], parts[1]);
                    }
                    break;
                }
                case GREATER_THAN: {
                    return cb.greaterThan(root.get(term.getField()), term.getValue());
                }
                case GREATER_THAN_OR_EQUAL_TO: {
                    return cb.greaterThanOrEqualTo(root.get(term.getField()), term.getValue());
                }
                case LESS_THAN: {
                    return cb.lessThan(root.get(term.getField()), term.getValue());
                }
                case LESS_THAN_OR_EQUAL_TO: {
                    return cb.lessThanOrEqualTo(root.get(term.getField()), term.getValue());
                }
                case IN: {
                    final String[] parts = term.getValue().split(",");
                    return root.get(term.getField()).in(parts);
                }
                case IN_DICTIONARY: {
                    // Not sure how to handle this yet
                }

            }

        } else if (item instanceof ExpressionOperator) {
            final ExpressionOperator operator = (ExpressionOperator) item;

            final Predicate[] children = operator.getChildren().stream()
                    .map(c -> getPredicate(cb, root, c))
                    .toArray(Predicate[]::new);

            switch (operator.getOp()) {
                case AND:
                    return cb.and(children);
                case OR:
                    return cb.or(children);
                case NOT:

                    if (children.length == 1) {
                        // A single child, just apply the 'not' to that first item
                        return cb.not(children[0]);
                    } else if (children.length > 1) {
                        // If there are multiple children, apply an and around them all
                        return cb.and(Arrays.stream(children)
                                .map(cb::not)
                                .toArray(Predicate[]::new));
                    }
                default:
                    // Fall through to null if there aren't any children
                    break;
            }
        }

        return null;
    }

    @Override
    public Response destroy(final QueryKey queryKey) {
        return Response
                .accepted(Boolean.TRUE)
                .build();
    }
}
