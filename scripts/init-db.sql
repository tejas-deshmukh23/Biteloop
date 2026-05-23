-- This runs once when the Postgres container first starts.
-- Creates one database per service (database-per-service pattern).

CREATE DATABASE tiffin_users;
CREATE DATABASE tiffin_providers;
CREATE DATABASE tiffin_menus;
CREATE DATABASE tiffin_orders;
CREATE DATABASE tiffin_subscriptions;
CREATE DATABASE tiffin_payments;
CREATE DATABASE tiffin_admin;

-- Grant all to the tiffin user
GRANT ALL PRIVILEGES ON DATABASE tiffin_users TO tiffin;
GRANT ALL PRIVILEGES ON DATABASE tiffin_providers TO tiffin;
GRANT ALL PRIVILEGES ON DATABASE tiffin_menus TO tiffin;
GRANT ALL PRIVILEGES ON DATABASE tiffin_orders TO tiffin;
GRANT ALL PRIVILEGES ON DATABASE tiffin_subscriptions TO tiffin;
GRANT ALL PRIVILEGES ON DATABASE tiffin_payments TO tiffin;
GRANT ALL PRIVILEGES ON DATABASE tiffin_admin TO tiffin;
