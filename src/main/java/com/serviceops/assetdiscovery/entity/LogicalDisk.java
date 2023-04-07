package com.serviceops.assetdiscovery.entity;

import com.serviceops.assetdiscovery.entity.mapped.ExternalAssetBase;
import jakarta.persistence.Entity;

@Entity
public class LogicalDisk extends ExternalAssetBase {
    private String name;
    private String description;
    private String fileSystemType;
    private String driveType;

    private long size;
    private long freeSpace;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileSystemType() {
        return fileSystemType;
    }

    public void setFileSystemType(String fileSystemType) {
        this.fileSystemType = fileSystemType;
    }

    public String getDriveType() {
        return driveType;
    }

    public void setDriveType(String driveType) {
        this.driveType = driveType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogicalDisk that = (LogicalDisk) o;

        if (getSize() != that.getSize()) return false;
        if (getFreeSpace() != that.getFreeSpace()) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        if (getFileSystemType() != null ? !getFileSystemType().equals(that.getFileSystemType()) : that.getFileSystemType() != null)
            return false;
        return getDriveType() != null ? getDriveType().equals(that.getDriveType()) : that.getDriveType() == null;
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getFileSystemType() != null ? getFileSystemType().hashCode() : 0);
        result = 31 * result + (getDriveType() != null ? getDriveType().hashCode() : 0);
        result = 31 * result + (int) (getSize() ^ (getSize() >>> 32));
        result = 31 * result + (int) (getFreeSpace() ^ (getFreeSpace() >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "LogicalDisk{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", fileSystemType='" + fileSystemType + '\'' +
                ", driveType='" + driveType + '\'' +
                ", size=" + size +
                ", freeSpace=" + freeSpace +
                '}';
    }
}
