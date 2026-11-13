package com.flavor_immersed_daily_argo.fishing;

/** Dependency-free regression checks; run with the verifyFishingPull Gradle task. */
public final class FishingPullDynamicsTest {
    private static int checks;

    public static void main(String[] args) {
        directionsAndEnvelope();
        contactAndRelease();
        travelNotFrames();
        cubicSensitivity();
        System.out.println("Fishing pull regression checks passed: " + checks);
    }

    private static void directionsAndEnvelope() {
        FishingPullDynamics dynamics = new FishingPullDynamics();
        boolean[] quadrants = new boolean[4];
        int changingBearings = 0;
        for (int seed = 1; seed <= 32; seed++) {
            dynamics.reset(seed, 1.0F);
            for (int cycle = 0; cycle < 8; cycle++) {
                float start = cycle * 26.0F;
                var first = dynamics.sample(start + 2.0F);
                var later = dynamics.sample(start + 10.0F);
                if (Math.abs(Math.sin(first.bearing() - later.bearing())) > 1.0E-5D) {
                    changingBearings++;
                }
                var nextFrame = dynamics.sample(start + 2.1F);
                near(Math.cos(nextFrame.bearing()), nextFrame.forceX()
                                / Math.hypot(nextFrame.forceX(), nextFrame.forceY()), 1.0E-6D,
                        "fractional-tick bearing follows current force");
                near(Math.sin(nextFrame.bearing()), nextFrame.forceY()
                                / Math.hypot(nextFrame.forceX(), nextFrame.forceY()), 1.0E-6D,
                        "fractional-tick vertical direction follows current force");
                check(first.strength() > 0.9F, "fast attack");
                check(later.strength() < first.strength(), "release envelope");
                near(dynamics.sample(start + 22.0F).strength(), 0.0D, 0.0D, "slack between tugs");
                check(Math.hypot(first.yaw(), first.pitch()) <= 12.001D, "bounded camera torque");
                quadrants[(first.yaw() < 0 ? 1 : 0) + (first.pitch() < 0 ? 2 : 0)] = true;
                near(Math.cos(first.bearing()), first.yaw() / Math.hypot(first.yaw(), first.pitch()),
                        1.0E-6D, "HUD x bearing matches camera torque");
                near(Math.sin(first.bearing()), first.pitch() / Math.hypot(first.yaw(), first.pitch()),
                        1.0E-6D, "HUD y bearing matches camera torque");
            }
        }
        check(changingBearings > 200, "direction is live within each tug, not an initial bearing snapshot");
        for (boolean covered : quadrants) {
            check(covered, "all four screen quadrants represented");
        }
        dynamics.reset(123, Float.NaN);
        check(Float.isFinite(dynamics.sample(2).yaw()), "invalid tension cannot poison camera");
    }

    private static void contactAndRelease() {
        FishingPullDynamics dynamics = new FishingPullDynamics();
        dynamics.reset(42, 1);
        var pull = dynamics.sample(3);
        double x = pull.yaw() / Math.hypot(pull.yaw(), pull.pitch());
        double y = pull.pitch() / Math.hypot(pull.yaw(), pull.pitch());
        check(!dynamics.counterInput(3, 0, 0), "stationary mouse cannot latch");
        check(!dynamics.counterInput(3, x * 10, y * 10), "following tug cannot latch");
        near(dynamics.mouseGain(3, -y, x), 1, 1.0E-6, "perpendicular mouse stays free");
        near(dynamics.mouseGain(3, x, y), 1, 1.0E-6, "following mouse stays free");
        check(dynamics.counterInput(3, -x * 3, -y * 3), "opposing travel latches");
        near(dynamics.sample(3).impact(), 1, 0, "immediate contact feedback");
        near(dynamics.sample(4).yaw(), pull.yaw(), 1.0E-6, "brief visual latch");
        FishingPullDynamics reference = new FishingPullDynamics();
        reference.reset(42, 1);
        var current = dynamics.sample(4);
        var expected = reference.sample(4);
        near(current.forceX(), expected.forceX(), 0, "camera latch must not freeze live horizontal force");
        near(current.forceY(), expected.forceY(), 0, "camera latch must not freeze live vertical force");
        near(current.bearing(), expected.bearing(), 0, "HUD direction remains live during contact");
        double gain = dynamics.mouseGain(3, -x, -y);
        check(gain >= 0.15 && gain < 0.4, "strong but never fully locked mouse response");
        check(!dynamics.counterInput(4, -x * 100, -y * 100), "no repeated latch during same tug");
        near(dynamics.sample(8).impact(), 0, 0, "contact automatically expires");
        var next = dynamics.sample(29);
        check(dynamics.counterInput(29, -next.yaw(), -next.pitch()), "next tug rearms contact");
        dynamics.reset(42, 1);
        near(dynamics.sample(3).impact(), 0, 0, "reset clears contact");
    }

    private static void travelNotFrames() {
        float at30 = triggerAtFps(30);
        float at60 = triggerAtFps(60);
        float at240 = triggerAtFps(240);
        check(Math.abs(at30 - at240) <= 20.0F / 30 + 0.001F, "30 vs 240 FPS: at most one low-FPS sample late");
        check(Math.abs(at60 - at240) <= 20.0F / 60 + 0.001F, "60 vs 240 FPS: travel, not frame count");
    }

    private static float triggerAtFps(int fps) {
        FishingPullDynamics dynamics = new FishingPullDynamics();
        dynamics.reset(77, 1);
        var pull = dynamics.sample(2);
        double length = Math.hypot(pull.yaw(), pull.pitch());
        double pixelsPerFrame = 120.0D / fps;
        for (int frame = 1; frame <= fps; frame++) {
            float time = 2.0F + frame * 20.0F / fps;
            if (dynamics.counterInput(time, -pull.yaw() / length * pixelsPerFrame,
                    -pull.pitch() / length * pixelsPerFrame)) {
                return time;
            }
        }
        throw new AssertionError("No counter contact at FPS " + fps);
    }

    private static void cubicSensitivity() {
        for (double sensitivity : new double[]{0, 0.01, 0.25, 0.5, 1}) {
            for (double gain : new double[]{0.15, 0.23, 0.5, 0.78, 1}) {
                double result = FishingPullDynamics.sensitivityForGain(sensitivity, gain);
                double oldBase = sensitivity * 0.6F + 0.2F;
                double newBase = result * 0.6F + 0.2F;
                check(newBase > 0, "temporary sensitivity never reverses mouse");
                near(newBase * newBase * newBase / (oldBase * oldBase * oldBase), gain, 1.0E-10,
                        "linear gain must survive the cubic transform, even at minimum sensitivity");
            }
        }
    }

    private static void near(double actual, double expected, double epsilon, String message) {
        check(Double.isFinite(actual) && Math.abs(actual - expected) <= epsilon,
                message + ": actual=" + actual + ", expected=" + expected);
    }

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
