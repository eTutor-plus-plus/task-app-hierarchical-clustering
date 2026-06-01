ALTER TABLE submission ALTER COLUMN submission TYPE TEXT;

ALTER TABLE task RENAME COLUMN coordinate_list TO coordinate_system;

ALTER TABLE merge RENAME COLUMN cluster_left TO source_cluster_1;
ALTER TABLE merge RENAME COLUMN cluster_right TO source_cluster_2;
