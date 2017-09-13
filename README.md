# stroom-auth
Pre-release version of a Stroom annotation service.

## stroom-auth-svc
A service that handles CRUD operations for Annotations and the Annotation Types

## stroom-persistence
This module accesses the existing Stroom database. Eventually the relevant tables will be migrated to a service. But until then we'll access them in this fashion, using JOOQ.

### Making a database change in dev
Obviously you'll lose test data if you do this.

1. Stop the database container and delete it
2. Change the migrations to whatever SQL you need
3. Run the app to perform the migrations (or use the Flyway command line)
4. Delete the old models at `stroom-persistence/src/main/java/stroom`.
5. Run `./gradlew generateAuthdbJooqSchemaSource` to generate the models again
6. Restart app
