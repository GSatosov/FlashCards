package models.itemsAndDecks

import java.sql.Timestamp

import models.ids.Ids.ItemId

case class ItemEntry(id: ItemId = ItemId(0L),
                     text: String,
                     level: Int = 1,
                     meaningVariants: List[String],
                     readingVariants: List[String],
                     description: String,
                     precedence: Int = 10,
                     knowledgeLevel: Int = 1,
                     unlockDate: Option[Timestamp],
                     reviewDate: Option[Timestamp])