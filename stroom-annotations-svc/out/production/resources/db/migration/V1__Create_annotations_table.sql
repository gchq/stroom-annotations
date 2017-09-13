-- Following Simon Holywell's style guide: http://www.sqlstyle.guide/
CREATE TABLE annotations (
    id 				      VARCHAR(255) NOT NULL,
    content               VARCHAR(2048) NOT NULL,
    PRIMARY KEY           (id)
) ENGINE=InnoDB DEFAULT CHARSET latin1;
