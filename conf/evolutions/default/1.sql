# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table currency (
  id                        bigint auto_increment not null,
  code                      varchar(255),
  name                      varchar(255),
  factor                    integer,
  lowest_value              decimal(38),
  highest_value             decimal(38),
  avg_value                 decimal(38),
  constraint uq_currency_code unique (code),
  constraint pk_currency primary key (id))
;

create table currency_value (
  id                        bigint auto_increment not null,
  value                     decimal(38),
  currency_id               bigint,
  date_model_date           date,
  constraint pk_currency_value primary key (id))
;

create table date (
  date                      date not null,
  constraint pk_date primary key (date))
;

create sequence date_seq;

alter table currency_value add constraint fk_currency_value_currency_1 foreign key (currency_id) references currency (id) on delete restrict on update restrict;
create index ix_currency_value_currency_1 on currency_value (currency_id);
alter table currency_value add constraint fk_currency_value_dateModel_2 foreign key (date_model_date) references date (date) on delete restrict on update restrict;
create index ix_currency_value_dateModel_2 on currency_value (date_model_date);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists currency;

drop table if exists currency_value;

drop table if exists date;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists date_seq;

