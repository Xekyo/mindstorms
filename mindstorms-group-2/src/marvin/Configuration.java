package marvin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class Configuration {

    private final class CancelListener implements ButtonListener {
        @Override
        public void buttonReleased(Button b) {
            cancel = true;
        }

        @Override
        public void buttonPressed(Button b) {
        }
    }

    private static final String SENSOR_DATA_FILE_NAME = "sensorData.txt";
    private static final String LAST_POSITION_FILE_NAME = "lastPosition.txt";
    private static final boolean DEBUG_MODE = true;

    private final LightSensor light;
    private final NXTRegulatedMotor leftWheel;
    private final NXTRegulatedMotor rightWheel;
    private final NXTRegulatedMotor sensorMotor;
    private final ArrayList<DataSet> sensorData;
    private final DataOutputStream sensorDataFile;
    private final UltrasonicSensor ultraSonic;
    private final MovementPrimitives movementPrimitives;
    private final SensorDataCollector sensorDataCollector;
    private final FollowLine followLine;
    private boolean cancel = false;

    public Configuration() throws IOException {
        super();
        light = new LightSensor(SensorPort.S4);
        ultraSonic = new UltrasonicSensor(SensorPort.S2);
        leftWheel = Motor.B;
        rightWheel = Motor.A;
        sensorMotor = Motor.C;
        sensorMotor.setSpeed(sensorMotor.getMaxSpeed());
        sensorData = new ArrayList<>();
        movementPrimitives = new MovementPrimitives(this);
        followLine = new FollowLine(movementPrimitives);
        sensorDataCollector = new SensorDataCollector(this);
        Button.ESCAPE.addButtonListener(new CancelListener());
        File file = new File(SENSOR_DATA_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        sensorDataFile = new DataOutputStream(new FileOutputStream(file));
    }

    public LightSensor getLight() {
        return light;
    }

    public UltrasonicSensor getUltraSonic() {
        return ultraSonic;
    }

    public NXTRegulatedMotor getSensorMotor() {
        return sensorMotor;
    }

    public SensorDataCollector getSensorDataCollector() {
        return sensorDataCollector;
    }

    public MovementPrimitives getMovementPrimitives() {
        return movementPrimitives;
    }

    public void followLine() {
        followLine.run(this);
    }

    public boolean cancel() {
        return cancel;
    }

    public void displayInformation() {
        displayInformation(getLight());
    }

    private void displayInformation(LightSensor light) {
        LCD.drawInt(light.getLightValue(), 4, 0, 0);
        LCD.drawInt(light.getNormalizedLightValue(), 4, 0, 1);
        LCD.drawInt(SensorPort.S2.readRawValue(), 4, 0, 2);
        LCD.drawInt(SensorPort.S2.readValue(), 4, 0, 3);
        LCD.drawInt(SensorPort.S4.readRawValue(), 4, 0, 5);
        LCD.drawInt(SensorPort.S4.readValue(), 4, 0, 6);
    }

    public void updateSensorData(DataSet dataset) {
        sensorData.add(dataset);
        if (DEBUG_MODE) {
            try {
                sensorDataFile.writeUTF(dataset.toString());
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public NXTRegulatedMotor getLeftWheel() {
        return leftWheel;
    }

    public NXTRegulatedMotor getRightWheel() {
        return rightWheel;
    }

    public void save() throws IOException {
        sensorDataFile.writeUTF("\r\n");
        sensorDataFile.flush();
        sensorDataFile.close();
    }

    public ArrayList<DataSet> getSensorData() {
        return sensorData;
    }

    public DataSet getLastSensorData() {
        if (sensorData.isEmpty()) {
            return new DataSet(1);
        }
        return sensorData.get(sensorData.size() - 1);
    }

    public void saveLastSensorPosition() throws IOException {
        File file = new File(LAST_POSITION_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        DataOutputStream lastPosition = new DataOutputStream(new FileOutputStream(file));
        lastPosition.writeInt(sensorMotor.getPosition());
        lastPosition.flush();
        lastPosition.close();
    }

    public void restoreLastSensorPosition() throws IOException {
        File file = new File(LAST_POSITION_FILE_NAME);
        if (!file.exists()) {
            return;
        }
        try (DataInputStream lastInput = new DataInputStream(new FileInputStream(file))) {
            int lastPosition = lastInput.readInt();
            sensorMotor.rotateTo(-lastPosition);
            sensorMotor.resetTachoCount();
        }
    }

}
