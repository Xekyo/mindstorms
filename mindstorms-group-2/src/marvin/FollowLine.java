package marvin;

//import MovementPrimitives;

public class FollowLine implements Step {

    private final MovementPrimitives movPrim;

    public FollowLine(MovementPrimitives movPrim) {
        this.movPrim = movPrim;
    }

    @Override
    public void run(Configuration configuration) {
        evaluateStraightCase(configuration).adjustCourse(movPrim);
    }

    private enum StraightCase {
        LOST() {
            @Override
            public void adjustCourse(MovementPrimitives movPrim) {
                movPrim.crawl();
                movPrim.backup();
            }
        },

        STRAIGHT() {

            @Override
            public void adjustCourse(MovementPrimitives movPrim) {
                movPrim.slow();
            }
        },

        ORTHOGONAL() {

            @Override
            public void adjustCourse(MovementPrimitives movPrim) {
                movPrim.correctionLeft(); // TODO handle better
            }
        },

        TURN_LEFT() {

            @Override
            public void adjustCourse(MovementPrimitives movPrim) {
                movPrim.turnLeft();
            }
        },

        TURN_RIGHT() {

            @Override
            public void adjustCourse(MovementPrimitives movPrim) {
                movPrim.turnRight();
            }
        },
        CORRECTION_LEFT() {

            @Override
            public void adjustCourse(MovementPrimitives movPrim) {
                movPrim.correctionLeft();
            }
        },

        CORRECTION_RIGHT() {

            @Override
            public void adjustCourse(MovementPrimitives movPrim) {
                movPrim.correctionRight();
            }
        },
        SPIN_LEFT() {

            @Override
            public void adjustCourse(MovementPrimitives movPrim) {
                movPrim.spinLeft();
            }
        },

        SPIN_RIGHT() {

            @Override
            public void adjustCourse(MovementPrimitives movPrim) {
                movPrim.spinRight();
            }
        };

        public abstract void adjustCourse(MovementPrimitives movPrim);

    }

    private StraightCase evaluateStraightCase(Configuration config) {
        StraightCase currentCase = null;
        LineBorders lineBorders = config.getLines().get(config.getLines().size() - 1);
        int rightBorder = lineBorders.getBrightToDark();
        int leftBorder = lineBorders.getDarkToBright();
        int lineWidth = rightBorder - leftBorder;
        float lineCenter = (rightBorder + leftBorder) / 2;

        if (rightBorder == Integer.MIN_VALUE && leftBorder == Integer.MIN_VALUE) {
            currentCase = StraightCase.LOST;
        } else if (rightBorder == Integer.MIN_VALUE) {
            currentCase = StraightCase.TURN_RIGHT;
        } else if (leftBorder == Integer.MIN_VALUE) {
            currentCase = StraightCase.TURN_LEFT;
        } else if (lineWidth > 90) { // TODO: How wide is the line?
            // Line is too wide, might be orthogonal line or corner.
            // TODO what is with long lines which have both ends?
            if (leftBorder >= 115) {
                // Line is to the right of center
                currentCase = StraightCase.SPIN_RIGHT;
            } else {
                // Line is to the left of center
                currentCase = StraightCase.SPIN_LEFT;
            }
        } else if (lineWidth > 0) {
            // We're still on the line.
            if (64 < lineCenter && lineCenter < 94) {
                // Line is centrally in front of us.
                currentCase = StraightCase.STRAIGHT;
            } else if (lineCenter >= 94) {
                // Line is to the right of center
                currentCase = StraightCase.CORRECTION_RIGHT;
            } else {
                // Line is to the left of center
                currentCase = StraightCase.CORRECTION_LEFT;
            }
        } else {
            currentCase = StraightCase.LOST;
        }
        config.write(currentCase.name());
        return currentCase;
    }
}