package li.cil.oc.client.renderer.block

import java.util
import java.util.Collections

import li.cil.oc.client.Textures
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.block.model.BakedQuad
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.core.Direction

import scala.collection.JavaConverters.bufferAsJavaList
import scala.collection.mutable

object RobotModel extends SmartBlockModelBase {
  override def getOverrides: ItemOverrides = ItemOverride

  object ItemModel extends SmartBlockModelBase {
    private val size = 0.4f
    private val l = 0.5f - size
    private val h = 0.5f + size

    private val top = (0.5f, 1f, 0.5f, 0.25f, 0.25f)
    private val top1 = (l, 0.5f, h, 0f, 0f)
    private val top2 = (h, 0.5f, h, 0f, 0.5f)
    private val top3 = (h, 0.5f, l, 0.5f, 0.5f)
    private val top4 = (l, 0.5f, l, 0.5f, 0f)

    private val bottom = (0.5f, 0f, 0.5f, 0.75f, 0.25f)
    private val bottom1 = (l, 0.5f, l, 0.5f, 0.5f)
    private val bottom2 = (h, 0.5f, l, 0.5f, 0f)
    private val bottom3 = (h, 0.5f, h, 1f, 0f)
    private val bottom4 = (l, 0.5f, h, 1f, 0.5f)

    // I don't know why this is super-bright when using 0xFF888888 :/
    private val tint = 0xFF555555

    protected def robotTexture = Textures.getSprite(Textures.Item.Robot)

    private def interpolate(v0: (Float, Float, Float, Float, Float), v1: (Float, Float, Float, Float, Float)) =
      (v0._1 * 0.5f + v1._1 * 0.5f,
        v0._2 * 0.5f + v1._2 * 0.5f,
        v0._3 * 0.5f + v1._3 * 0.5f,
        v0._4 * 0.5f + v1._4 * 0.5f,
        v0._5 * 0.5f + v1._5 * 0.5f)

    private def quad(verts: (Float, Float, Float, Float, Float)*) = {
      val added = interpolate(verts.last, verts.head)
      (verts :+ added).flatMap {
        case ((x, y, z, u, v)) => rawData(
          (x - 0.5f) * 1.4f + 0.5f,
          (y - 0.5f) * 1.4f + 0.5f,
          (z - 0.5f) * 1.4f + 0.5f,
          Direction.UP, robotTexture, robotTexture.getU(u * 16), robotTexture.getV(v * 16),
          White)
      }.toArray
    }

    override def getQuads(state: BlockState, side: Direction, rand: util.Random): util.List[BakedQuad] = {
      val faces = mutable.ArrayBuffer.empty[BakedQuad]

      faces += new BakedQuad(quad(top, top1, top2), tint, Direction.NORTH, robotTexture, true)
      faces += new BakedQuad(quad(top, top2, top3), tint, Direction.EAST, robotTexture, true)
      faces += new BakedQuad(quad(top, top3, top4), tint, Direction.SOUTH, robotTexture, true)
      faces += new BakedQuad(quad(top, top4, top1), tint, Direction.WEST, robotTexture, true)

      faces += new BakedQuad(quad(bottom, bottom1, bottom2), tint, Direction.NORTH, robotTexture, true)
      faces += new BakedQuad(quad(bottom, bottom2, bottom3), tint, Direction.EAST, robotTexture, true)
      faces += new BakedQuad(quad(bottom, bottom3, bottom4), tint, Direction.SOUTH, robotTexture, true)
      faces += new BakedQuad(quad(bottom, bottom4, bottom1), tint, Direction.WEST, robotTexture, true)

      bufferAsJavaList(faces)
    }
  }

  object ItemOverride extends ItemOverrides {
    override def resolve(originalModel: BakedModel, stack: ItemStack, world: ClientLevel, entity: LivingEntity): BakedModel = ItemModel
  }

}
