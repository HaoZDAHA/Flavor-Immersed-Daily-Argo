// Save this class in your mod and generate all required imports

/**
 * Made with Blockbench 4.11.0
 * Exported for Minecraft version 1.19 or later with Mojang mappings
 * @author 15244
 */
public class waterwheelAnimation {
	public static final AnimationDefinition waterwheel_round = AnimationDefinition.Builder.withLength(16.0F).looping()
		.addAnimation("wheel", new AnimationChannel(AnimationChannel.Targets.ROTATION, 
			new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(3.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 90.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(7.9167F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 180.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(12.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 270.0F), AnimationChannel.Interpolations.LINEAR),
			new Keyframe(16.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 362.5F), AnimationChannel.Interpolations.LINEAR)
		))
		.build();
}