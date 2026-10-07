ALTER TABLE citas ADD COLUMN reagendada boolean;

update citas set reagendada = false