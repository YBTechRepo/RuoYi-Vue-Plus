alter table sys_notice
    add column client_id varchar(64) not null default '' comment '客户端ID' after status;

create index idx_sys_notice_client on sys_notice (client_id);
