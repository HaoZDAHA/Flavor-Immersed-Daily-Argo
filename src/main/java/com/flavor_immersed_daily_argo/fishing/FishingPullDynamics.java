package com.flavor_immersed_daily_argo.fishing;

/**
 * Tick-time fishing feedback, independent of Minecraft and render FPS.
 * One source of truth for camera torque, HUD bearing and counter-pull resistance.
 * Screen coordinates: +x turns right, +y turns down. No progress or catch authority lives here.
 */
public final class FishingPullDynamics {
    private static final float CYCLE_TICKS = 26.0F;
    private static final float PULL_TICKS = 19.0F;
    private long seed;
    private float amplitude;
    private long counterCycle = -1;
    private long inputCycle = -1;
    private double counterTravel;
    private float impactTime = -100.0F;
    private float heldYaw;
    private float heldPitch;

    // Instantaneous force is separate from the camera offset, which may briefly latch on contact.
    public record Sample(float yaw, float pitch, float strength, float impact, float forceX, float forceY) {
        public double bearing() {
            return Math.atan2(forceY, forceX);
        }
    }

    public FishingPullDynamics() {
        reset(0L, 1.0F);
    }

    public void reset(long newSeed, float tension) {
        seed = newSeed;
        amplitude = Float.isFinite(tension) ? clamp(tension, 0.35F, 2.0F) : 1.0F;
        counterCycle = -1;
        inputCycle = -1;
        counterTravel = 0.0D;
        impactTime = -100.0F;
        heldYaw = 0.0F;
        heldPitch = 0.0F;
    }

    public Sample sample(float time) {
        float age = Math.max(0.0F, time) % CYCLE_TICKS;
        // Fast attack, a toothy loaded plateau, then line tension releases into a short rest.
        float strength;
        if (age < 2.0F) {
            strength = age / 2.0F;
        } else if (age < 6.0F) {
            strength = 0.96F + 0.04F * (float) Math.cos((age - 2.0F) * Math.PI);
        } else if (age < PULL_TICKS) {
            float release = (age - 6.0F) / (PULL_TICKS - 6.0F);
            strength = (1.0F - release) * (1.0F - release);
        } else {
            strength = 0.0F;
        }
        long cycle = cycle(time);
        long bits = mix(seed + cycle * 0x9E3779B97F4A7C15L);
        double heading = (bits >>> 11) * 0x1.0p-53 * Math.PI * 2.0D;
        // Fish changes its pull continuously within a tug. All consumers sample the same
        // current vector at fractional tick time; never lock the indicator to the initial heading.
        double angle = heading + 0.55D * Math.sin(age * 0.19D + heading)
                + 0.16D * Math.sin(age * 0.43D + heading * 2.0D);
        float forceX = (float) Math.cos(angle) * strength * amplitude;
        float forceY = (float) Math.sin(angle) * strength * amplitude;
        float yaw = forceX * 12.0F;
        float pitch = forceY * 12.0F;
        float impact = impactAt(time);
        if (counterCycle == cycle) {
            // A brief visual latch, then a soft release. Never stop the game or the player tick.
            yaw += (heldYaw - yaw) * impact;
            pitch += (heldPitch - pitch) * impact;
        }
        return new Sample(yaw, pitch, strength, impact, forceX, forceY);
    }

    /** Accumulate mouse travel, not frames; a tug can latch at most once. */
    public boolean counterInput(float time, double mouseX, double mouseY) {
        long cycle = cycle(time);
        Sample pull = sample(time);
        double length = Math.hypot(pull.forceX, pull.forceY);
        if (pull.strength < 0.3F || length < 0.001D || counterCycle == cycle) {
            return false;
        }
        if (inputCycle != cycle) {
            inputCycle = cycle;
            counterTravel = 0.0D;
        }
        double opposite = -(mouseX * pull.forceX + mouseY * pull.forceY) / length;
        counterTravel = Math.max(0.0D, counterTravel + opposite);
        if (counterTravel < 2.0D) {
            return false;
        }
        counterCycle = cycle;
        impactTime = time;
        heldYaw = pull.yaw;
        heldPitch = pull.pitch;
        return true;
    }

    /** Gain in linear mouse-turn space, not in Minecraft's cubic sensitivity-option space. */
    public float mouseGain(float time, double mouseX, double mouseY) {
        Sample pull = sample(time);
        double dot = mouseX * pull.forceX + mouseY * pull.forceY;
        double lengths = Math.hypot(mouseX, mouseY) * Math.hypot(pull.forceX, pull.forceY);
        if (lengths < 1.0E-8D || dot >= -lengths * 0.15D || pull.strength < 0.05F) {
            return 1.0F;
        }
        return 1.0F - 0.22F * pull.strength - 0.55F * pull.impact;
    }

    /** Invert Minecraft's cubic sensitivity transform without changing the stored option. */
    public static double sensitivityForGain(double sensitivity, double gain) {
        if (!Double.isFinite(sensitivity) || !Double.isFinite(gain)) {
            return sensitivity;
        }
        double base = sensitivity * 0.6F + 0.2F;
        if (base <= 0.0D) {
            return sensitivity;
        }
        double retained = Math.max(0.15D, Math.min(1.0D, gain));
        // This temporary option-space value may be negative at low sensitivity, but
        // base*cbrt(retained) stays positive: the mouse direction can never be inverted.
        return (base * Math.cbrt(retained) - 0.2F) / 0.6F;
    }

    private float impactAt(float time) {
        float age = time - impactTime;
        if (age < 0.0F || age >= 4.0F) {
            return 0.0F;
        }
        if (age <= 1.5F) {
            return 1.0F; // ~75 ms of contact, followed by ~125 ms of release.
        }
        float release = (age - 1.5F) / 2.5F;
        return 1.0F - release * release * (3.0F - 2.0F * release);
    }

    private static long cycle(float time) {
        return (long) Math.floor(Math.max(0.0F, time) / CYCLE_TICKS);
    }

    private static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
