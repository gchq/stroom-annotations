/*
 * This file is generated by jOOQ.
*/
package stroom.db.annotations;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;

import stroom.db.DefaultCatalog;
import stroom.db.annotations.tables.AnnotationsHistory;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Annotations extends SchemaImpl {

    private static final long serialVersionUID = -793039442;

    /**
     * The reference instance of <code>annotations</code>
     */
    public static final Annotations ANNOTATIONS = new Annotations();

    /**
     * The table <code>annotations.annotations</code>.
     */
    public final stroom.db.annotations.tables.Annotations ANNOTATIONS_ = stroom.db.annotations.tables.Annotations.ANNOTATIONS_;

    /**
     * The table <code>annotations.annotations_history</code>.
     */
    public final AnnotationsHistory ANNOTATIONS_HISTORY = stroom.db.annotations.tables.AnnotationsHistory.ANNOTATIONS_HISTORY;

    /**
     * No further instances allowed
     */
    private Annotations() {
        super("annotations", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            stroom.db.annotations.tables.Annotations.ANNOTATIONS_,
            AnnotationsHistory.ANNOTATIONS_HISTORY);
    }
}
