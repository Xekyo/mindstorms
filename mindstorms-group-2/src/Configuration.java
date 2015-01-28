import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.UltrasonicSensor;

public class Configuration {

    private static final String LAST_POSITION_FILE_NAME = "lastPosition.txt";
    private final LightSensor light;
    private final NXTRegulatedMotor leftWheel;
    private final NXTRegulatedMotor rightWheel;
    private final NXTRegulatedMotor sensorMotor;
    private final ArrayList<DataSet> sensorData;
    private final DataOutputStream someFile;
    private final UltrasonicSensor ultraSonic;

    public Configuration() throws IOException {
        super();
        light = new LightSensor(SensorPort.S4);
        ultraSonic = new UltrasonicSensor(SensorPort.S2);
        leftWheel = Motor.A;
        rightWheel = Motor.B;
        sensorMotor = Motor.C;
        sensorMotor.setSpeed(0.05f * sensorMotor.getMaxSpeed());
        sensorData = new ArrayList<>();
        File file = new File("sensorData.txt");
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        someFile = new DataOutputStream(new FileOutputStream(file));
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

    public boolean cancel() {
        return Button.ESCAPE.isDown();
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
        try {
            someFile.writeUTF(dataset.toString());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public NXTRegulatedMotor getLeftWheel() {
        return leftWheel;
    }

    public NXTRegulatedMotor getRightWheel() {
        return rightWheel;
    }

    public void save() throws IOException {
        someFile.writeUTF("\r\n");
        someFile.flush();
        someFile.close();
    }

    public ArrayList<DataSet> getSensorData() {
        return sensorData;
    }

    public DataSet getLastSensorData() {
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
