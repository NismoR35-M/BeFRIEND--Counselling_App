package com.group.project.faceFilters;

@SuppressWarnings("ALL")
public interface CameraGrabberListener {
    void onCameraInitialized();
    void onCameraError(String errorMsg);
}
