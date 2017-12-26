package actors

import actors.ReviewItemsActor._
import akka.actor.{Actor, Props}
import models.items.ItemEntry
import services.ItemService


class ReviewItemsActor(implicit itemService: ItemService) extends Actor {


  override def receive: Receive = {
    case GetItems =>
      sender() ! itemService.getItems
    case AddItems(items) =>
      sender() ! itemService.addItems(items)
    case ParseItems(items) =>
      sender() ! itemService.getItemsOutOfString(items)
  }

}

object ReviewItemsActor {
  def props(implicit itemService: ItemService) = Props(new ReviewItemsActor)

  case object GetItems

  case class AddItems(items: Seq[ItemEntry])

  case class ParseItems(maybeItems: String)

}