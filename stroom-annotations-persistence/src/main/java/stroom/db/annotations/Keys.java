/*
 * This file is generated by jOOQ.
*/
package stroom.db.annotations;


import javax.annotation.Generated;

import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;

import stroom.db.annotations.tables.Annotations;
import stroom.db.annotations.tables.records.AnnotationsRecord;


/**
 * A class modelling foreign key relationships between tables of the <code>annotations</code> 
 * schema
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.3"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // IDENTITY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<AnnotationsRecord> KEY_ANNOTATIONS_PRIMARY = UniqueKeys0.KEY_ANNOTATIONS_PRIMARY;

    // -------------------------------------------------------------------------
    // FOREIGN KEY definitions
    // -------------------------------------------------------------------------


    // -------------------------------------------------------------------------
    // [#1459] distribute members to avoid static initialisers > 64kb
    // -------------------------------------------------------------------------

    private static class UniqueKeys0 extends AbstractKeys {
        public static final UniqueKey<AnnotationsRecord> KEY_ANNOTATIONS_PRIMARY = createUniqueKey(Annotations.ANNOTATIONS_, "KEY_annotations_PRIMARY", Annotations.ANNOTATIONS_.ID);
    }
}
