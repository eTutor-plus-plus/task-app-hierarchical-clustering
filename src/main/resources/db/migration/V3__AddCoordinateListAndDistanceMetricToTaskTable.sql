CREATE TYPE distance_metric AS ENUM ('euclidean', 'manhattan');

CREATE CAST (CHARACTER VARYING AS distance_metric) WITH INOUT AS IMPLICIT;

ALTER TABLE task
    ADD COLUMN coordinate_list JSONB,
    ADD COLUMN metric DISTANCE_METRIC;
