-- Following Simon Holywell's style guide: http://www.sqlstyle.guide/
CREATE TABLE annotations (
    id 				VARCHAR(255) NOT NULL,
    status          VARCHAR(255) NOT NULL,
    assignTo        VARCHAR(255),
    content         VARCHAR(8092),
    updatedBy       VARCHAR(255) NOT NULL,
    lastUpdated     BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY     (id)
) ENGINE=InnoDB DEFAULT CHARSET latin1;
