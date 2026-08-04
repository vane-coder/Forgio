-- Department scoping for raw materials and machines.
-- Nullable: a NULL department_id means the resource is factory-wide (shared),
-- visible to every department. A set department_id restricts visibility to
-- workers of that department (managers/dept-heads still see everything).

ALTER TABLE raw_materials
    ADD COLUMN department_id UUID NULL;

ALTER TABLE raw_materials
    ADD CONSTRAINT fk_raw_materials_department
        FOREIGN KEY (department_id) REFERENCES departments (dept_id);

CREATE INDEX idx_raw_materials_department ON raw_materials (department_id);

ALTER TABLE machines
    ADD COLUMN department_id UUID NULL;

ALTER TABLE machines
    ADD CONSTRAINT fk_machines_department
        FOREIGN KEY (department_id) REFERENCES departments (dept_id);

CREATE INDEX idx_machines_department ON machines (department_id);
