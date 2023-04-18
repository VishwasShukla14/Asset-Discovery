package com.serviceops.assetdiscovery.rest;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.serviceops.assetdiscovery.entity.enums.IpRangeType;
import com.serviceops.assetdiscovery.entity.enums.ScanType;
import com.serviceops.assetdiscovery.rest.base.SingleBaseRest;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;
import java.util.List;

public class NetworkScanRest extends SingleBaseRest {
    private String name;
    @Enumerated(EnumType.STRING)
    private ScanType scanType;
    private Long schedulerRefId;
    @Enumerated(EnumType.STRING)
    private IpRangeType ipRangeType;
    private List<Long> refIds;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd-MM-yyyy")
    private LocalDateTime lastScan;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd-MM-yyyy")

    private LocalDateTime nextScan;
    private boolean isEnabled;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScanType getScanType() {
        return scanType;
    }

    public void setScanType(ScanType scanType) {
        this.scanType = scanType;
    }

    public LocalDateTime getLastScan() {
        return lastScan;
    }

    public void setLastScan(LocalDateTime lastScan) {
        this.lastScan = lastScan;
    }

    public LocalDateTime getNextScan() {
        return nextScan;
    }

    public void setNextScan(LocalDateTime nextScan) {
        this.nextScan = nextScan;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public IpRangeType getIpRangeType() {
        return ipRangeType;
    }

    public void setIpRangeType(IpRangeType ipRangeType) {
        this.ipRangeType = ipRangeType;
    }

    public List<Long> getRefIds() {
        return refIds;
    }

    public void setRefIds(List<Long> refIds) {
        this.refIds = refIds;
    }

    public Long getSchedulerRefId() {
        return schedulerRefId;
    }

    public void setSchedulerRefId(Long schedulerRefId) {
        this.schedulerRefId = schedulerRefId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NetworkScanRest that = (NetworkScanRest) o;

        if (isEnabled() != that.isEnabled()) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getScanType() != that.getScanType()) return false;
        if (getSchedulerRefId() != null ? !getSchedulerRefId().equals(that.getSchedulerRefId()) : that.getSchedulerRefId() != null)
            return false;
        if (getIpRangeType() != that.getIpRangeType()) return false;
        if (getRefIds() != null ? !getRefIds().equals(that.getRefIds()) : that.getRefIds() != null) return false;
        if (getLastScan() != null ? !getLastScan().equals(that.getLastScan()) : that.getLastScan() != null)
            return false;
        return getNextScan() != null ? getNextScan().equals(that.getNextScan()) : that.getNextScan() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getScanType() != null ? getScanType().hashCode() : 0);
        result = 31 * result + (getSchedulerRefId() != null ? getSchedulerRefId().hashCode() : 0);
        result = 31 * result + (getIpRangeType() != null ? getIpRangeType().hashCode() : 0);
        result = 31 * result + (getRefIds() != null ? getRefIds().hashCode() : 0);
        result = 31 * result + (getLastScan() != null ? getLastScan().hashCode() : 0);
        result = 31 * result + (getNextScan() != null ? getNextScan().hashCode() : 0);
        result = 31 * result + (isEnabled() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NetworkScanRest{" +
                "name='" + name + '\'' +
                ", scanType=" + scanType +
                ", schedulerRefId=" + schedulerRefId +
                ", ipRangeType=" + ipRangeType +
                ", refIds=" + refIds +
                ", lastScan=" + lastScan +
                ", nextScan=" + nextScan +
                ", isEnabled=" + isEnabled +
                "} " + super.toString();
    }
}