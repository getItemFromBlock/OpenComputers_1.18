package li.cil.oc.client

import com.google.common.base.Strings
import li.cil.oc.OpenComputers
import li.cil.oc.api.detail.ManualAPI
import li.cil.oc.api.manual.ContentProvider
import li.cil.oc.api.manual.ImageProvider
import li.cil.oc.api.manual.ImageRenderer
import li.cil.oc.api.manual.PathProvider
import li.cil.oc.api.manual.TabIconRenderer
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

import scala.annotation.tailrec
import scala.collection.JavaConverters.asJavaIterable
import scala.collection.convert.ImplicitConversionsToJava._
import scala.collection.convert.ImplicitConversionsToScala._
import scala.collection.mutable

object Manual extends ManualAPI {
  final val LanguageKey = "%LANGUAGE%"

  final val FallbackLanguage = "en_us"

  class History(val path: String, var offset: Int = 0)

  class Tab(val renderer: TabIconRenderer, val tooltip: Option[String], val path: String)

  val tabs = mutable.Buffer.empty[Tab]

  val pathProviders = mutable.Buffer.empty[PathProvider]

  val contentProviders = mutable.Buffer.empty[ContentProvider]

  val imageProviders = mutable.Buffer.empty[(String, ImageProvider)]

  val history = new mutable.ArrayStack[History]

  reset()

  override def addTab(renderer: TabIconRenderer, tooltip: String, path: String): Unit = {
    tabs += new Tab(renderer, Option(tooltip), path)
    if (tabs.length > 7) {
      OpenComputers.log.warn("Gosh I'm popular! Too many tabs were added to the OpenComputers in-game manual, so some won't be shown. In case this actually happens, let me know and I'll look into making them scrollable or something...")
    }
  }

  override def addProvider(provider: PathProvider): Unit = {
    pathProviders += provider
  }

  override def addProvider(provider: ContentProvider): Unit = {
    contentProviders += provider
  }

  override def addProvider(prefix: String, provider: ImageProvider): Unit = {
    imageProviders += (if (Strings.isNullOrEmpty(prefix)) "" else prefix + ":") -> provider
  }

  override def pathFor(stack: ItemStack): String = {
    for (provider <- pathProviders) {
      val path = try provider.pathFor(stack) catch {
        case t: Throwable =>
          OpenComputers.log.warn("A path provider threw an error when queried with an item.", t)
          null
      }
      if (path != null) return path
    }
    null
  }

  override def pathFor(world: Level, pos: BlockPos): String = {
    for (provider <- pathProviders) {
      val path = try provider.pathFor(world, pos) catch {
        case t: Throwable =>
          OpenComputers.log.warn("A path provider threw an error when queried with a block.", t)
          null
      }
      if (path != null) return path
    }
    null
  }

  override def contentFor(path: String): java.lang.Iterable[String] = {
    val cleanPath = com.google.common.io.Files.simplifyPath(path)
    val language = Minecraft.getInstance().getLanguageManager().getSelected().getCode()
    contentForWithRedirects(cleanPath.replaceAll(LanguageKey, language)).
      orElse(contentForWithRedirects(cleanPath.replaceAll(LanguageKey, FallbackLanguage))).
      orNull
  }

  override def imageFor(href: String): ImageRenderer = {
    for ((prefix, provider) <- Manual.imageProviders.reverse) {
      if (href.startsWith(prefix)) {
        val image = try provider.getImage(href.stripPrefix(prefix)) catch {
          case t: Throwable =>
            OpenComputers.log.warn("An image provider threw an error when queried.", t)
            null
        }
        if (image != null) return image
      }
    }
    null
  }

  override def openFor(player: Player): Unit = {
    if (player.level.isClientSide) {
      val mc = Minecraft.getInstance
      if (player == mc.player) mc.pushGuiLayer(new gui.Manual())
    }
  }

  def reset(): Unit = {
    history.clear()
    history.push(new History(s"$LanguageKey/index.md"))
  }

  override def navigate(path: String): Unit = {
    Minecraft.getInstance.screen match {
      case manual: gui.Manual => manual.pushPage(path)
      case _ => history.push(new History(path))
    }
  }

  def makeRelative(path: String, base: String): String =
    if (path.startsWith("/")) path
    else {
      val splitAt = base.lastIndexOf('/')
      if (splitAt >= 0) base.splitAt(splitAt)._1 + "/" + path
      else path
    }

  @tailrec private def contentForWithRedirects(path: String, seen: List[String] = List.empty): Option[java.lang.Iterable[String]] = {
    if (seen.contains(path)) return Some(asJavaIterable(Iterable("Redirection loop: ") ++ seen ++ Iterable(path)))
    doContentLookup(path) match {
      case Some(content) => content.headOption match {
        case Some(line) if line.toLowerCase.startsWith("#redirect ") =>
          contentForWithRedirects(makeRelative(line.substring("#redirect ".length), path), seen :+ path)
        case _ => Some(content)
      }
      case _ => None
    }
  }

  private def doContentLookup(path: String): Option[java.lang.Iterable[String]] = {
    for (provider <- contentProviders) {
      val lines = try provider.getContent(path) catch {
        case t: Throwable =>
          OpenComputers.log.warn("A content provider threw an error when queried.", t)
          null
      }
      if (lines != null) return Some(lines)
    }
    None
  }
}
