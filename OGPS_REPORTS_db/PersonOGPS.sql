drop schema if exists OGPS_esquema cascade;
create schema OGPS_esquema;

CREATE TABLE person (
    id_person SERIAL PRIMARY KEY,
    user_name VARCHAR(255),
    token VARCHAR(255),
    mail VARCHAR(255) NOT NULL UNIQUE CHECK (
        mail ~* '^[a-zA-Z0-9._%+-]+@(gmail|outlook|hotmail)\.com$'
    ),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL CHECK (
        password ~ '^(?=.*[A-Z])(?=.*\d)(?=.*[\W_]).{8,}$'
    ),
    role VARCHAR(50) NOT NULL CHECK (role IN ('Administrator', 'User'))
);


CREATE TABLE administrator (
    id_admin SERIAL PRIMARY KEY,
    id_person INT NOT NULL UNIQUE REFERENCES person(id_person) ON DELETE CASCADE
);

CREATE TABLE users (
    id_user SERIAL PRIMARY KEY,
    id_person INT NOT NULL UNIQUE REFERENCES person(id_person) ON DELETE CASCADE
);

CREATE TABLE incident (
    id_incident SERIAL PRIMARY KEY,
    id_user INT REFERENCES users(id_user) ON DELETE SET NULL,
    id_admin INT REFERENCES administrator(id_admin) ON DELETE SET NULL,
    latitude DECIMAL(9,6) NOT null,
    longitude DECIMAL(9,6) NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('Potholes and Defects', 'Street Lighting', 'Traffic Accidents', 'Obstacles', 'Other')),
    description TEXT,
    status VARCHAR(50) CHECK (status IN ('Reported', 'In Process', 'Resolved')) NOT NULL,
    report_date VARCHAR(50)
);


CREATE TABLE evidence (
    id_evidence SERIAL PRIMARY KEY,
    id_incident INT NOT NULL REFERENCES incident(id_incident) ON DELETE CASCADE,
    url_photo TEXT NOT NULL
);

CREATE TABLE validation_request (
    id_request SERIAL PRIMARY KEY,
    id_user INT NOT NULL REFERENCES users(id_user) ON DELETE CASCADE,
  	id_admin INT NOT NULL REFERENCES administrator(id_admin) ON DELETE cascade,
  	id_evidence INT NOT NULL REFERENCES evidence(id_evidence) ON DELETE cascade,
    quantity INT NOT NULL
);

CREATE TABLE manage_status (
    id_incident INT NOT NULL REFERENCES incident(id_incident) ON DELETE CASCADE,
  	id_admin INT NOT NULL REFERENCES administrator(id_admin) ON DELETE cascade
);

CREATE TABLE reports (
    id_user INT NOT NULL REFERENCES users(id_user) ON DELETE CASCADE,
  	id_incident INT NOT NULL REFERENCES incident(id_incident) ON DELETE cascade
);






