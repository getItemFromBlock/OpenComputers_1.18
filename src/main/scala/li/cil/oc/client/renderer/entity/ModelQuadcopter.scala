package li.cil.oc.client.renderer.entity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import li.cil.oc.common.entity.Drone
import net.minecraft.client.renderer.LightTexture
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.model.geom.builders.PartDefinition
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.phys.Vec3
import com.mojang.math.Vector3f

final class ModelQuadcopter(rootPart : ModelPart) extends EntityModel[Drone](rootPart) {

  val body = rootPart.getChild("body")
  val wing0 = rootPart.getChild("wing0")
  val wing1 = rootPart.getChild("wing1")
  val wing2 = rootPart.getChild("wing2")
  val wing3 = rootPart.getChild("wing3")
  val light0 = rootPart.getChild("light0")
  val light1 = rootPart.getChild("light1")
  val light2 = rootPart.getChild("light2")
  val light3 = rootPart.getChild("light3")
  
  def createBodyLayer() : LayerDefinition = {
    val mesh = new MeshDefinition()
    val root = mesh.getRoot()
    root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 23).addBox(-3, 1, -3, 6, 1, 6).texOffs(0, 1).addBox(-1, 0, -1, 2, 1, 2)
      .texOffs(0, 17).addBox(-2, -1, -2, 4, 1, 4), PartPose.rotation(0, math.toRadians(45).toFloat, 0))
    root.addOrReplaceChild("wing0", CubeListBuilder.create().texOffs(0, 9).addBox(1, 0, -7, 6, 1, 6).texOffs(0, 27).addBox(2, -1, -3, 1, 3, 1), PartPose.ZERO)
    root.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(0, 9).addBox(1, 0, 1, 6, 1, 6).texOffs(0, 27).addBox(2, -1, 2, 1, 3, 1), PartPose.ZERO)
    root.addOrReplaceChild("wing2", CubeListBuilder.create().texOffs(0, 9).addBox(-7, 0, 1, 6, 1, 6).texOffs(0, 27).addBox(-3, -1, 2, 1, 3, 1), PartPose.ZERO)
    root.addOrReplaceChild("wing3", CubeListBuilder.create().texOffs(0, 9).addBox(-7, 0, -7, 6, 1, 6).texOffs(0, 27).addBox(-3, -1, -3, 1, 3, 1), PartPose.ZERO)
    root.addOrReplaceChild("light0", CubeListBuilder.create().texOffs(24, 0).addBox(1, 0, -7, 6, 1, 6), PartPose.ZERO)
    root.addOrReplaceChild("light1", CubeListBuilder.create().texOffs(24, 0).addBox(1, 0, 1, 6, 1, 6), PartPose.ZERO)
    root.addOrReplaceChild("light2", CubeListBuilder.create().texOffs(24, 0).addBox(-7, 0, 1, 6, 1, 6), PartPose.ZERO)
    root.addOrReplaceChild("light3", CubeListBuilder.create().texOffs(24, 0).addBox(-7, 0, -7, 6, 1, 6), PartPose.ZERO)
    return LayerDefinition.create(mesh, 64, 32)
  }

  private val up = new Vec3(0, 1, 0)

  private def doRender(drone: Drone, dt: Float, stack: PoseStack, builder: VertexConsumer, light: Int, overlay: Int, r: Float, g: Float, b: Float, a: Float): Unit = {
    stack.pushPose()
    if (drone.isRunning) {
      val timeJitter = drone.hashCode() ^ 0xFF
      stack.translate(0, (math.sin(timeJitter + (drone.level.getGameTime + dt) / 20.0) * (1 / 16f)).toFloat, 0)
    }

    val direction = drone.getDeltaMovement.normalize()
    if (direction.dot(up) < 0.99) {
      // Flying sideways.
      val rotationAxis = direction.cross(up)
      val relativeSpeed = drone.getDeltaMovement.length().toFloat / drone.maxVelocity
      stack.mulPose(new Vector3f(rotationAxis).rotationDegrees(relativeSpeed * -20))
    }

    stack.mulPose(Vector3f.YP.rotationDegrees(drone.bodyAngle))
    body.render(stack, builder, light, overlay, r, g, b, a)

    wing0.xRot = drone.flapAngles(0)(0)
    wing0.zRot = drone.flapAngles(0)(1)
    wing1.xRot = drone.flapAngles(1)(0)
    wing1.zRot = drone.flapAngles(1)(1)
    wing2.xRot = drone.flapAngles(2)(0)
    wing2.zRot = drone.flapAngles(2)(1)
    wing3.xRot = drone.flapAngles(3)(0)
    wing3.zRot = drone.flapAngles(3)(1)

    wing0.render(stack, builder, light, overlay, r, g, b, a)
    wing1.render(stack, builder, light, overlay, r, g, b, a)
    wing2.render(stack, builder, light, overlay, r, g, b, a)
    wing3.render(stack, builder, light, overlay, r, g, b, a)

    if (drone.isRunning) {
      light0.xRot = drone.flapAngles(0)(0)
      light0.zRot = drone.flapAngles(0)(1)
      light1.xRot = drone.flapAngles(1)(0)
      light1.zRot = drone.flapAngles(1)(1)
      light2.xRot = drone.flapAngles(2)(0)
      light2.zRot = drone.flapAngles(2)(1)
      light3.xRot = drone.flapAngles(3)(0)
      light3.zRot = drone.flapAngles(3)(1)

      val lightColor = drone.lightColor
      val rr = r * ((lightColor >>> 16) & 0xFF) / 255f
      val gg = g * ((lightColor >>> 8) & 0xFF) / 255f
      val bb = b * ((lightColor >>> 0) & 0xFF) / 255f
      val fullLight = LightTexture.pack(15, 15)

      light0.render(stack, builder, fullLight, OverlayTexture.NO_OVERLAY, rr, gg, bb, a)
      light1.render(stack, builder, fullLight, OverlayTexture.NO_OVERLAY, rr, gg, bb, a)
      light2.render(stack, builder, fullLight, OverlayTexture.NO_OVERLAY, rr, gg, bb, a)
      light3.render(stack, builder, fullLight, OverlayTexture.NO_OVERLAY, rr, gg, bb, a)
    }
    stack.popPose()
  }

  private var cachedEntity: Drone = null
  private var cachedDt = 0.0f

  override def setupAnim(drone: Drone, f1: Float, f2: Float, f3: Float, f4: Float, f5: Float): Unit = {}

  override def prepareMobModel(drone: Drone, f1: Float, f2: Float, dt: Float): Unit = {
    cachedEntity = drone
    cachedDt = dt
  }

  override def renderToBuffer(stack: PoseStack, builder: VertexConsumer, light: Int, overlay: Int, r: Float, g: Float, b: Float, a: Float): Unit = {
    doRender(cachedEntity, cachedDt, stack, builder, light: Int, overlay: Int, r: Float, g: Float, b: Float, a: Float)
  }
}
