CREATE DATABASE IF NOT EXISTS my_construction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'myconstruction_app'@'localhost' IDENTIFIED BY 'MyConstruction123!';
GRANT ALL PRIVILEGES ON my_construction.* TO 'myconstruction_app'@'localhost';
FLUSH PRIVILEGES;

