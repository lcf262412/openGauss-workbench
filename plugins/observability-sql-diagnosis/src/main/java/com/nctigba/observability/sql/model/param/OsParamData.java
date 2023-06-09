package com.nctigba.observability.sql.model.param;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public enum OsParamData {
    TcpMaxTwBuckets("net.ipv4.tcp_max_tw_buckets"),
    TcpTwReuse("net.ipv4.tcp_tw_reuse"),
    TcpTwRecycle("net.ipv4.tcp_tw_recycle"),
    TcpKeepaliveTime("net.ipv4.tcp_keepalive_time"),
    TcpKeepaliveProbes("net.ipv4.tcp_keepalive_probes"),
    TcpKeepaliveIntvl("net.ipv4.tcp_keepalive_intvl"),
    TcpRetries1("net.ipv4.tcp_retries1"),
    TcpSynRetries("net.ipv4.tcp_syn_retries"),
    TcpSynackRetries("net.ipv4.tcp_synack_retries"),
    TcpRetries2("net.ipv4.tcp_retries2"),
    OvercommitMemory("vm.overcommit_memory"),
    TcpRmem("net.ipv4.tcp_rmem"),
    TcpWmem("net.ipv4.tcp_wmem"),
    WmemMax("net.core.wmem_max"),
    RmemMax("net.core.rmem_max"),
    WmemDefault("net.core.wmem_default"),
    RmemDefault("net.core.rmem_default"),
    IpLocalPortRange("net.ipv4.ip_local_port_range"),
    Sem("kernel.sem"),
    MinFreeKbytes("vm.min_free_kbytes"),
    Somaxconn("net.core.somaxconn"),
    TcpSyncookies("net.ipv4.tcp_syncookies"),
    NetdevMaxBacklog("net.core.netdev_max_backlog"),
    TcpMaxSynBacklog("net.ipv4.tcp_max_syn_backlog"),
    TcpFinTimeout("net.ipv4.tcp_fin_timeout"),
    Shmall("kernel.shmall"),
    Shmmax("kernel.shmmax"),
    TcpSack("net.ipv4.tcp_sack"),
    TcpTimestamps("net.ipv4.tcp_timestamps"),
    ExtfragThreshold("vm.extfrag_threshold"),
    OvercommitRatio("vm.overcommit_ratio"),
    Mtu("MTU");
    private String paramName;

    OsParamData(String paramName) {
        this.paramName=paramName;
    }
}
