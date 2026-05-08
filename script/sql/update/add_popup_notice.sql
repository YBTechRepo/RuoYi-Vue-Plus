alter table sys_notice
    add column popup_flag char(1) default '0' comment '是否登录弹窗（0否 1是）' after status,
    add column popup_start_time datetime default null comment '弹窗开始时间' after popup_flag,
    add column popup_end_time datetime default null comment '弹窗结束时间' after popup_start_time;

create table if not exists sys_notice_read
(
    id          bigint(20)  not null comment '主键ID',
    tenant_id   varchar(20) not null default '000000' comment '租户编号',
    notice_id   bigint(20)  not null comment '公告ID',
    user_id     bigint(20)  not null comment '用户ID',
    read_time   datetime    not null comment '已读时间',
    create_dept bigint(20)  default null comment '创建部门',
    create_by   bigint(20)  default null comment '创建者',
    create_time datetime    default null comment '创建时间',
    update_by   bigint(20)  default null comment '更新者',
    update_time datetime    default null comment '更新时间',
    primary key (id),
    unique key uk_sys_notice_read (notice_id, user_id, tenant_id),
    key idx_sys_notice_read_user (user_id, tenant_id)
) engine=innodb default charset=utf8mb4 comment='通知公告已读记录表';
