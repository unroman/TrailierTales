package net.frozenblock.trailiertales.mixin.client.brush;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.frozenblock.trailiertales.config.TTItemConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin<S extends LivingEntityRenderState, M extends EntityModel<S> & ArmedModel> {

	@Inject(
		method = "renderArmWithItem",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V",
			shift = At.Shift.AFTER
		)
	)
	void trailierTales$injectBrushAnim(
		S renderState,
		@Nullable BakedModel bakedModel,
		ItemStack itemStack,
		ItemDisplayContext displayContext,
		HumanoidArm humanoidArm,
		PoseStack poseStack,
		MultiBufferSource buffer,
		int packedLight,
		CallbackInfo info,
		@Local(ordinal = 0) boolean isLeftArm
	) {
		if (renderState instanceof HumanoidRenderState humanoidRenderState) {
			InteractionHand interactionHand = humanoidArm == humanoidRenderState.mainArm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			if (
				humanoidRenderState.isUsingItem
				&& humanoidRenderState.useItemHand == interactionHand
				&& humanoidRenderState.attackTime < 1.0E-5F
				&& itemStack.is(Items.BRUSH)
				&& TTItemConfig.SMOOTH_BRUSH_ANIMATION
			) {
				float remainingTicks = humanoidRenderState.ticksUsingItem + 1F;
				float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
				float brushProgress = remainingTicks + partialTick;
				float brushRoll = Mth.cos((brushProgress * Mth.PI) / 5F) * 1.2F;

				if (isLeftArm) {
					poseStack.mulPose(Axis.ZP.rotation(brushRoll));
				} else {
					poseStack.mulPose(Axis.ZN.rotation(brushRoll));
				}
			}
		}
	}
}
