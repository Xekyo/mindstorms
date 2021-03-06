package marvin;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.RConsole;

public class Configuration implements CancelUpdater {

    private final class CancelListener implements ButtonListener {
        @Override
        public void buttonReleased(Button b) {
            cancel = true;
        }

        @Override
        public void buttonPressed(Button b) {
        }
    }

    public static final int MAX_ANGLE = 150;
    public static final int EDGE_DETECTION_ANGLE = 50;
    private static final String SENSOR_DATA_FILE_NAME = "sensorData.txt";
    private static final boolean DEBUG_MODE = false;
    private static final boolean REMOTE_DEBUG = false;
    private static final int MAX_LINE_SIZE = 20;

    private final LightSensor light;
    private final NXTRegulatedMotor leftWheel;
    private final NXTRegulatedMotor rightWheel;
    private final NXTRegulatedMotor sensorMotor;
    private final ArrayList<DataSet> sensorData;
    private final DataOutputStream sensorDataFile;
    private final UltrasonicSensor ultraSonic;
    private final MovementPrimitives movementPrimitives;
    private final SensorDataCollector sensorDataCollector;
    private boolean cancel = false;
    private final ArrayList<LineBorders> lines;
    private final TouchSensor rightTouchSensor;
    private final ArrayList<Step> steps = new ArrayList<>();
    private Step currentStep;
    private final TouchSensor leftTouchSensor;

    public Configuration() throws IOException {
        super();
        rightTouchSensor = new TouchSensor(SensorPort.S1);
        leftTouchSensor = new TouchSensor(SensorPort.S3);
        light = new LightSensor(SensorPort.S4);
        ultraSonic = new UltrasonicSensor(SensorPort.S2);
        leftWheel = Motor.B;
        rightWheel = Motor.A;
        sensorMotor = Motor.C;
        lines = new ArrayList<>();
        sensorData = new ArrayList<>();
        movementPrimitives = new MovementPrimitives(this);
        sensorDataCollector = new SensorDataCollector(this);
        Button.ESCAPE.addButtonListener(new CancelListener());
        File file = new File(SENSOR_DATA_FILE_NAME);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        sensorDataFile = new DataOutputStream(new FileOutputStream(file));
    }

    public TouchSensor getRightTouchSensor() {
        return rightTouchSensor;
    }

    public TouchSensor getLeftTouchSensor() {
        return leftTouchSensor;
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

    @Override
    public boolean isCancel() {
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
        if (DEBUG_MODE) {
            sensorDataFile.writeUTF("\r\n");
            sensorDataFile.flush();
        }
        sensorDataFile.close();
        sensorMotor.rotateTo(0);
    }

    public void write(String data) {
        if (DEBUG_MODE) {
            try {
                sensorDataFile.writeUTF(data);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                throw new RuntimeException(e);
            }
        }
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

    public void addNewLine(LineBorders line) {
        if (lines.size() > MAX_LINE_SIZE) {
            lines.remove(0);
        }
        lines.add(line);
    }

    public List<LineBorders> getLines() {
        return lines;
    }

    public void startConsole() {
        if (REMOTE_DEBUG) {
            RConsole.openUSB(0);
        }
    }

    public void addStep(Step step) {
        if (currentStep == null) {
            currentStep = step;
            return;
        }
        steps.add(step);
    }

    public void nextStep() {
        if (steps.isEmpty()) {
            return;
        }
        currentStep = steps.remove(0);
        sensorDataCollector.resetBarcode();
        System.out.println("Next step");
    }

    public void runCurrentStep() {
        if (currentStep == null) {
            return;
        }
        System.out.println("Step: " + currentStep.getName());
        currentStep.run(this);
    }

    public void printSteps() {
        System.out.println(currentStep.getName());
        for (Step step : steps) {
            System.out.println(step.getName());
        }
    }

}
