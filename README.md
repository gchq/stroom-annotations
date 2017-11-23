# stroom-annotations
Pre-release version of a Stroom annotation service.

## stroom-annotations-svc
A service that handles CRUD operations for Annotations and the Annotation Types

## stroom-persistence
This module connects to a MariaDB database for storing the annotations. Uses the Flyway module to build the database

### Making a database change in dev
Obviously you'll lose test data if you do this.

1. Stop the database container and delete it
2. Change the migrations to whatever SQL you need
3. Run the app to perform the migrations (or use the Flyway command line)
4. Delete the old models at `stroom-persistence/src/main/java/stroom`.
5. Run `./gradlew generateAnnotationdbJooqSchemaSource` to generate the models again
6. Restart app
