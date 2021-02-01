DROP TABLE IF EXISTS jc_student_child;
DROP TABLE IF EXISTS jc_student_order;
DROP TABLE IF EXISTS jc_passport_office;
DROP TABLE IF EXISTS jc_register_office;
DROP TABLE IF EXISTS jc_country_struct;
DROP TABLE IF EXISTS jc_street;

--таблица с улицами
CREATE TABLE jc_street (
	street_code integer not NULL,
	street_name varchar(300),
	PRIMARY KEY (street_code)
);

-- формат задания места выдачи паспорта 00 000 000 0000
CREATE TABLE jc_country_struct (
	area_id char(12) not null,
	area_name varchar(200),
	PRIMARY KEY (area_id)
);

create table jc_passport_office (
	p_office_id integer not null,
	p_office_name varchar(200),
	p_office_area_id char(12) not null,
	PRIMARY KEY (p_office_id),
	FOREIGN KEY(p_office_area_id) REFERENCES jc_country_struct(area_id) ON DELETE RESTRICT
);

CREATE TABLE jc_register_office (
	r_office_id integer not null,
	r_office_name varchar(200),
	r_office_area_id char(12) not null,
	PRIMARY KEY (r_office_id),
	FOREIGN KEY(r_office_area_id) REFERENCES jc_country_struct(area_id) ON DELETE RESTRICT

);

CREATE TABLE jc_student_order (
	student_order_id SERIAL,
	h_sur_name varchar(100) not null,
	h_given_name varchar(100) not null,
	h_patronimic_name varchar(100) not null,
	h_pasport_seria varchar(10) not null,
	h_pasport_number varchar(10) not null,
	h_date date not null,
	h_pasport_office integer not null,
	h_date_of_birth date not null,
	h_post_index varchar(10),
	h_street_code integer not null,
	h_building varchar(10) not null,
	h_extention varchar(10),
	h_apartment varchar(10),

	w_sur_name varchar(100) not null,
	w_given_name varchar(100) not null,
	w_patronimic_name varchar(100) not null,
	w_pasport_seria varchar(10) not null,
	w_pasport_number varchar(10) not null,
	w_date date not null,
	w_pasport_office integer not null,
	w_date_of_birth date not null,
	w_post_index varchar(10),
	w_street_code integer not null,
	w_building varchar(10) not null,
	w_extention varchar(10),
	w_apartment varchar(10),

	certeficate_id varchar(20) not null,
	register_office_id integer not null,
	marriage_date date not null,

	PRIMARY KEY(student_order_id),
	FOREIGN KEY(h_street_code) REFERENCES jc_street(street_code) ON DELETE RESTRICT,
	FOREIGN KEY(w_street_code) REFERENCES jc_street(street_code) ON DELETE RESTRICT,
	FOREIGN KEY(register_office_id) REFERENCES jc_register_office(r_office_id) ON DELETE RESTRICT
);

CREATE TABLE jc_student_child(
	student_child_id SERIAL,
	student_order_id integer NOT NULL,
	c_sur_name varchar(100) not null,
	c_given_name varchar(100) not null,
	c_patronimic_name varchar(100) not null,
	c_certificate_number varchar(10) not null,
	c_certificate_date date not null,
	c_register_office_id integer not null,
	c_date_of_birth date not null,
	c_post_index varchar(10),
	c_street_code integer not null,
	c_building varchar(10) not null,
	c_extention varchar(10),
	c_apartment varchar(10),

	PRIMARY KEY(student_child_id),
	FOREIGN KEY(c_street_code) REFERENCES jc_street(street_code) ON DELETE RESTRICT,
	FOREIGN KEY(c_register_office_id) REFERENCES jc_register_office(r_office_id) ON DELETE RESTRICT

);