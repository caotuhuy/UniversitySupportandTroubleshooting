package com.donga.qlhotro.dto;

import com.donga.qlhotro.enums.RoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoomRequestDTO {

    @NotBlank(message = "Mã phòng không được để trống")
    @Size(max = 30, message = "Mã phòng tối đa 30 ký tự")
    private String roomCode;

    @NotBlank(message = "Tên phòng không được để trống")
    @Size(max = 100, message = "Tên phòng tối đa 100 ký tự")
    private String roomName;

    @Size(max = 50, message = "Tòa nhà tối đa 50 ký tự")
    private String building;

    private Integer floor;

    @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
    private String description;

    private RoomStatus status;

    public RoomRequestDTO() {
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }
}
