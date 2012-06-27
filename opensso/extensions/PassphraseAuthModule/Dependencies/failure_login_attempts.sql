CREATE TABLE "failure_login_attempts" (
	"user_id" varchar2(50) NOT NULL,
	"failure_type" integer NOT NULL,
	"ip_address" varchar2(15) NOT NULL,
	"failure_time" timestamp NOT NULL,
	"is_read" integer NOT NULL
);