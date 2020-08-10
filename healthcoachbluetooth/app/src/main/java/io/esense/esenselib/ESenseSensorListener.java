package io.esense.esenselib;

import org.json.JSONException;

public interface ESenseSensorListener {
    /**
     * Called when there is new sensor data available
     * @param evt object containing the sensor samples received
     */
    void onSensorChanged(ESenseEvent evt) throws JSONException;
}
